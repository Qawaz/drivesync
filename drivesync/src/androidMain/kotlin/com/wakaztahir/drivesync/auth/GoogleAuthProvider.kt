package com.wakaztahir.drivesync.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.wakaztahir.drivesync.model.GoogleUser
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class GoogleAuthProvider(
    private val activity: Activity,
    clientId: String,
    val onFailure: (Throwable) -> Unit = { it.printStackTrace() }
) {

    // Internal Variables
    internal var googleSignInClient: GoogleSignInClient
    internal val googleSignInAccount: GoogleSignInAccount?
        get() {
            return GoogleSignIn.getLastSignedInAccount(activity)
        }

    // Initialization
    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestEmail()
            .requestScopes(Scope(Scopes.DRIVE_APPFOLDER))
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    // Actual Functions
    actual suspend fun silentSignIn() = suspendCoroutine<GoogleUser?> { continuation ->
        googleSignInClient.silentSignIn().addOnSuccessListener {
            continuation.resume(GoogleUser(it))
        }.addOnFailureListener {
            continuation.resume(null)
        }
    }

    actual suspend fun launchSignIn(user: (GoogleUser?) -> Unit) {
        if (activity is ComponentActivity) {
            with(activity) {
                val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    if (it.data == null) {
                        onFailure(Throwable("Activity Result Data is null"))
                        user(null)
                    } else {
                        user(extractUserFromIntent(it.data!!))
                    }
                }

                launcher.launch(googleSignInClient.signInIntent)
            }
        } else {
            activity.startActivityForResult(googleSignInClient.signInIntent, 289)
        }
    }

    actual suspend fun signOut() = suspendCoroutine<Boolean> { continuation ->
        googleSignInClient.signOut().addOnSuccessListener {
            continuation.resume(true)
        }.addOnFailureListener {
            continuation.resume(false)
        }
    }

    actual suspend fun revokeAccess() = suspendCoroutine<Boolean> { continuation ->
        googleSignInClient.revokeAccess().addOnSuccessListener {
            continuation.resume(true)
        }.addOnFailureListener {
            onFailure(it)
            continuation.resume(false)
        }
    }

    // Helper Methods
    fun extractUserFromIntent(intent: Intent): GoogleUser? {
        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(intent)
        return kotlin.runCatching { GoogleUser(task.result) }.onFailure(onFailure).getOrNull()
    }
}