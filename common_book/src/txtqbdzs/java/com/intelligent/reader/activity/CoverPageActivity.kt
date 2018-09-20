package com.intelligent.reader.activity

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.Book
import com.ding.basic.bean.RecommendBean
import com.ding.basic.net.api.service.RequestService
import com.dingyue.contract.router.BookRouter
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.util.showToastMessage
import com.dy.media.MediaLifecycle
import com.intelligent.reader.R
import com.intelligent.reader.adapter.CoverRecommendAdapter
import com.intelligent.reader.presenter.coverPage.CoverPageContract
import com.intelligent.reader.presenter.coverPage.CoverPagePresenter
import com.intelligent.reader.upush.OfflineNotifyActivity
import iyouqu.theme.BaseCacheableActivity
import kotlinx.android.synthetic.txtqbdzs.act_book_cover.*
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.utils.*
import swipeback.ActivityLifecycleHelper
import java.text.MessageFormat
import java.util.*
import java.util.concurrent.Callable
import kotlin.collections.ArrayList

@Route(path = RouterConfig.COVER_PAGE_ACTIVITY)
class CoverPageActivity : BaseCacheableActivity(), OnClickListener, CoverPageContract, CoverRecommendAdapter.RecommendItemClickListener {


    private var mBackground = 0
    private var loadingPage: LoadingPage? = null

    private var author: String? = null
    private var bookId: String? = null
    private var bookSourceId: String? = null
    private var bookChapterId: String = ""

    private var coverPagePresenter: CoverPagePresenter? = null
    private var mRecommendBooks: List<RecommendBean> = ArrayList()
    private var mRecommendAuthorOtherBooks: List<RecommendBean> = ArrayList()

    private var mBook: Book? = null

    private var isFromPush = false

    companion object {
        fun launcher(context: Context, host: String, book_id: String,
                     book_source_id: String, name: String, author: String, parameter: String, extra_parameter: String) {

            val intent = Intent()
            intent.setClass(context, CoverPageActivity::class.java)
            val bundle = Bundle()
            bundle.putString("author", author)
            bundle.putString("book_id", book_id)
            bundle.putString("book_source_id", book_source_id)

            try {
                intent.putExtras(bundle)
                context.startActivity(intent)
            } catch (e: ClassCastException) {
                e.printStackTrace()
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        StatServiceUtils.statAppBtnClick(this, StatServiceUtils.cover_into)

        setContentView(R.layout.act_book_cover)

        initIntent(intent)

        initListener()
    }

    override fun onNewIntent(intent: Intent) {
        if (book_cover_bookshelf != null) {
            book_cover_bookshelf!!.isClickable = true
            insertBookShelfResult(false)
        }
        coverPagePresenter?.destroy()
        initIntent(intent)
    }

    private fun initListener() {
        book_cover_back?.antiShakeClick(this)
        book_cover_author!!.antiShakeClick(this)
        book_cover_chapter_view!!.antiShakeClick(this)
        book_cover_last_chapter!!.antiShakeClick(this)

        book_cover_bookshelf!!.antiShakeClick(this)
        book_cover_reading!!.antiShakeClick(this)
        book_cover_download!!.antiShakeClick(this)
    }

    private fun initIntent(intent: Intent?) {
        if (intent != null) {

            if (intent.hasExtra("author")) {
                author = intent.getStringExtra("author")
            }

            if (intent.hasExtra("book_id")) {
                bookId = intent.getStringExtra("book_id")
            }

            if (intent.hasExtra("book_source_id")) {
                bookSourceId = intent.getStringExtra("book_source_id")
            }
            if (intent.hasExtra("book_chapter_id")) {
                bookChapterId = intent.getStringExtra("book_chapter_id")
            }

            isFromPush = intent.getBooleanExtra(IS_FROM_PUSH, false)
        }

        if (!TextUtils.isEmpty(bookId) && (!TextUtils.isEmpty(bookSourceId) || !TextUtils.isEmpty(bookChapterId))) {
            coverPagePresenter = CoverPagePresenter(bookId, bookSourceId, bookChapterId, this, this, this, author)
            requestBookDetail()
        } else {
            onBackPressed()
        }
    }

    private fun requestBookDetail() {

        if (loadingPage != null) {
            loadingPage!!.onSuccess()
        }

        loadingPage = LoadingPage(this, book_cover_main, LoadingPage.setting_result)

        coverPagePresenter?.requestBookDetail(false)
        coverPagePresenter?.requestAuthorOtherBookRecommend()
        coverPagePresenter?.requestCoverRecommend()


        if (loadingPage != null) {
            loadingPage!!.setReloadAction(Callable<Void> {
                if (coverPagePresenter != null) {
                    coverPagePresenter?.requestBookDetail(false)
                }
                null
            })
        }
    }

    override fun onResume() {
        super.onResume()

        if (coverPagePresenter != null) {
            coverPagePresenter!!.refreshNavigationState()
        }

    }

    override fun onDestroy() {
        try {
            setContentView(R.layout.empty)
        } catch (exception: Resources.NotFoundException) {
            exception.printStackTrace()
        }

        if (coverPagePresenter != null) {
            coverPagePresenter!!.destroy()
        }
        MediaLifecycle.onDestroy()
        super.onDestroy()
    }

    override fun showCoverDetail(book: Book?) {
        mBook = book
        if (isFinishing) {
            // Monkey
            return
        }
        book_cover_content?.smoothScrollTo(0, 0)

        if (book != null) {

            if (book_cover_image != null) {
                if (!TextUtils.isEmpty(book.img_url) && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
                    Glide.with(applicationContext).load(book.img_url).placeholder(R.drawable.icon_book_cover_default).error(R.drawable.icon_book_cover_default).diskCacheStrategy(DiskCacheStrategy.ALL).into(book_cover_image!!)
                } else {
                    Glide.with(applicationContext).load(R.drawable.icon_book_cover_default).into(book_cover_image!!)
                }
            }

            if (book_cover_title != null) {
                if (!TextUtils.isEmpty(book.name)) {
                    book_cover_title!!.text = book.name
                }
            }

            if (book_cover_author != null) {
                if (!TextUtils.isEmpty(book.author)) {
                    book_cover_author!!.text = book.author
                }
            }


            if (book_cover_category != null) {
                if (!TextUtils.isEmpty(book.genre)) {
                    book_cover_category!!.text = book.genre
                    book_cover_category!!.visibility = VISIBLE
                } else {
                    book_cover_category!!.visibility = GONE
                }
            }

            if (book.status == "SERIALIZE") {
                book_cover_status!!.text = ("—" + getString(R.string.book_cover_state_writing))
            } else {
                book_cover_status!!.text = ("—" + getString(R.string.book_cover_state_written))
            }


            if (!TextUtils.isEmpty(book.host)) {
                book_cover_source_form.text = ("来源：" + if (book.fromQingoo()) "青果阅读" else book.host)
            }

            if (book.desc != null && !TextUtils.isEmpty(book.desc)) {
                book_cover_description!!.text = book.desc
            } else {
                book_cover_description!!.text = resources.getString(R.string.book_cover_no_description)
            }

            if (book.last_chapter != null) {
                if (book_cover_update_time != null) {
                    book_cover_update_time!!.text = ("更新：${Tools.compareTime(AppUtils.formatter, book.last_chapter!!.update_time)}")
                }

                if (book_cover_last_chapter != null) {
                    if (!TextUtils.isEmpty(book.last_chapter!!.name)) {
                        book_cover_last_chapter!!.text = book.last_chapter!!.name
                    }
                }
            }

            if (flowLayout != null) {
                flowLayout!!.removeAllViews()
                if (!TextUtils.isEmpty(book.label) && !book.fromQingoo()) {
                    flowLayout!!.childSpacing = resources.getDimensionPixelOffset(R.dimen.dimen_5)
                    flowLayout!!.rowSpacing = 17f
                    flowLayout!!.maxRows = 1
                    if (book.label != null) {
                        val dummyTexts = book.label!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val lp = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        lp.rightMargin = AppUtils.dp2px(resources, 4f).toInt()
                        dummyTexts.indices
                                .filterNot { TextUtils.isEmpty(dummyTexts[it]) }
                                .map { buildLabel(dummyTexts[it], it) }
                                .forEach { flowLayout!!.addView(it, lp) }

                    }
                }
            }

        } else {
            this.applicationContext.showToastMessage(R.string.book_cover_no_resource)
            if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
                finish()
            }
        }
    }

    // 边框和文字颜色
    private val labelColor = intArrayOf(R.color.cover_label_pink, R.color.cover_label_green, R.color.cover_label_blue, R.color.cover_label_yellow, R.color.cover_label_purple)

    //填充色
    private val labelColorAlpha = intArrayOf(R.color.cover_label_pink_alpha, R.color.cover_label_green_alpha, R.color.cover_label_blue_alpha, R.color.cover_label_yellow_alpha, R.color.cover_label_purple_alpha)

    /**
     * 设置边框，背景，圆角
     */
    private fun getLabelBgColor(solidColor: Int, strokeColor: Int): GradientDrawable {
        val gd = GradientDrawable()
        gd.shape = GradientDrawable.RECTANGLE
        gd.cornerRadius = AppUtils.dp2px(this.resources, 2f)//圆角
        gd.setColor(ContextCompat.getColor(this, solidColor))//填充色
        gd.setStroke(2, ContextCompat.getColor(this, strokeColor))//边框
        return gd
    }

    /**
     * 添加标签，设置标签样式
     */
    private fun buildLabel(text: String, index: Int): TextView {
        var position=index%labelColor.size

        val left = resources.getDimensionPixelOffset(R.dimen.cover_book_flow_layout_right)
        val right = resources.getDimensionPixelOffset(R.dimen.cover_book_flow_layout_right)
        val top = resources.getDimensionPixelOffset(R.dimen.cover_book_flow_layout_top)
        val bottom = resources.getDimensionPixelOffset(R.dimen.cover_book_flow_layout_top)
        val textView = TextView(this)
        textView.text = text
        textView.textSize = 12f
        textView.gravity = Gravity.CENTER

        textView.setTextColor(ContextCompat.getColor(this, labelColor[position]))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            textView.background = getLabelBgColor(labelColorAlpha[position], labelColor[position])
        } else {
            textView.setTextColor(ContextCompat.getColor(this, R.color.cover_recommend_read))
            textView.setBackgroundResource(R.drawable.book_cover_label_bg)
        }

        textView.setPadding(left, top, right, bottom)
        textView.setOnClickListener {

            var data = HashMap<String, String>()
            data.put("bookid", mBook?.book_id + "")
            data.put("name", mBook?.name + "")
            data.put("lablekey", text)
            data.put("rank", (index+1).toString())
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.LABLECLICK, data)


            val intent = Intent()
            intent.setClass(this, LabelsDetailActivity::class.java)
            intent.putExtra("url", RequestService.LABEL_SEARCH_V4 + "?keyword=" + text)
            intent.putExtra("title", text)
            intent.putExtra("fromCover", true)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }


        return textView
    }

    override fun insertBookShelfResult(result: Boolean) {
        if (result) {
            initRemoveShelfButton(book_cover_bookshelf, R.string.remove_bookshelf)
        } else {
            initInsertShelfButton(book_cover_bookshelf, R.string.add_bookshelf)
        }
    }

    private val mHandler = Handler {
        val book: Book? = coverPagePresenter?.coverDetail
        if (book != null) {
            val progress = CacheManager.getBookTask(book).progress
            if (progress != 100) {
                changeDownloadButtonStatus()
            }
        }
        false
    }

    override fun changeDownloadButtonStatus() {
        if (book_cover_download == null) {
            return
        }

        val book: Book? = coverPagePresenter?.coverDetail

        if (book != null && book_cover_download != null) {
            val isSub = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(book.book_id) != null
            if (isSub) {
                val status = CacheManager.getBookStatus(book)
                if (status == DownloadState.FINISH) {
                    initRemoveShelfButton(book_cover_download, R.string.download_status_complete)
                } else if (status == DownloadState.WAITTING || status == DownloadState.DOWNLOADING) {
                    val text = MessageFormat.format(" 已缓存{0}%", CacheManager.getBookTask(book).progress)
                    initRemoveShelfButton(book_cover_download, R.string.download_status_already, text)
                    mHandler.sendEmptyMessage(0)
                } else {
                    initInsertShelfButton(book_cover_download, R.string.download_status_total)
                }
            } else {
                initInsertShelfButton(book_cover_download, R.string.download_status_total)
            }

        }
    }

    override fun changeShelfButtonClickable(clickable: Boolean) {
        if (book_cover_bookshelf != null) {
            book_cover_bookshelf.isClickable = clickable
        }
    }

    override fun bookSubscribeState(subscribe: Boolean) {
        if (subscribe) {
            initRemoveShelfButton(book_cover_bookshelf, R.string.remove_bookshelf)
        } else {
            initInsertShelfButton(book_cover_bookshelf, R.string.add_bookshelf)
        }
    }

    override fun showLoadingSuccess() {
        if (loadingPage != null) {
            loadingPage!!.onSuccess()
        }
    }

    override fun showLoadingFail() {
        if (loadingPage != null) {
            loadingPage!!.onError()
        }
        Toast.makeText(this, "请求失败", Toast.LENGTH_SHORT).show()
    }

    override fun showRecommendSuccessV4(recommends: ArrayList<Book>) {

    }

    override fun onRecommendItemClick(view: View, position: Int) {
        if (position < 0 || position > mRecommendBooks.size) return

        val recommendBooks = mRecommendBooks[position]

        val data = HashMap<String, String>()
        data.put("bookid", recommendBooks.bookId)
        data.put("TbookID", recommendBooks.bookId)
        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE,
                StartLogClickUtil.RECOMMENDEDBOOK, data)

        val book = Book()
        book.author = recommendBooks.authorName
        book.book_id = recommendBooks.bookId
        book.book_source_id = recommendBooks.id
        book.book_chapter_id = recommendBooks.bookChapterId
        BookRouter.navigateCover(this, book)
    }


    override fun showAuthorRecommendSuccess(recommends: ArrayList<RecommendBean>) {
        mRecommendAuthorOtherBooks = recommends

        if (recommends.size == 0) {
            line4.visibility = View.GONE
            tv_recommend_title_author.visibility = View.GONE
        } else {
            line4.visibility = View.VISIBLE
            tv_recommend_title_author.visibility = View.VISIBLE
        }

        if (recycler_view_author != null) {

            val coverRecommendAdapter = CoverRecommendAdapter(this, object : CoverRecommendAdapter.RecommendItemClickListener {
                override fun onRecommendItemClick(view: View, position: Int) {
                    if (position < 0 || position > mRecommendAuthorOtherBooks.size) return

                    val list = mRecommendAuthorOtherBooks[position]

                    val data = HashMap<String, String>()
                    data.put("bookid", bookId!!)
                    data.put("TbookID", list.bookId)
                    StartLogClickUtil.upLoadEventLog(this@CoverPageActivity, StartLogClickUtil.BOOOKDETAIL_PAGE,
                            StartLogClickUtil.AUTHORBOOKROCOM, data)

                    val book = Book()
                    book.author = list.authorName
                    book.book_id = list.bookId
                    book.book_source_id = list.id
                    book.book_chapter_id = list.bookChapterId
                    BookRouter.navigateCover(this@CoverPageActivity, book)
                }

            }, recommends)
            val ms = LinearLayoutManager(this)
            ms.orientation = LinearLayoutManager.HORIZONTAL
            recycler_view_author.layoutManager = ms
            recycler_view_author.adapter = coverRecommendAdapter
        }

    }

    override fun showRecommendSuccess(recommends: ArrayList<RecommendBean>) {
        mRecommendBooks = recommends

        if (recommends.size == 0) {
            tv_recommend_title.visibility = View.GONE
        } else {
            tv_recommend_title.visibility = View.VISIBLE
        }

        if (recycler_view != null) {


            val coverRecommendAdapter = CoverRecommendAdapter(this, object : CoverRecommendAdapter.RecommendItemClickListener {
                override fun onRecommendItemClick(view: View, position: Int) {
                    if (position < 0 || position > mRecommendBooks.size) return

                    val list = mRecommendBooks[position]

                    val data = HashMap<String, String>()
                    data.put("bookid", bookId!!)
                    data.put("TbookID", list.bookId)
                    StartLogClickUtil.upLoadEventLog(this@CoverPageActivity, StartLogClickUtil.BOOOKDETAIL_PAGE,
                            StartLogClickUtil.RECOMMENDEDBOOK, data)

                    val book = Book()
                    book.author = list.authorName
                    book.book_id = list.bookId
                    book.book_source_id = list.id
                    book.book_chapter_id = list.bookChapterId
                    BookRouter.navigateCover(this@CoverPageActivity, book)
                }

            }, recommends)
            val ms = LinearLayoutManager(this)
            ms.orientation = LinearLayoutManager.HORIZONTAL
            recycler_view.layoutManager = ms
            recycler_view.adapter = coverRecommendAdapter

            /* val coverRecommendAdapter = CoverRecommendAdapter(this, this, recommends)
             recycler_view.recycledViewPool.setMaxRecycledViews(0, 12)
             recycler_view.layoutManager = ShelfGridLayoutManager(this, 6)
             recycler_view.isNestedScrollingEnabled = false
             recycler_view.itemAnimator.addDuration = 0
             recycler_view.itemAnimator.changeDuration = 0
             recycler_view.itemAnimator.moveDuration = 0
             recycler_view.itemAnimator.removeDuration = 0
             (recycler_view.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
             recycler_view.adapter = coverRecommendAdapter*/
        }
    }

    override fun showRecommendFail() {

    }


    override fun onClick(view: View) {
        /*if (coverPagePresenter != null) {
            coverPagePresenter!!.goToBookSearchActivity(view)
        }*/
        when (view.id) {
            R.id.book_cover_back -> {
                val data = HashMap<String, String>()
                data["type"] = "1"
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.BACK, data)
                finish()
            }

            R.id.book_cover_bookshelf -> if (coverPagePresenter != null) {
                coverPagePresenter!!.handleBookShelfAction(true)
            }

            R.id.book_cover_reading -> {
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_trans_read)
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEREAD)

                if (coverPagePresenter != null) {
                    coverPagePresenter!!.handleReadingAction()
                }
            }

            R.id.book_cover_download -> {
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_all_load)
                val dataDownload = HashMap<String, String>()
                dataDownload["bookId"] = bookId!!

                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.CASHEALL, dataDownload)

                if (coverPagePresenter != null) {
                    coverPagePresenter!!.handleDownloadAction()
                }
            }
        /*R.id.book_cover_catalog_view_nobg, R.id.book_cover_catalog_view -> {
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_to_catalogue)
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.CATALOG)

            if (coverPagePresenter != null) {
                coverPagePresenter!!.startCatalogActivity(true)
            }
        }*/
            R.id.book_cover_chapter_view, R.id.book_cover_last_chapter -> {
                if (coverPagePresenter != null) {
                    coverPagePresenter!!.startCatalogActivity(false)
                }
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.LATESTCHAPTER)
            }
        }
    }

    private fun initRemoveShelfButton(textView: TextView, @StringRes textRes: Int, text: String = "") {

        if (TextUtils.isEmpty(text)) {
            textView.setText(textRes)
        } else {
            textView.text = text
        }
        textView.setTextColor(ContextCompat.getColor(this, R.color.color_theme_alpha))
        textView.isEnabled = false
    }

    private fun initInsertShelfButton(textView: TextView, @StringRes textRes: Int) {
        textView.setText(textRes)
        textView.setTextColor(ContextCompat.getColor(this, R.color.theme_primary))
        textView.isEnabled = true

    }

    override fun onTaskStatusChange() {
        super.onTaskStatusChange()
        changeDownloadButtonStatus()
    }

    override fun supportSlideBack(): Boolean {
        return ActivityLifecycleHelper.getActivities().size > 1
                && recycler_view_author.isSupport && recycler_view.isSupport
    }

    override fun finish() {
        super.finish()
        //离线消息 跳转到主页
        if (isFromPush && ActivityLifecycleHelper.getActivities().size <= 1) {
            startActivity(Intent(this, SplashActivity::class.java))
        }
    }

}
