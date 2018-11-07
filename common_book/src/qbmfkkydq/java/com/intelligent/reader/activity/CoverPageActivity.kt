package com.intelligent.reader.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.SimpleItemAnimator
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.Book
import com.ding.basic.bean.RecommendBean
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.dingyue.bookshelf.ShelfGridLayoutManager
import com.dingyue.searchbook.SearchBookActivity
import com.dy.media.MediaLifecycle
import com.intelligent.reader.R
import com.intelligent.reader.adapter.CoverRecommendAdapter
import com.intelligent.reader.view.TransformReadDialog
import kotlinx.android.synthetic.qbmfkkydq.act_book_cover.*
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.presenter.CoverPagePresenter
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.ui.widget.MyDialog
import net.lzbook.kit.ui.widget.RecommendItemView
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.antiShakeClick
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.utils.download.DownloadState
import net.lzbook.kit.utils.router.BookRouter
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.view.CoverPageContract
import java.util.*
import java.util.concurrent.Callable
import kotlin.collections.ArrayList

@Route(path = RouterConfig.COVER_PAGE_ACTIVITY)
class CoverPageActivity : BaseCacheableActivity(), OnClickListener, CoverPageContract, CoverRecommendAdapter.RecommendItemClickListener {

    private var loadingPage: LoadingPage? = null

    private var mBook: Book? = null

    private var bookId: String? = null
    private var bookSourceId: String? = null
    private var bookChapterId: String = ""

    private var coverPagePresenter: CoverPagePresenter? = null
    private var transformReadDialog: TransformReadDialog? = null
    private var coverDetail: Book? = null
    private var mRecommendBooks: List<RecommendBean> = ArrayList()

    companion object {
        fun launcher(context: Context, host: String, book_id: String,
                     book_source_id: String, name: String, author: String, parameter: String, extra_parameter: String) {

            val intent = Intent()
            intent.setClass(context, CoverPageActivity::class.java)
            val bundle = Bundle()
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
            transformReadDialog = TransformReadDialog(this)

            transformReadDialog?.insertContinueListener {
                val data = HashMap<String, String>()
                data["type"] = "1"

                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEPOPUP, data)

                intoReadingActivity()

                if (!this.isFinishing) {
                    transformReadDialog?.dismiss()
                }
            }

            transformReadDialog?.insertCancelListener {
                val data = HashMap<String, String>()
                data["type"] = "2"

                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEPOPUP, data)

                if (!this.isFinishing) {
                    transformReadDialog?.dismiss()
                }
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

//            updateBookInformation()

            bundle.putSerializable("book", book)
        } else {
            bundle.putSerializable("book", coverDetail)
        }

        RouterUtil.navigation(this, RouterConfig.READER_ACTIVITY, bundle, flags)
    }

    /***
     * 处理跳转阅读页请求
     * **/
    override fun handleReadingAction(coverDetail: Book?) {
        this.coverDetail = coverDetail
        if (this.isFinishing) {
            return
        }

        if (!this.isFinishing) {
            transformReadDialog?.isShowing?.let {
                if (!it) {
                    val isChecked = SPUtils.getDefaultSharedBoolean(SPKey.NOT_SHOW_NEXT_TIME, false)
                    if (isChecked) {
                        intoReadingActivity()
                    } else {
                        transformReadDialog?.show()
                    }
                }
            }

        }
    }

    /***
     * 处理跳转目录操作
     * **/
    override fun handleCatalogAction(intent: Intent, sequence: Int, indexLast: Boolean, coverDetail: Book?) {
        if (coverDetail != null) {

            val bundle = Bundle()
            bundle.putInt("sequence", sequence)
            bundle.putBoolean("fromCover", true)
            bundle.putBoolean("is_last_chapter", indexLast)
            bundle.putSerializable("cover", coverDetail)

            intent.setClass(this, CataloguesActivity::class.java)
            intent.putExtras(bundle)

            this.startActivity(intent)
        }
    }

    override fun showCleanDialog(): Dialog {
        val cleanDialog = MyDialog(this, R.layout.dialog_download_clean)
        cleanDialog.setCanceledOnTouchOutside(false)
        cleanDialog.setCancelable(false)
        cleanDialog.findViewById<TextView>(R.id.dialog_msg).setText(R.string.tip_cleaning_cache)
        cleanDialog.show()
        return cleanDialog
    }

    /***
     * 判断是否跳转到搜索页
     * **/
    override fun checkStartSearchActivity(view: View) {
        val intent = Intent()
        if (view is RecommendItemView) {
            intent.putExtra("word", view.title)
            intent.putExtra("search_type", "0")
            intent.putExtra("filter_type", "0")
            intent.putExtra("filter_word", "ALL")
            intent.putExtra("sort_type", "0")
            intent.setClass(this, SearchBookActivity::class.java)
            this.startActivity(intent)
            return
        }
    }


    private fun requestBookDetail() {

        if (loadingPage != null) {
            loadingPage!!.onSuccess()
        }

        loadingPage = LoadingPage(this, book_cover_main, LoadingPage.setting_result)

        coverPagePresenter?.requestBookDetail(false)
        coverPagePresenter?.requestCoverRecommendRandom(8)


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



            if (!TextUtils.isEmpty(book.host)) {
                book_cover_source_form.text = ("来源：" + if (book.fromQingoo()) "青果阅读" else book.host)
            }

            txt_score.text = (String.format("%.1f", book.score) + "分")

            if (book.desc != null && !TextUtils.isEmpty(book.desc)) {
                book_cover_description.text = book.desc
            } else {
                book_cover_description.text = resources.getString(R.string
                        .book_cover_no_description)
            }

            if (book.last_chapter != null) {

                if (book_cover_last_chapter != null) {
                    if (!TextUtils.isEmpty(book.last_chapter!!.name)) {
                        book_cover_last_chapter!!.text = book.last_chapter!!.name
                    }
                }
            }

            if (flowLayout != null) {
                val lp = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.rightMargin = AppUtils.dp2px(resources, 4f).toInt()
                flowLayout!!.removeAllViews()

                if (book.status == "SERIALIZE") {
                    flowLayout.addView(buildLabel("连载中", 0), lp)
                } else if (book.update_status == 1) {
                    flowLayout.addView(buildLabel("完结", 0), lp)
                }
                if (!TextUtils.isEmpty(book.sub_genre)) {
                    flowLayout.addView(buildLabel(book.sub_genre!!, 1), lp)
                }
                if (!TextUtils.isEmpty(book.word_count) && book.word_count!!.toLong() > 0) {
                    flowLayout.addView(buildLabel(AppUtils.getWordNums(book.word_count!!.toLong()), 2), lp)
                }

            }

        } else {
            ToastUtil.showToastMessage(R.string.book_cover_no_resource)
            if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
                finish()
            }
        }
    }


    // 边框和文字颜色
    private val labelColor = intArrayOf(R.color.cover_label_color1, R.color.cover_label_color2, R.color.cover_label_color3)


    /**
     * 设置边框，背景，圆角
     */
    private fun getLabelBgColor(strokeColor: Int): GradientDrawable {
        val gd = GradientDrawable()
        gd.shape = GradientDrawable.RECTANGLE
        gd.cornerRadius = AppUtils.dp2px(this.resources, 2f)//圆角
        gd.setColor(Color.WHITE)//填充色
        gd.setStroke(2, ContextCompat.getColor(this, strokeColor))//边框
        return gd
    }

    /**
     * 添加标签，设置标签样式
     */
    private fun buildLabel(text: String, index: Int): TextView {

        val position = index % labelColor.size
        val left = resources.getDimensionPixelOffset(R.dimen.dimen_margin_6)
        val right = resources.getDimensionPixelOffset(R.dimen.dimen_margin_6)
        val top = resources.getDimensionPixelOffset(R.dimen.dimen_margin_4)
        val bottom = resources.getDimensionPixelOffset(R.dimen.dimen_margin_4)
        val textView = TextView(this)
        textView.text = text
        textView.textSize = 10f
        textView.gravity = Gravity.CENTER

        textView.setTextColor(ContextCompat.getColor(this, labelColor[position]))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            textView.background = getLabelBgColor(labelColor[position])
        } else {
            textView.setTextColor(ContextCompat.getColor(this, R.color.cover_recommend_read))
            textView.setBackgroundResource(R.drawable.bg_cover_label)
        }

        textView.setPadding(left, top, right, bottom)



        return textView
    }

    override fun insertBookShelfResult(result: Boolean) {
        if (result) {

            initializeRemoveShelfButton()
        } else {

            initializeInsertShelfButton()
        }
    }

    override fun changeDownloadButtonStatus() {
        if (book_cover_download == null) {
            return
        }

        val book: Book? = coverPagePresenter?.coverDetail

        if (book != null && book_cover_download != null) {
            val status = CacheManager.getBookStatus(book)
            if (status == DownloadState.FINISH) {
                book_cover_download.setText(R.string.download_status_complete)
                book_cover_download.setTextColor(Color.parseColor("#4D5D646E"))
            } else if (status == DownloadState.DOWNLOADING) {
                book_cover_download.setText(R.string.download_status_underway)
            } else if (status == DownloadState.PAUSEED) {
                book_cover_download.setText("继续缓存")
            } else {
                book_cover_download.setText(R.string.download_status_total)
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
            initializeRemoveShelfButton()
        } else {
            initializeInsertShelfButton()
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
    }

    override fun showRecommendSuccessV4(recommends: ArrayList<Book>) {

    }

    override fun onRecommendItemClick(view: View, position: Int) {
        if (position < 0 || position > mRecommendBooks.size) return

        val recommendBooks = mRecommendBooks[position]

        val data = HashMap<String, String>()
        bookId?.let { data.put("bookid", it) }
        data.put("TbookID", recommendBooks.bookId)
        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE,
                StartLogClickUtil.RECOMMENDEDBOOK, data)

        val book = Book()
        book.book_id = recommendBooks.bookId
        book.book_source_id = recommendBooks.id
        book.book_chapter_id = recommendBooks.bookChapterId
        BookRouter.navigateCover(this, book)
    }


    override fun showRecommendSuccess(recommends: ArrayList<RecommendBean>) {
        mRecommendBooks = recommends

        if (tv_recommend_title != null) {
            if (recommends.size == 0) {
                tv_recommend_title.visibility = View.GONE
            } else {
                tv_recommend_title.visibility = View.VISIBLE
            }
        }
        if (recycler_view != null) {
            val coverRecommendAdapter = CoverRecommendAdapter(this, this, recommends)
            recycler_view.recycledViewPool.setMaxRecycledViews(0, 12)
            recycler_view.layoutManager = ShelfGridLayoutManager(this, 4)
            recycler_view.isNestedScrollingEnabled = false
            recycler_view.itemAnimator.addDuration = 0
            recycler_view.itemAnimator.changeDuration = 0
            recycler_view.itemAnimator.moveDuration = 0
            recycler_view.itemAnimator.removeDuration = 0
            (recycler_view.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            recycler_view.adapter = coverRecommendAdapter
        }
    }

    override fun showRecommendFail() {

    }


    override fun onClick(view: View) {
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

                coverPagePresenter?.handleReadingAction()
            }

            R.id.book_cover_download -> {
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_all_load)
                val dataDownload = HashMap<String, String>()
                dataDownload["bookId"] = bookId!!

                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.CASHEALL, dataDownload)

                if (coverPagePresenter != null) {
                    coverPagePresenter!!.handleDownloadContinueOrStop()
                }
            }
            R.id.book_cover_chapter_view, R.id.book_cover_last_chapter -> {
                if (coverPagePresenter != null) {
                    coverPagePresenter!!.startCatalogActivity(false)
                }
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.LATESTCHAPTER)
            }
        }
    }

    /**
     * 已经在书架
     */
    private fun initializeRemoveShelfButton() {
        book_cover_bookshelf!!.setTextColor(Color.parseColor("#4C2AD1BE"))
        book_cover_bookshelf!!.isEnabled = false
        book_cover_bookshelf!!.setText(R.string.have_in_bookshelf)
    }

    /**
     * 添加到书架
     */
    private fun initializeInsertShelfButton() {
        book_cover_bookshelf!!.setText(R.string.add_bookshelf)
        book_cover_bookshelf!!.setTextColor(Color.parseColor("#FF2AD1BE"))
        book_cover_bookshelf!!.isEnabled = true
    }

    override fun onTaskStatusChange() {
        super.onTaskStatusChange()
        changeDownloadButtonStatus()
    }

}
