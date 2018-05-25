package com.intelligent.reader.presenter.read

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.StringRes
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.dingyue.contract.util.CommonUtil
import com.dingyue.contract.util.showToastMessage
import com.intelligent.reader.R
import com.intelligent.reader.activity.CoverPageActivity
import com.intelligent.reader.activity.ReadingActivity
import com.intelligent.reader.read.DataProvider
import com.intelligent.reader.read.mode.ReadState
import com.intelligent.reader.reader.ReaderViewModel
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.Bookmark
import net.lzbook.kit.data.bean.RequestItem
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.*
import java.lang.Exception
import java.lang.ref.WeakReference

/**
 * Created by xian on 2017/8/8.
 */
class ReadOptionPresenter : ReadOption.Presenter {

    private val font_count = 50

    override var view: ReadOption.View? = null

    val bookDaoHelper: BookDaoHelper

    private var activity: WeakReference<Activity>


    private var mReaderViewModel: ReaderViewModel

    private var isSourceListShow: Boolean = false
    private var loadingPage: LoadingPage? = null

    constructor(act: Activity, factory: ReaderViewModel) {

        activity = WeakReference(act)

        mReaderViewModel = factory

        bookDaoHelper = BookDaoHelper.getInstance()
    }

    override fun cache() {
        if (!Book.isOnlineType(ReadState.book.book_type)) {
            showToastShort("网络不给力，请稍后再试")
            return
        }

        if (!bookDaoHelper.isBookSubed(ReadState.book_id) && !bookDaoHelper.insertBook(ReadState.book)) {
            return
        }
        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            showToastShort("网络不给力，请稍后再试")
            return
        }
        clickDownload(activity.get()!!, ReadState.book as Book, Math.max(ReadState.sequence, 0))
    }

    override fun showMore() {
        val data = java.util.HashMap<String, String>()
        data.put("bookid", ReadState.book_id)
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

        val bookTask = CacheManager.getBookTask(mBook)

        if (bookTask.state == DownloadState.FINISH) {
            context.showToastMessage("离线缓存已完成！")
            return
        }


        val dialog = MyDialog(activity.get(), R.layout.reading_cache, Gravity.BOTTOM, true)
        val reading_all_down = dialog.findViewById(R.id.reading_all_down) as TextView
        reading_all_down.setOnClickListener(View.OnClickListener {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_download_all)
            if (NetWorkUtils.getNetWorkType(context) == NetWorkUtils.NETWORK_NONE) {
                activity.get()?.showToastMessage(R.string.game_network_none)
                return@OnClickListener
            }
            BaseBookHelper.startDownBookTask(activity.get(), mBook, 0)
            dialog.dismiss()

            val data = java.util.HashMap<String, String>()
            data.put("bookid", ReadState.book_id)
            if (ReadState.currentChapter != null) {
                data.put("chapterid", ReadState.currentChapter!!.chapter_id)
            }
            data.put("type", "1")
            StartLogClickUtil.upLoadEventLog(activity.get()!!, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.CACHE, data)
        })
        val reading_current_down = dialog.findViewById(R.id.reading_current_down) as TextView
        reading_current_down.setOnClickListener(View.OnClickListener {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_download_from_now)
            if (NetWorkUtils.getNetWorkType(context) == NetWorkUtils.NETWORK_NONE) {
                activity.get()?.showToastMessage(R.string.game_network_none)
                return@OnClickListener
            }
            BaseBookHelper.startDownBookTask(activity.get(), mBook, sequence)

            dialog.dismiss()

            val data = java.util.HashMap<String, String>()
            data.put("bookid", ReadState.book_id)
            if (ReadState.currentChapter != null) {
                data.put("chapterid", ReadState.currentChapter!!.chapter_id)
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
                data.put("bookid", ReadState.book_id)
                if (ReadState.currentChapter != null) {
                    data.put("chapterid", ReadState.currentChapter!!.chapter_id)
                }
                data.put("type", "0")
                StartLogClickUtil.upLoadEventLog(activity.get()!!, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.CACHE, data)
            }
        }
        dialog.show()

    }

    private fun showToastShort(s: String) {
        if (activity.get() != null) {
            activity.get()?.showToastMessage(s)
        }
    }

    private fun showToastShort(@StringRes s: Int) {
        if (activity.get() != null) {
            activity.get()?.showToastMessage(s)
        }
    }


    override fun changeSource() {
        if (ReadState.sequence == -1) {
            showToastShort(R.string.read_changesource_tip)
            return
        }
        if (Constants.QG_SOURCE == ReadState.requestItem.host) {
            showToastShort("该小说暂无其他来源！")
            return
        }


        if (isSourceListShow) {
            isSourceListShow = false
        } else {
            if (Constants.QG_SOURCE == ReadState.requestItem.host || Constants.QG_SOURCE == ReadState.requestItem.host) {
                return
            }

            //先这样实现吧...
            if (activity.get() is ReadingActivity) {
                (activity.get() as ReadingActivity).showMenu(false)

            }

//            getCustomLoadingPage()
//            loadingPage!!.loading {
            mReaderViewModel.getBookSource(ReadState.book.book_id)
//                OtherRequestService.requestBookSourceChange(dataFactory.mHandler, ReadingActivity.MSG_SEARCH_CHAPTER, -144, ReadState.book_id)
//                null
//            }
        }
    }

    //ReadDataFactory
    //LoadingPage
    fun getCustomLoadingPage() {
        var curl = ""
        if (ReadState.sequence == -1) {
            //dataFactory
        } else if (ReadState.currentChapter != null && !TextUtils.isEmpty(ReadState.currentChapter!!.curl)) {
            //if (ReadState.book.dex == 1 && !TextUtils.isEmpty(currentChapter.curl)) {
            curl = ReadState.currentChapter!!.curl
            /*} else if (ReadState.book.dex == 0 && !TextUtils.isEmpty(currentChapter.curl1)) {
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
        return addOptionMark(bookDaoHelper, font_count, ReadState.book.book_type)
    }

    /**
     * 添加手动书签
     */
    fun addOptionMark(mBookDaoHelper: BookDaoHelper?, font_count: Int, type: Int): Int {
        if (activity.get() == null || mBookDaoHelper == null || ReadState == null) {
            return 0
        }

        if (!mBookDaoHelper.isBookSubed(ReadState.book_id) && !mBookDaoHelper.insertBook(ReadState.book)) {
            return 0
        }

        if (!mBookDaoHelper.isBookMarkExist(ReadState.book_id, ReadState.sequence, ReadState.offset, type)) {
            var logMap = HashMap<String, String>()
            logMap.put("type", "1")
            StartLogClickUtil.upLoadEventLog(activity.get(), StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.BOOKMARKEDIT, logMap)

            if (!mBookDaoHelper.isBookSubed(ReadState.book!!.book_id)) {
                if (!mBookDaoHelper.insertBook(ReadState.book)) {
                    return 0
                }
            }

            val chapter = ReadState.currentChapter ?: return 0

            val bookMark = Bookmark()
//            val requestItem = ReadState.getRequestItem()

            bookMark.book_id = ReadState.book!!.book_id
            bookMark.book_source_id = ReadState.book!!.book_source_id
            bookMark.sequence = if (ReadState.sequence + 1 > ReadState.chapterList.size) ReadState.chapterList.size else ReadState.sequence
            bookMark.offset = ReadState.offset
            bookMark.sort = chapter.sort
            bookMark.last_time = System.currentTimeMillis()
            //if (ReadState.book.dex == 1) {
            bookMark.book_url = chapter.curl
            /*} else if (ReadState.book.dex == 0) {
                bookMark.book_url = dataFactory.currentChapter.curl1;
            }*/
            bookMark.chapter_name = chapter.chapter_name
            //获取本页内容
            val content = DataProvider.getInstance().findCurrentPageNovelLineBean() ?: return 0

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
//        if (ReadState.mLineList == null) {
//            return listOf()
//        }
//        if (ReadState.currentPage == 0) {
//            ReadState.currentPage = 1
//        }
//        if (ReadState.currentPage > ReadState.pageCount) {
//            ReadState.currentPage = ReadState.pageCount
//        }
//        ReadState.offset = 0
//        // AppLog.d("initTextContent2", "ReadState.currentPage:" +
//        // ReadState.currentPage);
//        var pageContent: ArrayList<NovelLineBean>? = null
//        if (ReadState.currentPage - 1 < ReadState.mLineList.size) {
//            pageContent = ReadState.mLineList[ReadState.currentPage - 1]
//        } else {
//            pageContent = ArrayList<NovelLineBean>()
//        }
//
//        var i = 0
//        while (i < ReadState.currentPage - 1 && i < ReadState.mLineList.size) {
//            val pageList = ReadState.mLineList[i]
//            val size = pageList.size
//            // AppLog.d("initTextContent2", "size:" + size);
//            for (j in 0 until size) {
//                val string = pageList[j].lineContent
//                if (!TextUtils.isEmpty(string)) {
//                    ReadState.offset += string.length
//                }
//            }
//            i++
//        }
//        ReadState.offset++
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
        requestItem.book_id = ReadState.book_id
        requestItem.book_source_id = ReadState.book.book_source_id
        requestItem.host = ReadState.book.site
        requestItem.name = ReadState.book.name
        requestItem.author = ReadState.book.author

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
        if (ReadState.currentChapter != null) {
            //if (ReadState.book.dex == 1) {
            url = UrlUtils.buildContentUrl(ReadState.currentChapter!!.curl)
            /*} else if (ReadState.book.dex == 0) {
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
            if (ReadState != null) {
                data.put("bookid", ReadState.book_id)
            }
            StartLogClickUtil.upLoadEventLog(activity.get(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.ORIGINALLINK, data)
        } else {
            activity.get()?.showToastMessage("无法查看原文链接！")
        }
    }

    override fun back() {
        //先这样实现吧...
        val activity = this.activity.get()
        if (activity is ReadingActivity) {
            activity.onBackPressed()
            val data = java.util.HashMap<String, String>()
            data.put("type", "1")
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.BACK, data)
        }
    }

    override fun updateStatus() {
        view?.updateStatus(bookDaoHelper)
    }

    override fun dismissLoadingPage() {
        loadingPage?.onSuccess()
    }

    override fun feedback() {
        (activity.get() as ReadingActivity).onReadFeedBack()
    }
}