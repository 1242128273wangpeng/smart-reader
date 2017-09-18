package com.intelligent.reader.presenter.read

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.IdRes
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.intelligent.reader.R
import com.intelligent.reader.activity.CoverPageActivity
import com.intelligent.reader.activity.ReadingActivity
import com.intelligent.reader.read.help.BookHelper
import com.intelligent.reader.read.help.IReadDataFactory
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.NullCallBack
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.Bookmark
import net.lzbook.kit.data.bean.ReadStatus
import net.lzbook.kit.data.bean.RequestItem
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.request.own.OtherRequestService
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.toastShort
import java.lang.Exception
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.Callable
import kotlin.collections.HashMap

/**
 * Created by xian on 2017/8/8.
 */
class ReadOptionPresenter : ReadOption.Presenter {


    private val font_count = 50

    override var view: ReadOption.View? = null

    val bookDaoHelper: BookDaoHelper

    private var isSubed: Boolean
    private var readStatus: ReadStatus

    private var activity: WeakReference<Activity>


    private var dataFactory: IReadDataFactory

    private var isSourceListShow: Boolean = false

    constructor(act: Activity, rs: ReadStatus, factory: IReadDataFactory) {

        activity = WeakReference(act)

        readStatus = rs
        dataFactory = factory

        bookDaoHelper = BookDaoHelper.getInstance()
        isSubed = bookDaoHelper.isBookSubed(readStatus.book_id)
    }

    override fun cache() {
        if (!Book.isOnlineType(readStatus.book.book_type)) {
            showToastShort("网络不给力，请稍后再试")
            return
        }

        if (!isSubed) {
            val succeed = bookDaoHelper.insertBook(readStatus.book)
            if (succeed) {
                isSubed = true
            } else {
                return
            }
        }
        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            showToastShort("网络不给力，请稍后再试")
            return
        }
        clickDownload(activity.get()!!, readStatus.book as Book, Math.max(readStatus.sequence, 0))
    }

    override fun showMore() {
        val data = java.util.HashMap<String, String>()
        data.put("bookid", readStatus.book_id)
        if (dataFactory != null && dataFactory.currentChapter != null) {
            data.put("chapterid", dataFactory.currentChapter.chapter_id)
        }
        StartLogClickUtil.upLoadEventLog(activity.get()!!, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.MORE1, data)
    }

    /**
     * 点击下载按钮
     *
     *
     * context
     * gid
     * mBook
     * sequence
     */
    fun clickDownload(context: Context, mBook: Book, sequence: Int) {

        val bookTask = BookHelper.getDownBookTask(context, mBook.book_id)
        if (bookTask != null && BookHelper.getStartDownIndex(context, mBook) > -1) {
            if (bookTask.state == DownloadState.DOWNLOADING) {
                Toast.makeText(context, "请耐心等待，已存在缓存队列", Toast.LENGTH_SHORT).show()
                return
            } else if (bookTask.state == DownloadState.WAITTING || bookTask.state == DownloadState.NOSTART
                    || bookTask.state == DownloadState.PAUSEED || bookTask.state == DownloadState.REFRESH
                    || bookTask.state == DownloadState.LOCKED) {
                BookHelper.startDownBookTask(context, mBook.book_id)

                val downloadState = BookHelper.getDownloadState(context, mBook)
                if (downloadState != DownloadState.FINISH && downloadState != DownloadState.WAITTING && downloadState != DownloadState.DOWNLOADING) {
                    Toast.makeText(context, "马上开始为你缓存。。。", Toast.LENGTH_SHORT).show()
                }


                return
            } else if (bookTask.state == DownloadState.FINISH) {
                Toast.makeText(context, "离线缓存已完成", Toast.LENGTH_SHORT).show()
            }
        } else {

            val dialog = MyDialog(activity.get(), R.layout.reading_cache, Gravity.BOTTOM, true)
            val reading_all_down = dialog.findViewById(R.id.reading_all_down) as TextView
            reading_all_down.setOnClickListener(View.OnClickListener {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_download_all)
                if (NetWorkUtils.getNetWorkType(context) == NetWorkUtils.NETWORK_NONE) {
                    Toast.makeText(context, context.getText(R.string.game_network_none), Toast.LENGTH_LONG).show()
                    return@OnClickListener
                }
                BookHelper.addDownBookTask(context, mBook, NullCallBack(), true)
                BookHelper.startDownBookTask(context, mBook.book_id)
                BookHelper.writeDownIndex(context, mBook.book_id, false, 0)
                dialog.dismiss()
                Toast.makeText(context, R.string.reading_cache_hint, Toast.LENGTH_SHORT).show()
            })
            val reading_current_down = dialog.findViewById(R.id.reading_current_down) as TextView
            reading_current_down.setOnClickListener(View.OnClickListener {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_download_from_now)
                if (NetWorkUtils.getNetWorkType(context) == NetWorkUtils.NETWORK_NONE) {
                    Toast.makeText(context, context.getText(R.string.game_network_none), Toast.LENGTH_LONG).show()
                    return@OnClickListener
                }
                BookHelper.addDownBookTask(context, mBook, NullCallBack(), false)
                BookHelper.startDownBookTask(context, mBook.book_id, if (sequence > -1) sequence + 1 else 0)
                BookHelper.writeDownIndex(context, mBook.book_id, true, if (sequence > -1) sequence + 1 else 0)
                dialog.dismiss()
                Toast.makeText(context, R.string.reading_cache_hint, Toast.LENGTH_SHORT).show()

                val data = java.util.HashMap<String, String>()
                data.put("bookid", readStatus.book_id)
                if (dataFactory != null && dataFactory.currentChapter != null) {
                    data.put("chapterid", dataFactory.currentChapter.chapter_id)
                }
                data.put("type", "1")
                StartLogClickUtil.upLoadEventLog(activity.get()!!, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.CACHE, data)
            })
            val cancel = dialog.findViewById(R.id.reading_cache_cancel) as TextView

            cancel.setOnClickListener {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_download_cancel)
                if (dialog != null && dialog.isShowing) {
                    dialog.dismiss()
                    val data = java.util.HashMap<String, String>()
                    data.put("bookid", readStatus.book_id)
                    if (dataFactory != null && dataFactory.currentChapter != null) {
                        data.put("chapterid", dataFactory.currentChapter.chapter_id)
                    }
                    data.put("type", "0")
                    StartLogClickUtil.upLoadEventLog(activity.get()!!, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.CACHE, data)
                }
            }
            dialog.show()
        }
    }

    private fun showToastShort(s: String) {
        if (activity.get() != null) {
            Toast.makeText(activity.get(), s, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showToastShort(@IdRes s: Int) {
        if (activity.get() != null) {
            Toast.makeText(activity.get(), s, Toast.LENGTH_SHORT).show()
        }
    }


    override fun changeSource() {
        if (readStatus.sequence == -1) {
            showToastShort(R.string.read_changesource_tip)
            return
        }
        if (Constants.QG_SOURCE == readStatus.requestItem.host) {
            showToastShort("该小说暂无其他来源！")
            return
        }
        if (isSourceListShow) {
            isSourceListShow = false
        } else {
            if (Constants.QG_SOURCE == readStatus.getRequestItem().host || Constants.QG_SOURCE == readStatus.getRequestItem().host) {
                return
            }

            //先这样实现吧...
            if (activity.get() is ReadingActivity) {
                (activity.get() as ReadingActivity).showMenu(false)
            }

            val loadingPage = dataFactory.getCustomLoadingPage()
            loadingPage.loading(Callable<Void> {
                OtherRequestService.requestBookSourceChange(dataFactory.mHandler, ReadingActivity.MSG_SEARCH_CHAPTER, -144, readStatus.book_id)
                null
            })
            dataFactory.loadingError(loadingPage)
        }
    }

    override fun bookMark(): Int {
        StatServiceUtils.statAppBtnClick(activity.get(), StatServiceUtils.rb_click_add_book_mark_btn)
        return addOptionMark(bookDaoHelper, dataFactory, font_count, readStatus.book.book_type)
    }

    /**
     * 添加手动书签
     */
    fun addOptionMark(mBookDaoHelper: BookDaoHelper?, dataFactory: IReadDataFactory?,
                      font_count: Int, type: Int): Int {
        if (activity.get() == null) {
            return 0
        }

        if (mBookDaoHelper == null || dataFactory == null || readStatus == null) {
            return 0
        }
        if (!mBookDaoHelper.isBookMarkExist(readStatus.book_id, readStatus.sequence, readStatus.offset, type)) {
            var logMap = HashMap<String, String>()
            logMap.put("type", "1")
            StartLogClickUtil.upLoadEventLog(activity.get(), StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.BOOKMARKEDIT, logMap)

            if (!mBookDaoHelper.isBookSubed(readStatus.book_id)) {
                if (!mBookDaoHelper.insertBook(readStatus.book)) {
                    return 0
                }
            }
            if (dataFactory.currentChapter == null || getPageContent() == null) {
                return 0
            }

            val bookMark = Bookmark()
            val requestItem = readStatus.getRequestItem()

            bookMark.book_id = requestItem.book_id
            bookMark.book_source_id = requestItem.book_source_id
            bookMark.parameter = requestItem.parameter
            bookMark.extra_parameter = requestItem.extra_parameter
            bookMark.sequence = if (readStatus.sequence + 1 > readStatus.chapterCount) readStatus.chapterCount else readStatus.sequence
            bookMark.offset = readStatus.offset
            bookMark.sort = dataFactory.currentChapter.sort
            bookMark.last_time = System.currentTimeMillis()
            //if (readStatus.book.dex == 1) {
            bookMark.book_url = dataFactory.currentChapter.curl
            /*} else if (readStatus.book.dex == 0) {
                bookMark.book_url = dataFactory.currentChapter.curl1;
            }*/
            bookMark.chapter_name = dataFactory.currentChapter.chapter_name
            val content = getPageContent()
            val sb = StringBuilder()
            if (readStatus.sequence == -1) {
                bookMark.chapter_name = "《" + readStatus.book.name + "》书籍封面页"
            } else if (readStatus.currentPage == 1 && content.size - 3 >= 0) {
                for (i in 3..content.size - 1) {
                    sb.append(content.get(i))
                }
            } else {
                for (i in content.indices) {
                    sb.append(content.get(i))
                }
            }

            // 去除第一个字符为标点符号的情况
            var content_text = sb.toString().trim { it <= ' ' }
            content_text = content_text.trim { it <= ' ' }

            content_text = AppUtils.deleteTextPoint(content_text)
            // 控制字数
            if (content_text.length > font_count) {
                content_text = content_text.substring(0, font_count)
            }
            bookMark.chapter_content = content_text
            mBookDaoHelper.insertBookMark(bookMark, type)

            return 1
        } else {
            var logMap = HashMap<String, String>()
            logMap.put("type", "2")
            StartLogClickUtil.upLoadEventLog(activity.get(), StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.BOOKMARKEDIT, logMap)
            mBookDaoHelper.deleteBookMark(readStatus.book_id, readStatus.sequence, readStatus.offset, type)

            return 2

        }
    }

    @Synchronized
    fun getPageContent(): List<String> {
        if (readStatus.mLineList == null) {
            return listOf()
        }
        if (readStatus.currentPage == 0) {
            readStatus.currentPage = 1
        }
        if (readStatus.currentPage > readStatus.pageCount) {
            readStatus.currentPage = readStatus.pageCount
        }
        readStatus.offset = 0
        // AppLog.d("initTextContent2", "readStatus.currentPage:" +
        // readStatus.currentPage);
        var pageContent: ArrayList<String>? = null
        if (readStatus.currentPage - 1 < readStatus.mLineList.size) {
            pageContent = readStatus.mLineList[readStatus.currentPage - 1]
        } else {
            pageContent = ArrayList<String>()
        }

        var i = 0
        while (i < readStatus.currentPage - 1 && i < readStatus.mLineList.size) {
            val pageList = readStatus.mLineList[i]
            val size = pageList.size
            // AppLog.d("initTextContent2", "size:" + size);
            for (j in 0..size - 1) {
                val string = pageList[j]
                if (!TextUtils.isEmpty(string) && string != " ") {
                    readStatus.offset += string.length
                }
            }
            i++
        }
        readStatus.offset++
        return pageContent ?: listOf()

    }

    override fun bookInfo() {
        StartLogClickUtil.upLoadEventLog(activity.get(), StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.BOOKDETAIL)
        //先这样实现吧...
        if (activity.get() is ReadingActivity) {
            (activity.get() as ReadingActivity).showMenu(false)
        }
        val intent = Intent(activity.get(), CoverPageActivity::class.java)
        val requestItem = RequestItem()
        requestItem.book_id = readStatus.book_id
        requestItem.book_source_id = readStatus.book.book_source_id
        requestItem.host = readStatus.book.site
        requestItem.name = readStatus.book.name
        requestItem.author = readStatus.book.author
        requestItem.parameter = readStatus.book.parameter
        requestItem.extra_parameter = readStatus.book.extra_parameter

        val bundle = Bundle()
        bundle.putSerializable(Constants.REQUEST_ITEM, requestItem)
        intent.putExtras(bundle)

        activity.get()?.startActivity(intent)
    }

    override fun openWeb() {
        var url: String? = null
        if (dataFactory != null && dataFactory.currentChapter != null) {
            //if (readStatus.book.dex == 1) {
            url = UrlUtils.buildContentUrl(dataFactory.currentChapter.curl)
            /*} else if (readStatus.book.dex == 0) {
                    url = dataFactory.currentChapter.curl1;*/
            //}
        }
        if (!TextUtils.isEmpty(url)) {
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            try {
                activity.get()?.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            activity.get()?.toastShort("无法查看原文链接")
        }
    }

    override fun back() {

        //先这样实现吧...
        if (activity.get() is ReadingActivity) {
            (activity.get() as ReadingActivity).goBackToHome()
        }

    }

    override fun updateStatus() {
        view?.updateStatus(readStatus, dataFactory, bookDaoHelper)
    }
}