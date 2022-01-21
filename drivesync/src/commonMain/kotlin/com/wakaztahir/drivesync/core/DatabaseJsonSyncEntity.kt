package com.wakaztahir.drivesync.core

import com.wakaztahir.drivesync.model.SyncFile

interface DatabaseSyncEntity<T> {

    /**
     * Insert the newly downloaded item to the database
     */
    suspend fun insertItemInDB(item: T)

    /**
     * An item has been inserted in cloud its cloud id should be updated in the database
     */
    suspend fun updateItemCloudIDInDB(item: T, cloudID: String)

    /**
     * Update new item in the database
     * @param oldItem is the item in the database , null if there wasn't used to be
     * @param newItem is the new item downloaded from the cloud
     */
    suspend fun updateInDB(oldItem: T?, newItem: T)

    /**
     * completely delete from database
     */
    suspend fun deleteInDB(item: T)
}

interface JsonSyncEntity<T> {
    /**
     * This function is called to determine if [item] should be uploaded
     * Return true for a normal sync
     * This function is called after the filter has decided that item is
     * either not present or has become outdated in the cloud
     */
    fun shouldBeUploaded(item: T): Boolean = true

    /**
     * This function is called to determine if [item] should be downloaded
     * Return true for a normal sync
     * This function is called after the filter has decided that the item is
     * either not present or has become outdated in the local space / database
     */
    fun shouldBeDownloaded(item: T): Boolean = true

    /**
     * Same as [shouldBeDownloaded] but item isn't available and only new file's metadata
     * from the cloud has been downloaded
     */
    fun shouldBeDownloaded(syncFile: SyncFile) = true

    /**
     * Convert the item to json which will be saved in cloud
     */
    fun convertToJson(item: T): String

    /**
     * Convert the item from json downloaded from cloud
     */
    fun convertFromJson(json: String): T
}

/**
 * This entity is responsible for syncing records as json in the cloud
 */
abstract class DatabaseJsonSyncEntity<T> : SyncEntity<T>(), JsonSyncEntity<T>, DatabaseSyncEntity<T> {
    open suspend fun convertFromJsonAndInsertIntoDB(file: SyncFile, json: String) {
        insertItemInDB(convertFromJson(json))
    }
}