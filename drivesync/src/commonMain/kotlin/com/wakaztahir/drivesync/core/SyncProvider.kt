package com.wakaztahir.drivesync.core

import com.wakaztahir.drivesync.model.SyncFile
import com.wakaztahir.drivesync.model.type
import com.wakaztahir.drivesync.model.uuid

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
        if(filesMap == null) {
            filesMap = provider.getFilesMap()
            if (filesMap != null) {
                totalSize = filesMap!!.size.toFloat()
            }
        }
    }

    //----Database Json Sync Entity

    @Throws
    suspend fun sync(
        vararg entities: DatabaseJsonSyncEntity<*>,
        onProgress: (Float) -> Unit,
        onMessage: (MessageType, Throwable) -> Unit,
    ) = kotlin.runCatching {
        val entityMap = hashMapOf<String, DatabaseJsonSyncEntity<*>>()
        getFilesMap()
        entities.forEachIndexed { index, entity ->
            syncSingle(
                entity,
                onMessage = onMessage,
                onProgress = { progress ->
                    val cloudFilesProgress = if (totalSize != 0f) ((totalSize - filesMap!!.size) / totalSize) else 1f
                    onProgress(progress * (index / entities.size.toFloat()) * cloudFilesProgress)
                },
            )
        }
        if (filesMap == null) {
            onMessage(MessageType.Error, Throwable("Empty response received"))
            return@runCatching
        }
        entities.forEach {
            entityMap[it.typeKey] = it
        }
        filesMap!!.forEach { (_, syncFile) ->
            kotlin.runCatching {
                // Validating Sync File
                if (!syncFile.isPassingErrorChecks(
                        checkUUID = true,
                        checkCloudId = true,
                        checkEntityType = true,
                        onError = {
                            onMessage(MessageType.Error, Throwable(it))
                        }
                    )
                ) return@forEach

                val entity = entityMap[syncFile.type!!]

                if (entity != null) {
                    if (entity.shouldBeDownloaded(syncFile)) {
                        val json = syncFile.cloudId?.let { provider.downloadFileAsString(it) }
                        if (json != null) {
                            entity.convertFromJsonAndInsertIntoDB(syncFile, json)
                        }
                    }
                }
            }.onFailure {
                onMessage(MessageType.Warning, it)
            }
            onProgress(totalSize - filesMap!!.size.toFloat() / totalSize)
        }
        filesMap!!.clear()
    }.onFailure { onMessage(MessageType.Error, it) }

    @Throws
    internal suspend fun <T> syncSingle(
        entity: DatabaseJsonSyncEntity<T>,
        onProgress: (Float) -> Unit,
        onMessage: (MessageType, Throwable) -> Unit,
    ) = kotlin.runCatching {
        val items = entity.getAllItems()
        items.forEachIndexed { index, item ->
            kotlin.runCatching {
                entity.filter(
                    item,
                    localDelete = {
                        entity.deleteInDB(item)
                    },
                    updateLocally = {
                        if (entity.shouldBeDownloaded(item, it)) {
                            val file = provider.downloadFileAsString(entity.getCloudID(item)!!)
                            if (file != null) {
                                entity.updateInDB(oldItem = item, newItem = entity.convertFromJson(file))
                            } else {
                                onMessage(
                                    MessageType.Warning,
                                    Throwable("Sync File ${entity.getSyncUUID(item)} with type key ${entity.typeKey} does not exist in cloud")
                                )
                            }
                        }
                    },
                    updateInCloud = { syncFile ->
                        if (entity.shouldBeUploaded(item)) {
                            provider.uploadFile(
                                file = entity.onCreateSyncFile(item, mimeType = "application/json")
                                    .also { newSyncFile ->
                                        newSyncFile.cloudId = syncFile.cloudId
                                    },
                                content = entity.convertToJson(item)
                            )
                        }
                    }
                ) {
                    if (entity.shouldBeUploaded(item)) {
                        val file = provider.uploadFile(
                            entity.onCreateSyncFile(item, mimeType = "application/json"),
                            entity.convertToJson(item)
                        )
                        if (file?.cloudId != null) {
                            entity.updateItemCloudIDInDB(item, file.cloudId!!)
                        }
                    }
                }
            }.onFailure { onMessage(MessageType.Warning, it) }
            onProgress(index / items.size.toFloat())
        }
    }.onFailure { onMessage(MessageType.Error, it) }

    //----Binary Storage Sync Entity

    @Throws
    suspend fun sync(
        vararg entities: BinaryStorageSyncEntity<*>,
        onProgress: (Float) -> Unit,
        onMessage: (MessageType, Throwable) -> Unit,
    ) = runCatching {
        val entityMap = hashMapOf<String, BinaryStorageSyncEntity<*>>()
        getFilesMap()
        entities.forEachIndexed { index, entity ->
            syncSingle(
                entity,
                onProgress = { progress ->
                    val cloudFilesProgress = if (totalSize != 0f) ((totalSize - filesMap!!.size) / totalSize) else 1f
                    onProgress(progress * (index / entities.size.toFloat()) * cloudFilesProgress)
                },
                onMessage = onMessage
            )
        }
        entities.forEach {
            entityMap[it.typeKey] = it
        }
        if (filesMap == null) {
            onMessage(MessageType.Error, Throwable("Empty response received"))
            return@runCatching
        }
        filesMap!!.forEach { (_, syncFile) ->
            kotlin.runCatching {
                // Validating Sync File
                if (!syncFile.isPassingErrorChecks(
                        checkUUID = true,
                        checkCloudId = true,
                        checkEntityType = true,
                        onError = {
                            onMessage(MessageType.Warning, Throwable(it))
                        }
                    )
                ) return@forEach

                val entity = entityMap[syncFile.type!!]

                if (entity != null) {
                    if (entity.shouldDownloadFileFor(syncFile)) {
                        val inputStream = provider.downloadFile(syncFile.cloudId!!)
                        if (inputStream != null) {
                            entity.createItemFile(syncFile, inputStream)
                        }
                    }
                }
            }.onFailure {
                onMessage(MessageType.Warning, it)
            }
            onProgress(totalSize - filesMap!!.size.toFloat() / totalSize)
        }
        filesMap!!.clear()
    }.onFailure {
        onMessage(MessageType.Error, it)
    }

    @Throws
    internal suspend fun <T> syncSingle(
        entity: BinaryStorageSyncEntity<T>,
        onProgress: (Float) -> Unit,
        onMessage: (MessageType, Throwable) -> Unit
    ) = kotlin.runCatching {
        val items = entity.getAllItems()
        items.forEachIndexed { index, item ->
            kotlin.runCatching {
                entity.filter(
                    item,
                    localDelete = {
                        entity.deleteItemFile(item)
                    },
                    updateLocally = {
                        if (entity.shouldDownloadFileFor(item, it)) {
                            if (it.cloudId != null) {
                                val stream = provider.downloadFile(it.cloudId!!)
                                if (stream != null) {
                                    entity.updateItemFile(item, it, stream)
                                }
                            }
                        }
                    },
                    updateInCloud = { syncFile ->
                        if (entity.shouldUploadFileFor(item)) {
                            val stream = entity.inputStream(item)
                            if (stream != null) {
                                provider.uploadFile(
                                    file = entity.onCreateSyncFile(
                                        item = item,
                                        mimeType = entity.getMimeType(item).ifEmpty { "application/octet-stream" }
                                    ).also { newSyncFile ->
                                        newSyncFile.cloudId = syncFile.cloudId
                                    },
                                    content = stream
                                )
                                stream.close()
                            }
                        }
                    }
                ) {
                    if (entity.shouldUploadFileFor(item)) {
                        val stream = entity.inputStream(item)
                        if (stream != null) {
                            provider.uploadFile(
                                file = entity.onCreateSyncFile(
                                    item = item,
                                    mimeType = entity.getMimeType(item).ifEmpty { "application/octet-stream" }
                                ),
                                content = stream
                            )
                            stream.close()
                        }
                    }
                }
            }.onFailure { onMessage(MessageType.Warning, it) }
            onProgress(index / items.size.toFloat())
        }
    }.onFailure { onMessage(MessageType.Error, it) }


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
        insertInCloud: suspend () -> Unit
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

    fun reset(){
        filesMap = null
    }

}