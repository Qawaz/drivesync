package com.wakaztahir.drivesync.drive

import com.wakaztahir.drivesync.core.SyncServiceProvider
import com.wakaztahir.drivesync.model.SyncFile
import com.wakaztahir.kmpstorage.StorageInputStream

expect open class DriveServiceProvider : SyncServiceProvider {
    /**
     * Function to get list of files present in the drive
     */
    override suspend fun getFilesList(): List<SyncFile>

    /**
     * Function to get the drive file list containing multiple files
     * @param fields is a string containing name of the parameters required
     * comma separated , ex : id,name,description,mimeType,createdTime,modifiedTime,properties
     */
    override suspend fun getFilesMap(): HashMap<String, SyncFile>?

    /**
     * Function to only get the metadata of the file
     * @param fields is a string containing name of the parameters required
     * comma separated , ex : id,name,webContentLink,webViewLink
     */
    override suspend fun getSyncFile(fileId: String): SyncFile?

    /**
     * Make sure to set file mimetype
     * Set the id of the drive file to null if inserting and the actual id if updating
     */
    override suspend fun uploadFile(file: SyncFile, content: String): SyncFile?

    /**
     * Make sure to set file mimetype
     * Set the id of the drive file to null if inserting and the actual id if updating
     */
    override suspend fun uploadFile(file: SyncFile, content: StorageInputStream): SyncFile?

    /**
     * Download a file fileId which contains a string
     */
    override suspend fun downloadFileAsString(fileId: String): String?

    /**
     * Download file with fileId as input stream
     */
    override suspend fun downloadFile(fileId: String): StorageInputStream?

    /**
     * Delete a file with given fileId
     */
    override suspend fun deleteFile(fileId: String): Boolean
}