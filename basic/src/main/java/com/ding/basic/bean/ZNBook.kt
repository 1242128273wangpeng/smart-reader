package com.ding.basic.bean

import com.ding.basic.db.migration.StatusConverter

/**
 * 类描述：
 * 创建人：Zach
 * 创建时间：智能书籍建造类
 */
class ZNBook private constructor(builder: Builder) : Book() {

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
        var dex: Int = 0

        fun build(): ZNBook {
            return ZNBook(this)
        }

        fun bookId(bookId: String): Builder {
            this.bookId = bookId
            return this
        }

        fun bookSourceId(bookSourceId: String): Builder {
            this.bookSourceId = bookSourceId
            return this
        }

        fun bookName(bookName: String): Builder {
            this.bookName = bookName
            return this
        }

        fun category(category: String): Builder {
            this.category = category
            return this
        }

        fun author(author: String): Builder {
            this.author = author
            return this
        }

        fun chapterCount(chapterCount: Int): Builder {
            this.chapterCount = chapterCount
            return this
        }

        fun lastChapterName(lastChapterName: String): Builder {
            this.lastChapterName = lastChapterName
            return this
        }

        fun host(host: String): Builder {
            this.host = host
            return this
        }

        fun imgUrl(imgUrl: String?): Builder {
            this.imgUrl = imgUrl
            return this
        }

        fun status(status: Int): Builder {
            this.status = status
            return this
        }

        fun dex(dex: Int): Builder {
            this.dex = dex
            return this
        }

        fun updateTime(updateTime: Long): Builder {
            this.updateTime = updateTime
            return this
        }
    }
}