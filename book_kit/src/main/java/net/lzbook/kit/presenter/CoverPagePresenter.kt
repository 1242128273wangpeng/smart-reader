package net.lzbook.kit.presenter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.Book
import com.ding.basic.bean.CoverRecommendBean
import com.ding.basic.bean.RecommendBean
import com.ding.basic.bean.RecommendBooks
import com.ding.basic.net.RequestSubscriber
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.R
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.model.BookCoverViewModel
import net.lzbook.kit.ui.widget.RecommendItemView
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.book.BaseBookHelper
import net.lzbook.kit.utils.book.BookCoverUtil
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.utils.download.DownloadState
import net.lzbook.kit.utils.enterSearch
import net.lzbook.kit.utils.logger.AppLog
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.dingyue.statistics.DyStatService
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.view.CoverPageContract
import java.util.*
import kotlin.collections.ArrayList

/**
 * author:推荐该作者的其他作品
 * 目前铺开的壳：智胜电子书替
 */
class CoverPagePresenter(private val book_id: String?,
                         private var book_source_id: String?,
                         private var book_chapter_id: String?,
                         val coverPageContract: CoverPageContract,
                         val activity: Activity,
                         onClickListener: View.OnClickListener,
                         private var author: String? = "")
    : BookCoverUtil.OnDownloadState, BookCoverViewModel.BookCoverViewCallback {

    var coverDetail: Book? = null
    private var showMoreLabel: Boolean = false

    private var bookCoverUtil: BookCoverUtil? = null
    var bookCoverViewModel: BookCoverViewModel? = null

    var recommendList = ArrayList<RecommendBean>()

    private var recommendBookList = ArrayList<RecommendBean>()

    var recommendIndex = 0
    var recommendCount = 6 //标识推荐书籍的数量，txt全本免费阅读只有4个书籍
    var mRandom: Random? = null

    init {
        mRandom = Random()
        bookCoverViewModel = BookCoverViewModel()
        bookCoverViewModel?.setBookCoverViewCallback(this)

        bookCoverUtil = BookCoverUtil(activity, onClickListener)

        bookCoverUtil?.registReceiver()
        bookCoverUtil?.setOnDownloadState(this)
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

            //获得数据后第一时间更新书架信息
            updateBookInformation()

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
            if (coverDetail?.last_chapter != null) {
                handleCatalogAction(intent, coverDetail?.last_chapter!!.serial_number - 1, true)
            } else {
                handleCatalogAction(intent, 0, false)
            }
        }
    }

    /***
     * 处理跳转目录操作
     * **/
    private fun handleCatalogAction(intent: Intent, sequence: Int, indexLast: Boolean) {
        coverPageContract.handleCatalogAction(intent,sequence,indexLast,coverDetail)
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

                ToastUtil.showToastMessage("成功从书架移除！")

                DyStatService.onEvent(EventPoint.BOOOKDETAIL_SHELFADD, mapOf("type" to "2", "bookid" to coverDetail!!.book_id))

                coverPageContract.changeDownloadButtonStatus()

                coverPageContract.changeShelfButtonClickable(false)

                var cleanDialog=coverPageContract.showCleanDialog()

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
                ToastUtil.showToastMessage("已在书架中！")
            }
        } else {
            Logger.v("书籍未订阅！")

            if (coverDetail == null) {
                ToastUtil.showToastMessage("书籍信息异常，请稍后再试！")
            }

            coverDetail?.last_update_success_time = System.currentTimeMillis()

            val result = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).insertBook(coverDetail!!)

            if (result <= 0) {
                Logger.v("加入书架失败！")
                ToastUtil.showToastMessage("加入书架失败！")
            } else {
                Logger.v("加入书架成功！")
                DyStatService.onEvent(EventPoint.BOOOKDETAIL_SHELFADD, mapOf("type" to "1", "bookid" to coverDetail!!.book_id))

                ToastUtil.showToastMessage("成功添加到书架！")

                coverPageContract.insertBookShelfResult(true)
            }
        }
    }

    /***
     * 处理跳转阅读页请求
     * **/
    fun handleReadingAction() {
        coverPageContract.handleReadingAction(coverDetail)
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

//            updateBookInformation()

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
                //从H5页面直接添加书籍时部分字段补全
                book.book_id = coverDetail!!.book_id
                book.book_source_id = coverDetail!!.book_source_id
                book.book_chapter_id = coverDetail!!.book_chapter_id

                book.status = coverDetail?.status   //更新书籍状态
                book.name = coverDetail?.name
                book.desc = coverDetail?.desc
                book.book_type = coverDetail?.book_type
                book.host = coverDetail?.host
                book.author = coverDetail?.author
                book.img_url = coverDetail?.img_url
                book.label = coverDetail?.label
                book.sub_genre = coverDetail?.sub_genre
                book.genre = coverDetail?.genre
                book.score = coverDetail!!.score

                val result = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).updateBook(book)
                AppLog.e("result", result.toString())
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
            ToastUtil.showToastMessage(activity.resources.getString(R.string.download_app_nofify_title))
        }

        val book = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(coverDetail!!.book_id)

        if (book != null) {
            BaseBookHelper.startDownBookTask(activity, coverDetail, 0)
        } else {
            val result = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).insertBook(coverDetail!!)

            if (result > 0) {
                coverPageContract.insertBookShelfResult(true)
                ToastUtil.showToastMessage("成功添加到书架！")

                BaseBookHelper.startDownBookTask(activity, coverDetail, 0)
            }
        }
        coverPageContract.changeDownloadButtonStatus()
    }

    /**
     * 暂停下载书籍
     */
    fun handleDownloadContinueOrStop() {

        coverDetail?.let {
            val downloadState = CacheManager.getBookStatus(it)
            if (downloadState == DownloadState.DOWNLOADING) {
                CacheManager.stop(it.book_id)
                coverPageContract.changeDownloadButtonStatus()
            } else {
                handleDownloadAction()
            }
        }

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
        if (view is RecommendItemView) {
            activity.enterSearch(
                    word = view.title, search_type = "0", filter_type = "0", filter_word = "ALL", sort_type = "0")
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
            RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestBookRecommend(book_id, bookIDs, object : RequestSubscriber<RecommendBooks>() {
                override fun requestResult(result: RecommendBooks?) {
                    if (result != null) {
                        handleRecommendBooks(result)
                        changeRecommendBooks()
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

    /**
     * 获取封面页推荐书籍，随机推荐
     */

    fun requestCoverRecommendRandom(bookSize: Int) {

        if (book_id != null && !TextUtils.isEmpty(book_id)) {
            val bookIDs: String = loadBookShelfID()
            RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestBookRecommend(book_id, bookIDs, object : RequestSubscriber<RecommendBooks>() {
                override fun requestResult(result: RecommendBooks?) {

                    if (result?.znList != null) {
                        if (result.znList!!.size > bookSize) {
                            coverPageContract.showRecommendSuccess(getRandomBooks(bookSize, result.znList!!))
                        } else {
                            coverPageContract.showRecommendSuccess(result.znList!!)
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

    /**
     * 从数据源中随机获取定长度的书
     * 开始下标随机，后面书籍顺序累计添加
     */
    fun getRandomBooks(size: Int, books: ArrayList<RecommendBean>): ArrayList<RecommendBean> {
        var resultList = ArrayList<RecommendBean>()
        var randow = Random()
        var startIndex = randow.nextInt(books.size)
        for (i in 1..size) {
            resultList.add(books[startIndex % books.size])
            startIndex++
        }

        return resultList
    }


    /**
     * 推荐该作者的其他作品
     */
    fun requestAuthorOtherBookRecommend() {

        if (author != null && !TextUtils.isEmpty(author) && book_id != null && !TextUtils.isEmpty(book_id)) {
            RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestAuthorOtherBookRecommend(author!!, book_id, object : RequestSubscriber<ArrayList<RecommendBean>>() {
                override fun requestResult(result: ArrayList<RecommendBean>?) {
                    if (result != null) {
                        coverPageContract.showAuthorRecommendSuccess(result)
                    } else {
                        coverPageContract.showRecommendFail()
                    }
                }

                override fun requestError(message: String) {
                    Logger.e("获取作者推荐异常！")
                    coverPageContract.showRecommendFail()
                }
            })
        }
    }

    private val mRecommendBooks = ArrayList<Book>()
    private val markIndexs = ArrayList<Int>()//用于标记推荐书籍

    /***
     * 获取封面页推荐书籍,  例如 今日多看 使用的v4接口
     * **/
    fun requestCoverRecommendV4() {
        if (book_id != null && !TextUtils.isEmpty(book_id)) {
            val bookIDs: String = loadBookShelfID()
            RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestCoverRecommend(book_id, bookIDs, object : RequestSubscriber<CoverRecommendBean>() {
                override fun requestResult(bean: CoverRecommendBean?) {

                    mRecommendBooks.clear()

                    if (bean != null && bean.data != null
                            && bean!!.data!!.map != null) {

                        val scale = SPUtils.getOnlineConfigSharedString(SPKey.RECOMMEND_BOOKCOVER, "3,3,0").split(",")
                        if (scale.size >= 2) {
                            if (!TextUtils.isEmpty(scale[0])) {
                                addZNBooks(bean, Integer.parseInt(scale[0]))
                            }
                            if (!TextUtils.isEmpty(scale[1])) {
                                addQGBooks(bean, Integer.parseInt(scale[1]))
                            }

                        }
                        coverPageContract.showRecommendSuccessV4(mRecommendBooks)

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

    /**
     * 添加推荐的智能的书
     */
    fun addZNBooks(bean: CoverRecommendBean, znSize: Int) {
        var znIndex = -1
        markIndexs.clear()
        if (bean.data!!.map!!.znList != null && bean.data!!.map!!.znList!!.size > 0) {
            for (i in 0 until znSize) {//推荐位 智能只取 3本
                znIndex = mRandom!!.nextInt(bean.data!!.map!!.znList!!.size)
                if (markIndexs.contains(znIndex)) {
                    while (true) {
                        znIndex = mRandom!!.nextInt(bean.data!!.map!!.znList!!.size)
                        if (!markIndexs.contains(znIndex)) {
                            break
                        }
                    }
                }
                markIndexs.add(znIndex)
                val book = Book()
                val znBean = bean.data!!.map!!.znList!![znIndex]
                if (book_id != null && !book_id.equals(znBean.bookId)) {
                    book.status = znBean.serialStatus
                    book.book_id = znBean.bookId ?: ""
                    book.book_source_id = znBean.id ?: ""
                    book.name = znBean.bookName
                    book.label = znBean.label
                    book.author = znBean.authorName
                    book.img_url = znBean.sourceImageUrl
                    book.host = znBean.host
                    book.chapter_count = Integer.valueOf(znBean.chapterCount)
//                    if(!AppUtils.isContainChinese(znBean.readerCountDescp + "")){
//                        book.uv = java.lang.Long.valueOf(znBean.readerCountDescp + "")
//                    }
                    // 这里用desc字段临时接收readerCountDescp, 后期推荐接口统一升级为v5
                    book.desc = znBean.readerCountDescp + ""
                    mRecommendBooks.add(book)
                }

            }
        }
    }

//    private val mRandom: Random by lazy {
//        Random()
//    }

    /**
     * 添加推荐的青果的书
     */
    fun addQGBooks(bean: CoverRecommendBean, qgSize: Int) {
        markIndexs.clear()
        var qgIndex = -1
        if (bean.data!!.map!!.qgList != null && bean.data!!.map!!.qgList!!.size > 0) {
            for (i in 0 until qgSize) {//推荐位 青果只取 3本
                qgIndex = mRandom!!.nextInt(bean.data!!.map!!.qgList!!.size)
                if (markIndexs.contains(qgIndex)) {
                    while (true) {
                        qgIndex = mRandom!!.nextInt(bean.data!!.map!!.qgList!!.size)
                        if (!markIndexs.contains(qgIndex)) {
                            break
                        }
                    }
                }
                markIndexs.add(qgIndex)
                val book = Book()
                val qgBean = bean.data!!.map!!.qgList!![qgIndex]
                if (book_id != null && !book_id.equals(qgBean.id)) {

                    book.status = qgBean.serialStatus
                    book.book_id = qgBean.id ?: ""
                    book.book_source_id = qgBean.bookSourceId ?: ""
                    book.name = qgBean.bookName
                    book.label = qgBean.labels
                    book.author = qgBean.author_name
                    book.img_url = qgBean.image + ""
                    book.host = qgBean.host + ""
                    book.chapter_count = Integer.valueOf(qgBean.chapter_sn)
                    book.uv = qgBean.read_count.toLong()
                    mRecommendBooks.add(book)
                }
            }
        }


    }


    fun handleRecommendBooks(recommendBooks: RecommendBooks?) {
        if (recommendBooks != null) {

            recommendBookList.clear()

            var scale: List<String>? = ArrayList<String>()
            if (AppUtils.getPackageName().equals("cn.txtqbmfyd.reader")) {
                scale = SPUtils.getOnlineConfigSharedString(SPKey.RECOMMEND_BOOKCOVER, "2,2,0")?.split(",")
            } else {
                scale = SPUtils.getOnlineConfigSharedString(SPKey.RECOMMEND_BOOKCOVER, "3,3,0")?.split(",")
            }


            if (scale != null) {
                var znScale = 0
                var qgScale = 0
                var feeScale = 0

                if (scale.isNotEmpty()) {
                    znScale = Integer.parseInt(scale[0])
                }

                if (scale.size > 1) {
                    qgScale = Integer.parseInt(scale[1])
                }

                if (scale.size > 2) {
                    feeScale = Integer.parseInt(scale[2])
                }

                recommendCount = znScale + qgScale
                var znList: ArrayList<List<RecommendBean>>? = null
                if (znScale > 0 && recommendBooks.znList != null && recommendBooks.znList!!.size > 0) {
                    znList = subRecommendList(recommendBooks.znList!!, znScale)
                }

                var qgList: ArrayList<List<RecommendBean>>? = null
                if (qgScale > 0 && recommendBooks.qgList != null && recommendBooks.qgList!!.size > 0) {
                    qgList = subRecommendList(recommendBooks.qgList!!, qgScale)
                }

                var feeList: ArrayList<List<RecommendBean>>? = null
                if (feeScale > 0 && recommendBooks.feeList != null && recommendBooks.feeList!!.size > 0) {
                    feeList = subRecommendList(recommendBooks.feeList!!, feeScale)
                }

                var count = 0

                if (znList != null && znList.size > 0 && qgList != null && qgList.size > 0 && feeList != null && feeList.size > 0) {
                    count = Math.min(znList.size, Math.min(qgList.size, feeList.size))
                } else if (qgList != null && qgList.size > 0 && feeList != null && feeList.size > 0) {
                    count = Math.min(qgList.size, feeList.size)
                } else if (znList != null && znList.size > 0 && qgList != null && qgList.size > 0) {
                    count = Math.min(znList.size, qgList.size)
                } else if (znList != null && znList.size > 0 && feeList != null && feeList.size > 0) {
                    count = Math.min(znList.size, feeList.size)
                } else if (znList != null && znList.size > 0) {
                    count = znList.size
                } else if (qgList != null && qgList.size > 0) {
                    count = qgList.size
                } else if (feeList != null && feeList.size > 0) {
                    count = feeList.size
                }

                for (i in 0 until count) {
                    if (znList != null && i < znList.size) {
                        recommendBookList.addAll(znList[i])
                    }

                    if (qgList != null && i < qgList.size) {
                        recommendBookList.addAll(qgList[i])
                    }

                    if (feeList != null && i < feeList.size) {
                        recommendBookList.addAll(feeList[i])
                    }
                }

                znList?.clear()
                qgList?.clear()
                feeList?.clear()
            }
        }
    }

    private fun subRecommendList(recommends: ArrayList<RecommendBean>, scale: Int): ArrayList<List<RecommendBean>> {

        val result = ArrayList<List<RecommendBean>>()
        var list = ArrayList<RecommendBean>()

        for (i in 0 until recommends.size) {
            list.add(recommends[i])

            if (list.size == scale) {
                result.add(list)
                list = ArrayList()
            }
        }

        return result
    }

    fun changeRecommendBooks() {

        if (recommendIndex + recommendCount > recommendBookList.size) {
            recommendIndex = 0
        }

        recommendList.clear()

        (recommendIndex until recommendIndex + recommendCount)
                .filter { recommendBookList.size > it }
                .forEach { recommendList.add(recommendBookList[it]) }

        recommendIndex += recommendCount

        coverPageContract.showRecommendSuccess(recommendList)
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