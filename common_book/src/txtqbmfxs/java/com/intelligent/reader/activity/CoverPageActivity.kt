package com.intelligent.reader.activity

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.SimpleItemAnimator
import android.text.TextUtils
import android.view.View
import android.view.View.*
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ding.basic.bean.Book
import com.ding.basic.bean.RecommendBean
import com.ding.basic.repository.RequestRepositoryFactory
import com.dingyue.bookshelf.ShelfGridLayoutManager
import com.dingyue.contract.router.BookRouter
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.util.showToastMessage
import com.dy.media.MediaLifecycle
import com.intelligent.reader.R
import com.intelligent.reader.adapter.CoverRecommendAdapter
import com.intelligent.reader.presenter.coverPage.CoverPageContract
import com.intelligent.reader.presenter.coverPage.CoverPagePresenter
import iyouqu.theme.BaseCacheableActivity
import kotlinx.android.synthetic.txtqbmfxs.act_book_cover.*
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.utils.*
import swipeback.ActivityLifecycleHelper
import java.util.*
import java.util.concurrent.Callable
import kotlin.collections.ArrayList

/**
 * Function：书籍封面页
 *
 * Created by JoannChen on 2018/6/14 0013 21:08
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
@Route(path = RouterConfig.COVER_PAGE_ACTIVITY)
class CoverPageActivity : BaseCacheableActivity(),
        OnClickListener, CoverPageContract, CoverRecommendAdapter.RecommendItemClickListener {

    private var mRecommendBooks: List<RecommendBean> = ArrayList()

    /**
     * 推荐书籍子条目点击事件
     */
    override fun onItemClick(view: View?, position: Int) {
        if (view == null || position < 0 || position > mRecommendBooks.size) return

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

    private var mBackground = 0
    private var mTextColor = 0
    private var loadingPage: LoadingPage? = null

    private var bookId: String? = null
    private var bookSourceId: String? = null
    private var bookChapterId: String = ""

    private var coverPagePresenter: CoverPagePresenter? = null

    private var isFromPush = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatServiceUtils.statAppBtnClick(this, StatServiceUtils.cover_into)
        setContentView(R.layout.act_book_cover)

        initIntent(intent)
        initListener()
        /*initAD()*/
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
        book_cover_back.antiShakeClick(this)
        book_cover_author.antiShakeClick(this)
        book_cover_chapter_view.antiShakeClick(this)
        book_cover_last_chapter.antiShakeClick(this)

        book_cover_bookshelf.antiShakeClick(this)
        book_cover_reading.antiShakeClick(this)
        book_cover_download.antiShakeClick(this)
        tv_catalog_book_cover.antiShakeClick(this)
    }

    private fun initIntent(intent: Intent?) {

        if (intent != null) {
            if (intent.hasExtra(RouterUtil.BOOK_ID)) {
                bookId = intent.getStringExtra(RouterUtil.BOOK_ID)
            }
            if (intent.hasExtra(RouterUtil.BOOK_SOURCE_ID)) {
                bookSourceId = intent.getStringExtra(RouterUtil.BOOK_SOURCE_ID)
            }
            if (intent.hasExtra(RouterUtil.BOOK_CHAPTER_ID)) {
                bookChapterId = intent.getStringExtra(RouterUtil.BOOK_CHAPTER_ID)
            }

            isFromPush = intent.getBooleanExtra(IS_FROM_PUSH, false)
        }

        if (!TextUtils.isEmpty(bookId) && (!TextUtils.isEmpty(bookSourceId) || !TextUtils.isEmpty(bookChapterId))) {
            coverPagePresenter = CoverPagePresenter(bookId, bookSourceId, bookChapterId, this, this, this)
            requestBookDetail()
        } else {
            onBackPressed()
        }
    }

    private fun requestBookDetail() {

        loadingPage?.onSuccess()

        loadingPage = LoadingPage(this, book_cover_main, LoadingPage.setting_result)

        coverPagePresenter?.requestBookDetail(false)
        coverPagePresenter?.requestCoverRecommend()

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

            if (!TextUtils.isEmpty(book.genre)) {
                book_cover_category.text = book.genre
                book_cover_category.visibility = VISIBLE
            } else {
                book_cover_category.visibility = GONE
            }


            if (book.status == "SERIALIZE") {
                book_cover_status.text = "—连载中"
            } else {
                book_cover_status.text = ("—" + getString(R.string.book_cover_state_written))
            }


            if (Constants.QG_SOURCE == book.host) {
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
        } else {
            this.applicationContext.showToastMessage(R.string.book_cover_no_resource)
            if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
                finish()
            }
        }
    }

    override fun insertBookShelfResult(result: Boolean) {
        if (result) {
            book_cover_bookshelf!!.setText(R.string.remove_bookshelf)
            initializeRemoveShelfButton()
        } else {
            book_cover_bookshelf!!.setText(R.string.add_bookshelf)
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
            book_cover_bookshelf!!.setText(R.string.remove_bookshelf)
            initializeRemoveShelfButton()
        } else {
            initializeInsertShelfButton()
        }
    }

    override fun showLoadingSuccess() {

        loadingPage?.onSuccess()

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
            R.id.tv_catalog_book_cover -> {
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_to_catalogue)
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.CATALOG)

                if (coverPagePresenter != null) {
                    coverPagePresenter?.startCatalogActivity(true)
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

        book_cover_bookshelf!!.setTextColor(ContextCompat.getColor(this, mTextColor))
        book_cover_bookshelf!!.setBackgroundResource(mBackground)
    }

    private fun initializeInsertShelfButton() {
        mBackground = R.drawable.cover_bottom_btn_add_bg
        mTextColor = R.color.cover_bottom_btn_add_text_color

        book_cover_bookshelf!!.setTextColor(ContextCompat.getColor(this, mTextColor))
        book_cover_bookshelf!!.setBackgroundResource(mBackground)
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

}
