package com.wakaztahir.drivesync.model

expect class GoogleUser {
    val id: String?
    val displayName: String?
    val email: String?
    val familyName: String?
    val idToken: String?
    val givenName: String?
}
