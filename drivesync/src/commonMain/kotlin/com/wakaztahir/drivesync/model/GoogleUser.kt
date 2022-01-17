package com.wakaztahir.drivesync.model

expect class GoogleUser

expect val GoogleUser.userID: String?
expect val GoogleUser.userDisplayName: String?
expect val GoogleUser.userEmail: String?
expect val GoogleUser.userFamilyName: String?
expect val GoogleUser.userIdToken: String?
expect val GoogleUser.userGivenName: String?