package com.intelligent.reader.activity


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
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
import com.dingyue.searchbook.activity.SearchBookActivity
import com.intelligent.reader.R
import com.intelligent.reader.view.TransformReadDialog
import kotlinx.android.synthetic.zsmfqbxs.act_book_cover.*
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.presenter.CoverPagePresenter
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.ui.widget.MyDialog
import net.lzbook.kit.ui.widget.RecommendItemView
import net.lzbook.kit.utils.*
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.utils.download.DownloadState
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.swipeback.ActivityLifecycleHelper
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.view.CoverPageContract
import java.util.*
import java.util.concurrent.Callable


@Route(path = RouterConfig.COVER_PAGE_ACTIVITY)
class CoverPageActivity : BaseCacheableActivity(), OnClickListener, CoverPageContract {
    override fun showRecommendSuccess(recommends: ArrayList<RecommendBean>) {
    }

    private var mBackground = 0
    private var mTextColor = 0
    private var loadingPage: LoadingPage? = null

    private var bookId: String? = null
    private var bookSourceId: String? = null
    private var bookChapterId: String = ""

    private var coverPagePresenter: CoverPagePresenter? = null
    private var transformReadDialog: TransformReadDialog?=null
    private var coverDetail: Book? = null
    private var isFromPush = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatServiceUtils.statAppBtnClick(this, StatServiceUtils.cover_into)
        setContentView(R.layout.act_book_cover)

        initializeIntent(intent)
        initializeListener()

    }


    override fun onNewIntent(intent: Intent) {
        if (book_cover_bookshelf != null) {
            book_cover_bookshelf!!.isClickable = true
            insertBookShelfResult(false)
        }
        coverPagePresenter?.destroy()
        initializeIntent(intent)
    }

    private fun initializeListener() {
        book_cover_back?.antiShakeClick(this)
        book_cover_author!!.antiShakeClick(this)
        book_cover_chapter_view!!.antiShakeClick(this)
        book_cover_last_chapter!!.antiShakeClick(this)

        book_cover_bookshelf?.antiShakeClick(this)

        book_cover_reading?.antiShakeClick(this)

        book_cover_download?.antiShakeClick(this)

        book_cover_catalog_view?.antiShakeClick(this)

        book_cover_catalog_view_nobg?.antiShakeClick(this)

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

        if (!TextUtils.isEmpty(bookId)) {
            coverPagePresenter = CoverPagePresenter(bookId, bookSourceId, bookChapterId, this, this, this)
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

        loadingPage = LoadingPage(this, book_cover_main, LoadingPage.setting_result)

        if (coverPagePresenter != null) {
            coverPagePresenter!!.requestBookDetail(false)
        }

        if (loadingPage != null) {
            loadingPage!!.setReloadAction(Callable<Void> {
                if (coverPagePresenter != null) {
                    coverPagePresenter!!.requestBookDetail(false)
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
        super.onDestroy()
    }

    override fun showCoverDetail(bookVo: Book?) {
        if (isFinishing) {
            // Monkey
            return
        }
        book_cover_content?.smoothScrollTo(0, 0)

        if (bookVo != null) {

            if (book_cover_image != null && !TextUtils.isEmpty(bookVo.img_url) && bookVo
                    .img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
                Glide.with(applicationContext).load(bookVo.img_url).placeholder(
                        net.lzbook.kit.R.drawable.icon_book_cover_default).error(
                        net.lzbook.kit.R.drawable.icon_book_cover_default).diskCacheStrategy(
                        DiskCacheStrategy.ALL).into(book_cover_image!!)
            } else {
                Glide.with(applicationContext).load(
                        net.lzbook.kit.R.drawable.icon_book_cover_default).into(book_cover_image!!)
            }

            if (!TextUtils.isEmpty(bookVo.name)) {
                book_cover_title?.text = bookVo.name
            }

            if (!TextUtils.isEmpty(bookVo.author)) {
                book_cover_author?.text = bookVo.author
            }

            if (!TextUtils.isEmpty(bookVo.genre)) {
                book_cover_category?.text = bookVo.genre
            }

            if (!TextUtils.isEmpty(bookVo.genre)) {
                book_cover_category2?.text = bookVo.genre
                if (!mThemeHelper.isNight) {
                    book_cover_category2?.setBackgroundColor(Color.parseColor("#ffffff"))
                    book_cover_category2?.setTextColor(AppUtils.getRandomColor())
                } else {
                    book_cover_category2?.setTextColor(AppUtils.getRandomColor())
                }
            }

            if ("FINISH" != bookVo.status) {
                if (book_cover_category2?.visibility != View.VISIBLE) {
                    book_cover_status?.text = ("—" + getString(R.string.book_cover_state_writing))
                } else {
                    book_cover_status?.text = getString(R.string.book_cover_state_writing)
                    if (!mThemeHelper.isNight) {
                        book_cover_status?.setBackgroundResource(R.drawable.book_cover_label_bg)
                        val background = book_cover_status!!.background as GradientDrawable
                        background.setColor(ContextCompat.getColor(this, R.color.color_white_ffffff))
                        book_cover_status?.setTextColor(
                                ContextCompat.getColor(this, R.color.color_red_ff2d2d))
                    } else {
                        book_cover_status?.setTextColor(
                                ContextCompat.getColor(this, R.color.color_red_ff5656))
                    }
                }
            } else {
                if (book_cover_category2?.visibility != View.VISIBLE) {
                    book_cover_status?.text = ("—" + getString(R.string.book_cover_state_written))
                } else {
                    book_cover_status?.text = getString(R.string.book_cover_state_written)
                    if (!mThemeHelper.isNight) {
                        book_cover_status?.setBackgroundResource(R.drawable.book_cover_label_bg)
                        val background = book_cover_status!!.background as GradientDrawable
                        background.setColor(ContextCompat.getColor(this, R.color.color_white_ffffff))
                        book_cover_status?.setTextColor(
                                ContextCompat.getColor(this, R.color.color_brown_e9cfae))
                    } else {
                        book_cover_status?.setTextColor(
                                ContextCompat.getColor(this, R.color.color_brown_e2bd8d))
                    }

                }
            }

            if (bookVo.last_chapter != null) {
                book_cover_update_time?.text = Tools.compareTime(AppUtils.formatter, bookVo
                        .last_chapter!!.update_time)
            }

            if (bookVo.last_chapter != null && !TextUtils.isEmpty(bookVo.last_chapter!!.name)) {
                book_cover_last_chapter?.text = bookVo.last_chapter!!.name
            }

            if (bookVo.desc != null && !TextUtils.isEmpty(bookVo.desc)) {
                book_cover_description?.text = bookVo.desc
            } else {
                book_cover_description?.text = resources.getString(R.string
                        .book_cover_no_description)
            }

            if ("qg" == bookVo.book_type) {
                book_cover_source_form?.text = "青果阅读"
            } else {
                book_cover_source_form?.text = bookVo.host
            }

            book_cover_source_form?.setCompoundDrawables(null, null, null, null)

        } else {
            ToastUtil.showToastMessage(R.string.book_cover_no_resource)
            if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
                finish()
            }
        }
    }

    override fun insertBookShelfResult(result: Boolean) {
        if (result) {
            book_cover_bookshelf?.setText(R.string.remove_bookshelf)
            initializeRemoveShelfButton()
        } else {
            book_cover_bookshelf?.setText(R.string.add_bookshelf)
            initializeInsertShelfButton()
        }
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
        if (book_cover_bookshelf != null) {
            book_cover_bookshelf.isClickable = clickable
        }
    }

    override fun bookSubscribeState(subscribe: Boolean) {
        if (subscribe) {
            book_cover_bookshelf?.setText(R.string.remove_bookshelf)
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
        Toast.makeText(this, "请求失败", Toast.LENGTH_SHORT).show()
    }


    override fun showRecommendFail() {

    }


    override fun onClick(view: View) {
//        if (coverPagePresenter != null) {
//            coverPagePresenter!!.goToBookSearchActivity(view)
//        }

        when (view.id) {
            R.id.book_cover_back -> {
                val data = HashMap<String, String>()
                data.put("type", "1")
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE,
                        StartLogClickUtil.BACK, data)
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
            R.id.book_cover_catalog_view_nobg, R.id.book_cover_catalog_view -> {
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_to_catalogue)
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.CATALOG)

                if (coverPagePresenter != null) {
                    coverPagePresenter!!.startCatalogActivity(true)
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


    private fun initializeRemoveShelfButton() {
        mBackground = R.drawable.cover_bottom_btn_remove_bg
        mTextColor = R.color.cover_bottom_btn_remove_text_color
        book_cover_bookshelf?.setTextColor(ContextCompat.getColor(this, mTextColor))
        if (book_cover_category2?.visibility != View.VISIBLE) {
            book_cover_bookshelf?.setBackgroundResource(mBackground)
        }
    }

    private fun initializeInsertShelfButton() {
        mBackground = R.drawable.cover_bottom_btn_add_bg
        mTextColor = R.color.primary
        book_cover_bookshelf?.setTextColor(ContextCompat.getColor(this, mTextColor))
        if (book_cover_category2?.visibility != View.VISIBLE) {
            book_cover_bookshelf?.setBackgroundResource(mBackground)
        }
    }


    override fun onTaskStatusChange() {
        super.onTaskStatusChange()
        changeDownloadButtonStatus()
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


}
