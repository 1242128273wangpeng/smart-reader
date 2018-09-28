package com.dy.reader.setting

import com.ding.basic.bean.Book
import com.ding.basic.bean.Chapter
import com.dy.reader.data.DataProvider
import com.dy.reader.event.EventLoading
import com.dy.reader.page.PageManager
import com.dy.reader.page.Position
import org.greenrobot.eventbus.EventBus
import kotlin.properties.Delegates

object ReaderStatus {

    var initialized = false

    fun prepare(book: Book, callback: ((Boolean) -> Unit)? = null) {

        DataProvider.prepare(book, book.sequence - 1, callback)
        initialized = true
    }

    fun clear() {
        initialized = false
        chapterList.clear()
        PageManager.clear()
        DataProvider.clear()
    }


    fun isReady(): Boolean = initialized
//        return initialized && this::book.isInitialized && this::position.isInitialized

    var book by Delegates.notNull<Book>()

    var currentChapter: Chapter? = null
        private set
        get() {
            return if (position.group >= 0 && position.group < chapterList.size) {
                chapterList[position.group]
            } else {
                null
            }
        }

    //章节名
    var chapterName: String = ""
        private set
        get() {
            return if (position.group >= 0 && position.group < chapterList.size) {
                chapterList[position.group].name ?: ""
            } else {
                ""
            }
        }


    var chapterId: String = ""
        private set
        get() {
            return if (position.group >= 0 && position.group < chapterList.size) {
                chapterList[position.group].chapter_id
            } else {
                ""
            }
        }

    var chapterCount = 0
        private set
        get() {
            return chapterList.size
        }

    val chapterList = arrayListOf<Chapter>()

    var position: Position = Position(book_id = "")
        set(value) {
            if (field != value) {
                field = value
                EventBus.getDefault().post(EventLoading(EventLoading.Type.PROGRESS_CHANGE))
            }
        }

    var isMenuShow: Boolean = false

    var startTime = System.currentTimeMillis() / 1000L

}