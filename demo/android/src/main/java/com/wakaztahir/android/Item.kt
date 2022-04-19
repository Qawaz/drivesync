package com.wakaztahir.android

class MyItem {
    val id: String = ""
    val cloudId: String = ""
    val somethingElse: String = ""

    companion object {
        fun fromJson(json: String): MyItem {
            return MyItem()
        }
    }
}

fun MyItem.toJson(): String {
    return """"{ "id" : "$id","cloudId" : "$cloudId","somethingElse" : "$somethingElse" }"""
}