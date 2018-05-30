package com.intelligent.reader.activity

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ding.basic.bean.Book
import com.ding.basic.repository.RequestRepositoryFactory
import com.dingyue.contract.router.BookRouter
import com.dingyue.contract.util.showToastMessage
import com.intelligent.reader.R
import com.intelligent.reader.adapter.BookRecommendAdapter
import com.intelligent.reader.presenter.coverPage.CoverPageContract
import com.intelligent.reader.presenter.coverPage.CoverPagePresenter
import iyouqu.theme.BaseCacheableActivity
import kotlinx.android.synthetic.txtqbmfyd.act_book_cover.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.CallBackDownload
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.constants.ReplaceConstants
import com.dingyue.contract.router.RouterConfig
import com.intelligent.reader.read.mode.ReadState.requestItem
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.StatServiceUtils
import java.text.MessageFormat
import java.util.*
import java.util.concurrent.Callable

@Route(path = RouterConfig.COVER_PAGE_ACTIVITY)
class CoverPageActivity : BaseCacheableActivity(), OnClickListener, CoverPageContract, CallBackDownload {
    private var loadingPage: LoadingPage? = null
    private var coverPagePresenter: CoverPagePresenter? = null

    private var recommendList: ArrayList<Book>? = null
    private lateinit var bookRecommendAdapter: BookRecommendAdapter
    private var bookDownloadState: DownloadState = DownloadState.NOSTART


    private var bookId: String? = null
    private var bookSourceId: String? = null
    private var bookChapterId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatServiceUtils.statAppBtnClick(this, StatServiceUtils.cover_into)
        setContentView(R.layout.act_book_cover)
        initializeIntent(intent)
        initListener()
    }


    override fun onNewIntent(intent: Intent) {
        initializeIntent(intent)
    }

    private fun initListener() {
        book_cover_back?.setOnClickListener(this)
        book_cover_author.setOnClickListener(this)
//        book_cover_source_view.setOnClickListener(this)
        book_catalog_tv.setOnClickListener(this)
        book_cover_last_chapter_tv.setOnClickListener(this)

        book_cover_bookshelf.setOnClickListener(this)
        book_cover_reading.setOnClickListener(this)
        book_cover_download_iv.setOnClickListener(this)
//        book_cover_catalog_view_nobg.setOnClickListener(this)
        book_cover_content.topShadow = img_head_shadow
    }

    private fun initializeIntent(intent: Intent?) {
        if (intent != null) {
            if (intent.hasExtra("book_id")) {
                bookId = intent.getStringExtra("book_id")
            }

            if (intent.hasExtra("book_source_id")) {
                bookSourceId = intent.getStringExtra("book_source_id")
            }
            if (intent.hasExtra("book_chapter_id")) {
                bookChapterId = intent.getStringExtra("book_chapter_id")
            }
        }

        if (!TextUtils.isEmpty(bookId) && (!TextUtils.isEmpty(bookSourceId) || !TextUtils.isEmpty(bookChapterId))) {
            coverPagePresenter = CoverPagePresenter(bookId, bookSourceId, bookChapterId,this, this, this)
            requestBookDetail()


            bookRecommendAdapter = BookRecommendAdapter()

            book_recommend_lv.adapter = bookRecommendAdapter
            book_recommend_lv.setOnItemClickListener { _, _, position, _ ->
                recommendList?.let {
                    val book = it[position]
                    val data = HashMap<String, String>()
                    if (requestItem != null && requestItem!!.book_id != null) {
                        data["bookid"] = requestItem!!.book_id
                        data["Tbookid"] = book.book_id
                    }
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.RECOMMENDEDBOOK, data)
                    BookRouter.navigateCoverOrRead(this, book, BookRouter.NAVIGATE_TYPE_RECOMMEND)
                }
            }
        }
    }

    private fun requestBookDetail() {

        if (loadingPage != null) {
            loadingPage!!.onSuccess()
        }

        loadingPage = LoadingPage(this, findViewById(R.id.book_cover_main),
                LoadingPage.setting_result)

        if (coverPagePresenter != null) {
            coverPagePresenter?.requestBookDetail(false)
        }

        if (loadingPage != null) {
            loadingPage!!.setReloadAction(Callable<Void> {
                if (coverPagePresenter != null) {
                    coverPagePresenter?.requestBookDetail(false)
                }
                null
            })
        }
    }


    override fun onTaskStatusChange(book_id: String?) {
        requestBookDownloadState(book_id)
    }

    override fun onTaskFinish(book_id: String?) {
        requestBookDownloadState(book_id)
    }

    override fun onTaskFailed(book_id: String?, t: Throwable?) {
    }

    override fun onTaskProgressUpdate(book_id: String?) {
    }

    override fun onResume() {
        super.onResume()
        if (coverPagePresenter != null) {
            coverPagePresenter?.refreshNavigationState()
        }
        CacheManager.listeners.add(this)
    }

    override fun onPause() {
        super.onPause()
        CacheManager.listeners.remove(this)
    }


    override fun onClick(view: View) {
        if (coverPagePresenter != null) {
            coverPagePresenter?.checkStartSearchActivity(view)
        }
        when (view.id) {
            R.id.book_cover_back -> {
                val data = HashMap<String, String>()
                data["type"] = "1"
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.BACK, data)
                finish()
            }

            R.id.book_cover_bookshelf -> if (coverPagePresenter != null) {
                coverPagePresenter?.handleBookShelfAction(true)
            }

            R.id.book_cover_reading -> {
                //转码阅读点击的统计
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_trans_read)
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEREAD)
                if (coverPagePresenter != null) {
                    coverPagePresenter?.handleReadingAction()
                }
            }

            R.id.book_cover_download_iv -> {
                bookId?.let {
                    StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_all_load)
                    val dataDownload = HashMap<String, String>()
                    dataDownload["bookId"] = it

                    if (coverPagePresenter != null) {
                        requestBookDownloadState(it)

                        if (bookDownloadState == DownloadState.DOWNLOADING) {
                            CacheManager.stop(it)
                        } else {
                            coverPagePresenter?.handleDownloadAction()
                            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.CASHEALL, dataDownload)
                        }
                    }
                }
            }
            R.id.book_catalog_tv -> {
                //书籍详情页查看目录点击
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_to_catalogue)
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.CATALOG)
                if (coverPagePresenter != null) {
                    coverPagePresenter?.startCatalogActivity(true)
                }
            }
            R.id.book_cover_last_chapter_tv -> {
                if (coverPagePresenter != null) {
                    coverPagePresenter?.startCatalogActivity(false)
                }
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.LATESTCHAPTER)
            }
        }
    }


    override fun onDestroy() {
        try {
            setContentView(R.layout.common_empty)
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }

        if (coverPagePresenter != null) {
            coverPagePresenter?.destroy()
        }
        super.onDestroy()
    }

    /***
     * 获取下载状态
     * **/
    private fun requestBookDownloadState(book_id: String?) {
        if (!TextUtils.isEmpty(book_id)) {

            book_cover_download_iv.visibility = View.VISIBLE

            book_id?.let {
                val book = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(it)

                if (book != null) {

                    val downloadState = CacheManager.getBookStatus(book)

                    bookDownloadState = downloadState

                    when (downloadState) {
                        DownloadState.FINISH -> book_cover_download_iv.setImageResource(R.drawable.icon_cover_down_finish)
                        DownloadState.PAUSEED -> book_cover_download_iv.setImageResource(R.drawable.icon_cover_down_pause)
                        DownloadState.NOSTART -> book_cover_download_iv.setImageResource(R.drawable.icon_cover_down_normal)
                        DownloadState.DOWNLOADING -> book_cover_download_iv.setImageResource(R.drawable.icon_cover_down_running)
                        else -> {

                        }
                    }
                } else {
                    book_cover_download_iv.setImageResource(R.drawable.icon_cover_down_normal)
                }
            }
        } else {
            book_cover_download_iv.visibility = View.GONE
        }
    }


    override fun showLoadingFail() {
        if (loadingPage != null) {
            loadingPage?.onError()
        }
        this.showToastMessage("请求失败！")
    }

    override fun showLoadingSuccess() {
        if (loadingPage != null) {
            loadingPage?.onSuccess()
        }
    }

    override fun showCoverDetail(book: Book?) {
        if (isFinishing) {
            return
        }

        book_cover_content.smoothScrollTo(0, 0)

        if (book != null) {

            if (book_cover_image != null && !TextUtils.isEmpty(book.img_url) &&
                    book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
                Glide.with(applicationContext).load(book.img_url).placeholder(R.drawable.common_book_cover_default_icon).error(R.drawable.common_book_cover_default_icon).diskCacheStrategy(DiskCacheStrategy.ALL).into(book_cover_image)
            } else {
                Glide.with(applicationContext).load(R.drawable.common_book_cover_default_icon).into(book_cover_image)
            }

            if (book_cover_title != null && !TextUtils.isEmpty(book.name)) {
                book_cover_title.text = book.name
            }

            if (book_cover_author != null && !TextUtils.isEmpty(book.author)) {
                book_cover_author.text = book.author
            }

            if (book_cover_category2 != null) {
                if (!TextUtils.isEmpty(book.sub_genre)) {
                    book_cover_category2!!.text = book.sub_genre
                    book_cover_category2!!.visibility = View.VISIBLE
                } else {
                    book_cover_category2!!.visibility = View.GONE
                }
            }

            if (book.status == "SERIALIZE") {
                if (book_cover_category2.visibility != View.VISIBLE) {
                    book_cover_status.text = MessageFormat.format("—{0}", resources.getString(R.string.book_cover_state_writing))
                } else {
                    book_cover_status.text = resources.getString(R.string.book_cover_state_writing)
                }
            } else {
                if (book_cover_category2.visibility != View.VISIBLE) {
                    book_cover_status.text = MessageFormat.format("—{0}", resources.getString(R.string.book_cover_state_writing))
                } else {
                    book_cover_status.text = resources.getString(R.string.book_cover_state_written)
                }
            }

            if (!TextUtils.isEmpty(book.last_chapter?.name)) {
                book_cover_last_chapter_tv.text = MessageFormat.format("更新至：{0}", book.last_chapter?.name)
            }

            if (book.desc != null && !TextUtils.isEmpty(book.desc)) {
                book_cover_description.text = book.desc
            } else {
                book_cover_description.text = resources.getString(R.string
                        .book_cover_no_description)
            }

//            if (book.wordCountDescp != null) {
//                if (Constants.QG_SOURCE != book.host) {
//                    word_count_tv.text = book.wordCountDescp + "字"
//                } else {
//                    word_count_tv.text = AppUtils.getWordNums(java.lang.Long.valueOf(book.wordCountDescp)!!)
//                }
//            } else {
//                word_count_tv.text = "暂无"
//            }
//
//            if (book.readerCountDescp != null) {
//                if (Constants.QG_SOURCE == book.host) {
//                    reading_tv.text = AppUtils.getReadNums(java.lang.Long.valueOf(book.readerCountDescp)!!)
//                } else {
//                    reading_tv.text = book.readerCountDescp + "人在读"
//                }
//
//            } else {
//                reading_tv!!.text = "暂无"
//            }
//            if (book.score == 0.0) {
//                start_tv.text = "暂无评分"
//            } else {
//                if (Constants.QG_SOURCE != book.host) {
//                    book.score = java.lang.Double.valueOf(DecimalFormat("0.0").format(book.score))!!
//                }
//                start_tv.text = book.score.toString() + "分"
//
//            }
        } else {
            this.showToastMessage(R.string.book_cover_no_resource)

            if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
                finish()
            }
        }
    }

    override fun changeDownloadButtonStatus() {
        val book = coverPagePresenter?.loadCoverBook() ?: return
        val status = CacheManager.getBookStatus(book)
        bookDownloadState = status

        coverPagePresenter?.let {
            when (status) {
                DownloadState.FINISH -> book_cover_download_iv.setImageResource(R.drawable.icon_cover_down_finish)
                DownloadState.PAUSEED -> book_cover_download_iv.setImageResource(R.drawable.icon_cover_down_pause)
                DownloadState.NOSTART -> book_cover_download_iv.setImageResource(R.drawable.icon_cover_down_normal)
                DownloadState.DOWNLOADING -> book_cover_download_iv.setImageResource(R.drawable.icon_cover_down_running)
                else -> {

                }
            }
        }

        if (!coverPagePresenter!!.checkBookSubscribe()) {
            book_cover_download_iv.setImageResource(R.drawable.icon_cover_down_normal)
        }
    }

    override fun insertBookShelfResult(result: Boolean) {
        if (result) {
            book_cover_bookshelf!!.setText(R.string.book_cover_remove_bookshelf)
        } else {
            book_cover_bookshelf!!.setText(R.string.book_cover_add_bookshelf)
        }
    }

    override fun changeShelfButtonClickable(clickable: Boolean) {
        if (book_cover_bookshelf != null) {
            book_cover_bookshelf.isClickable = clickable
        }
    }

    override fun bookSubscribeState(subscribe: Boolean) {
        if (subscribe) {
            book_cover_bookshelf!!.setText(R.string.book_cover_remove_bookshelf)
        }
    }

    override fun showRecommendSuccess(recommendBean: ArrayList<Book>) {
        recommendList = recommendBean
        bookRecommendAdapter.setData(recommendBean)
    }

    override fun showRecommendFail() {

    }
}