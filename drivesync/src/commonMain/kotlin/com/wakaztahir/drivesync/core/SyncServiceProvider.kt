package com.wakaztahir.drivesync.core

import com.wakaztahir.drivesync.model.SyncFile

interface SyncServiceProvider {

    /**
     * Function to get the drive file list containing multiple files
     */
    suspend fun getFilesMap(onFailure : (Throwable)->Unit = {}): HashMap<String, SyncFile>?
    suspend fun getSyncFile(fileId: String,onFailure : (Throwable)->Unit = {}): SyncFile?
    suspend fun uploadStringFile(file: SyncFile,content: String,onFailure : (Throwable)->Unit = {}): SyncFile?
    suspend fun uploadBinaryFile(file: SyncFile, content: ByteArray,onFailure : (Throwable)->Unit = {}): SyncFile?
    suspend fun downloadStringFile(fileId: String,onFailure : (Throwable)->Unit = {}): String?
    suspend fun downloadBinaryFile(fileId: String,onFailure : (Throwable)->Unit = {}): ByteArray?
    suspend fun deleteFile(fileId: String,onFailure : (Throwable)->Unit = {}): Boolean
}