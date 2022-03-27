package com.wakaztahir.drivesync.drive

import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.UserCredentials
import com.wakaztahir.drivesync.core.SyncServiceProvider
import com.wakaztahir.drivesync.model.SyncFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

actual open class DriveServiceProvider(
    appName: String,
//    authProvider: GoogleAuthProvider,
    accessTokenValue: String,
    accessTokenExpiry: Date
) : SyncServiceProvider {

    private var driveService: Drive

    init {
        val accessToken = AccessToken(accessTokenValue, accessTokenExpiry)
        val credentials = UserCredentials.create(accessToken)
        val credentialsAdapter = HttpCredentialsAdapter(credentials)
        driveService = Drive.Builder(
            com.google.api.client.http.javanet.NetHttpTransport(),
            GsonFactory(),
            credentialsAdapter
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

    actual override suspend fun uploadStringFile(file: SyncFile, content: String): SyncFile? =
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

    actual override suspend fun uploadBinaryFile(file: SyncFile, content: ByteArray): SyncFile? =
        withContext(Dispatchers.IO) {
            if (file.mimeType == null) error("file mimetype cannot be null")
            file.file.parents = listOf("appDataFolder")
            val contentStream = InputStreamContent(file.mimeType, content.inputStream())
            if (file.cloudId == null) {
                return@withContext SyncFile(driveService.files().create(file.file, contentStream).execute())
            } else {
                return@withContext SyncFile(
                    driveService.files().update(file.cloudId, file.file, contentStream).execute()
                )
            }
        }


    actual override suspend fun downloadStringFile(fileId: String): String? = withContext(Dispatchers.IO) {
        driveService.files().get(fileId).executeMediaAsInputStream().use {
            val s: Scanner = Scanner(it).useDelimiter("\\A")
            val result = if (s.hasNext()) s.next() else ""
            result
        }
    }

    actual override suspend fun downloadBinaryFile(fileId: String): ByteArray? = withContext(Dispatchers.IO) {
        driveService.files().get(fileId).executeMediaAsInputStream().use { inputStream ->
            inputStream.readBytes()
        }
    }


    actual override suspend fun deleteFile(fileId: String): Boolean = withContext(Dispatchers.IO) {
        val operation = kotlin.runCatching {
            driveService.files().delete(fileId).execute()
        }
        return@withContext operation.isSuccess
    }
}