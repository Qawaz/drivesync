package com.wakaztahir.drivesync.model

import com.google.api.client.util.DateTime
import com.google.api.services.drive.model.File

actual class SyncFile internal constructor(internal val file: File) {
    actual var name: String?
        get() = file.name
        set(value) {
            file.name = value
        }
    actual var description: String?
        get() = file.description
        set(value) {
            file.description = value
        }
    actual var type: String?
        get() = file.properties["type"]
        set(value) {
            file.properties["type"] = value
        }
    actual var mimeType: String?
        get() = file.mimeType
        set(value) {
            file.mimeType = value
        }
    actual var uuid: String?
        get() = file.properties["uuid"]
        set(value) {
            file.properties["uuid"] = value
        }
    actual var cloudId: String?
        get() = file.id
        set(value) {
            file.id = value
        }
    actual var createdTime: Long?
        get() = if (file.createdTime != null && file.createdTime.value > 100) file.createdTime.value else file.properties["createdTime"]?.toLongOrNull()
        set(value) {
            file.createdTime = value?.let { DateTime(it) }
            file.properties["createdTime"] = value.toString()
        }
    actual var modifiedTime: Long?
        get() = if (file.modifiedTime != null && file.modifiedTime.value > 100) file.modifiedTime.value else file.properties["modifiedTime"]?.toLongOrNull()
        set(value) {
            file.modifiedTime = value?.let { DateTime(it) }
            file.properties["modifiedTime"] = value.toString()
        }
    actual var properties: MutableMap<String, String>?
        get() = file.properties
        set(value) {
            file.properties = value
        }
}

actual fun createNewSyncFile(
    name: String,
    description: String,
    type: String,
    mimeType: String,
    uuid: String,
    cloudId: String?,
    modifiedTime: Long
): SyncFile {
    return SyncFile(File().also { file ->
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
    })
}