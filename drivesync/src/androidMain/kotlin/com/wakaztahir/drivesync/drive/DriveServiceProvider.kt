package com.wakaztahir.drivesync.drive

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.wakaztahir.drivesync.core.SyncServiceProvider
import com.wakaztahir.drivesync.model.SyncFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

actual open class DriveServiceProvider(
    appName: String,
    context: Context,
    scopes: List<String> = listOf(DriveScopes.DRIVE_APPDATA),
    onFailure: (Throwable) -> Unit = { Log.e("TL_DriveService", "Error in Drive Service", it) }
) : SyncServiceProvider {

    private var driveService: Drive

    init {
        val credential: GoogleAccountCredential =
            GoogleAccountCredential.usingOAuth2(context.applicationContext, scopes)
        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context)
        if (googleSignInAccount == null) {
            onFailure(Throwable(message = "Google sign in account not found , sign in with auth provider before making an instance of service provider"))
        }
        credential.selectedAccount = googleSignInAccount?.account
        driveService = Drive.Builder(
            com.google.api.client.http.javanet.NetHttpTransport(),
            GsonFactory(),
            credential
        ).setApplicationName(appName).build()
    }

    actual override suspend fun getSyncFile(fileId: String, onFailure: (Throwable) -> Unit): SyncFile? = withContext(Dispatchers.IO) {
        val file = kotlin.runCatching {
            SyncFile(
                driveService.files().get(fileId)
                    .setFields("id,name,description,mimeType,createdTime,modifiedTime,properties")
                    .execute()
            )
        }.onFailure(onFailure).getOrNull()
        return@withContext file
    }

    actual override suspend fun getFilesMap(onFailure: (Throwable) -> Unit): HashMap<String, SyncFile>? = withContext(Dispatchers.IO) {
        return@withContext kotlin.runCatching {
            val filesList = driveService.files().list().setSpaces("appDataFolder")
                .setFields("files(id,name,description,mimeType,createdTime,modifiedTime,properties)")
                .execute()
            val filesMap = hashMapOf<String, SyncFile>()
            filesList.files.forEach {
                kotlin.runCatching {
                    val uuid = it.properties["uuid"]
                    if (!uuid.isNullOrEmpty()) {
                        filesMap[uuid] = SyncFile(it)
                    }
                }.onFailure { onFailure(Throwable(message = "Error during getting properties of a file", it.cause)) }
            }
            return@withContext filesMap
        }.onFailure(onFailure).getOrNull()
    }

    actual override suspend fun uploadStringFile(file: SyncFile, content: String, onFailure: (Throwable) -> Unit): SyncFile? =
        withContext(Dispatchers.IO) {
            if (file.mimeType == null) onFailure(Throwable("File mimetype cannot be null"))
            val uploadedFile = kotlin.runCatching {
                file.file.parents = listOf("appDataFolder")
                val contentStream = ByteArrayContent.fromString(file.mimeType, content)
                if (file.cloudId == null) {
                    SyncFile(driveService.files().create(file.file, contentStream).execute())
                } else {
                    SyncFile(driveService.files().update(file.cloudId, file.file, contentStream).execute())
                }
            }.getOrNull()
            if (uploadedFile != null) {
                return@withContext uploadedFile
            }
            return@withContext null
        }

    actual override suspend fun uploadBinaryFile(file: SyncFile, content: ByteArray, onFailure: (Throwable) -> Unit): SyncFile? =
        withContext(Dispatchers.IO) {
            val uploadedFile = kotlin.runCatching {
                if (file.mimeType == null) onFailure(Throwable("File mimetype cannot be null"))
                file.file.parents = listOf("appDataFolder")
                val contentStream = InputStreamContent(file.mimeType, content.inputStream())
                if (file.cloudId == null) {
                    SyncFile(driveService.files().create(file.file, contentStream).execute())
                } else {
                    SyncFile(driveService.files().update(file.cloudId, file.file, contentStream).execute())
                }
            }.onFailure(action = onFailure).getOrNull()
            if (uploadedFile != null) {
                return@withContext uploadedFile
            }
            return@withContext null
        }


    actual override suspend fun downloadStringFile(fileId: String, onFailure: (Throwable) -> Unit): String? = withContext(Dispatchers.IO) {
        kotlin.runCatching {
            driveService.files().get(fileId).executeMediaAsInputStream().use {
                val s: Scanner = Scanner(it).useDelimiter("\\A")
                val result = if (s.hasNext()) s.next() else ""
                result
            }
        }.onFailure(onFailure).getOrNull()
    }

    actual override suspend fun downloadBinaryFile(fileId: String, onFailure: (Throwable) -> Unit): ByteArray? = withContext(Dispatchers.IO) {
        kotlin.runCatching {
            driveService.files().get(fileId).executeMediaAsInputStream().use { inputStream ->
                inputStream.readBytes()
            }
        }.onFailure(onFailure).getOrNull()
    }


    actual override suspend fun deleteFile(fileId: String, onFailure: (Throwable) -> Unit): Boolean = withContext(Dispatchers.IO) {
        val operation = kotlin.runCatching {
            driveService.files().delete(fileId).execute()
        }.onFailure(onFailure)
        return@withContext operation.isSuccess
    }
}