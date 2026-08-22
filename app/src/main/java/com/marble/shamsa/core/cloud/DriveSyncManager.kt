package com.marble.shamsa.core.cloud

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.ClearTokenRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.RevokeAccessRequest
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import com.marble.shamsa.core.data.ReminderRepository
import com.marble.shamsa.core.data.SettingsStore
import com.marble.shamsa.core.model.CloudSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
import java.security.MessageDigest
import java.util.Locale
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
        private const val MAX_AUTH_ATTEMPTS = 3

        private const val PROJECT_ID_HINT = "(not provided)"
        private const val ANDROID_OAUTH_CLIENT_ID_HINT = "(not provided)"
    }

    private val authorizationClient by lazy {
        Identity.getAuthorizationClient(context)
    }

    private val requestedScopes = listOf(Scope(DRIVE_SCOPE))

    private fun request(): AuthorizationRequest =
        AuthorizationRequest.builder()
            .setRequestedScopes(requestedScopes)
            .build()

    private fun ensurePlayServices() {
        val code = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context)

        if (code != ConnectionResult.SUCCESS) {
            throw IOException(
                "Google Play services is unavailable or needs attention (code $code)."
            )
        }
    }

    private fun retryableAuthorization(code: Int): Boolean =
        code == CommonStatusCodes.INTERNAL_ERROR ||
            code == CommonStatusCodes.NETWORK_ERROR ||
            code == CommonStatusCodes.CONNECTION_SUSPENDED_DURING_CALL ||
            code == CommonStatusCodes.RECONNECTION_TIMED_OUT ||
            code == CommonStatusCodes.RECONNECTION_TIMED_OUT_DURING_UPDATE

    private suspend fun authorizeWithRetry(
        authClient: AuthorizationClient,
        authorizationRequest: AuthorizationRequest
    ): AuthorizationResult {
        var last: Throwable? = null

        repeat(MAX_AUTH_ATTEMPTS) { attempt ->
            try {
                return authClient.authorize(authorizationRequest).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                last = e
                if (!retryableAuthorization(e.statusCode) ||
                    attempt == MAX_AUTH_ATTEMPTS - 1
                ) {
                    throw e
                }
                delay(350L * (attempt + 1) * (attempt + 1))
            } catch (e: Exception) {
                last = e
                if (attempt == MAX_AUTH_ATTEMPTS - 1) throw e
                delay(350L * (attempt + 1) * (attempt + 1))
            }
        }

        throw last ?: IOException("Google authorization failed.")
    }

    suspend fun beginAuthorization(activity: Activity): AuthorizationResult {
        ensurePlayServices()
        val activityClient = Identity.getAuthorizationClient(activity)
        return authorizeWithRetry(activityClient, request())
    }

    fun authorizationFromIntent(
        activity: Activity,
        data: Intent?
    ): AuthorizationResult? =
        data?.let {
            Identity.getAuthorizationClient(activity)
                .getAuthorizationResultFromIntent(it)
        }

    suspend fun acceptAuthorization(result: AuthorizationResult): SyncResult {
        val token = result.accessToken ?: return SyncResult.NeedsAuthorization

        if (result.grantedScopes.none { it == DRIVE_SCOPE }) {
            return SyncResult.Failure(
                "Google Drive did not grant the required appDataFolder scope."
            )
        }

        settings.saveDriveToken(token)
        return syncWithToken(token)
    }

    suspend fun syncCached(): SyncResult {
        settings.cachedDriveToken()?.let { cached ->
            when (val result = syncWithToken(cached)) {
                SyncResult.NeedsAuthorization -> Unit
                else -> return result
            }
        }
        return refreshAuthorizationAndSync()
    }

    private suspend fun refreshAuthorizationAndSync(): SyncResult {
        return try {
            ensurePlayServices()
            val result = authorizeWithRetry(authorizationClient, request())
            val token = result.accessToken ?: return SyncResult.NeedsAuthorization

            if (result.grantedScopes.none { it == DRIVE_SCOPE }) {
                return SyncResult.NeedsAuthorization
            }

            settings.saveDriveToken(token)
            syncWithToken(token)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            SyncResult.Failure(describeAuthorizationError(e))
        }
    }

    suspend fun disconnect() {
        val storedToken = settings.storedDriveToken()

        if (!storedToken.isNullOrBlank()) {
            try {
                authorizationClient.clearToken(
                    ClearTokenRequest.builder()
                        .setToken(storedToken)
                        .build()
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

    fun oauthIdentitySummary(): String =
        "${context.packageName} • SHA-1 ${signingSha1()}"

    private fun setupHints(): String {
        val extras = buildList {
            if (PROJECT_ID_HINT.isNotBlank() && PROJECT_ID_HINT != "(not provided)") {
                add("Project: $PROJECT_ID_HINT")
            }
            if (
                ANDROID_OAUTH_CLIENT_ID_HINT.isNotBlank() &&
                ANDROID_OAUTH_CLIENT_ID_HINT != "(not provided)"
            ) {
                add("Android OAuth client: $ANDROID_OAUTH_CLIENT_ID_HINT")
            }
        }
        return if (extras.isEmpty()) {
            ""
        } else {
            " " + extras.joinToString(" • ")
        }
    }

    fun describeAuthorizationError(error: Throwable): String {
        val api = generateSequence<Throwable>(error) { it.cause }
            .filterIsInstance<ApiException>()
            .firstOrNull()

        if (api != null) {
            return when (api.statusCode) {
                CommonStatusCodes.DEVELOPER_ERROR ->
                    "Google OAuth rejected this installed build. Verify the Android OAuth client exactly matches " +
                        oauthIdentitySummary() +
                        ", make sure Drive API is enabled, and check the Google Cloud project configuration." +
                        setupHints()

                CommonStatusCodes.INTERNAL_ERROR ->
                    "Google Play services could not complete authorization. Update/repair Google Play services, " +
                        "confirm a Google account is signed in, then retry. If it keeps failing, re-check the " +
                        "Android OAuth package/SHA-1 and Drive API setup." +
                        setupHints()

                CommonStatusCodes.NETWORK_ERROR ->
                    "Google authorization could not reach Google Play services. Check connectivity and try again. " +
                        oauthIdentitySummary()

                CommonStatusCodes.CANCELED, 12501 ->
                    "Google authorization was cancelled by the user."

                else ->
                    "Google authorization failed (" +
                        api.statusCode + ": " +
                        CommonStatusCodes.getStatusCodeString(api.statusCode) +
                        "). " + oauthIdentitySummary() + setupHints()
            }
        }

        return error.message
            ?.takeIf { it.isNotBlank() }
            ?.take(360)
            ?: "Google Drive authorization failed. ${oauthIdentitySummary()}"
    }

    private suspend fun syncWithToken(token: String): SyncResult =
        withContext(Dispatchers.IO) {
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
                settings.clearDriveToken()
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
            .addQueryParameter(
                "q",
                "name='$SNAPSHOT_NAME' and trashed=false"
            )
            .addQueryParameter("orderBy", "modifiedTime desc")
            .addQueryParameter("pageSize", "1")
            .addQueryParameter("fields", "files(id,name,modifiedTime)")
            .build()

        client.newCall(
            auth(token, Request.Builder().url(url)).build()
        ).execute().use { response ->
            if (!response.isSuccessful) {
                throw response.asDriveException("Drive list")
            }

            val root = json.parseToJsonElement(
                response.body?.string().orEmpty()
            ).jsonObject

            return root["files"]
                ?.jsonArray
                ?.firstOrNull()
                ?.jsonObject
                ?.get("id")
                ?.jsonPrimitive
                ?.contentOrNull
        }
    }

    private fun downloadSnapshot(
        token: String,
        id: String
    ): CloudSnapshot? {
        val req = auth(
            token,
            Request.Builder()
                .url("https://www.googleapis.com/drive/v3/files/$id?alt=media")
        ).build()

        client.newCall(req).execute().use { response ->
            if (response.code == 404) return null
            if (!response.isSuccessful) {
                throw response.asDriveException("Drive download")
            }
            val body = response.body?.string().orEmpty()
            if (body.isBlank()) return null
            return json.decodeFromString(body)
        }
    }

    private fun uploadSnapshot(
        token: String,
        existingId: String?,
        snapshot: CloudSnapshot
    ) {
        val bodyText = json.encodeToString(snapshot)
        val jsonType = "application/json; charset=utf-8".toMediaType()
        val fileId = existingId ?: createSnapshotFile(token, jsonType)

        val request = auth(
            token,
            Request.Builder()
                .url(
                    "https://www.googleapis.com/upload/drive/v3/files/" +
                        "$fileId?uploadType=media"
                )
                .patch(bodyText.toRequestBody(jsonType))
        ).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw response.asDriveException("Drive upload")
            }
        }
    }

    private fun createSnapshotFile(
        token: String,
        jsonType: MediaType
    ): String {
        val metadata =
            """{"name":"$SNAPSHOT_NAME","parents":["appDataFolder"]}"""

        val request = auth(
            token,
            Request.Builder()
                .url("https://www.googleapis.com/drive/v3/files?fields=id")
                .post(metadata.toRequestBody(jsonType))
        ).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw response.asDriveException("Drive create")
            }

            val root = json.parseToJsonElement(
                response.body?.string().orEmpty()
            ).jsonObject

            return root["id"]
                ?.jsonPrimitive
                ?.contentOrNull
                ?: throw IOException("Drive create returned no file id")
        }
    }

    private fun Response.asDriveException(operation: String): IOException {
        val detail = runCatching {
            body?.string().orEmpty()
        }.getOrDefault("")
            .replace(Regex("\\s+"), " ")
            .take(320)

        if (code == 401) {
            return DriveAuthorizationExpired(
                "$operation authorization expired (HTTP 401)"
            )
        }

        val suffix = if (detail.isBlank()) "" else ": $detail"
        return IOException("$operation failed (HTTP $code)$suffix")
    }

    @Suppress("DEPRECATION")
    private fun signingSha1(): String {
        return runCatching {
            val signingFlags =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    PackageManager.GET_SIGNING_CERTIFICATES
                } else {
                    PackageManager.GET_SIGNATURES
                }

            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                signingFlags
            )

            val signatureBytes =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val info = packageInfo.signingInfo
                    val signatures =
                        if (info?.hasMultipleSigners() == true) {
                            info.apkContentsSigners
                        } else {
                            info?.signingCertificateHistory
                        }
                    signatures?.firstOrNull()?.toByteArray()
                } else {
                    packageInfo.signatures
                        ?.firstOrNull()
                        ?.toByteArray()
                } ?: return@runCatching "unavailable"

            MessageDigest.getInstance("SHA-1")
                .digest(signatureBytes)
                .joinToString(":") { byte ->
                    String.format(Locale.US, "%02X", byte.toInt() and 0xFF)
                }
        }.getOrDefault("unavailable")
    }
}
