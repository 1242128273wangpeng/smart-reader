package com.ding.basic.bean

import com.ding.basic.database.migration.StatusConverter

/**
 * 类描述：
 * 创建人：Zach
 * 智能书籍建造类
 */
class QGBook private constructor(builder: QGBook.Builder) : Book() {

    init {
        book_id = builder.bookId!!
        book_source_id = builder.bookSourceId!!
        name = builder.bookName
        label = builder.category
        author = builder.author
        chapter_count = builder.chapterCount
        img_url = builder.imgUrl
        status = StatusConverter().convert(builder.status)
        host = builder.host
        last_chapter?.update_time = builder.updateTime
    }

    class Builder {
        var bookId: String? = null
        var bookSourceId: String? = null
        var bookName: String? = null
        var category: String? = null
        var author: String? = null
        var chapterCount: Int = 0
        var lastChapterName: String? = null
        var imgUrl: String? = null
        var status = -1
        var host: String? = null
        //最新更新时间
        var updateTime: Long = 0

        fun build(): QGBook {
            return QGBook(this)
        }

        fun bookId(bookId: String): QGBook.Builder {
            this.bookId = bookId
            return this
        }

        fun bookSourceId(bookSourceId: String): QGBook.Builder {
            this.bookSourceId = bookSourceId
            return this
        }

        fun bookName(bookName: String): QGBook.Builder {
            this.bookName = bookName
            return this
        }

        fun category(category: String): QGBook.Builder {
            this.category = category
            return this
        }

        fun author(author: String): QGBook.Builder {
            this.author = author
            return this
        }

        fun chapterCount(chapterCount: Int): QGBook.Builder {
            this.chapterCount = chapterCount
            return this
        }

        fun lastChapterName(lastChapterName: String): QGBook.Builder {
            this.lastChapterName = lastChapterName
            return this
        }

        fun host(host: String?): QGBook.Builder {
            this.host = host
            return this
        }

        fun imgUrl(imgUrl: String?): QGBook.Builder {
            this.imgUrl = imgUrl
            return this
        }

        fun status(status: Int): QGBook.Builder {
            this.status = status
            return this
        }

        fun updateTime(updateTime: Long): QGBook.Builder {
            this.updateTime = updateTime
            return this
        }
    }

}