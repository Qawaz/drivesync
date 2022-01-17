package com.wakaztahir.drivesync.model

import com.google.api.services.drive.DriveScopes

actual val com.wakaztahir.drivesync.model.DriveScopes.Drive: String
    get() = DriveScopes.DRIVE
actual val com.wakaztahir.drivesync.model.DriveScopes.DriveAppData: String
    get() = DriveScopes.DRIVE_APPDATA
actual val com.wakaztahir.drivesync.model.DriveScopes.DriveFile: String
    get() = DriveScopes.DRIVE_FILE
actual val com.wakaztahir.drivesync.model.DriveScopes.DriveMetaData: String
    get() = DriveScopes.DRIVE_METADATA
actual val com.wakaztahir.drivesync.model.DriveScopes.DriveMetaDataReadOnly: String
    get() = DriveScopes.DRIVE_METADATA_READONLY
actual val com.wakaztahir.drivesync.model.DriveScopes.DrivePhotosReadOnly: String
    get() = DriveScopes.DRIVE_PHOTOS_READONLY
actual val com.wakaztahir.drivesync.model.DriveScopes.DriveReadOnly: String
    get() = DriveScopes.DRIVE_READONLY
actual val com.wakaztahir.drivesync.model.DriveScopes.DriveScripts: String
    get() = DriveScopes.DRIVE_SCRIPTS

actual fun com.wakaztahir.drivesync.model.DriveScopes.all(): Set<String> = DriveScopes.all()