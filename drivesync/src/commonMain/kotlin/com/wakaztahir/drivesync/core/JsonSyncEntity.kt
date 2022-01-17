package com.wakaztahir.drivesync.core

import com.wakaztahir.drivesync.model.SyncFile

interface JsonSyncEntity<T> {
    /**
     * This function is called to determine if [item] should be uploaded
     * Return true for a normal sync
     */
    fun shouldBeUploaded(item: T): Boolean

    /**
     * This function is called to determine if [item] should be downloaded
     * Return true for a normal sync
     */
    fun shouldBeDownloaded(item: T?, syncFile: SyncFile?): Boolean

    fun convertToJson(item: T): String
    fun convertFromJson(json: String): T
}