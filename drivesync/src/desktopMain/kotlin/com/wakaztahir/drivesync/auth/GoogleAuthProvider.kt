package com.wakaztahir.drivesync.auth

import com.wakaztahir.drivesync.model.GoogleUser
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class GoogleAuthProvider(
    clientId: String,
    val onFailure: (Throwable) -> Unit
) {


    // Initialization
    init {

    }

    actual fun isSignedIn(): Boolean {
        Throwable("Unimplemented Operation").printStackTrace()
        return false
    }

    // Actual Functions
    actual suspend fun silentSignIn() = suspendCoroutine<GoogleUser?> { continuation ->
        Throwable("Unimplemented Operation").printStackTrace()
        continuation.resume(null)
    }

    actual suspend fun launchSignIn(user: (GoogleUser?) -> Unit) {
        Throwable("Unimplemented Operation").printStackTrace()
    }

    actual suspend fun signOut() = suspendCoroutine<Boolean> { continuation ->
        Throwable("Unimplemented Operation").printStackTrace()
        continuation.resume(false)
    }

    actual suspend fun revokeAccess() = suspendCoroutine<Boolean> { continuation ->
        Throwable("Unimplemented Operation").printStackTrace()
        continuation.resume(false)
    }
}