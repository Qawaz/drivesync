package com.wakaztahir.drivesync.model

expect class SyncFile {
    var name: String?
    var description: String?
    var type: String?
    var mimeType: String?
    var uuid: String?
    var cloudId: String?
    var createdTime: Long?
    var modifiedTime: Long?
    var properties: MutableMap<String, String>?
}

expect fun createNewSyncFile(
    name: String,
    description: String,
    type: String,
    mimeType: String,
    uuid: String,
    cloudId: String?,
    modifiedTime: Long,
): SyncFile