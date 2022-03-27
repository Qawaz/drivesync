package com.wakaztahir.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import com.wakaztahir.drivesync.auth.GoogleAuthProvider
import com.wakaztahir.drivesync.core.*
import com.wakaztahir.drivesync.drive.DriveServiceProvider

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authProvider = GoogleAuthProvider(activity = this,clientId = "fake-id")
        val driveSyncProvider = DriveServiceProvider(appName = "DriveSyncTest", this)
        val syncProvider = SyncProvider(provider = driveSyncProvider)

        val myItems = listOf<MyItem>(MyItem(),MyItem(),MyItem(),MyItem())

        setContent {
            MaterialTheme {
                LaunchedEffect(null){
                    authProvider.launchSignIn {

                    }
        //            syncProvider.startSync()
        //            val entity = ItemEntity()
        //            myItems.forEach {
        //                syncProvider.syncWithDatabaseAsJson(entity,entity,entity,it)
        //            }
        //            syncProvider.finishSync {
        //                //todo with sync
        //            }
                }
            }
        }
    }
}