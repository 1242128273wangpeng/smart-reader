package com.intelligent.reader.presenter.coverPage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import com.ding.basic.bean.Book
import com.ding.basic.bean.CoverRecommendBean
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.RequestSubscriber
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.util.SharedPreUtil
import com.dingyue.contract.util.showToastMessage
import com.intelligent.reader.R
import com.intelligent.reader.activity.CataloguesActivity
import com.intelligent.reader.activity.SearchBookActivity
import com.intelligent.reader.cover.*
import com.intelligent.reader.view.TransformReadDialog
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.book.view.RecommendItemView
import net.lzbook.kit.data.db.help.ChapterDaoHelper
import net.lzbook.kit.utils.*
import java.util.*

class CoverPagePresenter(private val book_id: String?, private var book_source_id: String?, private var book_chapter_id: String?, val coverPageContract: CoverPageContract, val activity: Activity, onClickListener: View.OnClickListener)
    : BookCoverUtil.OnDownloadState, BookCoverViewModel.BookCoverViewCallback {

    var coverDetail: Book? = null
    private var showMoreLabel: Boolean = false

    var bookCoverUtil: BookCoverUtil? = null
    var sharePreUtil: SharedPreUtil? = null
    var bookCoverViewModel: BookCoverViewModel? = null

    init {
        sharePreUtil = SharedPreUtil(SharedPreUtil.SHARE_ONLINE_CONFIG)
        bookCoverViewModel = BookCoverViewModel()
        bookCoverViewModel?.setBookCoverViewCallback(this)

        bookCoverUtil = BookCoverUtil(activity, onClickListener)

        bookCoverUtil?.registReceiver()
        bookCoverUtil?.setOnDownloadState(this)
    }

    private val transformReadDialog: TransformReadDialog by lazy {

        val dialog = TransformReadDialog(activity)

        dialog.insertContinueListener {
            val data = HashMap<String, String>()
            data["type"] = "1"

            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEPOPUP, data)

            intoReadingActivity()

            if (!activity.isFinishing) {
                dialog.dismiss()
            }
        }

        dialog.insertCancelListener {
            val data = HashMap<String, String>()
            data["type"] = "2"

            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEPOPUP, data)

            if (!activity.isFinishing) {
                dialog.dismiss()
            }
        }
        dialog
    }


    /***
     * 获取书籍详情
     * **/
    fun requestBookDetail(showMoreLabel: Boolean) {
        this.showMoreLabel = showMoreLabel
        bookCoverViewModel?.requestBookDetail(book_id, book_source_id, book_chapter_id)
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

    /**
     * 去搜索页
     */
    fun goToBookSearchActivity(view: View) {
        val intent = Intent()
        if (view is RecommendItemView) {
            val item = view
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

        val book = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(book_id!!)

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

        if (!activity.isFinishing) {
            if(!transformReadDialog.isShow()){
                    transformReadDialog.show()
                }


        }
    }

    /***
     * 进入阅读页
     * **/
    private fun intoReadingActivity() {
        if (coverDetail == null || TextUtils.isEmpty(coverDetail!!.book_id)) {
            return
        }

        val bundle = Bundle()
        val flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val book = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(coverDetail!!.book_id)

        if (book != null) {

            if (coverDetail != null && coverDetail?.last_chapter != null) {
                book.last_chapter = coverDetail?.last_chapter
            }

            if (book.sequence != -2) {
                bundle.putInt("sequence", book.sequence)
                bundle.putInt("offset", book.offset)
            } else {
                bundle.putInt("sequence", -1)
                bundle.putInt("offset", 0)
            }

            updateBookInformation()

            bundle.putSerializable("book", book)
        } else {
            bundle.putSerializable("book", coverDetail)
        }

        RouterUtil.navigation(activity, RouterConfig.READER_ACTIVITY, bundle, flags)
    }

    /***
     * 更新本地书籍信息
     * **/
    private fun updateBookInformation() {
        if (coverDetail != null) {

            val book = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(coverDetail!!.book_id)

            if (book != null) {
                coverDetail?.last_chapter = book.last_chapter

                RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).updateBook(book)
            }
        }
    }

    /***
     * 缓存书籍内容
     * **/
    fun handleDownloadAction() {
        if (coverDetail == null || TextUtils.isEmpty(coverDetail?.book_id)) {
            return
        }
        val downloadState = CacheManager.getBookStatus(coverDetail!!)
        if (downloadState != DownloadState.FINISH && downloadState != DownloadState.WAITTING && downloadState != DownloadState.DOWNLOADING) {
            activity.applicationContext.showToastMessage("正在缓存中...")
        }

        val book = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(coverDetail!!.book_id)

        if (book != null) {
            BaseBookHelper.startDownBookTask(activity, coverDetail, 0)
        } else {
            val result = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).insertBook(coverDetail!!)

            if (result > 0) {
                coverPageContract.insertBookShelfResult(true)
                activity.applicationContext.showToastMessage("成功添加到书架！")

                BaseBookHelper.startDownBookTask(activity, coverDetail, 0)
            }
        }
        coverPageContract.changeDownloadButtonStatus()
    }

    /***
     * 刷新底部按钮状态
     * **/
    fun refreshNavigationState() {
        if (book_id == null || TextUtils.isEmpty(book_id)) {
            return
        }

        coverPageContract.changeDownloadButtonStatus()

        val book = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(book_id)
        if (book != null) {
            coverPageContract.bookSubscribeState(true)
        } else {
            coverPageContract.bookSubscribeState(false)
        }
    }

    /***
     * 相关资源注销
     * **/
    fun destroy() {
        if (bookCoverUtil != null) {
            bookCoverUtil!!.unRegisterReceiver()
            bookCoverUtil = null
        }
        bookCoverViewModel?.unSubscribe()
    }

    /***
     * 获取封面页书籍
     * **/
    fun loadCoverBook(): Book? {
        return when {
            coverDetail != null -> coverDetail
            !TextUtils.isEmpty(book_id) -> RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(book_id!!)
            else -> null
        }
    }

    /***
     * 判断是否跳转到搜索页
     * **/
    fun checkStartSearchActivity(view: View) {
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

    /***
     * 判断是否存在书架
     * **/
    fun checkBookSubscribe(): Boolean = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(book_id!!) != null

    /***
     * 下载状态改变监听方法
     * **/
    override fun changeState() {
        coverPageContract.changeDownloadButtonStatus()
    }

    /***
     * 获取封面页推荐书籍
     * **/
    fun requestCoverRecommend() {
        if (book_id != null && !TextUtils.isEmpty(book_id)) {
            val bookIDs: String = loadBookShelfID()
            RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestCoverRecommend(book_id, bookIDs, object : RequestSubscriber<CoverRecommendBean>() {
                override fun requestResult(result: CoverRecommendBean?) {
                    if (result?.data != null && result.data?.map != null) {
                        if (sharePreUtil != null) {

                            val scale = sharePreUtil!!.getString(SharedPreUtil.RECOMMEND_BOOKCOVER, "2,2,0").split(",")

                            if (scale.size >= 2) {
                                if (!TextUtils.isEmpty(scale[0])) {

                                }
                                if (!TextUtils.isEmpty(scale[1])) {

                                }
                            }
//                            coverPageContract.showRecommendSuccess(mRecommendBooks)
                        }
                    } else {
                        coverPageContract.showRecommendFail()
                    }
                }

                override fun requestError(message: String) {
                    Logger.e("获取封面推荐异常！")
                    coverPageContract.showRecommendFail()
                }
            })
        }
    }

    /***
     * 获取书架书籍ID
     * **/
    private fun loadBookShelfID(): String {
        val books = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBooks()

        if (books != null && books.isNotEmpty()) {
            val stringBuilder = StringBuilder()
            for (i in books.indices) {
                val book = books[i]
                stringBuilder.append(book.book_id)
                stringBuilder.append(if (i == books.size - 1) "" else ",")
            }
            return stringBuilder.toString()
        }
        return ""
    }
}