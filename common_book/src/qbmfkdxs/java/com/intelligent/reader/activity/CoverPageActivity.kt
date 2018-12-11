package com.intelligent.reader.activity


import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.support.v7.widget.SimpleItemAnimator
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.Book
import com.ding.basic.bean.RecommendBean
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.dingyue.bookshelf.ShelfGridLayoutManager

import com.dingyue.searchbook.activity.SearchBookActivity
import com.dy.media.MediaLifecycle
import com.intelligent.reader.R
import com.intelligent.reader.adapter.CoverRecommendAdapter
import com.intelligent.reader.view.TransformReadDialog
import kotlinx.android.synthetic.qbmfkdxs.act_book_cover.*
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.bean.CoverPage
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.presenter.CoverPagePresenter
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity
import net.lzbook.kit.ui.widget.*
import net.lzbook.kit.utils.*
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.utils.download.DownloadState
import net.lzbook.kit.utils.router.BookRouter
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.swipeback.ActivityLifecycleHelper
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.view.CoverPageContract
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.Callable


@Route(path = RouterConfig.COVER_PAGE_ACTIVITY)
class CoverPageActivity : BaseCacheableActivity(), OnClickListener, CoverPageContract, CoverRecommendAdapter.RecommendItemClickListener, MyScrollView.ScrollChangedListener {
    private var mTextColor = 0
    private var loadingPage: LoadingPage? = null

    private var mRecommendBooks: List<Book> = ArrayList()

    private var bookVo: CoverPage.BookVoBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatServiceUtils.statAppBtnClick(this, StatServiceUtils.cover_into)
        setContentView(R.layout.act_book_cover)

        initIntent(intent)
        initListener()
//        initAD()
    }

    private val applicationShareDialog: ApplicationShareDialog by lazy {
        val dialog = ApplicationShareDialog(this)
        dialog
    }

    override fun onNewIntent(intent: Intent) {
        if (book_cover_bookshelf != null) {
            book_cover_bookshelf!!.isClickable = true
            insertBookShelfResult(false)
        }

        coverPagePresenter?.destroy()
        initIntent(intent)
    }

    private var bookId: String? = null
    private var bookSourceId: String? = null
    private var bookChapterId: String = ""

    private var coverPagePresenter: CoverPagePresenter? = null
    private var transformReadDialog: TransformReadDialog? = null
    private var coverDetail: Book? = null

    private var isFromPush = false

    private fun initIntent(intent: Intent?) {
        if (intent == null) return

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
            if (!transformReadDialog!!.isShow()) {
                if (SPUtils.getDefaultSharedBoolean(SPKey.NOT_SHOW_NEXT_TIME, false)) {
                    intoReadingActivity()
                } else {
                    transformReadDialog?.show()
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

        loadingPage?.onSuccess()

        loadingPage = LoadingPage(this, book_cover_main, LoadingPage.setting_result)

        coverPagePresenter?.requestBookDetail(false)
        coverPagePresenter?.requestCoverRecommendV4()

        loadingPage?.setReloadAction(Callable<Void> {
            coverPagePresenter?.requestBookDetail(false)
            null
        })
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

    /**
     * 推荐书籍子条目点击事件
     */
    override fun onItemClick(view: View?, position: Int) {
//        if (shake.check()) {
//            return
//        }
        if (view == null || position < 0 || position > mRecommendBooks.size) return

        val bookRec = mRecommendBooks[position]

        val data = HashMap<String, String>()
        data.put("bookid", bookRec.book_id)
        data.put("TbookID", bookRec.book_id)
        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE,
                StartLogClickUtil.RECOMMENDEDBOOK, data)

        val book = Book()
        book.book_id = bookRec.book_id
        book.book_source_id = bookRec.book_source_id
        book.book_chapter_id = bookRec.book_chapter_id
        BookRouter.navigateCover(this, book)

    }

    override fun showRecommendSuccess(recommends: ArrayList<RecommendBean>) {

    }

    override fun showRecommendSuccessV4(recommends: ArrayList<Book>) {
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
            recycler_view!!.recycledViewPool.setMaxRecycledViews(0, 12)
            recycler_view!!.layoutManager = ShelfGridLayoutManager(this, 3)
            recycler_view!!.isNestedScrollingEnabled = false
            recycler_view!!.itemAnimator.addDuration = 0
            recycler_view!!.itemAnimator.changeDuration = 0
            recycler_view!!.itemAnimator.moveDuration = 0
            recycler_view!!.itemAnimator.removeDuration = 0
            (recycler_view!!.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            recycler_view!!.adapter = coverRecommendAdapter
        }

    }


    protected fun initListener() {
        book_cover_back.antiShakeClick(this)
        book_cover_author.antiShakeClick(this)
        book_cover_last_chapter.antiShakeClick(this)
        cover_latest_section.antiShakeClick(this)
        book_cover_bookshelf.antiShakeClick(this)
        book_cover_reading.antiShakeClick(this)
        book_cover_download.antiShakeClick(this)
        img_app_share.antiShakeClick(this)
        book_cover_content!!.setScrollChangedListener(this)
        if (!Constants.SHARE_SWITCH_ENABLE) {
            img_app_share.visibility = View.GONE
        }
    }

    override fun showCoverDetail(book: Book?) {
        if (isFinishing) {
            // Monkey
            return
        }

        book_cover_content?.smoothScrollTo(0, 0)

        if (book != null) {

            if (!TextUtils.isEmpty(book.img_url) && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
                Glide.with(applicationContext).load(book.img_url).placeholder(R.drawable.icon_book_cover_default).error(R.drawable.icon_book_cover_default).diskCacheStrategy(DiskCacheStrategy.ALL).into(book_cover_image!!)
            } else {
                Glide.with(applicationContext).load(R.drawable.icon_book_cover_default).into(book_cover_image!!)
            }

            if (!TextUtils.isEmpty(book.name)) {
                book_cover_title!!.text = book.name
            }

            book_cover_author.text = book.author

            if (book.status == "SERIALIZE") {
                book_cover_status.text = "连载中"
            } else {
                book_cover_status.text = getString(R.string.book_cover_state_written)
            }


            if (book.fromQingoo()) {
                book_cover_source_form.text = "青果阅读"
            } else {
                book_cover_source_form.text = book.host
            }


            if (book.desc != null && !TextUtils.isEmpty(book.desc)) {
                book_cover_description.text = book.desc
            } else {
                book_cover_description.text = resources.getString(R.string.book_cover_no_description)
            }

            if (book.last_chapter != null) {
                if (book_cover_update_time != null) {
                    book_cover_update_time!!.text = Tools.compareTime(AppUtils.formatter, book.last_chapter!!.update_time)
                }

                if (book_cover_last_chapter != null) {
                    if (!TextUtils.isEmpty(book.last_chapter!!.name)) {
                        book_cover_last_chapter!!.text = book.last_chapter!!.name
                    }
                }
            }


            if (tv_text_number != null && book.word_count != null && !AppUtils.isContainChinese(book.word_count)) {
                tv_text_number!!.text = AppUtils.getWordNums(java.lang.Long.parseLong(book.word_count))
            } else {
                tv_text_number!!.text = "暂无"
            }

            val str = AppUtils.getCommonReadNums(book.uv)
            if (!TextUtils.isEmpty(str)) {
                tv_read_num!!.text = str + "值"
            } else {
                tv_read_num!!.text = ""
            }

            if (book.score == 0.0f) {
                tv_score!!.text = "暂无评分"
            } else {
                book.score = java.lang.Float.valueOf(DecimalFormat("0.00").format(book.score))!!

                tv_score!!.text = book.score.toString() + "分"
                if (book.score > 0.4) {
                    ratingBar!!.rating = java.lang.Float.valueOf((book.score / 2 - 0.2).toString() + "")!!
                } else {
                    ratingBar!!.rating = java.lang.Float.valueOf((book.score / 2).toString() + "")!!
                }

            }

            if (flowlayout != null && !TextUtils.isEmpty(book.sub_genre)) {
                flowlayout!!.childSpacing = getResources().getDimensionPixelOffset(R.dimen.cover_book_flowlayout)
                flowlayout!!.rowSpacing = 17f
                flowlayout!!.removeAllViews()
                val dummyTexts = book.sub_genre!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (text in dummyTexts) {
                    if (!TextUtils.isEmpty(text)) {
                        val textView = buildLabel(text)
                        flowlayout!!.addView(textView)
                    }
                }
            }

        } else {
            ToastUtil.showToastMessage(R.string.book_cover_no_resource)
            if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
                finish()
            }
        }
    }

    override fun insertBookShelfResult(result: Boolean) {
        if (result) {
            book_cover_bookshelf!!.setText("已在书架")
            setRemoveBtn()
        } else {
            book_cover_bookshelf!!.setText("加入书架")
            setInsertBtn()
        }
    }


    private fun setRemoveBtn() {
        mTextColor = R.color.home_title_search_text
        book_cover_bookshelf!!.setTextColor(getResources().getColor(mTextColor))
    }

    private fun setInsertBtn() {
        mTextColor = R.color.cover_title_color
        book_cover_bookshelf!!.setTextColor(getResources().getColor(mTextColor))
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
                    book_cover_download.setText(R.string.download_status_complete)
                } else if (status == DownloadState.WAITTING || status == DownloadState.DOWNLOADING) {
                    book_cover_download.setText(R.string.download_status_underway)
                } else {
                    book_cover_download.setText(R.string.download_status_total)
                }
            } else {
                book_cover_download.setText(R.string.download_status_total)
            }

        }
    }

    override fun changeShelfButtonClickable(clickable: Boolean) {
    }

    override fun bookSubscribeState(subscribe: Boolean) {
        if (subscribe) {
            book_cover_bookshelf!!.setText(R.string.have_in_bookshelf)
            setRemoveBtn()
        }
    }

    /**
     * 添加标签
     */
    private fun buildLabel(text: String): TextView {
        val left = getResources().getDimensionPixelOffset(R.dimen.cover_book_flowlayout_padding)
        val right = getResources().getDimensionPixelOffset(R.dimen.cover_book_flowlayout_padding)
        val top = getResources().getDimensionPixelOffset(R.dimen.cover_book_flowlayout_top)
        val bottom = getResources().getDimensionPixelOffset(R.dimen.cover_book_flowlayout_top)
        val textView = TextView(this)
        textView.text = text
        textView.textSize = 11f
        textView.setPadding(left, top, right, bottom)
        textView.setTextColor(getResources().getColor(R.color.cover_recommend_read))
        textView.setBackgroundResource(R.drawable.cover_label_shape)

        return textView
    }

    override fun showLoadingSuccess() {
        loadingPage?.onSuccess()
        checkShowCoverPrompt()
    }

    override fun showLoadingFail() {
        loadingPage?.onError()
        Toast.makeText(this, "请求失败", Toast.LENGTH_SHORT).show()
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
                coverPagePresenter!!.handleBookShelfAction(false)
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
            R.id.cover_latest_section, R.id.book_cover_last_chapter -> {
                if (coverPagePresenter != null) {
                    coverPagePresenter!!.startCatalogActivity(false)
                }
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.LATESTCHAPTER)
            }
            R.id.img_app_share -> {
                applicationShareDialog.show()
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.ACTION_SHARE)
            }
        }
    }

    override fun onTaskStatusChange() {
        super.onTaskStatusChange()
        changeDownloadButtonStatus()
    }

    override fun onScrollChanged(top: Int, oldTop: Int) {
        if (AppUtils.px2dip(this, top.toFloat()) > 36 && bookVo != null) {
            if (tv_title != null && !TextUtils.isEmpty(bookVo!!.name)) {
                tv_title!!.text = bookVo!!.name
            }
        } else {
            tv_title!!.text = "书籍详情"
        }

    }

    override fun supportSlideBack(): Boolean {
        return ActivityLifecycleHelper.getActivities().size > 1
    }

    override fun finish() {
        super.finish()
        //离线消息 跳转到主页
        if (isFromPush && ActivityLifecycleHelper.getActivities().size <= 1) {
            startActivity(Intent(this, SplashActivity::class.java))
        }
    }

    private fun checkShowCoverPrompt() {
        if (!Constants.SHARE_SWITCH_ENABLE) return
        val hasShareDialogShowed = SPUtils.getDefaultSharedBoolean(SPKey.COVER_SHARE_PROMPT)
        if (!hasShareDialogShowed) {
            fl_cover_share_prompt.visibility = View.VISIBLE

            fl_cover_share_prompt.setOnClickListener {
                fl_cover_share_prompt.visibility = View.GONE
                SPUtils.editDefaultShared {
                    putBoolean(SPKey.COVER_SHARE_PROMPT, true)
                }
            }
        }
    }
}
