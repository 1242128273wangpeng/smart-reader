package com.intelligent.reader.activity

import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.*
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ding.basic.bean.Book
import com.ding.basic.repository.RequestRepositoryFactory
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.util.showToastMessage
import com.dy.media.MediaControl
import com.dy.media.MediaLifecycle
import com.intelligent.reader.R
import com.intelligent.reader.presenter.coverPage.CoverPageContract
import com.intelligent.reader.presenter.coverPage.CoverPagePresenter
import iyouqu.theme.BaseCacheableActivity
import kotlinx.android.synthetic.qbzsydq.act_book_cover.*
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

@Route(path = RouterConfig.COVER_PAGE_ACTIVITY)
class CoverPageActivity : BaseCacheableActivity(), OnClickListener, CoverPageContract {
    private var mBackground = 0
    private var mTextColor = 0
    private var loadingPage: LoadingPage? = null

    private var bookId: String? = null
    private var bookSourceId: String? = null
    private var bookChapterId: String = ""

    private var coverPagePresenter: CoverPagePresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        StatServiceUtils.statAppBtnClick(this, StatServiceUtils.cover_into)

        setContentView(R.layout.act_book_cover)

        initializeIntent(intent)

        initializeListener()
        initAD()
    }

    override fun onNewIntent(intent: Intent) {
        initializeIntent(intent)
    }

    private fun initializeListener() {
        book_cover_back?.antiShakeClick(this)
        book_cover_author!!.antiShakeClick(this)
        book_cover_chapter_view!!.antiShakeClick(this)
        book_cover_last_chapter!!.antiShakeClick(this)

        book_cover_bookshelf!!.antiShakeClick(this)
        book_cover_reading!!.antiShakeClick(this)
        book_cover_download!!.antiShakeClick(this)
        book_cover_catalog_view!!.antiShakeClick(this)
        book_cover_catalog_view_nobg!!.antiShakeClick(this)
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

            if (book_cover_category2 != null && !TextUtils.isEmpty(book.label)) {
                if (!TextUtils.isEmpty(book.sub_genre)) {
                    book_cover_category2!!.text = book.sub_genre
                    book_cover_category2!!.visibility = VISIBLE
                } else {
                    book_cover_category2!!.visibility = GONE
                }
            }

            if (book.status == "SERIALIZE") {
                if (book_cover_category2!!.visibility != View.VISIBLE) {
                    book_cover_status!!.text = "—" + getString(R.string.book_cover_state_writing)
                } else {
                    book_cover_status!!.text = getString(R.string.book_cover_state_writing)
                    if (!mThemeHelper.isNight) {
                        book_cover_status!!.setBackgroundResource(R.drawable.book_cover_label_bg)
                        val background = book_cover_status!!.background as GradientDrawable
                        background.setColor(resources.getColor(R.color.color_white_ffffff))
                        book_cover_status!!.setTextColor(resources.getColor(R.color.color_red_ff2d2d))
                    } else {
                        book_cover_status!!.setTextColor(resources.getColor(R.color.color_red_ff5656))
                    }
                }
            } else {
                if (book_cover_category2!!.visibility != View.VISIBLE) {
                    book_cover_status!!.text = "—" + getString(R.string.book_cover_state_written)
                } else {
                    book_cover_status!!.text = getString(R.string.book_cover_state_written)
                    if (!mThemeHelper.isNight) {
                        book_cover_status!!.setBackgroundResource(R.drawable.book_cover_label_bg)
                        val background = book_cover_status!!.background as GradientDrawable
                        background.setColor(resources.getColor(R.color.color_white_ffffff))
                        book_cover_status!!.setTextColor(resources.getColor(R.color.color_brown_e9cfae))
                    } else {
                        book_cover_status!!.setTextColor(resources.getColor(R.color.color_brown_e2bd8d))
                    }
                }
            }

            if (!TextUtils.isEmpty(book.host)) {
                if (Constants.QG_SOURCE == book.host) {
                    book_cover_source_form.text = "青果阅读"
                } else {
                    book_cover_source_form.text = book.host
                }
            }

            if (book.desc != null && !TextUtils.isEmpty(book.desc)) {
                book_cover_description!!.text = book.desc
            } else {
                book_cover_description!!.text = resources.getString(R.string.book_cover_no_description)
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

    override fun showRecommendSuccess(recommendBean: ArrayList<Book>) {

    }

    override fun showRecommendFail() {

    }


    override fun onClick(view: View) {
        if (coverPagePresenter != null) {
            coverPagePresenter!!.goToBookSearchActivity(view)
        }
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

        book_cover_bookshelf!!.setTextColor(resources.getColor(mTextColor))
        book_cover_bookshelf!!.setBackgroundResource(mBackground)
    }

    private fun initializeInsertShelfButton() {
        mBackground = R.drawable.cover_bottom_btn_add_bg
        mTextColor = R.color.cover_bottom_btn_add_text_color

        book_cover_bookshelf!!.setTextColor(resources.getColor(mTextColor))
        book_cover_bookshelf!!.setBackgroundResource(mBackground)
    }

    override fun onTaskStatusChange() {
        super.onTaskStatusChange()
        changeDownloadButtonStatus()
    }

    private fun initAD() {
        if (!Constants.isHideAD) {
            MediaControl.loadBookCoverAd(this, { view ->
                if (ad_view != null && !this.isFinishing()) {
                    ad_view.visibility = View.VISIBLE
                    ad_view.removeAllViews()
                    ad_view.addView(view)
                }
            })
        }
    }

}
