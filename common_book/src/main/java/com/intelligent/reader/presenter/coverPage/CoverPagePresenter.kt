package com.intelligent.reader.presenter.coverPage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.*
import com.ding.basic.bean.Book
import com.ding.basic.repository.RequestRepositoryFactory
import com.dingyue.contract.util.SharedPreUtil
import com.dingyue.contract.util.showToastMessage
import com.intelligent.reader.R
import com.intelligent.reader.activity.CataloguesActivity
import com.intelligent.reader.activity.ReadingActivity
import com.intelligent.reader.activity.SearchBookActivity
import com.intelligent.reader.cover.*
import com.intelligent.reader.widget.ConfirmDialog
import com.intelligent.reader.widget.TransformReadDialog
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.component.service.DownloadService
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.book.view.RecommendItemView
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.CoverPage
import net.lzbook.kit.data.recommend.CoverRecommendBean
import net.lzbook.kit.utils.*
import java.util.*
import kotlin.collections.ArrayList

class CoverPagePresenter(private val bookId: String?, private var bookSourceId: String?, private var bookChapterId: String?, val coverPageContract: CoverPageContract, val activity: Activity, val onClickListener: View.OnClickListener)
    : BookCoverUtil.OnDownloadState, BookCoverUtil.OnDownLoadService, BookCoverViewModel.BookCoverViewCallback {
    
    var coverDetail: Book? = null
    var showMoreLabel: Boolean = false

    var downloadService: DownloadService? = null
    var bookCoverUtil: BookCoverUtil? = null
    var sharePreUtil: SharedPreUtil? = null
    var books = ArrayList<Book>()
    var mBookCoverViewModel: BookCoverViewModel? = null
    val mDisposables: CompositeDisposable = CompositeDisposable()

    init {
        sharePreUtil = SharedPreUtil(SharedPreUtil.SHARE_ONLINE_CONFIG)
        mBookCoverViewModel = BookCoverViewModel()
        mBookCoverViewModel?.setBookCoverViewCallback(this)

        bookCoverUtil = BookCoverUtil(activity, onClickListener)

        bookCoverUtil?.registReceiver()
        bookCoverUtil?.setOnDownloadState(this)
        bookCoverUtil?.setOnDownLoadService(this)
    }

    private val transformReadDialog: TransformReadDialog by lazy {

        val dialog = TransformReadDialog(activity)

//        dialog.setOnConfirmListener {
//            val data = HashMap<String, String>()
//            data.put("type", "1")
//            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEPOPUP, data)
//
//            requestItem.fromType = 3// 打点统计 当前页面来源，所有可能来源的映射唯一字符串。书架(0)/目录页(1)/上一页翻页(2)/书籍封面(3)
//            if (bookDaoHelper!!.isBookSubed(bookVo!!.book_id)) {
//                val book = bookDaoHelper!!.getBook(bookVo!!.book_id, 0)
//                if (Constants.QG_SOURCE == requestItem.host) {
//                    readingCustomaryBook(null, false)
//
//                } else {
//                    if (currentSource?.book_source_id == book?.book_source_id) {
//                        //直接进入阅读
//                        readingCustomaryBook(currentSource, true)
//                    } else {
//                        //弹出切源提示
//                        showChangeSourceNoticeDialog(currentSource!!)
//                    }
//                }
//            } else {
//                continueReading()
//            }
//            dialog?.dismiss()
//        }
//        dialog.setOnCancelListener {
//            val data = HashMap<String, String>()
//            data.put("type", "2")
//            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEPOPUP, data)
//            if (!activity.isFinishing) {
//                dialog.dismiss()
//            }
//        }
        dialog
    }
    
    /***
     * 获取书籍详情
     * **/
    fun requestBookDetail(showMoreLabel: Boolean) {
        this.showMoreLabel = showMoreLabel
        mBookCoverViewModel!!.requestBookDetail(bookId, bookSourceId, bookChapterId)
    }

    /***
     * 获取书籍详情失败
     * **/
    override fun requestCoverDetailFail(msg: String?) {
        coverPageContract.showLoadingFail()
    }

    /***
     * 获取书籍详情成功
     * **/
    override fun requestCoverDetailSuccess(book: Book?) {
        handleCoverDetailSuccess(book)
    }

    /***
     * 处理书籍信息
     * **/
    private fun handleCoverDetailSuccess(book: Book?) {
        if (book != null) {
            this.coverDetail = book

            if (coverDetail != null && bookCoverUtil != null) {
                bookCoverUtil?.saveHistory(coverDetail)
            }
        }

        coverPageContract.showLoadingSuccess()
        coverPageContract.showCoverDetail(coverDetail)
        coverPageContract.changeDownloadButtonStatus()
    }
    
    /***
     * 跳转到目录页
     * **/
    fun startCatalogActivity(clickedCatalog: Boolean) {
        if (coverDetail == null) {
            return
        }

        val intent = Intent()

        if (clickedCatalog) {
            handleCatalogAction(intent, 0, false)
        } else {
            handleCatalogAction(intent, coverDetail!!.last_chapter!!.serial_number - 1, true)
        }
    }
    
    /***
     * 处理跳转目录操作
     * **/
    private fun handleCatalogAction(intent: Intent, sequence: Int, indexLast: Boolean) {
        if (coverDetail != null && bookCoverUtil != null) {

            val bundle = Bundle()
            bundle.putInt("sequence", sequence)
            bundle.putBoolean("fromCover", true)
            bundle.putBoolean("is_last_chapter", indexLast)
            bundle.putSerializable("cover", coverDetail)

            intent.setClass(activity, CataloguesActivity::class.java)
            intent.putExtras(bundle)

            activity.startActivity(intent)
        }
    }
    
    /***
     * 处理添加、移除书架操作
     * **/
    fun handleBookShelfAction(removeAble: Boolean) {
        if (coverDetail == null || TextUtils.isEmpty(coverDetail!!.book_id)) {
            return
        }

        val book = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(bookId!!)

        if (book != null) {
            Logger.v("书籍已订阅！")

            if (removeAble) {
                coverPageContract.insertBookShelfResult(false)

                //移除书架的打点
                StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.b_details_click_book_remove)

                activity.applicationContext.showToastMessage("成功从书架移除！")

                val data = HashMap<String, String>()
                data["type"] = "2"
                data["bookid"] = coverDetail!!.book_id

                StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.SHELFEDIT, data)

                coverPageContract.changeDownloadButtonStatus()

                coverPageContract.changeShelfButtonClickable(false)

                val cleanDialog = MyDialog(activity, R.layout.dialog_download_clean)
                cleanDialog.setCanceledOnTouchOutside(false)
                cleanDialog.setCancelable(false)
                (cleanDialog.findViewById(R.id.dialog_msg) as TextView).setText(R.string.tip_cleaning_cache)
                cleanDialog.show()

                Observable.create(ObservableOnSubscribe<Boolean> { emitter ->
                    CacheManager.remove(coverDetail!!.book_id)

                    RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).deleteBook(coverDetail!!.book_id)

                    BaseBookHelper.removeChapterCacheFile(coverDetail!!)

                    emitter.onNext(true)
                    emitter.onComplete()
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            Logger.v("移除书架成功！")
                            cleanDialog.dismiss()
                            coverPageContract.changeShelfButtonClickable(true)
                            coverPageContract.changeDownloadButtonStatus()
                        }

            } else {
                activity.applicationContext.showToastMessage("已在书架中！")
            }
        } else {
            Logger.v("书籍未订阅！")

            if (coverDetail == null) {
                activity.applicationContext.showToastMessage("书籍信息异常，请稍后再试！")
            }

            coverDetail?.last_update_success_time = System.currentTimeMillis()

            val result = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).insertBook(coverDetail!!)

            if (result <= 0) {
                Logger.v("加入书架失败！")
                activity.applicationContext.showToastMessage("加入书架失败！")
            } else {
                Logger.v("加入书架成功！")

                val data = HashMap<String, String>()
                data["type"] = "1"
                data["bookid"] = coverDetail!!.book_id

                StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.SHELFEDIT, data)

                activity.applicationContext.showToastMessage("成功添加到书架！")

                coverPageContract.insertBookShelfResult(true)
            }
        }
    }

    /***
     * 处理跳转阅读页请求
     * **/
    fun handleReadingAction() {
        if (activity.isFinishing) {
            return
        }
        val readingSourceDialog = MyDialog(activity, R.layout.dialog_read_source, Gravity.CENTER)
        readingSourceDialog.setCanceledOnTouchOutside(true)

        val change_source_head = readingSourceDialog.findViewById(R.id.dialog_top_title) as TextView
        change_source_head.text = "转码"

        val change_source_original_web = readingSourceDialog.findViewById(R.id.change_source_original_web) as TextView
        change_source_original_web.setText(R.string.cancel)

        val change_source_continue = readingSourceDialog.findViewById(R.id.change_source_continue) as TextView

        change_source_original_web.setOnClickListener {
            val data = HashMap<String, String>()
            data["type"] = "2"
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEPOPUP, data)

            readingSourceDialog.dismiss()
        }

        change_source_continue.setOnClickListener {
            val data = HashMap<String, String>()
            data["type"] = "1"
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEPOPUP, data)

            val book = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(bookId!!)
            if (book != null) {
                if (book.book_type == "qg") {
                    readingShelfBook()
                } else {
                    if (coverDetail?.book_source_id == book.book_source_id) {
                        readingShelfBook()
                        readingSourceDialog.dismiss()
                    } else {
                        intoReadingActivity()
                        readingSourceDialog.dismiss()
                    }
                }
            } else {
                continueReading()
            }

            if (readingSourceDialog.isShowing) {
                readingSourceDialog.dismiss()
            }
        }

        if (!readingSourceDialog.isShowing) {
            try {
                readingSourceDialog.show()
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }















    /**
     * 进入阅读页
     */
    private fun continueReading() {
        val intent = Intent()
        val bundle = Bundle()
        if (bookDaoHelper == null || bookCoverUtil == null || bookVo == null) {
            return
        }
        val book = bookCoverUtil!!.getCoverBook(bookDaoHelper, bookVo)
        if (bookDaoHelper!!.isBookSubed(requestItem.book_id) && book != null && book.sequence != -2) {
            bundle.putInt("sequence", book.sequence)
            bundle.putInt("offset", book.offset)
        } else {
            bundle.putInt("sequence", -1)
        }
        if (book != null) {
            if (Constants.QG_SOURCE == bookVo!!.host) {
                book.last_updatetime_native = bookVo!!.update_time
            } else {
                if (currentSource != null) {
                    book.last_updatetime_native = currentSource!!.update_time
                }
            }
            bundle.putSerializable("book", book)
        }
        bundle.putSerializable(Constants.REQUEST_ITEM, requestItem)
        intent.setClass(activity, ReadingActivity::class.java)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        activity.startActivity(intent)
    }

    private val clearCacheDialog: ConfirmDialog by lazy {
        val dialog = ConfirmDialog(activity)
        dialog.setTitle("转码")
        dialog.setContent(activity.getString(R.string.translate_code_read))
        dialog.setConfirmName(activity.getString(R.string.reading_continue))
        dialog.setOnCancelName(activity.getString(R.string.cancel))
        dialog.setOnConfirmListener {
            val data = HashMap<String, String>()
            data.put("type", "1")
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEPOPUP, data)

            requestItem.fromType = 3// 打点统计 当前页面来源，所有可能来源的映射唯一字符串。书架(0)/目录页(1)/上一页翻页(2)/书籍封面(3)
            if (bookDaoHelper!!.isBookSubed(bookVo!!.book_id)) {
                val book = bookDaoHelper!!.getBook(bookVo!!.book_id, 0)
                if (Constants.QG_SOURCE == requestItem.host) {
                    readingCustomaryBook(null, false)

                } else {
                    if (currentSource?.book_source_id == book?.book_source_id) {
                        //直接进入阅读
                        readingCustomaryBook(currentSource, true)
                    } else {
                        //弹出切源提示
                        showChangeSourceNoticeDialog(currentSource!!)
                    }
                }
            } else {
                continueReading()
            }
            dialog?.dismiss()
        }
        dialog.setOnCancelListener {
            val data = HashMap<String, String>()
            data.put("type", "2")
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEPOPUP, data)
            if (!activity.isFinishing) {
                dialog.dismiss()
            }
        }
        dialog
    }

    private fun showReadingSourceDialog() {
        if (!activity.isFinishing) {
            clearCacheDialog.show()
        }
//        ConfirmPopWindow.newBuilder(activity).title("转码")
//                .cancelButtonName(activity.getString(R.string.cancel))
//                .confirmButtonName(activity.getString(R.string.reading_continue))
//                .setOnConfirmListener(object : ConfirmPopWindow.OnConfirmListener {
//                    override fun onConfirm(view: View?) {
//                        val data = HashMap<String, String>()
//                        data.put("type", "2")
//                        StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEPOPUP, data)
//                    }
//
//                    override fun onCancel(view: View?) {
//                        val data = HashMap<String, String>()
//                        data.put("type", "1")
//                        StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEPOPUP, data)
//
//                        requestItem.fromType = 3// 打点统计 当前页面来源，所有可能来源的映射唯一字符串。书架(0)/目录页(1)/上一页翻页(2)/书籍封面(3)
//                        if (bookDaoHelper!!.isBookSubed(bookVo!!.book_id)) {
//                            val book = bookDaoHelper!!.getBook(bookVo!!.book_id, 0)
//                            if (Constants.QG_SOURCE == requestItem.host) {
//                                readingCustomaryBook(null, false)
//
//                            } else {
//                                if (currentSource?.book_source_id == book?.book_source_id) {
//                                    //直接进入阅读
//                                    readingCustomaryBook(currentSource, true)
//                                } else {
//                                    //弹出切源提示
//                                    showChangeSourceNoticeDialog(currentSource!!)
//                                }
//                            }
//                        } else {
//                            continueReading()
//                        }
//                    }
//
//                }).build().show()
//        val readingSourceDialog = MyDialog(activity, R.layout.dialog_read_source, Gravity.CENTER)
//        readingSourceDialog.setCanceledOnTouchOutside(true)
//        val change_source_head = readingSourceDialog.findViewById(R.id.dialog_top_title) as TextView
//        change_source_head.text = "转码"
//        val change_source_original_web = readingSourceDialog.findViewById(R.id.change_source_original_web) as TextView
//        change_source_original_web.setText(R.string.cancel)
//        val change_source_continue = readingSourceDialog.findViewById(R.id.change_source_continue) as TextView
//
//        change_source_original_web.setOnClickListener {
//            val data = HashMap<String, String>()
//            data.put("type", "2")
//            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEPOPUP, data)
//
//            readingSourceDialog.dismiss()
//        }
//        change_source_continue.setOnClickListener {
//            val data = HashMap<String, String>()
//            data.put("type", "1")
//            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEPOPUP, data)
//
//            requestItem.fromType = 3// 打点统计 当前页面来源，所有可能来源的映射唯一字符串。书架(0)/目录页(1)/上一页翻页(2)/书籍封面(3)
//            if (bookDaoHelper!!.isBookSubed(bookVo!!.book_id)) {
//                val book = bookDaoHelper!!.getBook(bookVo!!.book_id, 0)
//                if (Constants.QG_SOURCE == requestItem.host) {
//                    readingCustomaryBook(null, false)
//
//                } else {
//                    if (currentSource?.book_source_id == book?.book_source_id) {
//
//                        //直接进入阅读
//                        readingCustomaryBook(currentSource, true)
//                        readingSourceDialog.dismiss()
//                    } else {
//                        //弹出切源提示
//                        readingSourceDialog.dismiss()
//                        showChangeSourceNoticeDialog(currentSource!!)
//                    }
//                }
//            } else {
//                continueReading()
//            }
//            if (readingSourceDialog.isShowing()) {
//                readingSourceDialog.dismiss()
//            }
//        }
//
//        if (!readingSourceDialog.isShowing()) {
//            try {
//                readingSourceDialog.show()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//
//        }
    }

    fun bookCoverReading() {
        if (bookSourceList.size == 0) {
            if (Constants.QG_SOURCE == requestItem.host) {
                showReadingSourceDialog()
                requestItem.fromType = 1// 打点统计 当前页面来源，所有可能来源的映射唯一字符串。书架(0)/目录页(1)/上一页翻页(2)/书籍封面(3)
            } else {
                showToastShort("当前书籍不能阅读，先去看看其他书吧")
            }
        } else {
            showReadingSourceDialog()
        }
    }

    private fun showChangeSourceNoticeDialog(source: CoverPage.SourcesBean) {
        if (!activity.isFinishing) {
            mDialog = MyDialog(activity, R.layout.pop_confirm_layout)

            mDialog?.let {
                it.setCanceledOnTouchOutside(true)
                val dialogCancel = it.findViewById(R.id.cancelBt) as Button
                dialogCancel.setText(R.string.book_cover_continue_read_cache)

                val dialogConfirm = it.findViewById(R.id.okBt) as Button
                dialogConfirm.setText(R.string.book_cover_confirm_change_source)
                val dialogInformation = it.findViewById(R.id.publish_content) as TextView
                dialogInformation.setText(R.string.book_cover_change_source_prompt)
                dialogCancel.setOnClickListener {
                    mDialog?.let {
                        if (it.isShowing) {
                            it.dismiss()
                        }
                    }
                    readingCustomaryBook(source, false)
                }
                dialogConfirm.setOnClickListener {

                    mDialog?.let {
                        if (it.isShowing) {
                            it.dismiss()
                        }
                        intoReadingActivity(source)
                    }

                }

                it.setOnCancelListener({
                    mDialog?.dismiss()
                })
                if (!it.isShowing) {
                    try {
                        it.show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }


        }
    }


    private fun intoReadingActivity(source: CoverPage.SourcesBean) {
        //进入阅读页逻辑
        val intent = Intent()
        val bundle = Bundle()
        if (bookDaoHelper == null || bookCoverUtil == null || bookVo == null) {
            return
        }
        var book: Book? = bookCoverUtil!!.getCoverBook(bookDaoHelper, bookVo)
        if (bookDaoHelper!!.isBookSubed(requestItem.book_id) && book != null && book.sequence != -2) {
            bundle.putInt("sequence", book.sequence)
        } else {
            bundle.putInt("sequence", -1)
        }


//        if (requestItem != null) {
        requestItem.book_id = source.book_id
        requestItem.book_source_id = source.book_source_id
        requestItem.host = source.host
        requestItem.dex = source.dex
        val iterator = source.source.entries.iterator()
        val list = java.util.ArrayList<String>()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val value = entry.value
            list.add(value)
        }
        if (list.size > 0) {
            requestItem.parameter = list[0]
        }
        if (list.size > 1) {
            requestItem.extra_parameter = list[1]
        }
        bundle.putSerializable(Constants.REQUEST_ITEM, requestItem)
//        }

        if (book != null) {
            book = changeBookInformation(source, book)
            bundle.putSerializable("book", book)
        }
        intent.setClass(activity, ReadingActivity::class.java)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        activity.startActivity(intent)
    }

    private fun changeBookInformation(source: CoverPage.SourcesBean, book: Book): Book {
        val bookDaoHelper = BookDaoHelper.getInstance()
        book.book_source_id = source.book_source_id
        book.site = source.host
        book.last_updatetime_native = source.update_time
        book.dex = source.dex
        //        if ("b.easou.com".equals(source.host)) {
        //            book.parameter = source.source.get(Constants.SOURCE_GID);
        //        } else if ("k.sogou.com".equals(source.host)) {
        //            book.parameter = source.source.get(Constants.SOURCE_MD);
        //            book.extra_parameter = source.source.get(Constants.SOURCE_ID);
        //        }
        val iterator = source.source.entries.iterator()
        val list = java.util.ArrayList<String>()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val value = entry.value
            list.add(value)
        }
        if (list.size > 0) {
            book.parameter = list[0]
        }
        if (list.size > 1) {
            book.extra_parameter = list[1]
        }

        if (bookDaoHelper.isBookSubed(source.book_id)) {
            bookDaoHelper.updateBook(book)
        }

        val bookChapterDao = BookChapterDao(activity, source.book_id)

        bookChapterDao.deleteBookChapters(0)
        return book
    }

    //阅读书架上的书籍
    private fun readingCustomaryBook(source: CoverPage.SourcesBean?, isCurrentSource: Boolean) {
        val intent = Intent()
        val bundle = Bundle()

        if (bookDaoHelper == null || bookCoverUtil == null || bookVo == null) {
            return
        }

        val book: Book? = if (source == null) {//说明是青果源的书
            bookDaoHelper!!.getBook(requestItem.book_id, 0)
        } else {
            bookDaoHelper!!.getBook(source.book_id, 0)
        }

        if (book != null && book.sequence != -2) {
            bundle.putInt("sequence", book.sequence)
            bundle.putInt("offset", book.offset)
        } else {
            bundle.putInt("sequence", -1)
        }

        if (book != null) {
            if (isCurrentSource) {
                book.last_updatetime_native = source!!.update_time
            }
            if (Constants.QG_SOURCE == bookVo?.host) {
                book.last_updatetime_native = bookVo!!.update_time
            }
            bundle.putSerializable("book", book)
        }
        bundle.putSerializable(Constants.REQUEST_ITEM, requestItem)
        intent.setClass(activity, ReadingActivity::class.java)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        activity.startActivity(intent)
    }
    
    /**
     * onresume 时设置底部三个按钮的状态
     */
    fun checkBookStatus() {
        if (bookDaoHelper == null || requestItem == null)
            return

        coverPageContract.changeDownloadButtonStatus()
        if (bookDaoHelper!!.isBookSubed(requestItem.book_id)) {
            coverPageContract.onStartStatus(true)
        } else {
            coverPageContract.onStartStatus(false)
        }
    }

    fun destory() {
        try {

            mDialog?.let {
                it.dismiss()
                it.cancel()
            }


            if (!bookDaoHelper!!.isBookSubed(requestItem.book_id)) {
                activity.deleteDatabase("book_chapter_" + requestItem.book_id)
            }
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
        if (bookCoverUtil != null) {
            bookCoverUtil!!.unRegistReceiver()
            bookCoverUtil = null
        }
    }

    /**
     * 缓存书籍
     */
    fun downLoadBook() {
        if (bookVo == null)
            return
        val book = bookCoverUtil!!.getCoverBook(bookDaoHelper, bookVo)
        val downloadState = CacheManager.getBookStatus(book)
        if (downloadState != DownloadState.FINISH && downloadState != DownloadState.WAITTING && downloadState != DownloadState.DOWNLOADING) {
            showToastShort("正在缓存中...")
        }
        if (Constants.QG_SOURCE == requestItem.host) {
            if (bookDaoHelper == null) {
                bookDaoHelper = BookDaoHelper.getInstance()
            }
            if (bookDaoHelper != null && bookCoverUtil != null) {
                if (!bookDaoHelper!!.isBookSubed(requestItem.book_id)) {
                    val insertBook = bookCoverUtil!!.getCoverBook(bookDaoHelper, bookVo)

                    insertBook.last_updatetime_native = bookVo!!.update_time

                    val succeed = bookDaoHelper!!.insertBook(insertBook)
                    if (succeed) {
                        coverPageContract.successAddIntoShelf(true)
                        showToastShort("成功添加到书架！")
                        BaseBookHelper.startDownBookTask(activity, requestItem.toBook(), 0)
                    }
                } else {
                    BaseBookHelper.startDownBookTask(activity, requestItem.toBook(), 0)
                }
            }
        } else {
            if (bookSourceList.size == 0) {
                showToastShort("当前书籍不能缓存，先去看看其他书吧")
            } else {
                if (bookDaoHelper == null) {
                    bookDaoHelper = BookDaoHelper.getInstance()
                }
                if (bookDaoHelper != null && bookCoverUtil != null) {
                    if (!bookDaoHelper!!.isBookSubed(requestItem.book_id)) {
                        val insertBook = bookCoverUtil!!.getCoverBook(bookDaoHelper, bookVo)

                        if (currentSource != null) {
                            insertBook.last_updatetime_native = currentSource!!.update_time
                        }
                        val succeed = bookDaoHelper!!.insertBook(insertBook)
                        if (succeed) {
                            coverPageContract.successAddIntoShelf(true)
                            showToastShort("成功添加到书架！")
                            BaseBookHelper.startDownBookTask(activity, requestItem.toBook(), 0)
                        }
                    } else {
                        BaseBookHelper.startDownBookTask(activity, requestItem.toBook(), 0)
                    }
                }
            }
        }
        coverPageContract.changeDownloadButtonStatus()
    }
    
    fun goToBookSearchActivity(view: View) {
        val intent = Intent()
        if (view is RecommendItemView) {
            intent.putExtra("word", view.title)
            intent.putExtra("search_type", "0")
            intent.putExtra("filter_type", "0")
            intent.putExtra("filter_word", "ALL")
            intent.putExtra("sort_type", "0")
            intent.setClass(activity, SearchBookActivity::class.java)
            activity.startActivity(intent)
            return
        }
    }

    /**
     * 是否存在书架
     */
    fun isBookSubed(): Boolean = bookDaoHelper!!.isBookSubed(requestItem.book_id)
    
    /**
     * 点击查看目录或者最新章节后的跳转操作

     * @param locationSequence 将要定位到的章节序号
     */
    private fun enterCatalogues(intent: Intent, locationSequence: Int, isLastChapter: Boolean) {
        if (bookVo != null && bookCoverUtil != null) {
            val bundle = Bundle()
            bundle.putSerializable("cover", bookCoverUtil!!.getCoverBook(bookDaoHelper, bookVo))
            bundle.putInt("sequence", locationSequence)
            bundle.putBoolean("fromCover", true)
            bundle.putBoolean("is_last_chapter", isLastChapter)
            bundle.putSerializable(Constants.REQUEST_ITEM, requestItem)
            intent.setClass(activity, CataloguesActivity::class.java)
            intent.putExtras(bundle)
            activity.startActivity(intent)
        }
    }

    private fun showToastShort(s: String) {
        activity.applicationContext.showToastMessage(s)
    }

    override fun changeState() {
        coverPageContract.changeDownloadButtonStatus()
    }

    override fun downLoadService() {
        coverPageContract.changeDownloadButtonStatus()
    }


    /**
     * 获取推荐的书
     */
    fun getRecommend() {
        val bookIds: String = getBookOnLineIds(bookDaoHelper!!)
//        if (requestItem != null && requestItem.book_id != null && !TextUtils.isEmpty(bookIds)) {
        NetService.userService.requestCoverRecommend(requestItem.book_id, bookIds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<CoverRecommendBean> {
                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(bean: CoverRecommendBean) {
                        AppLog.e("aaa", bean.toString())
                        if (bean.data != null && bean.data.map != null) {
                            if (sharePreUtil != null) {
                                val scale = sharePreUtil!!.getString(SharedPreUtil.RECOMMEND_BOOKCOVER, "2,2,0").split(",")

                                if (scale.size >= 2) {
                                    if (!TextUtils.isEmpty(scale[0])) {
                                        AppLog.e("cover", scale[0])
                                        addZNBooks(bean, Integer.parseInt(scale[0]))
                                    }
                                    if (!TextUtils.isEmpty(scale[1])) {
                                        AppLog.e("cover", scale[1])
                                        addQGBooks(bean, Integer.parseInt(scale[1]))
                                    }
                                }
                                coverPageContract.showRecommend(mRecommendBooks)
                            }
                        } else {
                            coverPageContract.showRecommendError()
                        }
                    }

                    override fun onError(e: Throwable) {
                        coverPageContract.showRecommendError()
                    }

                    override fun onComplete() {

                    }
                })
//        }

    }

    /**
     * 添加推荐的智能的书
     */
    fun addZNBooks(bean: CoverRecommendBean, znSize: Int) {
        var znIndex = -1
        markIndex.clear()
        if (bean.data.map.znList != null && bean.data.map.znList.size > 0) {
            for (i in 0 until znSize) {//推荐位 智能只取 3本

                znIndex = mRandom.nextInt(bean.data.map.znList.size)
                if (markIndex.contains(znIndex)) {
                    while (true) {
                        znIndex = mRandom.nextInt(bean.data.map.znList.size)
                        if (!markIndex.contains(znIndex)) {
                            break
                        }
                    }
                }
                markIndex.add(znIndex)
                val book = Book()
                val znBean = bean.data.map.znList[znIndex]
                if (requestItem.book_id != znBean.bookId) {
                    if (znBean.serialStatus == "FINISH") {
                        book.status = 2
                    } else {
                        book.status = 1
                    }
                    book.book_id = znBean.bookId
                    book.book_source_id = znBean.id
                    book.name = znBean.bookName
                    book.category = znBean.label
                    book.author = znBean.authorName
                    book.img_url = znBean.sourceImageUrl
                    book.site = znBean.host
                    book.last_chapter_name = znBean.lastChapterName + ""
                    book.chapter_count = Integer.valueOf(znBean.chapterCount)!!
                    book.last_updatetime_native = znBean.updateTime
                    book.parameter = "0"
                    book.extra_parameter = "0"
                    book.dex = znBean.dex
                    book.desc = znBean.description
                    book.last_updateSucessTime = System.currentTimeMillis()
                    book.readPersonNum = znBean.readerCountDescp + ""
                    mRecommendBooks.add(book)
                }

            }
        }
    }

    /**
     * 添加推荐的青果的书
     */
    fun addQGBooks(bean: CoverRecommendBean, qgSize: Int) {
        markIndex.clear()
        var qgIndex = -1
        for (i in 0 until qgSize) {//推荐位 青果只取 3本
            qgIndex = mRandom.nextInt(bean.data.map.qgList.size)
            if (markIndex.contains(qgIndex)) {
                while (true) {
                    qgIndex = mRandom.nextInt(bean.data.map.qgList.size)
                    if (!markIndex.contains(qgIndex)) {
                        break
                    }
                }
            }
            markIndex.add(qgIndex)
            val book = Book()
            val qgBean = bean.data.map.qgList[qgIndex]
            if (requestItem.book_id != qgBean.id) {
                if (qgBean.serialStatus == "FINISH") {
                    book.status = 2
                } else {
                    book.status = 1
                }
                book.book_id = qgBean.id
                book.book_source_id = qgBean.bookSourceId
                book.name = qgBean.bookName
                book.category = qgBean.labels
                book.author = qgBean.author_name
                book.img_url = qgBean.image + ""
                book.site = qgBean.host + ""
                book.last_chapter_name = qgBean.chapter_name + ""
                book.chapter_count = Integer.valueOf(qgBean.chapter_sn)!!
                book.last_updatetime_native = qgBean.update_time
                book.parameter = "0"
                book.extra_parameter = "0"
                book.dex = 1
                book.desc = qgBean.description
                book.last_updateSucessTime = System.currentTimeMillis()
                book.readPersonNum = qgBean.read_count.toString() + ""
                mRecommendBooks.add(book)
            }
        }
    }

    /**
     * 获取书架上的书Id
     */
    private fun getBookOnLineIds(bookDaoHelper: BookDaoHelper): String {

        books.clear()
        books = bookDaoHelper.booksOnLineList
        val sb = StringBuilder()
        if (books.size > 0) {
            for (i in books.indices) {
                val book = books.get(i)
                sb.append(book.book_id)
                sb.append(if (i == books.size - 1) "" else ",")
            }
            return sb.toString()
        }

        return ""
    }

//    //解绑
//    fun unSub() {
//        mBookCoverViewModel?.unSubscribe()
//    }

    fun getBook(): Book? {
        if (bookCoverUtil == null || bookDaoHelper == null || bookVo == null) {
            return null
        }
        return bookCoverUtil!!.getCoverBook(bookDaoHelper, bookVo)
    }
}