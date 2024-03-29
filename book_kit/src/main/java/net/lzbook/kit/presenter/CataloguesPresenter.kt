package net.lzbook.kit.presenter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.Book
import com.ding.basic.bean.Bookmark
import com.ding.basic.bean.Chapter
import com.ding.basic.net.RequestSubscriber
import com.ding.basic.net.rx.SchedulerHelper
import com.dingyue.statistics.DyStatService
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.R
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.model.BookCoverViewModel
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.ui.widget.MyDialog
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.book.BaseBookHelper
import net.lzbook.kit.utils.book.BookCoverUtil
import net.lzbook.kit.utils.book.RepairHelp
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.utils.download.DownloadState
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.view.CataloguesContract
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.set

class CataloguesPresenter(private val activity: Activity, private val book: Book,
                          private val cataloguesContract: CataloguesContract,
                          private val onClickListener: View.OnClickListener?,
                          private val fromCover: Boolean) {

    var chapterList: ArrayList<Chapter> = ArrayList()
    private var bookmarkList: ArrayList<Bookmark> = ArrayList()
    private val DELAY_OVERLAY = 3

    private val bookCoverUtil: BookCoverUtil by lazy {
        val util = BookCoverUtil(activity, onClickListener)
        util.registReceiver()
        util.setOnDownloadState {
            cataloguesContract.changeDownloadButtonStatus()
        }
        util.setOnDownLoadService {
            cataloguesContract.changeDownloadButtonStatus()
        }
        util
    }

    private val bookCoverViewModel: BookCoverViewModel by lazy {
        val viewModel = BookCoverViewModel()
        viewModel.setBookChapterViewCallback { bookmarks ->
            if (bookmarks != null) {
                bookmarkList = bookmarks
                cataloguesContract.notifyDataChange(false, bookmarkList)
            }
        }
        viewModel
    }


    fun requestCatalogList(changeSource: Boolean) {

        val requestSubscriber = object : RequestSubscriber<List<Chapter>>() {
            override fun requestResult(result: List<Chapter>?) {
                if (result != null) {
                    chapterList.clear()
                    chapterList.addAll(result)
                    cataloguesContract.requestCatalogSuccess(chapterList)
                } else {
                    cataloguesContract.requestCatalogError()
                }

                Observable.create<Boolean> {
                    CacheManager.freshBook(book.book_id, false)
                    it.onNext(true)
                    it.onComplete()
                }.subscribeOn(Schedulers.io()).subscribe { }
            }

            override fun requestError(message: String) {
                cataloguesContract.requestCatalogError()
            }
        }

        if (changeSource) {
            RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                    .requestBookCatalog(book.book_id, book.book_source_id, book.book_chapter_id, requestSubscriber, SchedulerHelper.Type_Main)
        } else {
            RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                    .requestCatalog(book.book_id, book.book_source_id, book.book_chapter_id, requestSubscriber, SchedulerHelper.Type_Main)
        }
    }

    fun loadBookMark() {
        bookCoverViewModel.getBookMarkList(book.book_id)
    }

    fun addToBookShelf() {
        val data = HashMap<String, String>()
        val requestFactory = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
        val subscribedBook = requestFactory.checkBookSubscribe(book.book_id)

        if (subscribedBook != null) {
            cataloguesContract.successAddIntoShelf(false)
        } else {
            val insert = requestFactory.insertBook(book)
            if (insert >= 0) {
                data["type"] = "1"
                cataloguesContract.successAddIntoShelf(true)
            }
        }
    }


    /***
     * 刷新底部按钮状态
     * **/
    fun refreshBottomState() {

        val book = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(book.book_id)
        if (book != null) {
            cataloguesContract.successAddIntoShelf(true)
        } else {
            cataloguesContract.successAddIntoShelf(false)
        }
    }

    fun showReadDialog() {
        cataloguesContract.showReadDialog()
    }


    /**
     * 点击item 进入阅读页
     * isCatalog true 目录点击  false 书签点击
     */
    fun catalogToReading(position: Int, isCatalog: Boolean) {
        val bundle = Bundle()
        val flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        book.fromType = 1
        if (isCatalog) {
            if (!chapterList.isEmpty()) {
                val tempChapter = chapterList[Math.max(0, position)]
                val loadFactory = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                if (book.fromQingoo()) {
                    book.channel_code = 1
                } else {
                    book.channel_code = 2
                }
                if (!loadFactory.isChapterCacheExist(tempChapter) && NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
                    ToastUtil.showToastMessage("网络不给力，请稍后重试！")
                    return
                }

                book.sequence = tempChapter.sequence
                book.offset = 0

                val logData = HashMap<String, String>()
                logData["bookid"] = book.book_id
                logData["chapterid"] = tempChapter.chapter_id
                DyStatService.onEvent(EventPoint.BOOKCATALOG_CATALOGCHAPTER, logData)
            }
        } else {
            val bookmark = bookmarkList[position]
            book.sequence = bookmark.sequence
            book.offset = bookmark.offset
        }
        bundle.putSerializable("book", book)
        bundle.putInt("sequence", book.sequence)

        navigateToReading(bundle, flags)

    }

    fun continueReading() {

        val requestFactory = RequestRepositoryFactory
                .loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
        val bundle = Bundle()
        val subscribedBook = requestFactory.checkBookSubscribe(book.book_id)
        if (subscribedBook != null && book.sequence != -2) {
            bundle.putInt("sequence", book.sequence ?: -2)
            bundle.putInt("offset", book.offset ?: -1)
        } else {
            bundle.putInt("sequence", -1)
        }

        book.fromType = 1// 打点统计 当前页面来源，所有可能来源的映射唯一字符串。书架(0)/目录页(1)/上一页翻页(2)/书籍封面(3)

        if (book.fromQingoo()) {
            book.channel_code = 1
        } else {
            book.channel_code = 2
        }
        bundle.putSerializable("book", book)

        val flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        navigateToReading(bundle, flags)
    }

    private fun navigateToReading(bundle: Bundle, flags: Int) {
        if (fromCover) {
            RouterUtil.navigation(activity, RouterConfig.READER_ACTIVITY, bundle, flags)
        } else {
            val intent = Intent()
            intent.putExtras(bundle)
            activity.setResult(Activity.RESULT_OK, intent)
        }
        activity.finish()
    }

    fun activityResult(sequence: Int) {
        val intent = Intent()
        val bundle = Bundle()
        bundle.putInt("sequence", sequence)
        bundle.putSerializable("book", book)
        intent.putExtras(bundle)
        activity.setResult(Activity.RESULT_OK, intent)
    }

    fun startDownload() {
        //全本缓存的点击统计
        StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.b_details_click_all_load)
        DyStatService.onEvent(EventPoint.BOOOKDETAIL_CASHEALL, mapOf("bookid" to book.book_id))

        val requestFactory = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
        val subscribedBook = requestFactory.checkBookSubscribe(book.book_id)
        if (subscribedBook != null) {
            BaseBookHelper.startDownBookTask(activity, book, 0)
        } else {
            val insert = requestFactory.insertBook(book)
            if (insert >= 0) {
                cataloguesContract.successAddIntoShelf(true)
                BaseBookHelper.startDownBookTask(activity, book, 0)
            }
        }
        cataloguesContract.changeDownloadButtonStatus()
    }

    fun registerRec() {
        bookCoverUtil.registReceiver()
    }

    fun removeHandler() {
        myHandler.removeCallbacksAndMessages(null)
    }

    fun unRegisterRec() {
        bookCoverUtil.unRegisterReceiver()
    }

    fun delayOverLayHandler() {
        cataloguesContract.handOverLay()
    }

    fun onEventReceive(bookmark: Bookmark) {
        val deleteList = ArrayList<Int>()
        deleteList.add(bookmark.id)
        cataloguesContract.deleteBookmarks(deleteList)

    }

    //删除标签

    fun doDeleteBookmarks(list: ArrayList<Int>) {

        val bookHelper = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
        bookHelper.deleteBookMark(list)
        val marks = bookHelper.getBookMarks(book.book_id)
        bookmarkList.clear()
        for (bookmark in marks) {
            bookmarkList.add(bookmark)
        }
        cataloguesContract.notifyDataChange(true, bookmarkList)

    }


    //修复
    fun fixBook() {
        RepairHelp.fixBook(activity, book, {
            try {
                RouterUtil.navigation(activity, RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
                activity.finish()
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        })
    }


    fun handleReadingAction() {
        if (activity.isFinishing) {
            return
        }

        if (!activity.isFinishing) {
            intoReadingActivity()
        }
    }

    /***
     * 进入阅读页
     * **/
    private fun intoReadingActivity() {
        if (TextUtils.isEmpty(book.book_id)) {
            return
        }

        val bundle = Bundle()

        val flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val localBook = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(book.book_id)

        if (localBook != null) {
            if (book.sequence != -2) {
                bundle.putInt("sequence", localBook.sequence)
                bundle.putInt("offset", localBook.offset)
            } else {
                bundle.putInt("sequence", -1)
                bundle.putInt("offset", 0)
            }

            bundle.putSerializable("book", localBook)
        } else {
            bundle.putSerializable("book", book)
        }

        RouterUtil.navigation(activity, RouterConfig.READER_ACTIVITY, bundle, flags)
    }

    /***
     * 处理添加、移除书架操作
     * **/
    fun handleBookShelfAction(removeAble: Boolean) {
        if (TextUtils.isEmpty(book.book_id)) {
            return
        }

        val localBook = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(book.book_id)

        if (localBook != null) {
            Logger.v("书籍已订阅！")

            if (removeAble) {
                cataloguesContract.insertBookShelfResult(false)

                ToastUtil.showToastMessage("成功从书架移除！")

                DyStatService.onEvent(EventPoint.BOOOKDETAIL_SHELFADD, mapOf("type" to "2", "bookid" to localBook.book_id))

                cataloguesContract.changeDownloadButtonStatus()

                cataloguesContract.changeShelfButtonClickable(false)

                val cleanDialog = MyDialog(activity, R.layout.dialog_download_clean)
                cleanDialog.setCanceledOnTouchOutside(false)
                cleanDialog.setCancelable(false)
                cleanDialog.findViewById<TextView>(R.id.dialog_msg).setText(R.string.tip_cleaning_cache)
                cleanDialog.show()

                Observable.create(ObservableOnSubscribe<Boolean> { emitter ->
                    CacheManager.remove(localBook.book_id)

                    RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).deleteBook(localBook.book_id)

                    BaseBookHelper.removeChapterCacheFile(localBook)

                    emitter.onNext(true)
                    emitter.onComplete()
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            Logger.v("移除书架成功！")
                            cleanDialog.dismiss()
                            cataloguesContract.changeShelfButtonClickable(true)
                            cataloguesContract.changeDownloadButtonStatus()
                        }

            } else {
                ToastUtil.showToastMessage("已在书架中！")
            }
        } else {
            Logger.v("书籍未订阅！")

            book.last_update_success_time = System.currentTimeMillis()

            val result = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).insertBook(book)

            if (result <= 0) {
                Logger.v("加入书架失败！")
                if (result != Constants.INSERT_BOOKSHELF_FULL) {
                    ToastUtil.showToastMessage("加入书架失败！")
                }
            } else {
                Logger.v("加入书架成功！")

                DyStatService.onEvent(EventPoint.BOOOKDETAIL_SHELFADD, mapOf("type" to "1","bookid" to book.book_id))

                ToastUtil.showToastMessage("成功添加到书架！")

                cataloguesContract.insertBookShelfResult(true)
            }
        }
    }

    /***
     * 缓存书籍内容
     * **/
    fun handleDownloadAction() {
        DyStatService.onEvent(EventPoint.BOOOKDETAIL_CASHEALL, mapOf("bookid" to book.book_id))

        if (TextUtils.isEmpty(book.book_id)) {
            return
        }
        val downloadState = CacheManager.getBookStatus(book)
        if (downloadState != DownloadState.FINISH && downloadState != DownloadState.WAITTING && downloadState != DownloadState.DOWNLOADING) {
            ToastUtil.showToastMessage("正在缓存中...")
        }

        val localBook = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(book.book_id)

        if (localBook != null) {
            BaseBookHelper.startDownBookTask(activity, book, 0)
        } else {
            val result = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).insertBook(book)

            if (result > 0) {
                cataloguesContract.insertBookShelfResult(true)
                ToastUtil.showToastMessage("成功添加到书架！")

                BaseBookHelper.startDownBookTask(activity, book, 0)
            }
        }
        cataloguesContract.changeDownloadButtonStatus()
    }


    /***
     * 刷新底部按钮状态
     * **/
    fun refreshNavigationState() {
        if (TextUtils.isEmpty(book.book_id)) {
            return
        }

        cataloguesContract.changeDownloadButtonStatus()

        val book = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(book.book_id)
        if (book != null) {
            cataloguesContract.bookSubscribeState(true)
        } else {
            cataloguesContract.bookSubscribeState(false)
        }
    }

    /***
     * 判断是否存在书架
     * **/
    fun checkBookSubscribe(): Boolean = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(book.book_id) != null


    private val myHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                DELAY_OVERLAY -> cataloguesContract.handOverLay()
            }
        }
    }

}