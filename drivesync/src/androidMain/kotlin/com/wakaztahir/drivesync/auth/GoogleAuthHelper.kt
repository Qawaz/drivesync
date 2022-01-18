package com.wakaztahir.drivesync.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.wakaztahir.drivesync.model.GoogleUser

actual class GoogleAuthHelper(private val context : Context) {
    actual fun isSignedIn() : Boolean{
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }
    actual fun getLastUser() : GoogleUser? {
        return GoogleSignIn.getLastSignedInAccount(context)?.let { GoogleUser(it) }
    }
}