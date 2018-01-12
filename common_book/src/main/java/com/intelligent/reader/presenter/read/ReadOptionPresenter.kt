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
import com.intelligent.reader.read.DataProvider
import com.intelligent.reader.read.help.BookHelper
import com.intelligent.reader.read.mode.ReadState
import com.intelligent.reader.reader.ReaderViewModel
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.NullCallBack
import net.lzbook.kit.data.bean.*
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

    private var readStatus: ReadStatus

    private var activity: WeakReference<Activity>


    private var mReaderViewModel: ReaderViewModel

    private var isSourceListShow: Boolean = false
    private var loadingPage: LoadingPage? = null

    constructor(act: Activity, rs: ReadStatus, factory: ReaderViewModel) {

        activity = WeakReference(act)

        readStatus = rs
        mReaderViewModel = factory

        bookDaoHelper = BookDaoHelper.getInstance()
    }

    override fun cache() {
        if (!Book.isOnlineType(readStatus.book.book_type)) {
            showToastShort("网络不给力，请稍后再试")
            return
        }

        if (!bookDaoHelper.isBookSubed(readStatus.book_id) && !bookDaoHelper.insertBook(readStatus.book)) {
            return
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
        ReadState.chapterId?.let {
            data.put("chapterid", it)
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
            if (bookTask.state == DownloadState.DOWNLOADING || bookTask.state == DownloadState.WAITTING) {
                Toast.makeText(context, "请耐心等待，已存在缓存队列", Toast.LENGTH_SHORT).show()
                return
            } else if (bookTask.state == DownloadState.NOSTART
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

                val data = java.util.HashMap<String, String>()
                data.put("bookid", readStatus.book_id)
                if (mReaderViewModel != null && mReaderViewModel.currentChapter != null) {
                    data.put("chapterid", mReaderViewModel!!.currentChapter!!.chapter_id)
                }
                data.put("type", "1")
                StartLogClickUtil.upLoadEventLog(activity.get()!!, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.CACHE, data)
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
                if (mReaderViewModel != null && mReaderViewModel.currentChapter != null) {
                    data.put("chapterid", mReaderViewModel!!.currentChapter!!.chapter_id)
                }
                data.put("type", "2")
                StartLogClickUtil.upLoadEventLog(activity.get()!!, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.CACHE, data)
            })
            val cancel = dialog.findViewById(R.id.reading_cache_cancel) as TextView

            cancel.setOnClickListener {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_download_cancel)
                if (dialog != null && dialog.isShowing) {
                    dialog.dismiss()
                    val data = java.util.HashMap<String, String>()
                    data.put("bookid", readStatus.book_id)
                    if (mReaderViewModel != null && mReaderViewModel.currentChapter != null) {
                        data.put("chapterid", mReaderViewModel!!.currentChapter!!.chapter_id)
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

            getCustomLoadingPage()
            loadingPage!!.loading(Callable<Void> {
                mReaderViewModel.getBookSource(ReadState.book!!.book_id)
//                OtherRequestService.requestBookSourceChange(dataFactory.mHandler, ReadingActivity.MSG_SEARCH_CHAPTER, -144, readStatus.book_id)
                null
            })
        }
    }

    //ReadDataFactory
    //LoadingPage
    fun getCustomLoadingPage() {
        var curl = ""
        if (mReaderViewModel.readStatus!!.sequence == -1) {
            curl = mReaderViewModel.readStatus!!.firstChapterCurl
            //dataFactory
        } else if (mReaderViewModel.currentChapter != null && !TextUtils.isEmpty(mReaderViewModel.currentChapter!!.curl)) {
            //if (readStatus.book.dex == 1 && !TextUtils.isEmpty(currentChapter.curl)) {
            curl = mReaderViewModel.currentChapter!!.curl
            /*} else if (readStatus.book.dex == 0 && !TextUtils.isEmpty(currentChapter.curl1)) {
                curl = currentChapter.curl1;
            }*/
        }
        if (loadingPage == null) {
            loadingPage = LoadingPage(activity.get()!!, true, curl, LoadingPage.setting_result)
        }
        loadingPage!!.setCustomBackgroud()
    }


    override fun bookMark(): Int {
        StatServiceUtils.statAppBtnClick(activity.get(), StatServiceUtils.rb_click_add_book_mark_btn)
        return addOptionMark(bookDaoHelper, font_count, readStatus.book.book_type)
    }

    /**
     * 添加手动书签
     */
    fun addOptionMark(mBookDaoHelper: BookDaoHelper?, font_count: Int, type: Int): Int {
        if (activity.get() == null || mBookDaoHelper == null || readStatus == null) {
            return 0
        }
        if (!mBookDaoHelper.isBookMarkExist(ReadState.book!!.book_id, ReadState.sequence, ReadState.offset, type)) {
            var logMap = HashMap<String, String>()
            logMap.put("type", "1")
            StartLogClickUtil.upLoadEventLog(activity.get(), StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.BOOKMARKEDIT, logMap)

            if (!mBookDaoHelper.isBookSubed(ReadState.book!!.book_id)) {
                if (!mBookDaoHelper.insertBook(ReadState.book)) {
                    return 0
                }
            }

            val chapter = DataProvider.getInstance().chapterMap[ReadState.sequence]
            if (chapter == null || DataProvider.getInstance().findCurrentPageNovelLineBean().isEmpty()) {
                return 0
            }

            val bookMark = Bookmark()
//            val requestItem = readStatus.getRequestItem()

            bookMark.book_id = ReadState.book!!.book_id
            bookMark.book_source_id = ReadState.book!!.book_source_id
            bookMark.parameter = ReadState.book!!.parameter
            bookMark.extra_parameter = ReadState.book!!.extra_parameter
            bookMark.sequence = if (ReadState.sequence + 1 > ReadState.chapterList.size) ReadState.chapterList.size else ReadState.sequence
            bookMark.offset = ReadState.offset
            bookMark.sort = chapter.sort
            bookMark.last_time = System.currentTimeMillis()
            //if (readStatus.book.dex == 1) {
            bookMark.book_url = chapter.curl
            /*} else if (readStatus.book.dex == 0) {
                bookMark.book_url = dataFactory.currentChapter.curl1;
            }*/
            bookMark.chapter_name = chapter.chapter_name
            //获取本页内容
            val content = DataProvider.getInstance().findCurrentPageNovelLineBean()

            val sb = StringBuilder()
            if (ReadState.sequence == -1) {
                bookMark.chapter_name = "《" + ReadState.book!!.name + "》书籍封面页"
            } else if (ReadState.currentPage == 1 && ReadState.pageCount - 3 >= 0) {
                for (i in 3 until content.size) {
                    sb.append(content[i].lineContent)
                }
            } else {
                for (i in content.indices) {
                    sb.append(content[i].lineContent)
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
            mBookDaoHelper.deleteBookMark(ReadState.book!!.book_id, ReadState.sequence, ReadState.offset, type)
            return 2
        }
    }

//    @Synchronized
//    fun getPageContent(): List<NovelLineBean> {
//        if (readStatus.mLineList == null) {
//            return listOf()
//        }
//        if (readStatus.currentPage == 0) {
//            readStatus.currentPage = 1
//        }
//        if (readStatus.currentPage > readStatus.pageCount) {
//            readStatus.currentPage = readStatus.pageCount
//        }
//        readStatus.offset = 0
//        // AppLog.d("initTextContent2", "readStatus.currentPage:" +
//        // readStatus.currentPage);
//        var pageContent: ArrayList<NovelLineBean>? = null
//        if (readStatus.currentPage - 1 < readStatus.mLineList.size) {
//            pageContent = readStatus.mLineList[readStatus.currentPage - 1]
//        } else {
//            pageContent = ArrayList<NovelLineBean>()
//        }
//
//        var i = 0
//        while (i < readStatus.currentPage - 1 && i < readStatus.mLineList.size) {
//            val pageList = readStatus.mLineList[i]
//            val size = pageList.size
//            // AppLog.d("initTextContent2", "size:" + size);
//            for (j in 0 until size) {
//                val string = pageList[j].lineContent
//                if (!TextUtils.isEmpty(string)) {
//                    readStatus.offset += string.length
//                }
//            }
//            i++
//        }
//        readStatus.offset++
//        return pageContent ?: listOf()
//
//    }

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

        val data = java.util.HashMap<String, String>()
        data.put("ENTER", "READPAGE")
        StartLogClickUtil.upLoadEventLog(activity.get(), StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.ENTER, data)

        activity.get()?.startActivity(intent)
    }

    override fun openWeb() {
        var url: String? = null
        if (mReaderViewModel != null && mReaderViewModel!!.currentChapter != null) {
            //if (readStatus.book.dex == 1) {
            url = UrlUtils.buildContentUrl(mReaderViewModel!!.currentChapter!!.curl)
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
            val data = java.util.HashMap<String, String>()
            if (readStatus != null) {
                data.put("bookid", readStatus.book_id)
            }
            StartLogClickUtil.upLoadEventLog(activity.get(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.ORIGINALLINK, data)
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
        view?.updateStatus(readStatus, mReaderViewModel, bookDaoHelper)
    }

    override fun dismissLoadingPage() {
        loadingPage?.onSuccess()
    }
}