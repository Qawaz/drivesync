package com.wakaztahir.drivesync.model

expect class SyncFile {
    var name: String?
    var description: String?
    var mimeType: String?
    var cloudId: String?
    var createdTime: Long?
    var modifiedTime: Long?
    var properties: MutableMap<String, String>?
}

expect fun createDriveSyncFile(
    name: String,
    description: String,
    mimeType: String,
    cloudId: String?,
    modifiedTime: Long,
    properties: Map<String, String>
): SyncFile

// Helpers

internal var SyncFile.type: String?
    get() = properties?.get("type")
    set(value) {
        if (value != null) properties?.set("type", value)
    }

internal var SyncFile.uuid: String?
    get() = properties?.get("uuid")
    set(value) {
        if (value != null) properties?.set("uuid", value)
    }