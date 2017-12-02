package com.intelligent.reader.activity

import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.intelligent.reader.R
import com.intelligent.reader.presenter.coverPage.CoverPageContract
import com.intelligent.reader.presenter.coverPage.CoverPagePresenter
import kotlinx.android.synthetic.qbzsydq.act_book_cover.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.CoverPage
import net.lzbook.kit.data.bean.RequestItem
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.Tools
import java.util.*
import java.util.concurrent.Callable

class CoverPageActivity : BaseCacheableActivity(), OnClickListener, CoverPageContract {
    //private RelativeLayout book_cover_reading_view;
    private var mBackground = 0
    private var mTextColor = 0
    private var loadingPage: LoadingPage? = null


    private var requestItem: RequestItem? = null


    private var mCoverPagePresenter: CoverPagePresenter? = null

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

    protected fun initListener() {
        book_cover_back?.setOnClickListener(this)
        book_cover_author!!.setOnClickListener(this)
        book_cover_source_view!!.setOnClickListener(this)
        book_cover_chapter_view!!.setOnClickListener(this)
        book_cover_last_chapter!!.setOnClickListener(this)

        book_cover_bookshelf!!.setOnClickListener(this)
        book_cover_reading!!.setOnClickListener(this)
        book_cover_download!!.setOnClickListener(this)
        book_cover_catalog_view!!.setOnClickListener(this)
        book_cover_catalog_view_nobg!!.setOnClickListener(this)
    }

    protected fun initData(intent: Intent?) {

        if (intent != null) {

            if (intent.hasExtra(Constants.REQUEST_ITEM)) {
                requestItem = intent.getSerializableExtra(Constants.REQUEST_ITEM) as RequestItem
            }
        }
        if (requestItem != null) {
            mCoverPagePresenter = CoverPagePresenter(requestItem!!, this, this, this)
            loadCoverInfo()
        }
    }

    protected fun loadCoverInfo() {

        if (loadingPage != null) {
            loadingPage!!.onSuccess()
        }

        loadingPage = LoadingPage(this, findViewById(R.id.book_cover_main) as ViewGroup,
                LoadingPage.setting_result)


        if (requestItem != null) {
            if (mCoverPagePresenter != null) {
                mCoverPagePresenter!!.getBookCoverInfo(false)
            }
            requestItem!!.channel_code = 2
        }

        if (loadingPage != null) {
            loadingPage!!.setReloadAction(Callable<Void> {
                if (mCoverPagePresenter != null) {
                    mCoverPagePresenter!!.getBookCoverInfo(false)
                }
                null
            })
        }
    }

    private fun setRemoveBtn() {
        mBackground = R.drawable.cover_bottom_btn_remove_bg
        mTextColor = R.color.cover_bottom_btn_remove_text_color
        book_cover_bookshelf!!.setTextColor(resources.getColor(mTextColor))
        if (book_cover_category2!!.visibility != View.VISIBLE) {
            book_cover_bookshelf!!.setBackgroundResource(mBackground)
        }
    }

    private fun setAddShelfBtn() {
        mBackground = R.drawable.cover_bottom_btn_add_bg
        mTextColor = R.color.cover_bottom_btn_add_text_color
        book_cover_bookshelf!!.setTextColor(resources.getColor(mTextColor))
        if (book_cover_category2!!.visibility != View.VISIBLE) {
            book_cover_bookshelf!!.setBackgroundResource(mBackground)
        }
    }

    override fun onResume() {
        super.onResume()

        if (mCoverPagePresenter != null) {
            mCoverPagePresenter!!.checkBookStatus()
        }

    }


    override fun onClick(view: View) {
        if (mCoverPagePresenter != null) {
            mCoverPagePresenter!!.goToBookSearchActivity(view)
        }
        when (view.id) {
            R.id.book_cover_back -> {
                val data = HashMap<String, String>()
                data.put("type", "1")
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.BACK, data)
                finish()
            }

            R.id.book_cover_source_view -> {
                //书籍详情页换源点击
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_ch_source)
                if (mCoverPagePresenter != null) {
                    mCoverPagePresenter!!.showCoverSourceDialog()
                }
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.SOURCECHANGE)
            }

            R.id.book_cover_bookshelf -> if (mCoverPagePresenter != null) {
                mCoverPagePresenter!!.addBookIntoShelf(true)
            }

            R.id.book_cover_reading -> {
                //转码阅读点击的统计
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_trans_read)
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEREAD)
                if (mCoverPagePresenter != null) {
                    mCoverPagePresenter!!.bookCoverReading()
                }
            }

            R.id.book_cover_download -> {
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_all_load)
                val data3 = HashMap<String, String>()
                data3.put("bookId", requestItem!!.book_id)
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.CASHEALL, data3)
                if (mCoverPagePresenter != null) {
                    mCoverPagePresenter!!.downLoadBook()
                }
            }
            R.id.book_cover_catalog_view_nobg, R.id.book_cover_catalog_view -> {
                //书籍详情页查看目录点击
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_to_catalogue)
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.CATALOG)
                if (mCoverPagePresenter != null) {
                    mCoverPagePresenter!!.goToCataloguesAct(true)
                }
            }
            R.id.book_cover_chapter_view, R.id.book_cover_last_chapter -> {
                if (mCoverPagePresenter != null) {
                    mCoverPagePresenter!!.goToCataloguesAct(false)
                }
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.LATESTCHAPTER)
            }
        }
    }


    override fun onBackPressed() {
        val data = HashMap<String, String>()
        data.put("type", "2")
        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.BACK, data)
        finish()
    }

    override fun onDestroy() {
        try {
            setContentView(R.layout.empty)
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }

        if (mCoverPagePresenter != null) {
            mCoverPagePresenter!!.destory()
        }
        super.onDestroy()
    }


    override fun showRecommend(recommendBean: ArrayList<Book>) {

    }

    override fun showCoverError() {
        if (loadingPage != null) {
            loadingPage!!.onError()
        }
        Toast.makeText(this, "请求失败", Toast.LENGTH_SHORT).show()

    }

    override fun showRecommendError() {

    }

    override fun successAddIntoShelf(isAddIntoShelf: Boolean) {
        if (isAddIntoShelf) {
            book_cover_bookshelf!!.setText(R.string.book_cover_remove_bookshelf)
            setRemoveBtn()
        } else {
            book_cover_bookshelf!!.setText(R.string.book_cover_add_bookshelf)
            setAddShelfBtn()
        }
    }

    override fun showLoadingSuccess() {
        if (loadingPage != null) {
            loadingPage!!.onSuccess()
        }
    }

    override fun showArrow(isShow: Boolean, isQGTitle: Boolean) {
        if (isQGTitle) {
            book_cover_source_form!!.text = "青果阅读"
        }
    }

    override fun showCurrentSources(currentSource: String) {
        book_cover_source_form!!.text = currentSource
    }

    override fun showCoverDetail(bookVo: CoverPage.BookVoBean) {
        book_cover_content!!.smoothScrollTo(0, 0)
        if (bookVo != null) {

            if (book_cover_image != null && !TextUtils.isEmpty(bookVo.img_url) && bookVo
                    .img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
                Glide.with(applicationContext).load(bookVo.img_url).placeholder(net.lzbook.kit.R.drawable.icon_book_cover_default).error(net.lzbook.kit.R.drawable.icon_book_cover_default).diskCacheStrategy(DiskCacheStrategy.ALL).into(book_cover_image!!)
            } else {
                Glide.with(applicationContext).load(net.lzbook.kit.R.drawable.icon_book_cover_default).into(book_cover_image!!)
            }

            if (book_cover_title != null && !TextUtils.isEmpty(bookVo.name)) {
                book_cover_title!!.text = bookVo.name
            }

            if (book_cover_author != null && !TextUtils.isEmpty(bookVo.author)) {
                book_cover_author!!.text = bookVo.author
            }

            if (book_cover_category != null && !TextUtils.isEmpty(bookVo.labels)) {
                book_cover_category!!.text = bookVo.labels
            }

            if (book_cover_category2 != null && !TextUtils.isEmpty(bookVo.labels)) {
                book_cover_category2!!.text = bookVo.labels
                if (!mThemeHelper.isNight) {
                    book_cover_category2!!.setBackgroundResource(R.drawable.book_cover_label_bg)
                    val background = book_cover_category2!!.background as GradientDrawable
                    background.setColor(resources.getColor(R.color.color_white_ffffff))
                    book_cover_category2!!.setTextColor(AppUtils.getRandomColor())
                } else {
                    book_cover_category2!!.setTextColor(AppUtils.getRandomColor())
                }
            }

            if (1 == bookVo.status) {
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

            if (book_cover_update_time != null) {
                book_cover_update_time!!.text = Tools.compareTime(AppUtils.formatter, bookVo
                        .update_time)
            }

            if (book_cover_last_chapter != null && bookVo != null && !TextUtils.isEmpty(bookVo.last_chapter_name)) {
                book_cover_last_chapter!!.text = bookVo.last_chapter_name
            }

            if (bookVo.desc != null && !TextUtils.isEmpty(bookVo.desc)) {
                book_cover_description!!.text = bookVo.desc
            } else {
                book_cover_description!!.text = resources.getString(R.string
                        .book_cover_no_description)
            }
        } else {
            showToastShort(R.string.book_cover_no_resource)
            if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
                finish()
            }
        }
    }

    override fun changeDownloadButtonStatus(type: Int) {
        if (mCoverPagePresenter != null) {
            if (type == mCoverPagePresenter!!.DOWNLOAD_STATE_FINISH) {
                book_cover_download!!.setText(R.string.download_status_complete)
            } else if (type == mCoverPagePresenter!!.DOWNLOAD_STATE_LOCKED) {
                book_cover_download!!.setText(R.string.download_status_complete)
            } else if (type == mCoverPagePresenter!!.DOWNLOAD_STATE_NOSTART) {
                book_cover_download!!.setText(R.string.download_status_total)
            } else {
                book_cover_download!!.setText(R.string.download_status_underway)
            }
        }
    }


    override fun loadCoverWhenSourceChange() {
        loadCoverInfo()
    }

    override fun onStartStatus(isBookSubed: Boolean) {
        if (isBookSubed) {
            book_cover_bookshelf!!.setText(R.string.book_cover_remove_bookshelf)
            setRemoveBtn()
        } else {
            setAddShelfBtn()
        }
    }
}
