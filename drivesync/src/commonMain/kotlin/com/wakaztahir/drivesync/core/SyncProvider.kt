package com.wakaztahir.drivesync.core

import com.wakaztahir.drivesync.model.*

class SyncProvider(
    val provider: SyncServiceProvider,
    private val onProgress: (MessageType, String, Float) -> Unit = { messageType, message, progress -> },
) {

    var filesMap: MutableMap<String, SyncFile>? = null

    private var progress = 0f
    private var totalSize = 0

    enum class MessageType {
        Info,
        Warning,
        Error,
    }

    // Start Sync
    suspend fun startSync() = runCatching {
        filesMap = provider.getFilesMap() ?: return@runCatching
        filesMap!!.size.let { totalSize = it }
    }

    //-----------------Sync Functions

    /**
     * Core of Sync
     * If cloudId == null : upload and insert into cloud
     * If cloudId != null
     *      If item is not found in cloud : delete locally
     *      If item is found in cloud : remove from filesList
     *          If item is deleted : delete from cloud & then delete locally
     *          If item is not deleted
     *              If localEditTime > cloudEditTime : upload & update item in cloud
     *              If localEditTime < cloudEditTime : download & update item locally
     */

    suspend fun <T> SyncEntity<T>.filter(
        item: T,
        localDelete: suspend () -> Unit,
        updateLocally: suspend (SyncFile) -> Unit,
        updateInCloud: suspend (SyncFile) -> Unit,
        insertInCloud: suspend () -> Unit,
        updateProgress: Boolean = true,
    ) {
        if (filesMap == null) throw NullPointerException("Files Map is null , Call start sync before")
        val files = filesMap!!
        // Getting variables
        val cloudId = getCloudID(item)
        val uuid = getSyncUUID(item)
        val modifiedTime = getModifiedTime(item)

        if (cloudId != null) {
            val contains = files.containsKey(uuid)
            val syncFile = files[uuid]
            if (!contains) {
                localDelete()
            } else if (syncFile != null) {
                val cloudModifiedTime = syncFile.syncFileModifiedTime ?: return
                if (!getIsDeleted(item)) {
                    if (modifiedTime > cloudModifiedTime) {
                        updateInCloud(syncFile)
                    } else if (modifiedTime < cloudModifiedTime) {
                        updateLocally(syncFile)
                    }
                } else {
                    if (syncFile.syncFileCloudId != null && provider.deleteFile(syncFile.syncFileCloudId!!)) {
                        localDelete()
                    }
                }
                files.remove(uuid)
            }
        } else {
            insertInCloud()
        }
        if (updateProgress) {
            progress = (totalSize - files.size.toFloat()) / totalSize
            onProgress(MessageType.Info, "Synced ${getTypeKey()} Item With UUID $uuid ", progress)
        }
    }

    @Throws(NullPointerException::class)
    suspend fun <T> syncWithDatabaseAsJson(
        entity: SyncEntity<T>,
        dbSyncEntity: DatabaseSyncEntity<T>,
        jsonSyncEntity: JsonSyncEntity<T>,
        item: T,
    ) {
        entity.filter(
            item,
            localDelete = {
                dbSyncEntity.delete(item)
            },
            updateLocally = {
                val file = provider.downloadStringFile(entity.getCloudID(item)!!)
                if (file != null) {
                    dbSyncEntity.update(oldItem = item, newItem = jsonSyncEntity.convertFromJson(file))
                } else {
                    error("Sync File ${entity.getSyncUUID(item)} with type key ${entity.getTypeKey()} does not exist in cloud")
                }
            },
            updateInCloud = {
                provider.uploadStringFile(
                    file = entity.onCreateSyncFile(item, mimeType = "application/json"),
                    content = jsonSyncEntity.convertToJson(item)
                )
            },
            insertInCloud = {
                provider.uploadStringFile(
                    entity.onCreateSyncFile(item, mimeType = "application/json"),
                    jsonSyncEntity.convertToJson(item)
                )
            }
        )
    }

    suspend fun finishSync(downloadAndInsert: suspend (SyncFile) -> Unit) {
        if (filesMap == null) return

        filesMap!!.forEach { (key, syncFile) ->
            // Validating Sync File
            if (!syncFile.isPassingErrorChecks(
                    checkUUID = true,
                    checkCloudId = true,
                    checkEntityType = true
                )
            ) return

            downloadAndInsert(syncFile)

            progress = (totalSize - filesMap!!.size.toFloat()) / totalSize
            info("Synced ${syncFile.syncFileType} Item with UUID ${syncFile.syncFileUUID}")
        }
        filesMap!!.clear()
    }

    // Helper Functions

    private fun error(message: String) {
        onProgress(MessageType.Error, message, progress)
    }

    private fun info(message: String) {
        onProgress(MessageType.Info, message, progress)
    }

    private fun SyncFile.isPassingErrorChecks(
        checkUUID: Boolean = false,
        checkEntityType: Boolean = false,
        checkCloudId: Boolean = false,
    ): Boolean {

        if (checkUUID && syncFileUUID == null) {
            error("Entity ${syncFileType}'s uuid is null")
            return false
        }
        if (checkEntityType && syncFileType == null) {
            error("Entity with uuid : $syncFileUUID has null entity type")
            return false
        }
        if (checkCloudId && syncFileCloudId == null) {
            error("Entity $syncFileType with uuid : $syncFileUUID has null cloudId")
            return false
        }
        return true
    }

}