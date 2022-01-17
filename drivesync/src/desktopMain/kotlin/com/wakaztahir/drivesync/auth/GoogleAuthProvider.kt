package com.wakaztahir.drivesync.auth

import com.wakaztahir.drivesync.model.GoogleUser
import kotlin.coroutines.suspendCoroutine

actual class GoogleAuthProvider(
    clientId: String,
    val onFailure: (Throwable) -> Unit
) {


    // Initialization
    init {

    }

    // Actual Functions
    actual suspend fun silentSignIn() = suspendCoroutine<GoogleUser?> { continuation ->
        TODO("Unimplemented")
    }

    actual suspend fun launchSignIn(user: (GoogleUser?) -> Unit) {
        TODO("Unimplemented")
    }

    actual suspend fun signOut() = suspendCoroutine<Boolean> { continuation ->
        TODO("Unimplemented")
    }

    actual suspend fun revokeAccess() = suspendCoroutine<Boolean> { continuation ->
        TODO("Unimplemented")
    }
}