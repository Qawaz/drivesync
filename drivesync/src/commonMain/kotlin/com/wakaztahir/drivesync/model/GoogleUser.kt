package com.wakaztahir.drivesync.model

expect class GoogleUser {
    val id: String?
    val idToken: String?
    val displayName: String?
    val email: String?
    val photoUrl : String?
    val familyName: String?
    val givenName: String?
    val isExpired : Boolean
}
