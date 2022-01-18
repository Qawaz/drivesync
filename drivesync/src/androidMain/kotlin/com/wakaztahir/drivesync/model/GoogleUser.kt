package com.wakaztahir.drivesync.model

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

actual class GoogleUser internal constructor(internal val signInAccount: GoogleSignInAccount) {
    actual val id: String?
        get() = signInAccount.id
    actual val idToken: String?
        get() = signInAccount.idToken
    actual val displayName: String?
        get() = signInAccount.displayName
    actual val email: String?
        get() = signInAccount.email
    actual val photoUrl: String?
        get() = signInAccount.photoUrl?.path
    actual val familyName: String?
        get() = signInAccount.familyName
    actual val givenName: String?
        get() = signInAccount.givenName
    actual val isExpired: Boolean
        get() = signInAccount.isExpired

}
