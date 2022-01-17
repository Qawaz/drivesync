package com.wakaztahir.android

import com.wakaztahir.drivesync.core.DatabaseSyncEntity
import com.wakaztahir.drivesync.core.JsonSyncEntity
import com.wakaztahir.drivesync.core.SyncEntity
import com.wakaztahir.drivesync.model.SyncFile

class ItemEntity : SyncEntity<MyItem>(),DatabaseSyncEntity<MyItem>,JsonSyncEntity<MyItem> {

    override fun getTypeKey(): String {
        TODO("Not yet implemented")
    }

    override suspend fun updateItemCloudID(cloudID: String) {
        TODO("Not yet implemented")
    }

    override suspend fun update(oldItem: MyItem?, newItem: MyItem) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(item: MyItem) {
        TODO("Not yet implemented")
    }

    override fun shouldBeUploaded(item: MyItem): Boolean {
        TODO("Not yet implemented")
    }

    override fun shouldBeDownloaded(item: MyItem?, syncFile: SyncFile?): Boolean {
        TODO("Not yet implemented")
    }

    override fun convertToJson(item: MyItem): String {
        TODO("Not yet implemented")
    }

    override fun convertFromJson(json: String): MyItem {
        TODO("Not yet implemented")
    }

    override fun getSyncUUID(item: MyItem): String {
        TODO("Not yet implemented")
    }

    override fun getCloudID(item: MyItem): String? {
        TODO("Not yet implemented")
    }

    override fun getIsDeleted(item: MyItem): Boolean {
        TODO("Not yet implemented")
    }

    override fun getModifiedTime(item: MyItem): Long {
        TODO("Not yet implemented")
    }
}