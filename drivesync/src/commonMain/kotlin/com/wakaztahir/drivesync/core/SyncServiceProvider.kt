package com.wakaztahir.drivesync.core

import com.wakaztahir.drivesync.model.SyncFile

interface SyncServiceProvider {
    /**
     * Function to get list of files present in the drive
     */
    suspend fun getFilesList() : List<SyncFile>

    /**
     * Function to get the drive file list containing multiple files
     * @param fields is a string containing name of the parameters required
     * comma separated , ex : id,name,description,mimeType,createdTime,modifiedTime,properties
     */
    suspend fun getFilesMap(): HashMap<String, SyncFile>?

    /**
     * Function to only get the metadata of the file
     * @param fields is a string containing name of the parameters required
     * comma separated , ex : id,name,webContentLink,webViewLink
     */
    suspend fun getSyncFile(fileId: String): SyncFile?

    /**
     * Make sure to set file mimetype
     * Set the id of the drive file to null if inserting and the actual id if updating
     */
    suspend fun uploadStringFile(file: SyncFile,content: String): SyncFile?

    /**
     * Make sure to set file mimetype
     * Set the id of the drive file to null if inserting and the actual id if updating
     */
    suspend fun uploadBinaryFile(file: SyncFile, content: ByteArray): SyncFile?

    /**
     * Download a file fileId which contains a string
     */
    suspend fun downloadStringFile(fileId: String): String?

    /**
     * Download file with fileId which contains a byte array
     */
    suspend fun downloadBinaryFile(fileId: String): ByteArray?


    /**
     * Delete a file with given fileId
     */
    suspend fun deleteFile(fileId: String): Boolean
}