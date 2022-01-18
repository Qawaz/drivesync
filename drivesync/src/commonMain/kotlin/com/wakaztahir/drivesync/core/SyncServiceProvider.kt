package com.wakaztahir.drivesync.core

import com.wakaztahir.drivesync.model.SyncFile

interface SyncServiceProvider {

    /**
     * Function to get the drive file list containing multiple files
     */
    suspend fun getFilesMap(): HashMap<String, SyncFile>?
    suspend fun getSyncFile(fileId: String): SyncFile?
    suspend fun uploadStringFile(file: SyncFile,content: String): SyncFile?
    suspend fun uploadBinaryFile(file: SyncFile, content: ByteArray): SyncFile?
    suspend fun downloadStringFile(fileId: String): String?
    suspend fun downloadBinaryFile(fileId: String): ByteArray?
    suspend fun deleteFile(fileId: String): Boolean
}