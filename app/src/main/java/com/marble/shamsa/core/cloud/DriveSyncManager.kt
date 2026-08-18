package com.marble.shamsa.core.cloud

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.ClearTokenRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.RevokeAccessRequest
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import com.marble.shamsa.core.data.ReminderRepository
import com.marble.shamsa.core.data.SettingsStore
import com.marble.shamsa.core.model.CloudSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SyncResult {
    data object Success : SyncResult
    data object NeedsAuthorization : SyncResult
    data class Failure(val message: String) : SyncResult
}

private class DriveAuthorizationExpired(message: String) : IOException(message)

@Singleton
class DriveSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ReminderRepository,
    private val settings: SettingsStore,
    private val client: OkHttpClient,
    private val json: Json
) {
    companion object {
        const val DRIVE_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
        private const val SNAPSHOT_NAME = "shamsa-sync-v1.json"
    }

    private val authorizationClient by lazy { Identity.getAuthorizationClient(context) }
    private val requestedScopes = listOf(Scope(DRIVE_SCOPE))

    private fun request() = AuthorizationRequest.builder()
        .setRequestedScopes(requestedScopes)
        .build()

    suspend fun beginAuthorization(activity: Activity): AuthorizationResult =
        Identity.getAuthorizationClient(activity).authorize(request()).await()

    fun authorizationFromIntent(activity: Activity, data: Intent?): AuthorizationResult? =
        data?.let {
            Identity.getAuthorizationClient(activity).getAuthorizationResultFromIntent(it)
        }

    suspend fun acceptAuthorization(result: AuthorizationResult): SyncResult {
        val token = result.accessToken ?: return SyncResult.NeedsAuthorization
        if (result.grantedScopes.none { it == DRIVE_SCOPE }) {
            return SyncResult.Failure("Google Drive did not grant the required appDataFolder scope.")
        }
        settings.saveDriveToken(token)
        return syncWithToken(token)
    }

    suspend fun syncCached(): SyncResult {
        val token = settings.cachedDriveToken() ?: return SyncResult.NeedsAuthorization
        return syncWithToken(token)
    }

    suspend fun disconnect() {
        val storedToken = settings.storedDriveToken()
        if (!storedToken.isNullOrBlank()) {
            try {
                authorizationClient.clearToken(
                    ClearTokenRequest.builder().setToken(storedToken).build()
                ).await()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
        }

        try {
            authorizationClient.revokeAccess(
                RevokeAccessRequest.builder()
                    .setScopes(requestedScopes)
                    .build()
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
        }

        settings.clearDrive()
    }

    fun describeAuthorizationError(error: Throwable): String {
        val api = generateSequence<Throwable>(error) { it.cause }
            .filterIsInstance<ApiException>()
            .firstOrNull()

        if (api != null) {
            return when (api.statusCode) {
                10 -> "Google OAuth rejected this build (status 10 / DEVELOPER_ERROR). Configure an Android OAuth client for package com.marble.shamsa and the RELEASE SHA-1 printed by GitHub Actions."
                7 -> "Google authorization could not reach Google Play services. Check the network and try again."
                16 -> "Google authorization was cancelled."
                else -> "Google authorization failed (${api.statusCode}: ${CommonStatusCodes.getStatusCodeString(api.statusCode)})."
            }
        }

        return error.message?.takeIf { it.isNotBlank() }?.take(220)
            ?: "Google Drive authorization failed."
    }

    private suspend fun syncWithToken(token: String): SyncResult = withContext(Dispatchers.IO) {
        try {
            val remoteFile = findSnapshot(token)
            if (remoteFile != null) {
                downloadSnapshot(token, remoteFile)?.let { remote ->
                    repository.merge(remote)
                    repository.rescheduleAll()
                }
            }

            val mergedLocal = repository.snapshot(settings.deviceId())
            uploadSnapshot(token, remoteFile, mergedLocal)
            settings.markSynced()
            SyncResult.Success
        } catch (e: DriveAuthorizationExpired) {
            settings.clearDrive()
            SyncResult.NeedsAuthorization
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            SyncResult.Failure(e.message ?: "Drive network error")
        } catch (e: Exception) {
            SyncResult.Failure(e.message ?: e.javaClass.simpleName)
        }
    }

    private fun auth(token: String, builder: Request.Builder) =
        builder.header("Authorization", "Bearer $token")

    private fun findSnapshot(token: String): String? {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("www.googleapis.com")
            .addPathSegments("drive/v3/files")
            .addQueryParameter("spaces", "appDataFolder")
            .addQueryParameter("q", "name='$SNAPSHOT_NAME' and trashed=false")
            .addQueryParameter("orderBy", "modifiedTime desc")
            .addQueryParameter("pageSize", "1")
            .addQueryParameter("fields", "files(id,name,modifiedTime)")
            .build()

        client.newCall(auth(token, Request.Builder().url(url)).build()).execute().use { response ->
            if (!response.isSuccessful) throw response.asDriveException("Drive list")
            val root = json.parseToJsonElement(response.body?.string().orEmpty()).jsonObject
            return root["files"]
                ?.jsonArray
                ?.firstOrNull()
                ?.jsonObject
                ?.get("id")
                ?.jsonPrimitive
                ?.contentOrNull
        }
    }

    private fun downloadSnapshot(token: String, id: String): CloudSnapshot? {
        val req = auth(
            token,
            Request.Builder().url("https://www.googleapis.com/drive/v3/files/$id?alt=media")
        ).build()

        client.newCall(req).execute().use { response ->
            if (response.code == 404) return null
            if (!response.isSuccessful) throw response.asDriveException("Drive download")
            val body = response.body?.string().orEmpty()
            if (body.isBlank()) return null
            return json.decodeFromString(body)
        }
    }

    private fun uploadSnapshot(token: String, existingId: String?, snapshot: CloudSnapshot) {
        val bodyText = json.encodeToString(snapshot)
        val jsonType = "application/json; charset=utf-8".toMediaType()
        val fileId = existingId ?: createSnapshotFile(token, jsonType)

        val request = auth(
            token,
            Request.Builder()
                .url("https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=media")
                .patch(bodyText.toRequestBody(jsonType))
        ).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw response.asDriveException("Drive upload")
        }
    }

    private fun createSnapshotFile(token: String, jsonType: MediaType): String {
        val metadata = """{"name":"$SNAPSHOT_NAME","parents":["appDataFolder"]}"""
        val request = auth(
            token,
            Request.Builder()
                .url("https://www.googleapis.com/drive/v3/files?fields=id")
                .post(metadata.toRequestBody(jsonType))
        ).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw response.asDriveException("Drive create")
            val root = json.parseToJsonElement(response.body?.string().orEmpty()).jsonObject
            return root["id"]?.jsonPrimitive?.contentOrNull
                ?: throw IOException("Drive create returned no file id")
        }
    }

    private fun Response.asDriveException(operation: String): IOException {
        val detail = runCatching { body?.string().orEmpty() }
            .getOrDefault("")
            .replace(Regex("\\s+"), " ")
            .take(240)

        if (code == 401) {
            return DriveAuthorizationExpired("$operation authorization expired (HTTP 401)")
        }

        val suffix = if (detail.isBlank()) "" else ": $detail"
        return IOException("$operation failed (HTTP $code)$suffix")
    }
}
