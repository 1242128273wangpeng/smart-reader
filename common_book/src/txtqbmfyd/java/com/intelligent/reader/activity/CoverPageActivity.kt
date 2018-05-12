package com.intelligent.reader.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.intelligent.reader.R
import com.intelligent.reader.adapter.BookRecommendAdapter
import com.intelligent.reader.presenter.coverPage.CoverPageContract
import com.intelligent.reader.presenter.coverPage.CoverPagePresenter
import com.intelligent.reader.read.help.BookHelper
import iyouqu.theme.BaseCacheableActivity
import kotlinx.android.synthetic.txtqbmfyd.act_book_cover.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.CallBackDownload
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.CoverPage
import net.lzbook.kit.data.bean.RequestItem
import net.lzbook.kit.router.RouterConfig
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.StatServiceUtils
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.Callable

@Route(path = RouterConfig.COVER_PAGE_ACTIVITY)
class CoverPageActivity : BaseCacheableActivity(), OnClickListener, CoverPageContract, CallBackDownload {
    private var loadingPage: LoadingPage? = null
    private var requestItem: RequestItem? = null
    private var mCoverPagePresenter: CoverPagePresenter? = null
    private var mRecommendList: ArrayList<Book>? = null
    private lateinit var mBookRecommedAdapter: BookRecommendAdapter
    private var mBookDownlLoadState: DownloadState = DownloadState.NOSTART

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatServiceUtils.statAppBtnClick(this, StatServiceUtils.cover_into)
        setContentView(R.layout.act_book_cover)
        initData(intent)
        initListener()
    }


    override fun onNewIntent(intent: Intent) {
        initData(intent)
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

    private fun initData(intent: Intent?) {

        if (intent != null) {
            if (intent.hasExtra(Constants.REQUEST_ITEM)) {
                requestItem = intent.getSerializableExtra(Constants.REQUEST_ITEM) as RequestItem
            }
        }
        requestItem?.let {
            mCoverPagePresenter = CoverPagePresenter(it, this, this, this)
            loadCoverInfo()
        }
        mBookRecommedAdapter = BookRecommendAdapter()
        book_recommend_lv.adapter = mBookRecommedAdapter
        book_recommend_lv.setOnItemClickListener { _, _, position, _ ->
            mRecommendList?.let {
                val book = it[position]
                val data = HashMap<String, String>()
                if (requestItem != null && requestItem!!.book_id != null) {
                    data.put("bookid", requestItem!!.book_id)
                    data.put("Tbookid", book.book_id)
                }
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.RECOMMENDEDBOOK, data)
                BookHelper.goToCoverOrRead(this, this, book, 2)
            }
        }
    }

    private fun loadCoverInfo() {

        if (loadingPage != null) {
            loadingPage?.onSuccess()
        }

        loadingPage = LoadingPage(this, findViewById(R.id.book_cover_main) as ViewGroup,
                LoadingPage.setting_result)

        requestItem?.let {
            mCoverPagePresenter?.getBookCoverInfo(false)
            it.channel_code = 2
        }

        if (loadingPage != null) {
            loadingPage?.setReloadAction(Callable<Void> {
                mCoverPagePresenter?.getBookCoverInfo(false)
                null
            })
        }

        mCoverPagePresenter?.getRecommend()
    }


    override fun onTaskStatusChange(book_id: String?) {
        getBookDownLoadState(book_id)
    }

    override fun onTaskFinish(book_id: String?) {
        getBookDownLoadState(book_id)
    }

    private fun getBookDownLoadState(book_id: String?) {
        requestItem?.let {
            if (book_id == it.toBook().book_id) {
                val downlLoadState = CacheManager.getBookStatus(it.toBook())
                mBookDownlLoadState = downlLoadState
                Log.d("Cover Page", "getBookDownLoadState downlLoadState: " + downlLoadState)
                when (downlLoadState) {
                    DownloadState.FINISH -> book_cover_download_iv.setImageResource(R.drawable.icon_cover_down_finish)
                    DownloadState.PAUSEED -> book_cover_download_iv.setImageResource(R.drawable.icon_cover_down_pause)
                    DownloadState.NOSTART -> book_cover_download_iv.setImageResource(R.drawable.icon_cover_down_normal)
                    DownloadState.DOWNLOADING -> book_cover_download_iv.setImageResource(R.drawable.icon_cover_down_running)
                    else -> {
                    }
                }
            }
            if (!mCoverPagePresenter!!.isBookSubed()) {
                book_cover_download_iv.setImageResource(R.drawable.icon_cover_down_normal)
            }
        }
    }

    override fun onTaskFailed(book_id: String?, t: Throwable?) {
    }

    override fun onTaskProgressUpdate(book_id: String?) {
    }

    override fun onResume() {
        super.onResume()
        if (mCoverPagePresenter != null) {
            mCoverPagePresenter?.checkBookStatus()
        }
        CacheManager.listeners.add(this)
    }

    override fun onPause() {
        super.onPause()
        CacheManager.listeners.remove(this)
    }


    override fun onClick(view: View) {
        if (mCoverPagePresenter != null) {
            mCoverPagePresenter?.goToBookSearchActivity(view)
        }
        when (view.id) {
            R.id.book_cover_back -> {
                val data = HashMap<String, String>()
                data.put("type", "1")
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.BACK, data)
                finish()
            }

            R.id.book_cover_source_view -> {
                //书籍详情页换源点击
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_ch_source)
                if (mCoverPagePresenter != null) {
                    mCoverPagePresenter?.showCoverSourceDialog()
                }
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.SOURCECHANGE)
            }

            R.id.book_cover_bookshelf -> if (mCoverPagePresenter != null) {
                mCoverPagePresenter?.addBookIntoShelf(true)
            }

            R.id.book_cover_reading -> {
                //转码阅读点击的统计
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_trans_read)
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEREAD)
                if (mCoverPagePresenter != null) {
                    mCoverPagePresenter?.bookCoverReading()
                }
            }

            R.id.book_cover_download_iv -> {
                requestItem?.let {
                    StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_all_load)
                    val data3 = HashMap<String, String>()
                    data3.put("bookId", it.book_id)
                    if (mCoverPagePresenter != null) {
                        getBookDownLoadState(it.book_id)
                        if (mBookDownlLoadState == DownloadState.DOWNLOADING) {
                            CacheManager.stop(it.book_id)
                        } else {
                            mCoverPagePresenter?.downLoadBook()
                            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.CASHEALL, data3)
                        }
                    }
                }
            }
            R.id.book_catalog_tv -> {
                //书籍详情页查看目录点击
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_to_catalogue)
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.CATALOG)
                if (mCoverPagePresenter != null) {
                    mCoverPagePresenter?.goToCataloguesAct(true)
                }
            }
            R.id.book_cover_last_chapter_tv -> {
                if (mCoverPagePresenter != null) {
                    mCoverPagePresenter?.goToCataloguesAct(false)
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

        if (mCoverPagePresenter != null) {
            mCoverPagePresenter?.destory()
        }
        super.onDestroy()
    }


    override fun showRecommend(recommendBean: ArrayList<Book>) {
        mRecommendList = recommendBean
//        Log.e("showRecommend", "showRecommend : recommendBeans size" + recommendBean.size)
        mBookRecommedAdapter.setData(recommendBean)
    }

    override fun showCoverError() {
        if (loadingPage != null) {
            loadingPage?.onError()
        }
        Toast.makeText(this, "请求失败", Toast.LENGTH_SHORT).show()

    }

    override fun showRecommendError() {

    }

    override fun successAddIntoShelf(isAddIntoShelf: Boolean) {
        if (isAddIntoShelf) {
            book_cover_bookshelf.setText(R.string.book_cover_remove_bookshelf)
//            setRemoveBtn()
        } else {
            book_cover_bookshelf.setText(R.string.book_cover_add_bookshelf)
//            setAddShelfBtn()
        }
    }

    override fun showLoadingSuccess() {
        if (loadingPage != null) {
            loadingPage?.onSuccess()
        }
    }

    override fun showArrow(isQGTitle: Boolean) {
        if (isQGTitle) {
            if (book_cover_source_form != null) {
                book_cover_source_form.text = "青果阅读"
            }
        }
    }

    override fun showCurrentSources(currentSource: String) {
        book_cover_source_form.text = currentSource
    }

    @SuppressLint("SetTextI18n")
    override fun showCoverDetail(bookVo: CoverPage.BookVoBean) {
        book_cover_content.smoothScrollTo(0, 0)
        if (bookVo != null) {

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

            if (book_cover_category2 != null && !TextUtils.isEmpty(bookVo.labels)) {
                if (bookVo.labels.contains(",")) {
                    book_cover_category2.text = bookVo.labels.split(",")[0]
                } else{
                    book_cover_category2.text = bookVo.labels
                }
//                if (!mThemeHelper.isNight) {
//                    book_cover_category2.setBackgroundResource(R.drawable.book_cover_label_bg)
//                    val background = book_cover_category2.background as GradientDrawable
//                    background.setColor(resources.getColor(R.color.color_white_ffffff))
//                    book_cover_category2.setTextColor(AppUtils.getRandomColor())
//                } else {
//                    book_cover_category2.setTextColor(AppUtils.getRandomColor())
//                }
            }

            if (1 == bookVo.book_status) {
                if (book_cover_category2.visibility != View.VISIBLE) {
                    book_cover_status.text = "—" + getString(R.string.book_cover_state_writing)
                } else {
                    book_cover_status.text = getString(R.string.book_cover_state_writing)
//                    if (!mThemeHelper.isNight) {
//                        book_cover_status.setBackgroundResource(R.drawable.book_cover_label_bg)
//                        val background = book_cover_status.background as GradientDrawable
//                        background.setColor(resources.getColor(R.color.color_white_ffffff))
//                        book_cover_status.setTextColor(resources.getColor(R.color.color_red_ff2d2d))
//                    } else {
//                        book_cover_status.setTextColor(resources.getColor(R.color.color_red_ff5656))
//                    }
                }
            } else {
                if (book_cover_category2.visibility != View.VISIBLE) {
                    book_cover_status.text = "—" + getString(R.string.book_cover_state_written)
                } else {
                    book_cover_status.text = getString(R.string.book_cover_state_written)
//                    if (!mThemeHelper.isNight) {
//                        book_cover_status.setBackgroundResource(R.drawable.book_cover_label_bg)
//                        val background = book_cover_status.background as GradientDrawable
//                        background.setColor(resources.getColor(R.color.color_white_ffffff))
//                        book_cover_status.setTextColor(resources.getColor(R.color.color_brown_e9cfae))
//                    } else {
//                        book_cover_status.setTextColor(resources.getColor(R.color.color_brown_e2bd8d))
//                    }
                }
            }

//            if (book_cover_update_time != null) {
//                book_cover_update_time.text = Tools.compareTime(AppUtils.formatter, bookVo.update_time)
//            }

            if (!TextUtils.isEmpty(bookVo.last_chapter_name)) {
                book_cover_last_chapter_tv.text = "更新至：" + bookVo.last_chapter_name
            }

            if (bookVo.desc != null && !TextUtils.isEmpty(bookVo.desc)) {
                book_cover_description.text = bookVo.desc
            } else {
                book_cover_description.text = resources.getString(R.string
                        .book_cover_no_description)
            }

            if (bookVo.wordCountDescp != null) {
                if (Constants.QG_SOURCE != bookVo.host) {
                    word_count_tv.text = bookVo.wordCountDescp + "字"
                } else {
                    word_count_tv.text = AppUtils.getWordNums(java.lang.Long.valueOf(bookVo.wordCountDescp)!!)
                }
            } else {
                word_count_tv.text = "暂无"
            }

            if (bookVo.readerCountDescp != null) {
                if (Constants.QG_SOURCE == bookVo.host) {
                    reading_tv.text = AppUtils.getReadNums(java.lang.Long.valueOf(bookVo.readerCountDescp)!!)
                } else {
                    reading_tv.text = bookVo.readerCountDescp + "人在读"
                }

            } else {
                reading_tv!!.text = "暂无"
            }
            if (bookVo.score == 0.0) {
                start_tv.text = "暂无评分"
            } else {
                if (Constants.QG_SOURCE != bookVo.host) {
                    bookVo.score = java.lang.Double.valueOf(DecimalFormat("0.0").format(bookVo.score))!!
                }
                start_tv.text = bookVo.score.toString() + "分"

            }
        } else {
            showToastShort(R.string.book_cover_no_resource)
            if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
                finish()
            }
        }
    }

    override fun setCompound() {
        book_cover_source_form.setCompoundDrawables(null, null, null, null)
    }

    override fun setShelfBtnClickable(clickable: Boolean) {
        book_cover_bookshelf.isClickable = clickable
    }

    override fun changeDownloadButtonStatus() {
        val book = mCoverPagePresenter?.getBook() ?: return
        val status = CacheManager.getBookStatus(book)
        mBookDownlLoadState = status
        mCoverPagePresenter?.let {
            when (status) {
                DownloadState.FINISH -> book_cover_download_iv.setImageResource(R.drawable.icon_cover_down_finish)
                DownloadState.PAUSEED -> book_cover_download_iv.setImageResource(R.drawable.icon_cover_down_pause)
                DownloadState.NOSTART -> book_cover_download_iv.setImageResource(R.drawable.icon_cover_down_normal)
                DownloadState.DOWNLOADING -> book_cover_download_iv.setImageResource(R.drawable.icon_cover_down_running)
            }
        }
        if (!mCoverPagePresenter!!.isBookSubed()) {
            book_cover_download_iv.setImageResource(R.drawable.icon_cover_down_normal)
        }
    }


    override fun loadCoverWhenSourceChange() {
        loadCoverInfo()
    }

    override fun onStartStatus(isBookSubed: Boolean) {
        if (isBookSubed) {
            book_cover_bookshelf.setText(R.string.book_cover_remove_bookshelf)
//            setRemoveBtn()
        } else {
//            setAddShelfBtn()
        }
    }
}
