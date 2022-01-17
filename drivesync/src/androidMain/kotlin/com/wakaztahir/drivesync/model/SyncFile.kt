package com.wakaztahir.drivesync.model

import com.google.api.client.util.DateTime
import com.google.api.services.drive.model.File

actual typealias SyncFile = File

actual fun createNewSyncFile(
    name: String,
    description: String,
    type: String,
    mimeType : String,
    uuid: String,
    cloudId: String?,
    modifiedTime: Long
): SyncFile {
    return File().also { file ->
        file.name = name
        file.description = description
        file.id = cloudId
        file.mimeType = mimeType
        file.modifiedTime = DateTime(modifiedTime)
        file.properties = mutableMapOf(
            "type" to type,
            "uuid" to uuid,
            "modifiedTime" to modifiedTime.toString()
        )
    }
}

actual var SyncFile.syncFileName: String?
    get() = this.name
    set(value) {
        this.name = value
    }
actual var SyncFile.syncFileDescription: String?
    get() = this.description
    set(value) {
        this.description = value
    }
actual var SyncFile.syncFileType: String?
    get() = this.properties["type"]
    set(value) {
        this.properties["type"] = value
    }
actual var SyncFile.syncFileMimeType : String?
    get() = this.mimeType
    set(value){
        this.mimeType = value
    }
actual var SyncFile.syncFileUUID: String?
    get() = this.properties["uuid"]
    set(value) {
        this.properties["uuid"] = value
    }
actual var SyncFile.syncFileCloudId: String?
    get() = this.id
    set(value) {
        this.id = value
    }
actual var SyncFile.syncFileCreatedTime: Long?
    get() = if (this.createdTime != null && this.createdTime.value > 100) this.createdTime.value else this.properties["createdTime"]?.toLongOrNull()
    set(value) {
        this.createdTime = value?.let { DateTime(it) }
        this.properties["createdTime"] = value.toString()
    }
actual var SyncFile.syncFileModifiedTime: Long?
    get() = if (this.modifiedTime != null && this.modifiedTime.value > 100) this.modifiedTime.value else this.properties["modifiedTime"]?.toLongOrNull()
    set(value) {
        this.modifiedTime = value?.let { DateTime(it) }
        this.properties["modifiedTime"] = value.toString()
    }
actual var SyncFile.syncFileProperties: MutableMap<String, String>?
    get() = this.properties
    set(value) {
        this.properties = value
    }