package com.intelligent.reader.activity

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.support.v7.widget.SimpleItemAnimator
import android.text.TextUtils
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.intelligent.reader.R
import com.intelligent.reader.adapter.CoverRecommendAdapter
import com.intelligent.reader.presenter.coverPage.CoverPageContract
import com.intelligent.reader.presenter.coverPage.CoverPagePresenter
import com.intelligent.reader.read.help.BookHelper
import com.intelligent.reader.util.ShelfGridLayoutManager
import com.intelligent.reader.view.MyScrollView
import kotlinx.android.synthetic.mfqbxssc.act_book_cover.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.CoverPage
import net.lzbook.kit.data.bean.RequestItem
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.Tools
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.Callable

class CoverPageActivity : BaseCacheableActivity(), OnClickListener, MyScrollView.ScrollChangedListener, CoverRecommendAdapter.RecommendItemClickListener, CoverPageContract {
    private var mTextColor = 0
    private var loadingPage: LoadingPage? = null


    private var requestItem: RequestItem? = null
    private var layoutManager: ShelfGridLayoutManager? = null
    private var coverRecommendAdapter: CoverRecommendAdapter? = null
    private var mRecommendBooks: MutableList<Book> = ArrayList()


    private var bookVo: CoverPage.BookVoBean? = null
    private var mCoverPagePresenter: CoverPagePresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatServiceUtils.statAppBtnClick(this, StatServiceUtils.cover_into)
        setContentView(R.layout.act_book_cover)
        initData(intent)
        initListener()

    }


    private fun initRecyclerView() {
        if (mRecommendBooks.size == 0) {
            tv_recommend_title!!.visibility = View.GONE
        } else {
            tv_recommend_title!!.visibility = View.VISIBLE
        }
        if (coverRecommendAdapter == null) {
            coverRecommendAdapter = CoverRecommendAdapter(this, this, mRecommendBooks)
        }

        recycler_view!!.recycledViewPool.setMaxRecycledViews(0, 12)
        layoutManager = ShelfGridLayoutManager(this, 3)
        recycler_view!!.layoutManager = layoutManager
        recycler_view!!.isNestedScrollingEnabled = false
        recycler_view!!.itemAnimator.addDuration = 0
        recycler_view!!.itemAnimator.changeDuration = 0
        recycler_view!!.itemAnimator.moveDuration = 0
        recycler_view!!.itemAnimator.removeDuration = 0
        (recycler_view!!.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recycler_view!!.adapter = coverRecommendAdapter
    }

    protected fun initListener() {
        book_cover_back?.setOnClickListener(this)
        book_cover_author?.setOnClickListener(this)
        book_cover_source_view?.setOnClickListener(this)
        book_cover_last_chapter?.setOnClickListener(this)
        book_cover_bookshelf?.setOnClickListener(this)
        book_cover_reading?.setOnClickListener(this)
        book_cover_download?.setOnClickListener(this)
        cover_latest_section?.setOnClickListener(this)
        book_cover_content?.setScrollChangedListener(this)
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
            mCoverPagePresenter!!.getRecommend()
        }
    }

    protected fun loadCoverInfo() {
        if (loadingPage != null) {
            loadingPage!!.onSuccess()
        }
        loadingPage = LoadingPage(this, findViewById(R.id.book_cover_main) as ViewGroup, LoadingPage.setting_result)
        if (requestItem != null) {
            if (mCoverPagePresenter != null) {
                mCoverPagePresenter!!.getBookCoverInfo(true)
            }
            requestItem!!.channel_code = 2
        }
        if (loadingPage != null) {
            loadingPage!!.setReloadAction(Callable<Void> {
                if (mCoverPagePresenter != null) {
                    mCoverPagePresenter!!.getBookCoverInfo(true)
                }
                null
            })
        }
    }


    override fun showCoverError() {
        if (loadingPage != null) {
            loadingPage!!.onError()
        }
        Toast.makeText(this, "请求失败", Toast.LENGTH_SHORT).show()
    }

    override fun showCurrentSources(host: String) {
        if (book_cover_source_form != null) {
            book_cover_source_form!!.text = host
        }
    }


    override fun showLoadingSuccess() {
        if (loadingPage != null) {
            loadingPage!!.onSuccess()
        }
    }

    override fun showArrow(isShow: Boolean, isQGTitle: Boolean) {
        if (iv_arrow != null) {
            if (isShow) {
                iv_arrow!!.visibility = View.VISIBLE
            } else {
                iv_arrow!!.visibility = View.GONE
            }
            if (isQGTitle) {
                if (book_cover_source_form != null) {
                    book_cover_source_form!!.text = "青果阅读"
                }
            }
        }
    }

    override fun showCoverDetail(bookVo: CoverPage.BookVoBean) {
        this.bookVo = bookVo
        book_cover_content!!.smoothScrollTo(0, 0)
        if (bookVo != null) {
            if (book_cover_image != null && !TextUtils.isEmpty(bookVo.img_url)) {
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
            if (tv_text_number != null && bookVo.wordCountDescp != null) {
                if (Constants.QG_SOURCE != bookVo.host) {
                    tv_text_number!!.text = bookVo.wordCountDescp + "字"
                } else {
                    tv_text_number!!.text = AppUtils.getWordNums(java.lang.Long.valueOf(bookVo.wordCountDescp)!!)
                }
            } else {
                tv_text_number!!.text = "暂无"
            }

            if (tv_read_num != null && bookVo.readerCountDescp != null) {
                if (Constants.QG_SOURCE == bookVo.host) {
                    tv_read_num!!.text = AppUtils.getReadNums(java.lang.Long.valueOf(bookVo.readerCountDescp)!!)
                } else {
                    tv_read_num!!.text = bookVo.readerCountDescp + "人在读"
                }

            } else {
                tv_read_num!!.text = ""
            }
            if (bookVo.score == 0.0) {
                tv_score!!.text = "暂无评分"
            } else {
                if (Constants.QG_SOURCE != bookVo.host) {
                    bookVo.score = java.lang.Double.valueOf(DecimalFormat("0.0").format(bookVo.score))!!
                }
                tv_score!!.text = bookVo.score.toString() + "分"
                if (bookVo.score > 0.4) {
                    ratingBar!!.rating = java.lang.Float.valueOf((bookVo.score / 2 - 0.2).toString() + "")!!
                } else {
                    ratingBar!!.rating = java.lang.Float.valueOf((bookVo.score / 2).toString() + "")!!
                }
            }

            if (flowlayout != null && !TextUtils.isEmpty(bookVo.labels)) {
                flowlayout!!.childSpacing = resources.getDimensionPixelOffset(R.dimen.cover_book_flowlayout)
                flowlayout!!.rowSpacing = 17f
                flowlayout!!.removeAllViews()
                val dummyTexts = bookVo.labels.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (text in dummyTexts) {
                    if (!TextUtils.isEmpty(text)) {
                        val textView = buildLabel(text)
                        flowlayout!!.addView(textView)
                    }
                }
            }

            if (1 == bookVo.status) {
                book_cover_status!!.text = getString(R.string.book_cover_state_writing)
            } else {
                book_cover_status!!.text = getString(R.string.book_cover_state_written)
            }
            if (book_cover_update_time != null) {
                book_cover_update_time!!.text = Tools.compareTime(AppUtils.formatter, bookVo.update_time) + "更新"
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

    /**
     * 添加标签
     */
    private fun buildLabel(text: String): TextView {
        val left = resources.getDimensionPixelOffset(R.dimen.cover_book_flowlayout_padding)
        val right = resources.getDimensionPixelOffset(R.dimen.cover_book_flowlayout_padding)
        val top = resources.getDimensionPixelOffset(R.dimen.cover_book_flowlayout_top)
        val bottom = resources.getDimensionPixelOffset(R.dimen.cover_book_flowlayout_top)
        val textView = TextView(this)
        textView.text = text
        textView.textSize = 11f
        textView.setPadding(left, top, right, bottom)
        textView.setTextColor(resources.getColor(R.color.cover_recommend_read))
        textView.setBackgroundResource(R.drawable.cover_label_shape)

        return textView
    }

    override fun showRecommendError() {
        if (tv_recommend_title != null) {
            tv_recommend_title!!.visibility = View.GONE
        }
    }

    override fun showRecommend(books: ArrayList<Book>) {
        mRecommendBooks.clear()
        mRecommendBooks = books
        if (tv_recommend_title != null) {
            tv_recommend_title!!.visibility = View.VISIBLE
        }
        initRecyclerView()
    }

    override fun loadCoverWhenSourceChange() {
        loadCoverInfo()
    }

    override fun successAddIntoShelf(isAddIntoShelf: Boolean) {
        if (isAddIntoShelf) {
            book_cover_bookshelf!!.setText(R.string.book_cover_havein_bookshelf)
            setRemoveBtn()
        }
    }

    private fun setRemoveBtn() {
        mTextColor = R.color.home_title_search_text
        book_cover_bookshelf!!.setTextColor(resources.getColor(mTextColor))
    }

    override fun onStartStatus(isBookSubed: Boolean) {

        if (isBookSubed) {
            book_cover_bookshelf!!.setText(R.string.book_cover_havein_bookshelf)
            setRemoveBtn()
        }

    }

    override fun onResume() {
        super.onResume()
        if (mCoverPagePresenter != null) {
            mCoverPagePresenter!!.checkBookStatus()
        }
    }

    /**
     * 改变缓存状态值
     */
    override fun changeDownloadButtonStatus(type: Int) {
        if (mCoverPagePresenter != null) {
            if (type == mCoverPagePresenter!!.DOWNLOAD_STATE_FINISH) {
                book_cover_download!!.setText(R.string.download_status_complete)
                book_cover_download!!.setTextColor(resources.getColor(R.color.home_title_search_text))
            } else if (type == mCoverPagePresenter!!.DOWNLOAD_STATE_LOCKED) {
                book_cover_download!!.setText(R.string.download_status_complete)
                book_cover_download!!.setTextColor(resources.getColor(R.color.home_title_search_text))
            } else if (type == mCoverPagePresenter!!.DOWNLOAD_STATE_NOSTART) {
                book_cover_download!!.setText(R.string.download_status_total)
            } else {
                book_cover_download!!.setText(R.string.download_status_underway)
            }
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
                mCoverPagePresenter!!.addBookIntoShelf(false)
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
            R.id.cover_latest_section, R.id.book_cover_last_chapter -> {
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.LATESTCHAPTER)
                if (mCoverPagePresenter != null) {
                    mCoverPagePresenter!!.goToCataloguesAct(false)
                }
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

    override fun onScrollChanged(top: Int, oldTop: Int) {
        if (AppUtils.px2dip(this, top.toFloat()) > 34) {
            if (tv_title != null && bookVo != null && !TextUtils.isEmpty(bookVo!!.name)) {
                tv_title!!.text = bookVo!!.name
            }
        } else {
            tv_title!!.text = "书籍详情"
        }
    }

    override fun onItemClick(view: View?, position: Int) {
        if (view == null || position < 0 || position > mRecommendBooks.size)
            return
        val book = mRecommendBooks[position] ?: return
        val data = HashMap<String, String>()
        if (requestItem != null && requestItem!!.book_id != null) {
            data.put("bookid", requestItem!!.book_id)
            data.put("Tbookid", book.book_id)
        }
        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.RECOMMENDEDBOOK, data)
        BookHelper.goToCoverOrRead(this, this, book, 2)
    }


}
