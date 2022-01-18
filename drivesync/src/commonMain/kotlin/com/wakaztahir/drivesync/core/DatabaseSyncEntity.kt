package com.wakaztahir.drivesync.core

interface DatabaseSyncEntity<T> {

    /**
     * Insert the newly downloaded item to the database
     */
    suspend fun insertItemInDB(item : T)

    /**
     * An item has been inserted in cloud its cloud id should be updated in the database
     */
    suspend fun updateItemCloudIDInDB(item : T, cloudID: String)

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