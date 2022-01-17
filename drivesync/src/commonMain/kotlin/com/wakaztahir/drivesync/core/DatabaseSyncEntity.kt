package com.wakaztahir.drivesync.core

interface DatabaseSyncEntity<T> {
    /**
     * An item has been inserted in cloud its cloud id should be updated in the database
     */
    suspend fun updateItemCloudID(cloudID: String)

    /**
     * Update new item in the database
     * @param oldItem is the item in the database , null if there wasn't used to be
     * @param newItem is the new item downloaded from the cloud
     */
    suspend fun update(oldItem: T?, newItem: T)

    /**
     * completely delete from database
     */
    suspend fun delete(item: T)
}