package com.ding.basic.bean

import com.google.gson.annotations.SerializedName

class CacheTaskConfig {

    companion object {
        val USE_CHAPTER_BY_CHAPTER = 60001
    }

    @SerializedName("bookAuthor")
    var bookAuthor: String? = null
    @SerializedName("bookName")
    var bookName: String? = null
    @SerializedName("bookSourceId")
    var bookSourceId: BookSourceIdCacheTaskConfig? = null
    @SerializedName("fileUrlList")
    var fileUrlList: List<String>? = null
    @SerializedName("lastChapterId")
    var lastChapterId: BookSourceIdCacheTaskConfig? = null
    @SerializedName("version")
    var version: Int = 0

    class BookSourceIdCacheTaskConfig {
        @SerializedName("counter")
        var counter: Int = 0
        @SerializedName("date")
        var date: Long = 0
        @SerializedName("machineIdentifier")
        var machineIdentifier: Int = 0
        @SerializedName("processIdentifier")
        var processIdentifier: Int = 0
        @SerializedName("time")
        var time: Long = 0
        @SerializedName("timeSecond")
        var timeSecond: Int = 0
        @SerializedName("timestamp")
        var timestamp: Int = 0

        override fun toString(): String {
            return "BookSourceIdCacheTaskConfig{timestamp=" + this.timestamp + ", machineIdentifier=" + this.machineIdentifier + ", processIdentifier=" + this.processIdentifier + ", counter=" + this.counter + ", timeSecond=" + this.timeSecond + ", time=" + this.time + ", date=" + this.date + '}'
        }
    }

    override fun toString(): String {
        return "DataCacheTaskConfig{bookSourceId=" + this.bookSourceId + ", bookName='" + this.bookName + '\'' + ", bookAuthor='" + this.bookAuthor + '\'' + ", lastChapterId=" + this.lastChapterId + ", version=" + this.version + ", fileUrlList=" + this.fileUrlList + '}'
    }
}
