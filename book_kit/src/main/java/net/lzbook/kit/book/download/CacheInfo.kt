package net.lzbook.kit.book.download

class CacheInfo {
    var bookSourceId: String? = null
    var errorLog: Any? = null
    var downUrl: String? = null
    var messageType: Int? = null
    var minDowmCount: Int? = null
    var success: Boolean = false
    var host: String? = null
    var params: Any? = null


    var fileName: String? = null
        get() {
            return downUrl?.substring(downUrl?.lastIndexOf('/') ?: 0, downUrl?.lastIndexOf('?') ?: 0)
        }

    var md5: String? = null
        get() = fileName?.split("-")?.get(2)

    var chapterCount: Int? = null
        get() = fileName?.split("-")?.get(1)?.toInt() ?: 0

    override fun toString(): String {
        return "CacheInfo(bookSourceId=$bookSourceId, errorLog=$errorLog, downUrl=$downUrl, messageType=$messageType, minDowmCount=$minDowmCount, success=$success, host=$host, params=$params)"
    }
}
