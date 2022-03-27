package com.wakaztahir.drivesync.core

import com.wakaztahir.drivesync.model.SyncFile
import com.wakaztahir.drivesync.model.createDriveSyncFile

abstract class SyncEntity<T> {

    /**
     * This would usually be the name of the entity
     * It should always be same unless the object's properties in json have changed
     */
    abstract val typeKey: String

    /**
     * Get all the items from database , even deleted
     */
    abstract suspend fun getAllItems(): List<T>

    /**
     * Arbitrary name
     */
    open fun getName(item: T): String? = null

    /**
     * Arbitrary description
     */
    open fun getDescription(item: T): String? = null

    /**
     * UUID of the object that should be same across multiple databases
     */
    abstract fun getSyncUUID(item: T): String

    /**
     * Cloud ID of the object in the server
     */
    abstract fun getCloudID(item: T): String?

    /**
     * Is the object deleted in local database
     * local deleted objects need to be stored to be queued for deletion in server
     * so deletion succeeds when the network connection is available
     */
    abstract fun getIsDeleted(item: T): Boolean

    /**
     * last time the object's contents were modified in the database
     * excluding the modification of cloudId , modifiedTime
     * If object's not modified since it was created return creation time
     */
    abstract fun getModifiedTime(item: T): Long

    open fun onCreateSyncFile(item: T, mimeType: String): SyncFile {
        return createDriveSyncFile(
            name = getName(item) ?: "Sync Entity's Name",
            description = getDescription(item) ?: "Sync Entity's Description",
            mimeType = mimeType,
            cloudId = getCloudID(item),
            modifiedTime = getModifiedTime(item),
            properties = mapOf(
                "type" to typeKey,
                "uuid" to getSyncUUID(item)
            )
        )
    }
}