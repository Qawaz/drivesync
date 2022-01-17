package com.wakaztahir.drivesync.model

object DriveScopes

expect val DriveScopes.Drive: String
expect val DriveScopes.DriveAppData: String
expect val DriveScopes.DriveFile: String
expect val DriveScopes.DriveMetaData: String
expect val DriveScopes.DriveMetaDataReadOnly: String
expect val DriveScopes.DrivePhotosReadOnly: String
expect val DriveScopes.DriveReadOnly: String
expect val DriveScopes.DriveScripts: String
expect fun DriveScopes.all(): Set<String>