package com.wakaztahir.drivesync.model

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

actual typealias GoogleUser = GoogleSignInAccount

actual val GoogleUser.userID: String?
    get() = this.id
actual val GoogleUser.userDisplayName: String?
    get() = this.displayName
actual val GoogleUser.userEmail: String?
    get() = this.email
actual val GoogleUser.userFamilyName: String?
    get() = this.familyName
actual val GoogleUser.userIdToken: String?
    get() = this.idToken
actual val GoogleUser.userGivenName: String?
    get() = this.givenName