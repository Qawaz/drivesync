package com.wakaztahir.drivesync.drive

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.wakaztahir.drivesync.core.SyncServiceProvider
import com.wakaztahir.drivesync.model.SyncFile
import com.wakaztahir.kmpstorage.StorageInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

actual open class DriveServiceProvider(
    appName: String,
    context: Context,
    scopes: List<String> = listOf(DriveScopes.DRIVE_APPDATA)
) : SyncServiceProvider {

    private var driveService: Drive

    init {
        val credential: GoogleAccountCredential =
            GoogleAccountCredential.usingOAuth2(context.applicationContext, scopes)
        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(context)
        credential.selectedAccount = googleSignInAccount?.account
        driveService = Drive.Builder(
            com.google.api.client.http.javanet.NetHttpTransport(),
            GsonFactory(),
            credential
        ).setApplicationName(appName).build()
    }

    actual override suspend fun getSyncFile(fileId: String): SyncFile? = withContext(Dispatchers.IO) {
        return@withContext SyncFile(
            driveService.files().get(fileId)
                .setFields("id,name,description,mimeType,createdTime,modifiedTime,properties")
                .execute()
        )
    }

    actual override suspend fun getFilesList(): List<SyncFile> {
        val filesList = driveService.files().list().setSpaces("appDataFolder")
            .setFields("files(id,name,description,mimeType,createdTime,modifiedTime,properties)")
            .execute()
        return filesList.files.map { SyncFile(it) }
    }

    actual override suspend fun getFilesMap(): HashMap<String, SyncFile>? = withContext(Dispatchers.IO) {
        val filesList = driveService.files().list().setSpaces("appDataFolder")
            .setFields("files(id,name,description,mimeType,createdTime,modifiedTime,properties)")
            .execute()
        val filesMap = hashMapOf<String, SyncFile>()
        filesList.files.forEach {
            kotlin.runCatching {
                val uuid = it.properties["uuid"] ?: it.id
                if (!uuid.isNullOrEmpty()) {
                    filesMap[uuid] = SyncFile(it)
                }
            }
        }
        return@withContext filesMap
    }

    actual override suspend fun uploadFile(file: SyncFile, content: String): SyncFile? =
        withContext(Dispatchers.IO) {
            if (file.mimeType == null) error(Throwable("File mimetype cannot be null"))
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

    actual override suspend fun uploadFile(file: SyncFile, content: StorageInputStream): SyncFile? =
        withContext(Dispatchers.IO) {
            if (file.mimeType == null) error("file mimetype cannot be null")
            file.file.parents = listOf("appDataFolder")
            val contentStream = InputStreamContent(file.mimeType, content)
            if (file.cloudId == null) {
                return@withContext SyncFile(driveService.files().create(file.file, contentStream).execute())
            } else {
                return@withContext SyncFile(
                    driveService.files().update(file.cloudId, file.file, contentStream).execute()
                )
            }
        }

    actual override suspend fun downloadFileAsString(fileId: String): String? = withContext(Dispatchers.IO) {
        driveService.files().get(fileId).executeMediaAsInputStream().use {
            val s: Scanner = Scanner(it).useDelimiter("\\A")
            val result = if (s.hasNext()) s.next() else ""
            result
        }
    }

    actual override suspend fun downloadFile(fileId: String): StorageInputStream? = withContext(Dispatchers.IO) {
        driveService.files().get(fileId).executeMediaAsInputStream()
    }


    actual override suspend fun deleteFile(fileId: String): Boolean = withContext(Dispatchers.IO) {
        val operation = kotlin.runCatching {
            driveService.files().delete(fileId).execute()
        }
        return@withContext operation.isSuccess
    }
}