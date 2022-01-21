package com.wakaztahir.drivesync.core

import com.wakaztahir.drivesync.model.SyncFile

class SyncProvider(val provider: SyncServiceProvider) {

    private var filesMap: HashMap<String, SyncFile>? = null
    private var totalSize = 0f

    enum class MessageType {
        Info,
        Warning,
        Error,
    }


    @Throws
    private suspend fun getFilesMap() {
        filesMap = provider.getFilesMap()
        totalSize = filesMap!!.size.toFloat()
    }

    //----Database Json Sync Entity

    @Throws
    suspend fun sync(
        vararg entities: DatabaseJsonSyncEntity<*>,
        onProgress: (Float) -> Unit,
        onMessage: (MessageType, String) -> Unit,
    ) {
        val entityMap = hashMapOf<String, DatabaseJsonSyncEntity<*>>()
        getFilesMap()
        entities.forEachIndexed { index, entity ->
            syncSingle(entity) { progress ->
                onProgress(progress * (index / entities.size.toFloat()) * ((totalSize - filesMap!!.size) / totalSize))
            }
        }
        entities.forEach {
            entityMap[it.typeKey] = it
        }
        filesMap!!.forEach { (key, syncFile) ->
            // Validating Sync File
            if (!syncFile.isPassingErrorChecks(
                    checkUUID = true,
                    checkCloudId = true,
                    checkEntityType = true,
                    onError = {
                        onMessage(MessageType.Error, it)
                    }
                )
            ) return@forEach

            val entity = entityMap[syncFile.type!!]

            if (entity != null) {
                if (entity.shouldBeDownloaded(syncFile)) {
                    val json = syncFile.cloudId?.let { provider.downloadStringFile(it) }
                    if (json != null) {
                        entity.convertFromJsonAndInsertIntoDB(syncFile, json)
                    }
                }
            }

            onProgress(totalSize - filesMap!!.size.toFloat() / totalSize)
        }
        filesMap!!.clear()
    }

    @Throws
    internal suspend fun <T> syncSingle(entity: DatabaseJsonSyncEntity<T>, onProgress: (Float) -> Unit) {
        val items = entity.getAllItems()
        items.forEachIndexed { index, item ->
            entity.filter(
                item,
                localDelete = {
                    entity.deleteInDB(item)
                },
                updateLocally = {
                    if (entity.shouldBeDownloaded(item,it)) {
                        val file = provider.downloadStringFile(entity.getCloudID(item)!!)
                        if (file != null) {
                            entity.updateInDB(oldItem = item, newItem = entity.convertFromJson(file))
                        } else {
                            error("Sync File ${entity.getSyncUUID(item)} with type key ${entity.typeKey} does not exist in cloud")
                        }
                    }
                },
                updateInCloud = { syncFile ->
                    if (entity.shouldBeUploaded(item)) {
                        provider.uploadStringFile(
                            file = entity.onCreateSyncFile(item, mimeType = "application/json").also { newSyncFile ->
                                newSyncFile.cloudId = syncFile.cloudId
                            },
                            content = entity.convertToJson(item)
                        )
                    }
                },
                insertInCloud = {
                    if (entity.shouldBeUploaded(item)) {
                        val file = provider.uploadStringFile(
                            entity.onCreateSyncFile(item, mimeType = "application/json"),
                            entity.convertToJson(item)
                        )
                        if (file?.cloudId != null) {
                            entity.updateItemCloudIDInDB(item, file.cloudId!!)
                        }
                    }
                }
            )
            onProgress(index / items.size.toFloat())
        }
    }

    //----Binary Storage Sync Entity

    @Throws
    suspend fun sync(
        vararg entities: BinaryStorageSyncEntity<*>,
        onProgress: (Float) -> Unit,
        onMessage: (MessageType, String) -> Unit
    ) {
        val entityMap = hashMapOf<String, BinaryStorageSyncEntity<*>>()
        getFilesMap()
        entities.forEachIndexed { index, entity ->
            syncSingle(entity) { progress ->
                onProgress(progress * (index / entities.size.toFloat()) * ((totalSize - filesMap!!.size) / totalSize))
            }
        }
        entities.forEach {
            entityMap[it.typeKey] = it
        }
        filesMap!!.forEach { (key, syncFile) ->
            // Validating Sync File
            if (!syncFile.isPassingErrorChecks(
                    checkUUID = true,
                    checkCloudId = true,
                    checkEntityType = true,
                    onError = {
                        onMessage(MessageType.Error, it)
                    }
                )
            ) return@forEach

            val entity = entityMap[syncFile.type!!]

            if (entity != null) {
                if (entity.shouldDownloadFileFor(syncFile)) {
                    val byteArray = provider.downloadBinaryFile(syncFile.cloudId!!)
                    if (byteArray != null) {
                        entity.createItemFile(syncFile, byteArray)
                    }
                }
            }

            onProgress(totalSize - filesMap!!.size.toFloat() / totalSize)
        }
        filesMap!!.clear()
    }

    @Throws
    internal suspend fun <T> syncSingle(entity: BinaryStorageSyncEntity<T>, onProgress: (Float) -> Unit) {
        val items = entity.getAllItems()
        items.forEachIndexed { index, item ->
            entity.filter(
                item,
                localDelete = {
                    entity.deleteItemFile(item)
                },
                updateLocally = {
                    if (entity.shouldDownloadFileFor(item,it)) {
                        if (it.cloudId != null) {
                            val byteArray = provider.downloadBinaryFile(it.cloudId!!)
                            if (byteArray != null) {
                                entity.updateItemFile(item, it, byteArray)
                            }
                        }
                    }
                },
                updateInCloud = { syncFile ->
                    if (entity.shouldUploadFileFor(item)) {
                        val byteArray = entity.readBytes(item)
                        if (byteArray != null) {
                            provider.uploadBinaryFile(
                                file = entity.onCreateSyncFile(
                                    item = item,
                                    mimeType = entity.getMimeType(item).ifEmpty { "application/octet-stream" }
                                ).also { newSyncFile ->
                                    newSyncFile.cloudId = syncFile.cloudId
                                },
                                content = byteArray
                            )
                        }
                    }
                },
                insertInCloud = {
                    if (entity.shouldUploadFileFor(item)) {
                        val byteArray = entity.readBytes(item)
                        if (byteArray != null) {
                            provider.uploadBinaryFile(
                                file = entity.onCreateSyncFile(
                                    item = item,
                                    mimeType = entity.getMimeType(item).ifEmpty { "application/octet-stream" }
                                ),
                                content = byteArray
                            )
                        }
                    }
                }
            )
            onProgress(index / items.size.toFloat())
        }
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
                val cloudModifiedTime = syncFile.modifiedTime ?: return
                if (!getIsDeleted(item)) {
                    if (modifiedTime > cloudModifiedTime) {
                        updateInCloud(syncFile)
                    } else if (modifiedTime < cloudModifiedTime) {
                        updateLocally(syncFile)
                    }
                } else {
                    if (syncFile.cloudId != null && provider.deleteFile(syncFile.cloudId!!)) {
                        localDelete()
                    }
                }
                files.remove(uuid)
            }
        } else {
            insertInCloud()
        }
    }

    // Helper Functions
    private fun SyncFile.isPassingErrorChecks(
        checkUUID: Boolean = false,
        checkEntityType: Boolean = false,
        checkCloudId: Boolean = false,
        onError: (String) -> Unit
    ): Boolean {
        if (checkUUID && uuid == null) {
            onError("Entity ${type}'s uuid is null")
            return false
        }
        if (checkEntityType && type == null) {
            onError("Entity with uuid : $uuid has null entity type")
            return false
        }
        if (checkCloudId && cloudId == null) {
            onError("Entity $type with uuid : $uuid has null cloudId")
            return false
        }
        return true
    }

}