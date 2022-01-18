package com.wakaztahir.drivesync.auth

import com.wakaztahir.drivesync.model.GoogleUser

expect class GoogleAuthHelper{
    expect fun isSignedIn() : Boolean
    expect fun getLastUser() : GoogleUser?
}