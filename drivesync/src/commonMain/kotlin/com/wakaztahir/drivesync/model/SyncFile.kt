package com.wakaztahir.drivesync.model

expect class SyncFile

expect fun createNewSyncFile(
    name: String,
    description: String,
    type: String,
    mimeType : String,
    uuid: String,
    cloudId: String?,
    modifiedTime: Long,
) : SyncFile

expect var SyncFile.syncFileName: String?
expect var SyncFile.syncFileDescription: String?
expect var SyncFile.syncFileType: String?
expect var SyncFile.syncFileMimeType : String?
expect var SyncFile.syncFileUUID: String?
expect var SyncFile.syncFileCloudId: String?
expect var SyncFile.syncFileCreatedTime: Long?
expect var SyncFile.syncFileModifiedTime: Long?
expect var SyncFile.syncFileProperties: MutableMap<String, String>?