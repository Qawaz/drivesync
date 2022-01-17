package com.wakaztahir.drivesync.model

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

actual class GoogleUser internal constructor (internal val signInAccount: GoogleSignInAccount){
    actual val id: String?
        get() = signInAccount.id
    actual val displayName: String?
        get() = signInAccount.displayName
    actual val email: String?
        get() = signInAccount.email
    actual val familyName: String?
        get() = signInAccount.familyName
    actual val idToken: String?
        get() = signInAccount.idToken
    actual val givenName: String?
        get() = signInAccount.givenName
}
