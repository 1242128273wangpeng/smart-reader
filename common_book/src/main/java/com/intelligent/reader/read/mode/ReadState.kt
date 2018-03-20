package com.intelligent.reader.read.mode

import android.app.Activity
import net.lzbook.kit.data.bean.*
import java.util.*
import kotlin.properties.Delegates

/**
 * 阅读状态
 * Created by wt on 2018/1/4.
 */
object ReadState : Observable() {

    enum class STATE_EVENT {
        SEQUENCE_CHANGE
    }

    //阅读当前章节顺序
    var sequence = 0
        set(value) {
            if (field != value) {
                field = value
                setChanged()
                notifyObservers(STATE_EVENT.SEQUENCE_CHANGE)
            }
        }
    //阅读当前页偏移量
    var offset = 0

    var currentChapter: Chapter? = null
        private set
        get() {
            if (sequence >= 0 && sequence < chapterList.size) {
                return chapterList[sequence]
            } else {
                return null
            }
        }

    //目录
    val chapterList: ArrayList<Chapter> = ArrayList()

    var chapterCount = 0
        get() {
            return chapterList.size
        }

    //chapterId
    var chapterId: String? = null
    //章节名
    var chapterName: String = ""
        get() {
            if (sequence < chapterList.size && sequence >= 0) {
                return chapterList[sequence].chapter_name
            } else {
                return ""
            }
        }
    //总页数
    var pageCount: Int = 0
    //当前页数
    var currentPage: Int = 0
    //当前页总长度
    var contentLength: Int = 0

    var book: Book by Delegates.notNull()

    var book_id: String = ""
        get() = book.book_id

    var requestItem: RequestItem = RequestItem()
        get() {
            return RequestItem.fromBook(book)
        }

    var isMenuShow: Boolean = false

    //跳章时显示menu菜单
    var isJumpMenuShow: Boolean = false

}