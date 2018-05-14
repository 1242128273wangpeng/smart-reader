package com.intelligent.reader.presenter.catalogues

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.Toast
import com.intelligent.reader.activity.ReadingActivity
import com.intelligent.reader.cover.*
import com.intelligent.reader.read.help.BookHelper
import com.quduquxie.network.DataCache
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.component.service.DownloadService
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.*
import net.lzbook.kit.data.db.BookChapterDao
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.net.custom.service.NetService
import net.lzbook.kit.repair_books.RepairHelp
import net.lzbook.kit.request.RequestFactory
import net.lzbook.kit.router.RouterConfig
import net.lzbook.kit.router.RouterUtil
import net.lzbook.kit.utils.*
import java.lang.ref.WeakReference
import java.util.HashMap

/**
 * Created by zhenXiang on 2017\11\22 0022.
 */

class CataloguesPresenter(var act: Activity, var book: Book, var requestItem: RequestItem, var cataloguesContract: CataloguesContract,
                          val onClickListener: View.OnClickListener, val fromCover: Boolean)
    : BookCoverUtil.OnDownloadState, BookCoverUtil.OnDownLoadService, BookCoverViewModel.BookChapterViewCallback {


    var activity: WeakReference<Activity>? = null
    var chapterList: ArrayList<Chapter> = ArrayList<Chapter>()
    var bookmarkList: ArrayList<Bookmark> = ArrayList<Bookmark>()
    private var requestFactory: RequestFactory? = null
    val MESSAGE_FETCH_CATALOG = 0
    val MESSAGE_FETCH_BOOKMARK = MESSAGE_FETCH_CATALOG + 1
    val MESSAGE_FETCH_ERROR = MESSAGE_FETCH_BOOKMARK + 1
    private val DELAY_OVERLAY = MESSAGE_FETCH_ERROR + 1
    var mBookDaoHelper: BookDaoHelper? = null
    var downloadService: DownloadService? = null
    val DOWNLOAD_STATE_FINISH = 1
    val DOWNLOAD_STATE_LOCKED = 2
    val DOWNLOAD_STATE_NOSTART = 3
    val DOWNLOAD_STATE_OTHER = 4
    var bookCoverUtil: BookCoverUtil? = null
    var bookDaoHelper: BookDaoHelper? = null
    var mBookCoverViewModel: BookCoverViewModel? = null


    init {
        activity = WeakReference(act)
        requestFactory = RequestFactory()
        mBookDaoHelper = BookDaoHelper.getInstance()

        mBookCoverViewModel = BookCoverViewModel(BookCoverRepositoryFactory.getInstance(BookCoverOtherRepository.getInstance(NetService.userService),
                BookCoverQGRepository.getInstance(OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext())), BookCoverLocalRepository.getInstance(BaseBookApplication.getGlobalContext())))
        mBookCoverViewModel?.setBookChapterViewCallback(this)

        bookCoverUtil = BookCoverUtil(activity!!.get(), onClickListener)
        bookCoverUtil?.registReceiver()
        bookCoverUtil?.setOnDownloadState(this)
        bookCoverUtil?.setOnDownLoadService(this)


    }

    fun requestCatalogList() {
        val chapterDao = BookChapterDao(activity!!.get(), book.book_id)
        chapterList = chapterDao.queryBookChapter()

        if (chapterList != null && chapterList.size != 0) {
            cataloguesContract!!.requestCatalogSuccess(chapterList)
        } else {
            getRequest()
        }
    }

    //model层回调成功 和 失败
    override fun onFail(msg: String?) {
        cataloguesContract?.requestCatalogError()
    }

    override fun onChapterList(chapters: MutableList<Chapter>?) {
        if (chapters == null) {
            showToastShort("获取数据失败")
        } else {
            this.chapterList = chapters as ArrayList<Chapter>
            cataloguesContract!!.requestCatalogSuccess(chapterList)
        }
    }

    override fun onBookMarkList(bookmarks: MutableList<Bookmark>?) {

        bookmarkList = (bookmarks as ArrayList<Bookmark>?)!!

        if (bookmarks != null) {
            cataloguesContract.notifyDataChange(false, bookmarkList)
        }
    }


    fun loadBookMark() {
        mBookCoverViewModel!!.getBookMarkList(requestItem.book_id)
    }

    //请求书籍目录
    fun getRequest() {
//        if (requestItem != null) {
//            if (Constants.SG_SOURCE == requestItem.host) {
//                myHandler.sendEmptyMessage(RequestExecutor.REQUEST_CATALOG_ERROR)
//            } else {
//                requestFactory!!.requestExecutor(requestItem).requestCatalogList(activity!!.get(), myHandler, requestItem)
//            }
//        }
        if (requestItem != null) {
            mBookCoverViewModel!!.getChapterList(requestItem)
        }
    }

    override fun changeState() {
        cataloguesContract.changeDownloadButtonStatus()
    }

    override fun downLoadService() {
        cataloguesContract.changeDownloadButtonStatus()
    }

    //进入阅读页
    private fun readingBook() {
        if (requestItem == null || book == null) {
            return
        }
        val intent = Intent()
        val bundle = Bundle()
        if (mBookDaoHelper != null && mBookDaoHelper!!.isBookSubed(requestItem.book_id) && book != null && book.sequence != -2) {
            bundle.putInt("sequence", book.sequence)
            bundle.putInt("offset", book.offset)
        } else {
            bundle.putInt("sequence", -1)
        }

        if (book != null) {
            bundle.putSerializable("book", book)
        }
        if (requestItem != null) {
            requestItem.fromType = 1// 打点统计 当前页面来源，所有可能来源的映射唯一字符串。书架(0)/目录页(1)/上一页翻页(2)/书籍封面(3)
            bundle.putSerializable(Constants.REQUEST_ITEM, requestItem)
        }

        if (book != null && requestItem != null && Constants.QG_SOURCE == book.site) {
            requestItem.channel_code = 1
        } else {
            requestItem.channel_code = 2
        }

        intent.setClass(activity!!.get(), ReadingActivity::class.java)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        activity!!.get()!!.startActivity(intent)
        activity!!.get()!!.finish()
    }

    //加入书架 目前只有免费小说书城有这个功能
    fun addBookIntoShelf() {
        if (mBookDaoHelper == null) {
            return
        }
        val data2 = HashMap<String, String>()

        if (requestItem != null && !mBookDaoHelper!!.isBookSubed(requestItem.book_id)) {


            if (book != null) {
                val succeed = mBookDaoHelper!!.insertBook(book)
                if (succeed) {
                    cataloguesContract.successAddIntoShelf(true)
                    data2.put("type", "1")
                    //添加书架打点
                    showToastShort("成功添加到书架!")
                }
            }
        } else {
            showToastShort("已在书架中！")
        }

        StartLogClickUtil.upLoadEventLog(activity!!.get(), StartLogClickUtil.BOOKCATALOG, StartLogClickUtil.CATALOG_CASHEALL, data2)
    }


    /**
     * 点击item 进入阅读页
     * isCatalog true 目录点击  false 书签点击
     */
    fun catalogToReading(position: Int, isCatalog: Boolean) {
        val intent = Intent()
        val bundle = Bundle()
        requestItem.fromType = 1 // 打点统计 当前页面来源，所有可能来源的映射唯一字符串。书架(0)/目录页(1)/上一页翻页(2)/书籍封面(3)
        if (isCatalog) {
            if (chapterList != null && !chapterList.isEmpty()) {
                val isChapterExist: Boolean
                val tempChapter = chapterList.get(position)
                if (requestItem.host == Constants.QG_SOURCE) {
                    requestItem.channel_code = 1
                    isChapterExist = DataCache.isChapterExists(tempChapter.chapter_id, tempChapter.book_id)
                } else {
                    requestItem.channel_code = 2
                    isChapterExist = BookHelper.isChapterExist(tempChapter)
                }
                if (!isChapterExist && NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
                    showToastShort("网络不给力，请稍后重试")
                    return
                }

                bundle.putSerializable(Constants.REQUEST_ITEM, requestItem)
                bundle.putInt("sequence", chapterList.get(position).sequence)

                val data1 = HashMap<String, String>()
                data1.put("bookid", requestItem.book_id)
                data1.put("chapterid", tempChapter.chapter_id)
                StartLogClickUtil.upLoadEventLog(activity!!.get(), StartLogClickUtil.BOOKCATALOG, StartLogClickUtil.CATALOG_CATALOGCHAPTER, data1)
            }
        } else {
            if (bookmarkList != null) {
                val bookmark = bookmarkList.get(position)
                if (bookmark != null) {
                    bundle.putInt("sequence", bookmark.sequence)
                    bundle.putInt("offset", bookmark.offset)
                    bundle.putSerializable(Constants.REQUEST_ITEM, requestItem)
                }
            }
        }
        bundle.putSerializable("book", book)
//        bundle.putString("thememode", mThemeHelper.getMode())
        intent.putExtras(bundle)
        if (fromCover) {
            intent.setClass(activity!!.get(), ReadingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            activity!!.get()!!.startActivity(intent)
        } else {
            activity!!.get()!!.setResult(Activity.RESULT_OK, intent)
        }
        activity!!.get()!!.finish()

    }

    fun activityResult(sequence: Int) {
        val intent = Intent()
        val bundle = Bundle()
        bundle.putSerializable(Constants.REQUEST_ITEM, requestItem)
        bundle.putInt("sequence", sequence)
        bundle.putSerializable("book", book)
        intent.putExtras(bundle)
        activity!!.get()!!.setResult(Activity.RESULT_OK, intent)
    }

    //缓存
    fun startDownLoader() {

        if (book == null)
            return

        var downloadState = CacheManager.getBookStatus(book)
        if (downloadState != DownloadState.FINISH && downloadState != DownloadState.WAITTING && downloadState != DownloadState.DOWNLOADING) {
            showToastShort("正在缓存中。。。")
        }

        //全本缓存的点击统计
        StatServiceUtils.statAppBtnClick(activity!!.get(), StatServiceUtils.b_details_click_all_load)

        if (mBookDaoHelper == null) {
            mBookDaoHelper = BookDaoHelper.getInstance()
        }
        if (mBookDaoHelper != null && bookCoverUtil != null) {
            if (!mBookDaoHelper!!.isBookSubed(requestItem.book_id)) {

                val succeed = mBookDaoHelper!!.insertBook(book)
                if (succeed) {
                    cataloguesContract.successAddIntoShelf(true)
                    showToastShort("成功添加到书架!")


                    BaseBookHelper.startDownBookTask(activity!!.get(), requestItem.toBook(), 0);
                }
            } else {
                BaseBookHelper.startDownBookTask(activity!!.get(), requestItem.toBook(), 0);
            }
        }
        cataloguesContract.changeDownloadButtonStatus()
    }


    fun removeHandler() {
        myHandler?.removeCallbacksAndMessages(null)
    }

    fun unRegisterRec() {
        if (bookCoverUtil != null) {
            bookCoverUtil!!.unRegistReceiver()
            bookCoverUtil = null
        }
    }

    fun delayOverLayHandler() {
        cataloguesContract.handOverLay()

//        if (myHandler != null) {
//            myHandler.removeMessages(DELAY_OVERLAY)
//            myHandler.sendEmptyMessageDelayed(DELAY_OVERLAY, 1500)
//        }
    }

    fun onEventReceive(bookmark: Bookmark) {
        if (bookmark != null) {
            val deleteList = java.util.ArrayList<Int>()
            deleteList.add(bookmark.id)
            cataloguesContract.deleteBookmarks(deleteList)
        }

    }


    //删除标签

    fun doDeleteBookmarks(list: ArrayList<Int>) {
        if (mBookDaoHelper == null) {
            mBookDaoHelper = BookDaoHelper.getInstance()
        }

        mBookDaoHelper!!.deleteBookMark(list as ArrayList<Int>, 0)

        val marks = mBookDaoHelper!!.getBookMarks(book.book_id)
        if (bookmarkList != null)
            bookmarkList.clear()
        if (marks != null && bookmarkList != null) {
            for (bookmark in marks) {
                bookmarkList.add(bookmark)
            }
        }
        cataloguesContract.notifyDataChange(true, bookmarkList)

    }


    //修复
    fun fixBook() {
        RepairHelp.fixBook(activity!!.get(), book, RepairHelp.FixCallBack {
            try {
                RouterUtil.navigation(act, RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
                activity!!.get()!!.finish()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }



    private val myHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val cataloguesActivity = activity!!.get() ?: return
            when (msg.what) {
                DELAY_OVERLAY -> cataloguesContract.handOverLay()
            }
        }
    }

    fun getDataSuccess(msg: Message) {

        if (msg != null && msg.obj != null) {
            chapterList = msg.obj as java.util.ArrayList<Chapter>
        }

        if (chapterList == null) {
            showToastShort("获取数据失败")
        } else {
            cataloguesContract!!.requestCatalogSuccess(chapterList)
        }

    }

    private fun showToastShort(s: String) {
        if (activity != null) {
            Toast.makeText(activity!!.get(), s, Toast.LENGTH_SHORT).show()
        }
    }


}
