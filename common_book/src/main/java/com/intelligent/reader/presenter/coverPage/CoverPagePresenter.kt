package com.intelligent.reader.presenter.coverPage

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.*
import com.intelligent.reader.R
import com.intelligent.reader.activity.CataloguesActivity
import com.intelligent.reader.activity.ReadingActivity
import com.intelligent.reader.activity.SearchBookActivity
import com.intelligent.reader.adapter.CoverSourceAdapter
import com.intelligent.reader.cover.*
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import com.intelligent.reader.widget.ConfirmDialog
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
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
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.CoverPage
import net.lzbook.kit.data.bean.RequestItem
import net.lzbook.kit.data.db.BookChapterDao
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.data.recommend.CoverRecommendBean
import net.lzbook.kit.net.custom.service.NetService
import net.lzbook.kit.utils.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by zhenXiang on 2017\11\15 0015.
 */

class CoverPagePresenter(val requestItem: RequestItem, val coverPageContract: CoverPageContract,
                         val activity: Activity, val onClickListener: View.OnClickListener)
    : BookCoverUtil.OnDownloadState, BookCoverUtil.OnDownLoadService, BookCoverViewModel.BookCoverViewCallback {

    var downloadService: DownloadService? = null
    var bookVo: CoverPage.BookVoBean? = null
    var bookCoverUtil: BookCoverUtil? = null
    var bookDaoHelper: BookDaoHelper? = null
    var preferences: SharedPreferences? = null
    var isNeedShowMoreTags: Boolean = false
    var currentSource: CoverPage.SourcesBean? = null
    var books = ArrayList<Book>()
    val markIndexs = ArrayList<Int>()
    var mRandom: Random = Random()
    var mRecommendBooks = ArrayList<Book>()
    var bookSourceList: ArrayList<CoverPage.SourcesBean> = ArrayList<CoverPage.SourcesBean>()
    val GET_CATEGORY_OK = 0x10 + 1
    val GET_CATEGORY_ERROR = GET_CATEGORY_OK + 1
    val DOWNLOAD_STATE_FINISH = 1;
    val DOWNLOAD_STATE_LOCKED = 2;
    val DOWNLOAD_STATE_NOSTART = 3;
    val DOWNLOAD_STATE_OTHER = 4;
    var mBookCoverViewModel: BookCoverViewModel? = null


    init {
        preferences = BaseBookApplication.getGlobalContext().getSharedPreferences("onlineconfig_agent_online_setting_" + AppUtils.getPackageName(), 0);
        mBookCoverViewModel = BookCoverViewModel(BookCoverRepositoryFactory.getInstance(BookCoverOtherRepository.getInstance(NetService.userService),
                BookCoverQGRepository.getInstance(OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext())), BookCoverLocalRepository.getInstance(BaseBookApplication.getGlobalContext())))
        mBookCoverViewModel?.setBookCoverViewCallback(this)

        if (bookDaoHelper == null) {
            bookDaoHelper = BookDaoHelper.getInstance()
        }

        bookCoverUtil = BookCoverUtil(activity, onClickListener)
        bookCoverUtil?.registReceiver()
        bookCoverUtil?.setOnDownloadState(this)
        bookCoverUtil?.setOnDownLoadService(this)

    }


    fun showCoverSourceDialog() {
        if (Constants.QG_SOURCE == requestItem.host || bookSourceList.size == 1) {//青果
            showToastShort("该小说暂无其他来源！")
            return
        }
        val coverSourceDialog = MyDialog(activity, R.layout.dialog_read_source, Gravity.CENTER)
        coverSourceDialog.setCanceledOnTouchOutside(true)
        val sourceView = coverSourceDialog.findViewById(R.id.change_source_list) as ListView
        val dialog_top_title = coverSourceDialog.findViewById(R.id.dialog_top_title) as TextView
        val change_source_statement = coverSourceDialog.findViewById(R.id.change_source_statement) as RelativeLayout
        change_source_statement.visibility = View.GONE


        val bookSourceAdapter = CoverSourceAdapter(activity, bookSourceList)

        sourceView.adapter = bookSourceAdapter

        if (bookSourceList.size > 4) {
            sourceView.layoutParams.height = activity.getResources().getDimensionPixelOffset(R.dimen.dimen_view_height_240)
        }


        sourceView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val source = bookSourceAdapter.getItem(position) as CoverPage.SourcesBean
            if (source != null) {
                if (requestItem != null && !TextUtils.isEmpty(source.book_source_id)) {
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
                }

                currentSource = source
                coverPageContract!!.loadCoverWhenSourceChange()

                coverPageContract!!.showCurrentSources(currentSource!!.host)

                coverSourceDialog.dismiss()
            }
        }

        val change_source_original_web = coverSourceDialog.findViewById(R.id.change_source_original_web) as TextView
        change_source_original_web.setText(R.string.cancel)
        val change_source_continue = coverSourceDialog.findViewById(R.id.change_source_continue) as TextView

        change_source_original_web.setOnClickListener {
            val data = HashMap<String, String>()
            data.put("type", "2")
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.SOURCECHANGEPOPUP, data)
            coverSourceDialog?.dismiss()
        }
        change_source_continue.setOnClickListener {
            if (bookSourceList != null && bookSourceList.size == 0) {
                showToastShort("当前书籍不能阅读，先去看看其他书吧")
                return@setOnClickListener
            }
            continueReading()
            coverSourceDialog.dismiss()
        }

        if (!coverSourceDialog.isShowing()) {
            try {
                coverSourceDialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //model层回调成功  和 失败
    override fun onCoverDetail(coverPage: CoverPage?) {
        handleOK(coverPage!!, coverPage!!.bookVo.host == Constants.QG_SOURCE, isNeedShowMoreTags)
    }

    override fun onFail(msg: String?) {
        coverPageContract.showCoverError()
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
        if (requestItem != null) {
            bundle.putSerializable(Constants.REQUEST_ITEM, requestItem)
        }
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
            dialog.dismiss()
        }
        dialog.setOnCancelListener {
            val data = HashMap<String, String>()
            data.put("type", "2")
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEPOPUP, data)
            dialog.dismiss()
        }
        dialog
    }

    private fun showReadingSourceDialog() {
        clearCacheDialog.show()
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
        val readingSourceDialog = MyDialog(activity, R.layout.dialog_read_source, Gravity.CENTER)
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
        if (bookSourceList != null && bookSourceList.size == 0) {
            if (requestItem != null && Constants.QG_SOURCE == requestItem.host) {
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
        if (!activity.isFinishing()) {
            val confirm_change_source_dialog = MyDialog(activity, R.layout.pop_confirm_layout)
            confirm_change_source_dialog.setCanceledOnTouchOutside(true)
            val dialog_cancel = confirm_change_source_dialog.findViewById(R.id.publish_stay) as Button
            dialog_cancel.setText(R.string.book_cover_continue_read_cache)
            val dialog_confirm = confirm_change_source_dialog.findViewById(R.id.publish_leave) as Button
            dialog_confirm.setText(R.string.book_cover_confirm_change_source)
            val dialog_information = confirm_change_source_dialog.findViewById(R.id.publish_content) as TextView
            dialog_information.setText(R.string.book_cover_change_source_prompt)
            dialog_cancel.setOnClickListener {
                if (confirm_change_source_dialog != null && confirm_change_source_dialog.isShowing()) {
                    confirm_change_source_dialog.dismiss()
                }
                readingCustomaryBook(source, false)
            }
            dialog_confirm.setOnClickListener {
                if (confirm_change_source_dialog != null && confirm_change_source_dialog.isShowing()) {
                    confirm_change_source_dialog.dismiss()
                }
                intoReadingActivity(source)
            }

            confirm_change_source_dialog.setOnCancelListener(DialogInterface.OnCancelListener { confirm_change_source_dialog.dismiss() })
            if (!confirm_change_source_dialog.isShowing()) {
                try {
                    confirm_change_source_dialog.show()
                } catch (e: Exception) {
                    e.printStackTrace()
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


        if (requestItem != null) {
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
        }

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
        val book: Book?

        if (source == null) {//说明是青果源的书
            book = bookDaoHelper!!.getBook(requestItem.book_id, 0)

        } else {
            book = bookDaoHelper!!.getBook(source.book_id, 0)
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
        if (requestItem != null) {
            bundle.putSerializable(Constants.REQUEST_ITEM, requestItem)
        }
        intent.setClass(activity, ReadingActivity::class.java)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        activity.startActivity(intent)
    }

    /**
     *
     * 获取书籍封面信息
     * isNeedShowMoreTags 是否需要显示多个标签，目前只有免费小说书城，其他都是单个标签
     */

    fun getBookCoverInfo(isNeedShowMoreTags: Boolean) {
        this.isNeedShowMoreTags = isNeedShowMoreTags
        if (requestItem != null) {
            mBookCoverViewModel!!.getCoverDetail(requestItem.book_id, requestItem.book_source_id, requestItem.host)
        }
    }

    /**
     * onresume 时设置底部三个按钮的状态
     */
    fun checkBookStatus() {
        if (bookDaoHelper == null || requestItem == null)
            return

        coverPageContract!!.changeDownloadButtonStatus()
        if (bookDaoHelper!!.isBookSubed(requestItem.book_id)) {
            coverPageContract!!.onStartStatus(true)
        } else {
            coverPageContract!!.onStartStatus(false)
        }
    }

    fun destory() {
        try {
            if (requestItem != null && !bookDaoHelper!!.isBookSubed(requestItem.book_id)) {
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
            showToastShort("正在缓存中。。。")
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
            if (bookSourceList != null && bookSourceList.size == 0) {
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
                            coverPageContract!!.successAddIntoShelf(true)
                            showToastShort("成功添加到书架！")
                            BaseBookHelper.startDownBookTask(activity, requestItem.toBook(), 0)
                        }
                    } else {
                        BaseBookHelper.startDownBookTask(activity, requestItem.toBook(), 0)
                    }
                }
            }
        }
        coverPageContract!!.changeDownloadButtonStatus()
    }

    /**
     * isNeedShowMoreTags 是否需要显示多个标签 目前免费小说书城改版需要显示多个标签，其他只需要一个标签
     */
    fun handleOK(objects: Any, isQG: Boolean, isNeedShowMoreTags: Boolean) {

        if (isQG) {//如果是青果的数据，就先进行一次类型转换
            bookVo = (objects as CoverPage).bookVo

            coverPageContract!!.showArrow(true)

        } else if (objects != null) {
            bookVo = (objects as CoverPage).bookVo
            if (bookVo != null) {
                if (requestItem != null && !TextUtils.isEmpty(requestItem.book_id)) {
                    bookVo?.book_id = requestItem.book_id
                }
                if (requestItem != null && !TextUtils.isEmpty(requestItem.book_source_id)) {
                    bookVo?.book_source_id = requestItem.book_source_id
                }
                if (requestItem != null && !TextUtils.isEmpty(requestItem.host)) {
                    bookVo?.host = requestItem.host
                }
                if (requestItem != null) {
                    bookVo?.dex = requestItem.dex
                }

                val sources = objects.sources

                if (sources.size < 2) {
                    coverPageContract!!.setCompound()
                }
                if (bookSourceList == null) {
                    bookSourceList = java.util.ArrayList<CoverPage.SourcesBean>()
                }
                bookSourceList.clear()
                if (sources != null) {
                    bookSourceList?.addAll(sources)
                    for (i in bookSourceList?.indices) {
                        val source = bookSourceList.get(i)
                        if (requestItem.book_source_id == source.book_source_id) {
                            currentSource = source
                        }
                    }
                    if (currentSource != null && !TextUtils.isEmpty(currentSource!!.host)) {
                        coverPageContract.showCurrentSources(currentSource!!.host)
                    } else {
                        if (bookSourceList != null && bookSourceList.size > 0) {
                            currentSource = bookSourceList.get(0)
                            coverPageContract!!.showCurrentSources(currentSource!!.host)
                        }
                    }
                }
            }
        }
        if (bookVo != null && currentSource != null) {
            bookVo?.wordCountDescp = currentSource?.wordCountDescp
            bookVo?.readerCountDescp = currentSource?.readerCountDescp
            bookVo?.score = currentSource?.score
            if (isNeedShowMoreTags) {
                bookVo?.labels = currentSource?.labels
            }
        }
        if (bookVo != null && bookCoverUtil != null) {
            bookCoverUtil?.saveHistory(bookVo)
        }
        coverPageContract!!.showLoadingSuccess()
        coverPageContract!!.showCoverDetail(bookVo!!)
        coverPageContract!!.changeDownloadButtonStatus()
    }


    fun goToBookSearchActivity(view: View) {
        val intent = Intent()
        if (view is RecommendItemView) {
            val item = view as RecommendItemView
            intent.putExtra("word", item.title)
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
     * isNeedRemoveFun 是否需要移除的功能 ， 目前免费小说书城不需要移出书架，其他几个壳任然保持之前移出书架
     */
    fun addBookIntoShelf(isNeedRemoveFun: Boolean) {
        if (bookDaoHelper == null || bookCoverUtil == null) {
            return
        }
        if (!bookDaoHelper!!.isBookSubed(requestItem.book_id)) {
            val insertBook = bookCoverUtil?.getCoverBook(bookDaoHelper, bookVo)
            if (currentSource != null) {
                insertBook?.last_updatetime_native = currentSource!!.update_time
            }
            if (bookVo!!.host == Constants.QG_SOURCE) {
                insertBook?.last_updatetime_native = bookVo?.update_time
            }
            insertBook?.last_updateSucessTime = System.currentTimeMillis()
            val succeed = bookDaoHelper!!.insertBook(insertBook)
            if (succeed && insertBook != null) {
                val data1 = HashMap<String, String>()
                data1.put("type", "1")
                data1.put("bookid", insertBook.book_id)
                StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.SHELFEDIT, data1)
                showToastShort("成功添加到书架！")
                coverPageContract!!.successAddIntoShelf(true)
            }
        } else {
            if (isNeedRemoveFun) {

                coverPageContract!!.successAddIntoShelf(false)

                //移除书架的打点
                StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.b_details_click_book_remove)
                showToastShort("成功从书架移除！")
                val data2 = HashMap<String, String>()
                data2.put("type", "2")
                data2.put("bookid", requestItem.book_id)
                StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.SHELFEDIT, data2)
                coverPageContract!!.changeDownloadButtonStatus()

                coverPageContract!!.setShelfBtnClickable(false)
                val cleanDialog = MyDialog(activity, R.layout.dialog_download_clean)
                cleanDialog.setCanceledOnTouchOutside(false)
                cleanDialog.setCancelable(false)
                (cleanDialog.findViewById(R.id.dialog_msg) as TextView).setText(R.string.tip_cleaning_cache)
                cleanDialog.show()

                Observable.create(ObservableOnSubscribe<Boolean> { e ->
                    CacheManager.remove(requestItem.book_id)

                    bookDaoHelper?.deleteBook(requestItem.toBook(), false)

                    e.onNext(true)
                    e.onComplete()
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            cleanDialog.dismiss()
                            coverPageContract!!.setShelfBtnClickable(true)
                            coverPageContract!!.changeDownloadButtonStatus()
                        }
            } else {
                showToastShort("已在书架中！")
            }

        }
    }

    /**
     * isClickCatalog 是否是点击查看目录进入到目录页
     */
    fun goToCataloguesAct(isClickCatalog: Boolean) {
        val intent = Intent()
        if (isClickCatalog) {
            if (Constants.QG_SOURCE == requestItem.host) {
                enterCatalogues(intent, 0, false)
            } else {
                if (bookSourceList != null && bookSourceList.size == 0) {
                    showToastShort("当前书籍不能阅读，先去看看其他书吧")
                } else {
                    enterCatalogues(intent, 0, false)
                }
            }
        } else {

            if (bookVo == null)
                return
            if (Constants.QG_SOURCE == requestItem.host) {
                enterCatalogues(intent, bookVo!!.serial_number - 1, true)
            } else {
                if (bookSourceList != null && bookSourceList.size == 0) {
                    showToastShort("当前书籍不能阅读，先去看看其他书吧")
                } else {
                    enterCatalogues(intent, bookVo!!.serial_number - 1, true)
                }
            }
        }

    }


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
        if (activity != null) {
            Toast.makeText(activity, s, Toast.LENGTH_SHORT).show()
        }
    }

    override fun changeState() {
        coverPageContract!!.changeDownloadButtonStatus()
    }

    override fun downLoadService() {
        coverPageContract!!.changeDownloadButtonStatus()
    }


    /**
     * 获取推荐的书
     */
    fun getRecommend() {
        var bookIds: String = getBookOnLineIds(bookDaoHelper!!)
        if (requestItem != null && requestItem.book_id != null && !TextUtils.isEmpty(bookIds)) {
            NetService.userService.requestCoverRecommend(requestItem.book_id, bookIds)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<CoverRecommendBean> {
                        override fun onSubscribe(d: Disposable) {}
                        override fun onNext(bean: CoverRecommendBean) {
                            AppLog.e("aaa", bean.toString())
                            if (bean != null && bean.data != null && bean.data.map != null) {
                                if (preferences != null) {
                                    var scale = preferences!!.getString(Constants.RECOMMEND_BOOKCOVER, "3,3,0")!!.split(",")
                                    if (scale != null && scale.size >= 2) {
                                        if (!TextUtils.isEmpty(scale[0])) {
                                            AppLog.e("cover", scale[0])
                                            addZNBooks(bean, Integer.parseInt(scale[0]))
                                        }
                                        if (!TextUtils.isEmpty(scale[1])) {
                                            AppLog.e("cover", scale[1])
                                            addQGBookss(bean, Integer.parseInt(scale[1]))
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
        }

    }

    /**
     * 添加推荐的智能的书
     */
    fun addZNBooks(bean: CoverRecommendBean, znSize: Int) {
        var znIndex = -1
        markIndexs.clear()
        if (bean.data.map.znList != null && bean.data.map.znList.size > 0) {
            for (i in 0..znSize - 1) {//推荐位 智能只取 3本

                znIndex = mRandom.nextInt(bean.data.map.znList.size)
                if (markIndexs.contains(znIndex)) {
                    while (true) {
                        znIndex = mRandom.nextInt(bean.data.map.znList.size)
                        if (!markIndexs.contains(znIndex)) {
                            break
                        }
                    }
                }
                markIndexs.add(znIndex)
                val book = Book()
                val znBean = bean.data.map.znList[znIndex]
                if (requestItem != null && requestItem.book_id != znBean.bookId) {
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
    fun addQGBookss(bean: CoverRecommendBean, qgSize: Int) {
        markIndexs.clear()
        var qgIndex = -1
        for (i in 0..qgSize - 1) {//推荐位 青果只取 3本
            qgIndex = mRandom.nextInt(bean.data.map.qgList.size)
            if (markIndexs.contains(qgIndex)) {
                while (true) {
                    qgIndex = mRandom.nextInt(bean.data.map.qgList.size)
                    if (!markIndexs.contains(qgIndex)) {
                        break
                    }
                }
            }
            markIndexs.add(qgIndex)
            val book = Book()
            val qgBean = bean.data.map.qgList[qgIndex]
            if (requestItem != null && requestItem.book_id != qgBean.id) {
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
    fun getBookOnLineIds(bookDaoHelper: BookDaoHelper): String {
        if (bookDaoHelper != null) {
            books.clear()
            books = bookDaoHelper!!.getBooksOnLineList()
            val sb = StringBuilder()
            if (books != null && books.size > 0) {
                for (i in books.indices) {
                    val book = books.get(i)
                    sb.append(book.book_id)
                    sb.append(if (i == books.size - 1) "" else ",")
                }
                return sb.toString()
            }
        }
        return ""
    }

    //解绑
    fun unSub() {
        mBookCoverViewModel?.unSubscribe()
    }

    fun getBook(): Book? {
        if (bookCoverUtil == null || bookDaoHelper == null || bookVo == null) {
            return null
        }
        return bookCoverUtil!!.getCoverBook(bookDaoHelper, bookVo)
    }

}
