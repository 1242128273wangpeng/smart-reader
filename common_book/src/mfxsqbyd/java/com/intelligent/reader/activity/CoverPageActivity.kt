package com.intelligent.reader.activity

import android.annotation.SuppressLint
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
import com.ding.basic.bean.RecommendBean
import com.ding.basic.repository.RequestRepositoryFactory
import com.dingyue.contract.router.BookRouter
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.util.CommonUtil
import com.intelligent.reader.R
import com.intelligent.reader.adapter.BookRecommendAdapter
import com.intelligent.reader.presenter.coverPage.CoverPageContract
import com.intelligent.reader.presenter.coverPage.CoverPagePresenter
import iyouqu.theme.BaseCacheableActivity
import kotlinx.android.synthetic.mfxsqbyd.act_book_cover.*
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.CallBackDownload
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.StatServiceUtils
import java.text.DecimalFormat
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
        if (book_cover_bookshelf != null) {
            book_cover_bookshelf!!.isClickable = true
            insertBookShelfResult(false)
        }
        coverPagePresenter?.destroy()
        initializeIntent(intent)
    }

    private fun initListener() {
        book_cover_back?.setOnClickListener(this)
        book_cover_author.setOnClickListener(this)
//        book_cover_source_view.setOnClickListener(this)
        book_catalog_tv.setOnClickListener(this)
        book_cover_last_chapter_tv.setOnClickListener(this)
        rl_catalog.setOnClickListener(this)

        book_cover_bookshelf.setOnClickListener(this)
        book_cover_reading.setOnClickListener(this)
        book_cover_download_iv.setOnClickListener(this)
//        book_cover_catalog_view_nobg.setOnClickListener(this)
        book_cover_content.topShadow = img_head_shadow

        book_cover_content.scrollChanged = {
            book_cover_bookname.visibility = if (it > AppUtils.dp2px(resources, 178f)) View.VISIBLE else View.GONE
        }
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
            coverPagePresenter = CoverPagePresenter(bookId, bookSourceId, bookChapterId, this, this, this)

            requestBookDetail()

            bookRecommendAdapter = BookRecommendAdapter()

            book_recommend_lv.adapter = bookRecommendAdapter
            book_recommend_lv.setOnItemClickListener { _, _, position, _ ->
                recommendList?.let {
                    val recommendBean = it[position]
                    val data = HashMap<String, String>()

                    if (bookId != null && !TextUtils.isEmpty(bookId)) {
                        data["bookid"] = bookId!!
                    }

                    if (!TextUtils.isEmpty(recommendBean.bookId)) {
                        data["Tbookid"] = recommendBean.bookId!!
                    }

                    val book = Book()
                    book.book_id = recommendBean.bookId
                    book.book_source_id = recommendBean.id
                    book.book_chapter_id = recommendBean.bookChapterId
                    BookRouter.navigateCover(this, book)

                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.RECOMMENDEDBOOK, data)
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
            R.id.book_cover_back -> {
                val data = HashMap<String, String>()
                data["type"] = "1"
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.BACK, data)
                finish()
            }

            R.id.book_cover_bookshelf -> if (coverPagePresenter != null) {
                coverPagePresenter?.handleBookShelfAction(false)
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
            R.id.book_catalog_tv,R.id.rl_catalog -> {
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
                        DownloadState.NOSTART -> {
                            book_cover_download_iv.setImageResource(R.mipmap.cover_down_normal_icon)
                        }
                        else -> {
                            book_cover_download_iv.setImageResource(R.mipmap.cover_down_unable_icon)
                        }
                    }
                } else {
                    book_cover_download_iv.setImageResource(R.mipmap.cover_down_normal_icon)
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
        CommonUtil.showToastMessage("请求失败！")
    }

    override fun showLoadingSuccess() {
        if (loadingPage != null) {
            loadingPage?.onSuccess()
        }
    }


    @SuppressLint("SetTextI18n")
    override fun showCoverDetail(bookVo: Book?) {
        book_cover_content.smoothScrollTo(0, 0)
        if (bookVo != null) {
            if (book_cover_bookname != null && !TextUtils.isEmpty(bookVo.name)) {
                book_cover_bookname.text = bookVo.name
            }

            if (book_cover_image != null && !TextUtils.isEmpty(bookVo.img_url) && bookVo
                    .img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
                Glide.with(applicationContext).load(bookVo.img_url).placeholder(net.lzbook.kit.R.drawable.icon_book_cover_default).error(net.lzbook.kit.R.drawable.icon_book_cover_default).diskCacheStrategy(DiskCacheStrategy.ALL).into(book_cover_image)
            } else {
                Glide.with(applicationContext).load(net.lzbook.kit.R.drawable.icon_book_cover_default).into(book_cover_image)
            }

            if (book_cover_title != null && !TextUtils.isEmpty(bookVo.name)) {
                book_cover_title.text = bookVo.name
            }

            if (book_cover_author != null && !TextUtils.isEmpty(bookVo.author)) {
                book_cover_author.text = bookVo.author
            }

            if (book_cover_category2 != null ) {
                    book_cover_category2.text = bookVo.sub_genre
            }

            if (bookVo.status == "SERIALIZE") {
                if (book_cover_category2.visibility != View.VISIBLE) {
                    book_cover_status.text = "—" + getString(R.string.book_cover_state_writing)
                } else {
                    book_cover_status.text = getString(R.string.book_cover_state_writing)
                }
            } else {
                if (book_cover_category2.visibility != View.VISIBLE) {
                    book_cover_status.text = "—" + getString(R.string.book_cover_state_written)
                } else {
                    book_cover_status.text = getString(R.string.book_cover_state_written)
                }
            }

            if (!TextUtils.isEmpty(bookVo.last_chapter?.name)) {
                book_cover_last_chapter_tv.text = "更新至：" + bookVo.last_chapter?.name
            }

            if (bookVo.desc != null && !TextUtils.isEmpty(bookVo.desc)) {
                book_cover_description.text = bookVo.desc
            } else {
                book_cover_description.text = resources.getString(R.string
                        .book_cover_no_description)
            }

            if (word_count_tv != null && bookVo.word_count != null && !AppUtils.isContainChinese(bookVo.word_count)) {
                word_count_tv.text = AppUtils.getWordNums(java.lang.Long.parseLong(bookVo.word_count))
            } else {
                word_count_tv.text = "暂无"
            }


            if (!TextUtils.isEmpty(AppUtils.getCommonReadNums(bookVo.uv))) {
                reading_tv.text = AppUtils.getCommonReadNums(bookVo.uv) + "值"
            } else {
                reading_tv!!.text = "暂无"
            }
            if (bookVo.score == 0.0f) {
                start_tv.text = "暂无评分"
            } else {
                bookVo.score = java.lang.Float.valueOf(DecimalFormat("0.00").format(bookVo.score))!!
                start_tv!!.text = bookVo.score.toString() + "分"

            }
        } else {
            CommonUtil.showToastMessage(R.string.book_cover_no_resource)
            if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
                finish()
            }
        }
    }

    override fun changeDownloadButtonStatus() {
        val book =coverPagePresenter?.coverDetail ?: return
        val status = CacheManager.getBookStatus(book)
        bookDownloadState = status

        coverPagePresenter?.let {
            when (status) {
                DownloadState.NOSTART -> {
                    book_cover_download_iv.setImageResource(R.mipmap.cover_down_normal_icon)
                }
                else -> {
                    book_cover_download_iv.setImageResource(R.mipmap.cover_down_unable_icon)
                }
            }

            if (!coverPagePresenter!!.checkBookSubscribe()) {
                book_cover_download_iv.setImageResource(R.mipmap.cover_down_normal_icon)
            }
        }


    }
    override fun insertBookShelfResult(result: Boolean) {
        if (result) {
            book_cover_bookshelf!!.setText(R.string.cover_bookshelf_had)
            val textCsl = getResources().getColor(R.color.cover_bottom_add)
            book_cover_bookshelf.setTextColor(textCsl)
        } else {
            val textCsl = getResources().getColor(R.color.text_color_dark)
            book_cover_bookshelf.setTextColor(textCsl)
            book_cover_bookshelf!!.setText(R.string.add_bookshelf)
        }
    }

    override fun changeShelfButtonClickable(clickable: Boolean) {
        if (book_cover_bookshelf != null) {
            book_cover_bookshelf.isClickable = clickable
        }
    }

    override fun bookSubscribeState(subscribe: Boolean) {
        if (subscribe) {
            book_cover_bookshelf!!.setText(R.string.cover_bookshelf_had)
            val textCsl = getResources().getColor(R.color.cover_bottom_add)
            book_cover_bookshelf.setTextColor(textCsl)
        }
    }

    override fun showRecommendSuccess(recommends: ArrayList<RecommendBean>) {
        ll_recommend_title.visibility = View.VISIBLE
        recommendList = recommends
        bookRecommendAdapter.setData(recommends)
    }

    override fun showRecommendFail() {

    }
}
