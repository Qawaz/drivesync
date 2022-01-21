package com.wakaztahir.drivesync.core

import com.wakaztahir.drivesync.model.SyncFile

interface BinarySyncEntity<T> {
    /**
     * Should this file be uploaded to cloud
     * This function is called after the filter has decided that the file
     * has become outdated or not present in the cloud so returning true default
     */
    fun shouldUploadFileFor(item: T): Boolean = true

    /**
     * Should this file be downloaded from cloud
     * You might not want to download every file
     * This function is called after the filter has decided that the file
     * has become outdated or not present in the local storage so returning true default
     */
    fun shouldDownloadFileFor(item: T,syncFile : SyncFile): Boolean = true

    /**
     * Same as [shouldDownloadFileFor] but since item is not available
     * when the file metadata is downloaded from cloud and its the only thing
     * present in the cloud
     */
    fun shouldDownloadFileFor(syncFile : SyncFile) = true

    /**
     * Get mimeType of the file
     * if empty "application/octet-stream" will be used
     */
    fun getMimeType(item : T) : String

    /**
     * Get bytearray of item that is going to be uploaded
     * Return null if file not found and it won't be uploaded
     */
    fun readBytes(item: T): ByteArray?

    /**
     * Update the downloaded byte array in storage because it has been
     * updated on cloud
     */
    fun updateItemFile(item: T,file : SyncFile, content: ByteArray)

    /**
     * Create the downloaded byte array in storage because it has been
     * created a new on cloud
     */
    fun createItemFile(file : SyncFile,content: ByteArray)

    /**
     * Delete the file present in storage since it has been deleted
     * on cloud
     */
    fun deleteItemFile(item : T)
}

/**
 * This entity is responsible for syncing files in the cloud
 */
abstract class BinaryStorageSyncEntity<T> : SyncEntity<T>(), BinarySyncEntity<T>{
//    override fun onCreateSyncFile(item: T, mimeType: String): SyncFile {
//        return super.onCreateSyncFile(item, mimeType).apply {
//            // todo put additional properties needed for file
//        }
//    }

}