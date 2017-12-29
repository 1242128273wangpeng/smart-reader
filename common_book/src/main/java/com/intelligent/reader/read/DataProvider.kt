package com.intelligent.reader.read

import android.text.TextUtils
import com.intelligent.reader.DisposableAndroidViewModel
import com.intelligent.reader.cover.BookCoverLocalRepository
import com.intelligent.reader.cover.BookCoverOtherRepository
import com.intelligent.reader.cover.BookCoverQGRepository
import com.intelligent.reader.cover.BookCoverRepositoryFactory
import com.intelligent.reader.read.help.ReadSeparateHelper
import com.intelligent.reader.read.mode.NovelPageBean
import com.intelligent.reader.read.mode.ReadViewEnums
import com.intelligent.reader.reader.ReaderOwnRepository
import com.intelligent.reader.reader.ReaderRepositoryFactory
import com.intelligent.reader.repository.BookCoverRepository
import com.intelligent.reader.repository.ReaderRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.RequestItem
import net.lzbook.kit.data.db.BookChapterDao
import net.lzbook.kit.net.custom.service.NetService
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.OpenUDID
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by wt on 2017/12/20.
 */
class DataProvider : DisposableAndroidViewModel() {

    companion object {
        fun getInstance() = Provider.INSTANCE
    }

    private object Provider {
        val INSTANCE = DataProvider()
    }

    //目录
    var chapterList: ArrayList<Chapter> = ArrayList()
    //分页前缓存容器
    val chapterMap: HashMap<Int, Chapter> = HashMap()

    //分页后缓存容器
    val chapterSeparate:HashMap<Int,ArrayList<NovelPageBean>> = HashMap()

    //工厂
    var mReaderRepository: ReaderRepository = ReaderRepositoryFactory.getInstance(ReaderOwnRepository.getInstance())
    var mBookCoverRepository: BookCoverRepository = BookCoverRepositoryFactory.getInstance(BookCoverOtherRepository.getInstance(NetService.userService)
            , BookCoverQGRepository.getInstance(OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()))
            , BookCoverLocalRepository.getInstance(BaseBookApplication.getGlobalContext()))

    /**
     * 加载章节
     *  @param mReadDataListener 请求单章后监听
     */
    fun loadChapter(book: Book, sequence: Int, type: ReadViewEnums.PageIndex, mReadDataListener: ReadDataListener) {
        val requestItem = RequestItem.fromBook(book)
        when (type) {
            ReadViewEnums.PageIndex.current -> {
                getChapterList(book, requestItem, sequence, type, mReadDataListener)
            }
            ReadViewEnums.PageIndex.next -> {
                getChapterList(book, requestItem, sequence + 1, type, mReadDataListener)
            }
            ReadViewEnums.PageIndex.previous -> {
                getChapterList(book, requestItem, sequence - 1, type, mReadDataListener)
            }
        }
    }

    fun loadChapter2(book: Book, sequence: Int, type: ReadViewEnums.PageIndex, mReadDataListener: ReadDataListener) {
        val requestItem = RequestItem.fromBook(book)
        when (type) {
            ReadViewEnums.PageIndex.current -> {
                getChapterList(book, requestItem, sequence, type, mReadDataListener)
            }
            ReadViewEnums.PageIndex.next -> {
                getChapterList(book, requestItem, sequence, type, mReadDataListener)
            }
            ReadViewEnums.PageIndex.previous -> {
                getChapterList(book, requestItem, sequence, type, mReadDataListener)
            }
        }
    }

    /**
     * 获取书籍目录 //复用BookCoverRepositoyFactory
     */
    fun getChapterList(book: Book, requestItem: RequestItem, sequence: Int, type: ReadViewEnums.PageIndex, mReadDataListener: ReadDataListener) {
        if (NetWorkUtils.getNetWorkType(BaseBookApplication.getGlobalContext()) == NetWorkUtils.NETWORK_NONE) {
            val bookChapterDao = BookChapterDao(BaseBookApplication.getGlobalContext(), requestItem.book_id)
            val chapterList = bookChapterDao.queryBookChapter()
            this.chapterList = chapterList
            if (chapterList.size != 0) {
                requestSingleChapter(book, chapterList, sequence, type, mReadDataListener)
                return
            } else {
                mReadDataListener.loadDataError("拉取章节时无网络")
                return
            }
        }
        addDisposable(
                mBookCoverRepository.getChapterList(requestItem)
                        .doOnNext { chapters ->
                            // 已被订阅则加入数据库
                            mBookCoverRepository.saveBookChapterList(chapters, requestItem)
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ chapters ->
                            this.chapterList = chapters
                            if (chapters.size != 0) {
                                requestSingleChapter(book, chapters, sequence, type, mReadDataListener)
                            } else {
                                mReadDataListener.loadDataError("拉取章节时无网络")
                            }
                        }, { _ ->
                            mReadDataListener.loadDataError("拉取章节时无网络")
                        }))
    }

    private fun requestSingleChapter(book: Book, chapters: List<Chapter>, sequence: Int, type: ReadViewEnums.PageIndex, mReadDataListener: ReadDataListener) {
        val chapter = chapters[sequence]
        addDisposable(mReaderRepository.requestSingleChapter(book.site, chapter)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ c ->
                    if (!TextUtils.isEmpty(chapter.content)) {
                        c.isSuccess = true
                        // 自动切源需要就更新目录
                        if (c.flag == 1 && !TextUtils.isEmpty(c.content)) {
                            mReaderRepository.updateBookCurrentChapter(c.book_id, c, c.sequence)
                        }
                    }
                    mReaderRepository.writeChapterCache(c, false)
                    chapterMap.put(sequence, c)
                    chapterSeparate.put(sequence,ReadSeparateHelper.getInstance().initTextSeparateContent(c.content,c.chapter_name))
                    mReadDataListener.loadDataSuccess(c, type)
                }, { throwable ->
                    mReadDataListener.loadDataError(throwable.message.toString())
                }))
    }

    fun onReSeparate() {
        for (it in chapterMap) {
            chapterSeparate.put(it.key,ReadSeparateHelper.getInstance().initTextSeparateContent(it.value.content,it.value.chapter_name))
        }
    }

    abstract class ReadDataListener {
        open fun loadDataSuccess(c: Chapter, type: ReadViewEnums.PageIndex) {
        }

        open fun loadDataError(message: String) {
        }
    }
}