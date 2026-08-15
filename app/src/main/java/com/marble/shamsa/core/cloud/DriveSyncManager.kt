package com.marble.shamsa.core.cloud

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.marble.shamsa.core.data.ReminderRepository
import com.marble.shamsa.core.data.SettingsStore
import com.marble.shamsa.core.model.CloudSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
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

@Singleton
class DriveSyncManager @Inject constructor(
    private val repository: ReminderRepository,
    private val settings: SettingsStore,
    private val client: OkHttpClient,
    private val json: Json
) {
    companion object {
        const val DRIVE_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
        private const val SNAPSHOT_NAME = "shamsa-sync-v1.json"
    }

    private fun request() = AuthorizationRequest.builder().setRequestedScopes(listOf(Scope(DRIVE_SCOPE))).build()

    suspend fun beginAuthorization(activity: Activity): AuthorizationResult =
        Identity.getAuthorizationClient(activity).authorize(request()).await()

    fun authorizationFromIntent(activity: Activity, data: Intent?): AuthorizationResult? =
        data?.let { Identity.getAuthorizationClient(activity).getAuthorizationResultFromIntent(it) }

    suspend fun acceptAuthorization(result: AuthorizationResult): SyncResult {
        val token = result.accessToken ?: return SyncResult.NeedsAuthorization
        settings.saveDriveToken(token)
        return syncWithToken(token)
    }

    suspend fun syncCached(): SyncResult {
        val token = settings.cachedDriveToken() ?: return SyncResult.NeedsAuthorization
        return syncWithToken(token)
    }

    suspend fun disconnect() = settings.clearDrive()

    private suspend fun syncWithToken(token: String): SyncResult = withContext(Dispatchers.IO) {
        try {
            val remoteFile = findSnapshot(token)
            if (remoteFile != null) {
                val remote = downloadSnapshot(token, remoteFile)
                if (remote != null) {
                    repository.merge(remote)
                    repository.rescheduleAll()
                }
            }
            val mergedLocal = repository.snapshot(settings.deviceId())
            uploadSnapshot(token, remoteFile, mergedLocal)
            settings.markSynced()
            SyncResult.Success
        } catch (e: IOException) {
            SyncResult.Failure(e.message ?: "Drive network error")
        } catch (e: Exception) {
            SyncResult.Failure(e.message ?: e.javaClass.simpleName)
        }
    }

    private fun auth(token: String, builder: Request.Builder) = builder.header("Authorization", "Bearer $token")

    private fun findSnapshot(token: String): String? {
        val url = HttpUrl.Builder().scheme("https").host("www.googleapis.com")
            .addPathSegments("drive/v3/files")
            .addQueryParameter("spaces", "appDataFolder")
            .addQueryParameter("q", "name='$SNAPSHOT_NAME' and trashed=false")
            .addQueryParameter("fields", "files(id,name,modifiedTime)")
            .build()
        client.newCall(auth(token, Request.Builder().url(url)).build()).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Drive list failed: ${response.code}")
            val text = response.body?.string().orEmpty()
            val root = json.parseToJsonElement(text).jsonObject
            return root["files"]?.jsonArray?.firstOrNull()?.jsonObject?.get("id")?.jsonPrimitive?.contentOrNull
        }
    }

    private fun downloadSnapshot(token: String, id: String): CloudSnapshot? {
        val req = auth(token, Request.Builder().url("https://www.googleapis.com/drive/v3/files/$id?alt=media")).build()
        client.newCall(req).execute().use { response ->
            if (response.code == 404) return null
            if (!response.isSuccessful) throw IOException("Drive download failed: ${response.code}")
            return json.decodeFromString(response.body?.string().orEmpty())
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
            if (!response.isSuccessful) throw IOException("Drive upload failed: ${response.code}")
        }
    }

    private fun createSnapshotFile(token: String, jsonType: MediaType): String {
        val metadata = "{\"name\":\"$SNAPSHOT_NAME\",\"parents\":[\"appDataFolder\"]}"
        val request = auth(
            token,
            Request.Builder()
                .url("https://www.googleapis.com/drive/v3/files?fields=id")
                .post(metadata.toRequestBody(jsonType))
        ).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Drive create failed: ${response.code}")
            val root = json.parseToJsonElement(response.body?.string().orEmpty()).jsonObject
            return root["id"]?.jsonPrimitive?.contentOrNull
                ?: throw IOException("Drive create returned no file id")
        }
    }
}
