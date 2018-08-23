package com.ding.basic.bean

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class CacheTaskConfig: Serializable {

    companion object {
        val USE_CHAPTER_BY_CHAPTER = 60001
    }

    @SerializedName("bookAuthor")
    var bookAuthor: String? = null
    @SerializedName("bookName")
    var bookName: String? = null
    @SerializedName("bookSourceId")
    var bookSourceId: String? = null
    @SerializedName("fileUrlList")
    var fileUrlList: List<String>? = null
    @SerializedName("lastChapterId")
    var lastChapterId: String? = null
    @SerializedName("version")
    var version: Int = 0

    override fun toString(): String {
        return "DataCacheTaskConfig{bookSourceId=" + this.bookSourceId + ", bookName='" + this.bookName + '\'' + ", bookAuthor='" + this.bookAuthor + '\'' + ", lastChapterId=" + this.lastChapterId + ", version=" + this.version + ", fileUrlList=" + this.fileUrlList + '}'
    }
}
