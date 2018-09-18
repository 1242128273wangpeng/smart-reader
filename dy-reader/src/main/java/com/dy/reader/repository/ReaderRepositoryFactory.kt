package com.dy.reader.repository

import android.text.TextUtils
import com.ding.basic.bean.Book
import com.ding.basic.bean.Chapter
import com.ding.basic.net.exception.WriteFileFailException
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.util.DataCache
import com.dy.reader.Reader
import com.dy.reader.setting.ReaderStatus
import io.reactivex.Flowable

/**
 * @desc 阅读模块数据源
 * @author lijun Lee
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/11/20 15:43
 */
class ReaderRepositoryFactory private constructor() : ReaderRepository {

    companion object {
        fun getInstance() = ReaderRepositoryFactory()
    }

    override fun requestSingleChapter(chapter: Chapter): Flowable<Chapter> {

        return RequestRepositoryFactory.loadRequestRepositoryFactory(Reader.context).requestChapterContent(chapter).doOnNext {
            writeChapterCache(it, ReaderStatus.book)
            if (it.content == "null"|| TextUtils.isEmpty(it.content)) {
                it.content = "文章内容较短，可能非正文，正在抓紧修复中..."
            }
        }
    }

    override fun writeChapterCache(chapter: Chapter?, book: Book) {
        chapter?.let {
            if (RequestRepositoryFactory.loadRequestRepositoryFactory(Reader.context).checkBookSubscribe(chapter.book_id) != null) {
                var content = chapter.content
                if (TextUtils.isEmpty(content)) {
                    content = "null"
                }

                val success = DataCache.saveChapter(content, chapter)

                if (!success) {
                    throw WriteFileFailException()
                }
            }
        }
    }

}