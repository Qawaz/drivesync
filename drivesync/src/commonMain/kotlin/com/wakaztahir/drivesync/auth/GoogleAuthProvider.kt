package com.wakaztahir.drivesync.auth

import com.wakaztahir.drivesync.model.GoogleUser

expect class GoogleAuthProvider {
    suspend fun silentSignIn(): GoogleUser?
    suspend fun launchSignIn(user: (GoogleUser?) -> Unit)
    suspend fun signOut(): Boolean
    suspend fun revokeAccess(): Boolean
}