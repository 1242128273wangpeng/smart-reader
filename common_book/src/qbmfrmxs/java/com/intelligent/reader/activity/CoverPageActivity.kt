package com.intelligent.reader.activity

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.Book
import com.ding.basic.bean.RecommendBean
import com.dingyue.contract.util.showToastMessage
import com.intelligent.reader.R
import com.intelligent.reader.adapter.BookRecommendAdapter
import com.intelligent.reader.presenter.coverPage.CoverPageContract
import com.intelligent.reader.presenter.coverPage.CoverPagePresenter
import iyouqu.theme.BaseCacheableActivity
import kotlinx.android.synthetic.qbmfrmxs.act_book_cover.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.CallBackDownload
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.constants.ReplaceConstants
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.Tools
import java.text.MessageFormat
import java.util.*
import java.util.concurrent.Callable

@Route(path = RouterConfig.COVER_PAGE_ACTIVITY)
class CoverPageActivity : BaseCacheableActivity(), OnClickListener, CoverPageContract, CallBackDownload {
    private var loadingPage: LoadingPage? = null
    private var coverPagePresenter: CoverPagePresenter? = null

    private var recommendList: ArrayList<RecommendBean>? = null
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
        if (txt_book_detail_shelf != null) {
            txt_book_detail_shelf!!.isClickable = true
            insertBookShelfResult(false)
        }
        coverPagePresenter?.destroy()
        initializeIntent(intent)
    }

    private fun initListener() {
        img_book_detail_back?.setOnClickListener(this)
        txt_book_detail_author.setOnClickListener(this)
        rl_book_detail_catalog.setOnClickListener(this)

        txt_book_detail_shelf.setOnClickListener(this)
        txt_book_detail_read.setOnClickListener(this)
        txt_book_detail_cache.setOnClickListener(this)
        txt_book_detail_recommend_change.setOnClickListener(this)
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

            sfgv_book_detail_recommend.adapter = bookRecommendAdapter

            sfgv_book_detail_recommend.setOnItemClickListener { _, _, position, _ ->
                recommendList?.let {
                    val recommendBean = it[position]
                    val data = HashMap<String, String>()

                    if (bookId != null && !TextUtils.isEmpty(bookId)) {
                        data["bookid"] = bookId!!
                    }

                    if (!TextUtils.isEmpty(recommendBean.bookId)) {
                        data["Tbookid"] = recommendBean.bookId
                    }

                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.RECOMMENDEDBOOK, data)

                    val bundle = Bundle()
                    bundle.putString("book_id", recommendBean.bookId)
                    bundle.putString("book_source_id", recommendBean.id)
                    bundle.putString("book_chapter_id", recommendBean.bookChapterId)

                    RouterUtil.navigation(this@CoverPageActivity, RouterConfig.COVER_PAGE_ACTIVITY, bundle)
                }
            }
        }
    }

    private fun requestBookDetail() {

        if (loadingPage != null) {
            loadingPage!!.onSuccess()
        }

        loadingPage = LoadingPage(this, findViewById(R.id.rl_book_detail_content),
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

        coverPagePresenter?.requestCoverRecommend()
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
            R.id.img_book_detail_back -> {
                val data = HashMap<String, String>()
                data["type"] = "1"
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.BACK, data)
                finish()
            }

            R.id.txt_book_detail_shelf -> if (coverPagePresenter != null) {
                coverPagePresenter?.handleBookShelfAction(false)
            }

            R.id.txt_book_detail_read -> {
                //转码阅读点击的统计
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_trans_read)
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEREAD)
                if (coverPagePresenter != null) {
                    coverPagePresenter?.handleReadingAction()
                }
            }

            R.id.txt_book_detail_cache -> {
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
            R.id.rl_book_detail_catalog -> {
                //书籍详情页查看目录点击
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_to_catalogue)
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.CATALOG)
                if (coverPagePresenter != null) {
                    coverPagePresenter?.startCatalogActivity(true)
                }
            }

            R.id.txt_book_detail_recommend_change -> {
                if (coverPagePresenter != null) {
                    coverPagePresenter?.changeRecommendBooks()
                }
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

            txt_book_detail_cache.visibility = View.VISIBLE

            book_id?.let {
                val book = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(it)

                if (book != null) {

                    val downloadState = CacheManager.getBookStatus(book)

                    bookDownloadState = downloadState

                    when (downloadState) {
                        DownloadState.FINISH -> txt_book_detail_cache.text = "缓存完成"
                        DownloadState.PAUSEED -> txt_book_detail_cache.text = "缓存已暂停"
                        DownloadState.NOSTART -> txt_book_detail_cache.text = "全本缓存"
                        DownloadState.DOWNLOADING -> txt_book_detail_cache.text = "缓存中"
                        else -> {

                        }
                    }
                } else {
                    txt_book_detail_cache.text = "全本缓存"
                }
            }
        } else {
            txt_book_detail_cache.visibility = View.GONE
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

            if (img_book_detail_cover != null && !TextUtils.isEmpty(book.img_url) &&
                    book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
                Glide.with(applicationContext)
                        .load(book.img_url)
                        .placeholder(R.drawable.common_book_cover_default_icon)
                        .error(R.drawable.common_book_cover_default_icon)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(img_book_detail_cover)
            } else {
                Glide.with(applicationContext)
                        .load(R.drawable.common_book_cover_default_icon)
                        .into(img_book_detail_cover)
            }

            if (book.status == "SERIALIZE") {
                img_book_detail_state.setImageResource(R.drawable.book_serialize_icon)
            } else {
                img_book_detail_state.setImageResource(R.drawable.book_finish_icon)
            }

            if (txt_book_detail_name != null && !TextUtils.isEmpty(book.name)) {
                txt_book_detail_name.text = book.name
            }

            if (txt_book_detail_word_count != null) {
                txt_book_detail_word_count.text = if (TextUtils.isEmpty(book.word_count) || AppUtils.isContainChinese(book.word_count)) "暂无" else AppUtils.getWordNums(java.lang.Long.parseLong(book.word_count))
            }

            if (txt_book_detail_author != null && !TextUtils.isEmpty(book.author)) {
                txt_book_detail_author.text = book.author
            }

            if (txt_book_detail_source != null && !TextUtils.isEmpty(book.book_type)) {
                if (book.book_type == "qg") {
                    txt_book_detail_source.text = "青果阅读"
                } else {
                    txt_book_detail_source.text = book.host
                }
            }

            rb_book_detail_score.rating = book.score / 2.0F

            if (book.score == 0.0F) {
                txt_book_detail_score.text = "暂无评分"
            } else {
                txt_book_detail_score.text = MessageFormat.format("{0}分", book.score)
            }


            if (book.uv == 0L) {
                txt_book_detail_popularity.visibility = View.GONE
            } else {
                txt_book_detail_popularity.visibility = View.VISIBLE
                txt_book_detail_popularity.text = AppUtils.getCommonReadNums(book.uv)
            }

            if (book.desc != null && !TextUtils.isEmpty(book.desc)) {
                txt_book_detail_desc.setText(book.desc)
            } else {
                txt_book_detail_desc.setText("暂无简介")
            }

            if (book.last_chapter != null) {

                if (!TextUtils.isEmpty(book.last_chapter?.name)) {
                    txt_book_detail_last_chapter.text = MessageFormat.format("最新章节：{0}", book.last_chapter?.name)
                }

                if (book.last_chapter?.update_time != 0L) {
                    val updateTime = "${Tools.compareTime(AppUtils.formatter, book.last_chapter!!.update_time)}更新"
                    txt_book_detail_update_time.text = updateTime
                }
            }
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
                DownloadState.FINISH -> txt_book_detail_cache.text = "缓存完成"
                DownloadState.PAUSEED -> txt_book_detail_cache.text = "缓存已暂停"
                DownloadState.NOSTART -> txt_book_detail_cache.text = "全本缓存"
                DownloadState.DOWNLOADING -> txt_book_detail_cache.text = "缓存中"
                else -> {

                }
            }
        }

        if (!coverPagePresenter!!.checkBookSubscribe()) {
            txt_book_detail_cache.text = "全本缓存"
        }
    }

    override fun insertBookShelfResult(result: Boolean) {
        if (result) {
            txt_book_detail_shelf?.text = "已在书架"
            txt_book_detail_shelf.setTextColor(Color.parseColor("#B9B9B9"))
        } else {
            txt_book_detail_shelf.setTextColor(Color.parseColor("#42BE54"))
            txt_book_detail_shelf?.text = "加入书架"
        }
    }

    override fun changeShelfButtonClickable(clickable: Boolean) {
        if (txt_book_detail_shelf != null) {
            txt_book_detail_shelf.isClickable = clickable
        }
    }

    override fun bookSubscribeState(subscribe: Boolean) {
        if (subscribe) {
            txt_book_detail_shelf?.text = "已在书架"
            txt_book_detail_shelf.setTextColor(Color.parseColor("#B9B9B9"))
        }
    }

    override fun showRecommendSuccess(recommends: ArrayList<RecommendBean>) {
        if (recommends.size == 0) {
            rl_book_detail_recommend.visibility = View.GONE
            sfgv_book_detail_recommend.visibility = View.GONE
        } else {
            rl_book_detail_recommend.visibility = View.VISIBLE
            sfgv_book_detail_recommend.visibility = View.VISIBLE

            recommendList = recommends
            bookRecommendAdapter.setData(recommends)

            initGridViewHeight()
        }
    }

    override fun showRecommendFail() {

    }

    private fun initGridViewHeight() {
        var childMaxHeight = 0

        for (i in 0 until bookRecommendAdapter.count) {
            val childView = bookRecommendAdapter.getView(i, null, sfgv_book_detail_recommend)
            if (childView != null) {
                childView.measure(0, 0)

                if (childView.measuredHeight > childMaxHeight) {
                    childMaxHeight = childView.measuredHeight
                }
            }
        }

        val layoutParameters = sfgv_book_detail_recommend.layoutParams
        layoutParameters.height = childMaxHeight * 2

        sfgv_book_detail_recommend.layoutParams = layoutParameters

        bookRecommendAdapter.notifyDataSetChanged()
    }
}