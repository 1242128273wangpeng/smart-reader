package com.intelligent.reader.read.mode

import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.ReadViewEnums
import net.lzbook.kit.data.bean.RequestItem
import kotlin.properties.Delegates

/**
 * 阅读状态
 * Created by wt on 2018/1/4.
 */
object ReadState {
    //阅读当前章节顺序
    var sequence = 0
    //阅读当前页偏移量
    var offset = 0

    var currentChapter: Chapter? = null
        private set
        get() {
            if(sequence >= 0 && sequence < chapterList.size) {
                return chapterList[sequence]
            }else{
                return null
            }
        }

    //目录
    var chapterList: ArrayList<Chapter> = ArrayList()

    var chapterCount = 0
        get() {
            return chapterList.size
        }

    //chapterId
    var chapterId: String? = null
    //章节名
    var chapterName: String? = null
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

    var orientationLimit = ReadViewEnums.ScrollLimitOrientation.NONE
}