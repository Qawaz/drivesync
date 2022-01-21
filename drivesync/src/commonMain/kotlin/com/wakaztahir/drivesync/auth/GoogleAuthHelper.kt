package com.wakaztahir.drivesync.auth

import com.wakaztahir.drivesync.model.GoogleUser

expect class GoogleAuthHelper{
    fun isSignedIn() : Boolean
    fun getLastUser() : GoogleUser?
}