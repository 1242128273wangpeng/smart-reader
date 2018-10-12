package com.intelligent.reader.activity

import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.baidu.mobstat.StatService
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.Book
import com.ding.basic.bean.RecommendBean
import com.dingyue.searchbook.SearchBookActivity
import com.intelligent.reader.R
import com.intelligent.reader.adapter.BookRecommendAdapter
import com.intelligent.reader.view.TransformReadDialog
import kotlinx.android.synthetic.txtqbmfyd.act_book_cover.*
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.presenter.CoverPagePresenter
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.ui.widget.MyDialog
import net.lzbook.kit.ui.widget.RecommendItemView
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.IS_FROM_PUSH
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.utils.download.CallBackDownload
import net.lzbook.kit.utils.download.DownloadState
import net.lzbook.kit.utils.router.BookRouter
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.swipeback.ActivityLifecycleHelper
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.view.CoverPageContract
import java.text.DecimalFormat
import java.text.MessageFormat
import java.util.*
import java.util.concurrent.Callable

@Route(path = RouterConfig.COVER_PAGE_ACTIVITY)
class CoverPageActivity : BaseCacheableActivity(), OnClickListener, CoverPageContract, CallBackDownload {
    private var loadingPage: LoadingPage? = null
    private var coverPagePresenter: CoverPagePresenter? = null
    private var transformReadDialog: TransformReadDialog?=null
    private var coverDetail: Book? = null

    private var recommendList: ArrayList<RecommendBean>? = null
    private lateinit var bookRecommendAdapter: BookRecommendAdapter
    private var bookDownloadState: DownloadState = DownloadState.NOSTART


    private var bookId: String? = null
    private var bookSourceId: String? = null
    private var bookChapterId: String = ""

    private var isFromPush = false

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
            isFromPush = intent.getBooleanExtra(IS_FROM_PUSH, false)
        }

        if (!TextUtils.isEmpty(bookId) && (!TextUtils.isEmpty(bookSourceId) || !TextUtils.isEmpty(bookChapterId))) {
            coverPagePresenter = CoverPagePresenter(bookId, bookSourceId, bookChapterId,this, this, this)

            requestBookDetail()

            transformReadDialog=TransformReadDialog(this)

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
        this.coverDetail=coverDetail
        if (this.isFinishing) {
            return
        }

        if (!this.isFinishing) {
            if (!transformReadDialog!!.isShow()) {
                transformReadDialog!!.show()
            }


        }
    }

    /***
     * 处理跳转目录操作
     * **/
    override fun handleCatalogAction(intent: Intent, sequence: Int, indexLast: Boolean,coverDetail: Book?) {
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
    override  fun checkStartSearchActivity(view: View) {
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
        StatService.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        CacheManager.listeners.remove(this)
        StatService.onPause(this)
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
        ToastUtil.showToastMessage("请求失败！")
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
            if (!TextUtils.isEmpty(book.host)) {
                book_cover_source_form.text = (if (book.fromQingoo()) "青果阅读" else book.host)
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
            val str = AppUtils.getCommonReadNums(book.uv)
            if (!TextUtils.isEmpty(str)) {
                tv_read_num.text = str + "值"
            } else {
                tv_read_num.text = ""
            }
            if (tv_text_number != null && book.word_count != null && !AppUtils.isContainChinese(book.word_count)) {
                tv_text_number.text = AppUtils.getWordNums(java.lang.Long.parseLong(book.word_count))
            } else {
                tv_text_number.text = "暂无"
            }
            if (book.score == 0.0f) {
                tv_score!!.text = "暂无评分"
            } else {
                book.score = java.lang.Float.valueOf(DecimalFormat("0.00").format(book.score))!!

                tv_score!!.text = book.score.toString() + "分"
            }

        } else {
            ToastUtil.showToastMessage(R.string.book_cover_no_resource)

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
            book_cover_bookshelf!!.setText(R.string.remove_bookshelf)
        } else {
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
            book_cover_bookshelf!!.setText(R.string.remove_bookshelf)
        }
    }

    override fun showRecommendSuccess(recommends: ArrayList<RecommendBean>) {
        ll_recommend_title.visibility = View.VISIBLE
        recommendList = recommends
        bookRecommendAdapter.setData(recommends)
    }

    override fun showRecommendFail() {

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
}