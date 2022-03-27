@file:JvmName("SyncFileDesktop")

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
    actual var mimeType: String?
        get() = file.mimeType
        set(value) {
            file.mimeType = value
        }
    actual var cloudId: String?
        get() = file.id
        set(value) {
            file.id = value
        }
    actual var createdTime: Long?
        get() = file.createdTime.value
        set(value) {
            file.createdTime = value?.let { DateTime(it) }
        }
    actual var modifiedTime: Long?
        get() = file.modifiedTime.value
        set(value) {
            file.modifiedTime = value?.let { DateTime(it) }
        }
    actual var properties: MutableMap<String, String>?
        get() = file.properties
        set(value) {
            file.properties = value
        }
}

actual fun createDriveSyncFile(
    name: String,
    description: String,
    mimeType: String,
    cloudId: String?,
    modifiedTime: Long,
    properties: Map<String, String>
): SyncFile {
    return SyncFile(File().also { file ->
        file.name = name
        file.description = description
        file.id = cloudId
        file.mimeType = mimeType
        file.modifiedTime = DateTime(modifiedTime)
        file.properties = properties
    })
}