package com.intelligent.reader.activity

import android.content.Context
import android.content.Intent
import android.content.res.Resources
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
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ding.basic.bean.Book
import com.ding.basic.bean.RecommendBean
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.RequestService
import com.dingyue.bookshelf.ShelfGridLayoutManager
import com.dingyue.contract.router.BookRouter
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.util.showToastMessage
import com.dy.media.MediaLifecycle
import com.intelligent.reader.R
import com.intelligent.reader.adapter.CoverRecommendAdapter
import com.intelligent.reader.presenter.coverPage.CoverPageContract
import com.intelligent.reader.presenter.coverPage.CoverPagePresenter
import iyouqu.theme.BaseCacheableActivity
import kotlinx.android.synthetic.txtqbdzs.act_book_cover.*
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.utils.*
import java.util.*
import java.util.concurrent.Callable
import kotlin.collections.ArrayList

@Route(path = RouterConfig.COVER_PAGE_ACTIVITY)
class CoverPageActivity : BaseCacheableActivity(), OnClickListener, CoverPageContract, CoverRecommendAdapter.RecommendItemClickListener {


    private var mBackground = 0
    private var mTextColor = 0
    private var loadingPage: LoadingPage? = null

    private var bookId: String? = null
    private var bookSourceId: String? = null
    private var bookChapterId: String = ""

    private var coverPagePresenter: CoverPagePresenter? = null
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
        /*book_cover_catalog_view!!.antiShakeClick(this)*/
        /*book_cover_catalog_view_nobg!!.antiShakeClick(this)*/
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
        }
    }

    private fun requestBookDetail() {

        if (loadingPage != null) {
            loadingPage!!.onSuccess()
        }

        loadingPage = LoadingPage(this, book_cover_main, LoadingPage.setting_result)

        coverPagePresenter?.requestBookDetail(false)
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

            /*     if (book_cover_category2 != null && !TextUtils.isEmpty(book.label)) {
                     if (!TextUtils.isEmpty(book.sub_genre)) {
                         book_cover_category2!!.text = book.sub_genre
                         book_cover_category2!!.visibility = VISIBLE
                     } else {
                         book_cover_category2!!.visibility = GONE
                     }
                 }*/

            if (book.status == "SERIALIZE") {
                book_cover_status!!.text = ("—" + getString(R.string.book_cover_state_writing))
            } else {
                book_cover_status!!.text = ("—" + getString(R.string.book_cover_state_written))
            }

            /* if (book.status == "SERIALIZE") {
                 *//*  if (book_cover_category2!!.visibility != View.VISIBLE) {
                      book_cover_status!!.text = "—" + getString(R.string.book_cover_state_writing)
                  } else {*//*
                book_cover_status!!.text = getString(R.string.book_cover_state_writing)

                if (!mThemeHelper.isNight) {
                    book_cover_status!!.setBackgroundResource(R.drawable.book_cover_label_bg)
                    val background = book_cover_status!!.background as GradientDrawable
                    background.setColor(resources.getColor(R.color.color_white_ffffff))
                    book_cover_status!!.setTextColor(resources.getColor(R.color.color_red_ff2d2d))
                } else {
                    book_cover_status!!.setTextColor(resources.getColor(R.color.color_red_ff5656))
                }
                *//* }*//*
            } else {
                *//* if (book_cover_category2!!.visibility != View.VISIBLE) {
                     book_cover_status!!.text = "—" + getString(R.string.book_cover_state_written)
                 } else {*//*
                book_cover_status!!.text = getString(R.string.book_cover_state_written)
                if (!mThemeHelper.isNight) {
                    book_cover_status!!.setBackgroundResource(R.drawable.book_cover_label_bg)
                    val background = book_cover_status!!.background as GradientDrawable
                    background.setColor(resources.getColor(R.color.color_white_ffffff))
                    book_cover_status!!.setTextColor(resources.getColor(R.color.color_brown_e9cfae))
                } else {
                    book_cover_status!!.setTextColor(resources.getColor(R.color.color_brown_e2bd8d))
                }
                *//*}*//*
            }*/

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
                        val lp = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
                        lp.rightMargin = AppUtils.dp2px(resources,4f).toInt()
                        dummyTexts.indices
                                .filterNot { TextUtils.isEmpty(dummyTexts[it]) }
                                .map { buildLabel(dummyTexts[it], it) }
                                .forEach { flowLayout!!.addView(it,lp) }

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
        val left = resources.getDimensionPixelOffset(R.dimen.cover_book_flow_layout_right)
        val right = resources.getDimensionPixelOffset(R.dimen.cover_book_flow_layout_right)
        val top = resources.getDimensionPixelOffset(R.dimen.cover_book_flow_layout_top)
        val bottom = resources.getDimensionPixelOffset(R.dimen.cover_book_flow_layout_top)
        val textView = TextView(this)
        textView.text = text
        textView.textSize = 12f
        textView.gravity = Gravity.CENTER

        textView.setTextColor(ContextCompat.getColor(this, labelColor[index]))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            textView.background = getLabelBgColor(labelColorAlpha[index], labelColor[index])
        } else {
            textView.setTextColor(ContextCompat.getColor(this, R.color.cover_recommend_read))
            textView.setBackgroundResource(R.drawable.bg_cover_label)
        }

        textView.setPadding(left, top, right, bottom)
        textView.setOnClickListener {

            var data = HashMap<String, String>()
            data.put("bookid", bookId + "")
            /*data.put("name", bookVo?.name + "")*/
            data.put("lablekey", text)
            data.put("rank", index.toString())
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
            book_cover_bookshelf!!.setText(R.string.book_cover_remove_bookshelf)
            initializeRemoveShelfButton()
        } else {
            book_cover_bookshelf!!.setText(R.string.book_cover_add_bookshelf)
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
            book_cover_bookshelf!!.setText(R.string.book_cover_remove_bookshelf)
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
            recycler_view.layoutManager = ShelfGridLayoutManager(this, 3)
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

    private fun initializeRemoveShelfButton() {
        mBackground = R.drawable.cover_bottom_btn_remove_bg
        mTextColor = R.color.color_theme_alpha

//        book_cover_bookshelf!!.setTextColor(resources.getColor(mTextColor))
        //        book_cover_bookshelf!!.setBackgroundResource(mBackground)
    }

    private fun initializeInsertShelfButton() {
        mBackground = R.drawable.cover_bottom_btn_add_bg
        mTextColor = R.color.color_theme_alpha

//        book_cover_bookshelf!!.setTextColor(resources.getColor(mTextColor))
        //        book_cover_bookshelf!!.setBackgroundResource(mBackground)
    }

    override fun onTaskStatusChange() {
        super.onTaskStatusChange()
        changeDownloadButtonStatus()
    }


}
