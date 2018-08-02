package com.intelligent.reader.activity

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.support.v7.widget.SimpleItemAnimator
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.intelligent.reader.R
import com.intelligent.reader.adapter.CommentsAdapter
import com.intelligent.reader.adapter.CoverCommentAdapter
import com.intelligent.reader.adapter.CoverRecommendAdapter
import com.intelligent.reader.presenter.coverPage.CoverPageContract
import com.intelligent.reader.presenter.coverPage.CoverPagePresenter
import com.intelligent.reader.read.help.BookHelper
import com.intelligent.reader.util.ShelfGridLayoutManager
import com.intelligent.reader.view.MyScrollView
import kotlinx.android.synthetic.mfqbxssc.act_book_cover.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.CoverPage
import net.lzbook.kit.data.bean.RequestItem
import net.lzbook.kit.data.comment.CommentEntity
import net.lzbook.kit.encrypt.URLBuilderIntterface
import net.lzbook.kit.utils.*
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.Callable
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CoverPageActivity : BaseCacheableActivity(), OnClickListener, MyScrollView.ScrollChangedListener, CoverRecommendAdapter.RecommendItemClickListener, CoverPageContract {
    override fun showCommentError() {
        if(tv_comment_title != null){
            tv_comment_title.visibility = View.GONE
        }
        if(tv_wonderful_comment != null){
            tv_wonderful_comment.visibility = View.GONE
        }
    }

    override fun showCommentSuccess(comments: ArrayList<CommentEntity.DataBean.EntityListBean>) {
        mComments.clear()
        mComments = comments;
        if(tv_comment_title != null){
            tv_comment_title.visibility = View.VISIBLE
        }
        if(tv_wonderful_comment != null){
            tv_wonderful_comment.visibility = View.VISIBLE
        }
        initComments()
    }

    private var mTextColor = 0
    private var mTextBlackColor = 0
    private var loadingPage: LoadingPage? = null


    private var requestItem: RequestItem? = null
    private var layoutManager: ShelfGridLayoutManager? = null
    private var coverRecommendAdapter: CoverRecommendAdapter? = null
    private var coverCommentAdapter: CoverCommentAdapter? = null
    private var mRecommendBooks: MutableList<Book> = ArrayList()
    private var isScrool: Boolean = true // 用于标记点击推荐重新加载 title 的内容变化
    private var isShouldReceiver = true //用于标记bookCoverUtil 是否需要注册广播接收
    private var mComments: MutableList<CommentEntity.DataBean.EntityListBean> = ArrayList()

    private var bookVo: CoverPage.BookVoBean? = null
    private var mCoverPagePresenter: CoverPagePresenter? = null


    companion object {
        fun launcher(context: Context, host: String, book_id: String, book_source_id: String, name: String, author: String, parameter: String, extra_parameter: String) {
            val requestItem = RequestItem()
            requestItem.book_id = book_id
            requestItem.book_source_id = book_source_id
            requestItem.host = host
            requestItem.name = name
            requestItem.author = author
            requestItem.parameter = parameter
            requestItem.extra_parameter = extra_parameter

            val intent = Intent()
            intent.setClass(context, CoverPageActivity::class.java)
            val bundle = Bundle()
            try {
                bundle.putSerializable(Constants.REQUEST_ITEM, requestItem)
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
        //百度移动统计上 酷派_8298-A01 RatingBar 该款手机报错 影响用户 1 报错次数13
        try {
            setContentView(R.layout.act_book_cover)
        } catch (e: Exception) {
        }
        initData(intent)
        initListener()

    }


    private fun initRecyclerView() {

        if(tv_recommend_title != null){
            if (mRecommendBooks.size == 0) {
                tv_recommend_title.visibility = View.GONE
            } else {
                tv_recommend_title.visibility = View.VISIBLE
            }
        }

//        if (coverRecommendAdapter == null) {
            coverRecommendAdapter = CoverRecommendAdapter(this, this, mRecommendBooks)
//        }
        if(recycler_view != null ){
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
    }

    private fun initComments(){
        coverCommentAdapter = CoverCommentAdapter(this, mComments,requestItem,mCoverPagePresenter?.getBook())
        if(comment_recycler_view != null ){
            comment_recycler_view!!.recycledViewPool.setMaxRecycledViews(0, 12)
            layoutManager = ShelfGridLayoutManager(this, 1)
            comment_recycler_view!!.layoutManager = layoutManager
            comment_recycler_view!!.isNestedScrollingEnabled = false
            comment_recycler_view!!.itemAnimator.addDuration = 0
            comment_recycler_view!!.itemAnimator.changeDuration = 0
            comment_recycler_view!!.itemAnimator.moveDuration = 0
            comment_recycler_view!!.itemAnimator.removeDuration = 0
            (comment_recycler_view!!.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            comment_recycler_view!!.adapter = coverCommentAdapter
        }
    }

    protected fun initListener() {
        book_cover_back?.setOnClickListener(this)
        book_cover_author?.setOnClickListener(this)
        book_cover_last_chapter?.setOnClickListener(this)
        book_cover_bookshelf?.setOnClickListener(this)
        book_cover_reading?.setOnClickListener(this)
        book_cover_download?.setOnClickListener(this)
        cover_latest_section?.setOnClickListener(this)
        book_cover_content?.setScrollChangedListener(this)
        tv_wonderful_comment?.setOnClickListener(this)
    }

    protected fun initData(intent: Intent?) {

        if (intent != null) {
            if (intent.hasExtra(Constants.REQUEST_ITEM)) {
                requestItem = intent.getSerializableExtra(Constants.REQUEST_ITEM) as RequestItem
            }
        }
        if (requestItem != null) {
            mCoverPagePresenter = CoverPagePresenter(requestItem!!, this, this, this,isShouldReceiver)
            loadCoverInfo()
            mCoverPagePresenter!!.getRecommend()
//            mCoverPagePresenter!!.getComment()
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
                    mCoverPagePresenter!!.getRecommend()
//                    mCoverPagePresenter!!.getComment()
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

    override fun showArrow(isQGTitle: Boolean) {
        if (isQGTitle) {
            if (book_cover_source_form != null) {
                book_cover_source_form!!.text = "青果阅读"
                book_cover_source_form.setCompoundDrawables(null, null, null, null)
            }
        }
    }

    override fun setCompound() {
        if (book_cover_source_form != null) {
            book_cover_source_form.setCompoundDrawables(null, null, null, null)
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
                if (Constants.QG_SOURCE.equals(bookVo.host) && !AppUtils.isContainChinese(bookVo.wordCountDescp)) {
                    tv_text_number!!.text = AppUtils.getWordNums(java.lang.Long.valueOf(bookVo.wordCountDescp)!!)

                } else {
                    tv_text_number!!.text = bookVo.wordCountDescp + "字"
                }

            } else {
                tv_text_number!!.text = "暂无"
            }

            if (tv_read_num != null && bookVo.readerCountDescp != null) {

                if (Constants.QG_SOURCE == bookVo.host && !AppUtils.isContainChinese(bookVo.readerCountDescp)) {
                    tv_read_num.text = AppUtils.getReadNums(java.lang.Long.valueOf(bookVo.readerCountDescp)!!)
                } else {
                    tv_read_num.text = bookVo.readerCountDescp + "人在读"
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

            if(flowlayout != null){
                flowlayout!!.removeAllViews()
                if (!TextUtils.isEmpty(bookVo.labels) && Constants.QG_SOURCE != bookVo.host) {
                    flowlayout!!.childSpacing = resources.getDimensionPixelOffset(R.dimen.cover_book_flowlayout)
                    flowlayout!!.rowSpacing = 17f
                    flowlayout!!.maxRows = 1
                    val dummyTexts = bookVo.labels.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    for (i in dummyTexts.indices) {
                        if (!TextUtils.isEmpty(dummyTexts.get(i))) {
                            val textView = buildLabel(dummyTexts.get(i),i)
                            flowlayout!!.addView(textView)
                        }
                    }
                }
            }

            if (1 == bookVo.book_status) {
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
            isScrool = false
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
    private fun buildLabel(text: String,index:Int): TextView {
        val left = resources.getDimensionPixelOffset(R.dimen.cover_book_flowlayout_padding)
        val right = resources.getDimensionPixelOffset(R.dimen.cover_book_flowlayout_padding_right)
        val top = resources.getDimensionPixelOffset(R.dimen.cover_book_flowlayout_top)
        val bottom = resources.getDimensionPixelOffset(R.dimen.cover_book_flowlayout_top)
        val textView = TextView(this)
        textView.text = text
        textView.textSize = 11f
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(resources.getColor(R.color.cover_recommend_read))
        textView.setBackgroundResource(R.drawable.cover_label_shape)
        textView.setPadding(left, top, right, bottom)
        textView.setOnClickListener(OnClickListener {

                var data = HashMap<String,String>()
                data.put("bookid",bookVo?.book_id+"")
                data.put("name",bookVo?.name+"")
                data.put("lablekey",text)
                data.put("rank",index.toString())
                StartLogClickUtil.upLoadEventLog(this,StartLogClickUtil.BOOOKDETAIL_PAGE,StartLogClickUtil.LABLECLICK,data)


            val intent = Intent()
            intent.setClass(this, LablesDetailActivity::class.java)
            intent.putExtra("url", URLBuilderIntterface.LABEL_SEARCH_V4+"?keyword=" + text)
            intent.putExtra("title", text)
            intent.putExtra("fromCover", true)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        })


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

    override fun setShelfBtnClickable(clickable: Boolean) {
        book_cover_bookshelf!!.setClickable(clickable)
    }

    private fun setRemoveBtn() {
        mTextColor = R.color.home_title_search_text
        book_cover_bookshelf!!.setTextColor(resources.getColor(mTextColor))
    }

    private fun setAddBtn() {
        mTextBlackColor = R.color.cover_title_color
        book_cover_bookshelf!!.setTextColor(resources.getColor(mTextBlackColor))
    }

    override fun onStartStatus(isBookSubed: Boolean) {

        if (isBookSubed) {
            book_cover_bookshelf.setText(R.string.book_cover_havein_bookshelf)
            setRemoveBtn()
        }else{
            book_cover_bookshelf!!.setClickable(true)
            book_cover_bookshelf.setText(R.string.book_cover_add_bookshelf)
            setAddBtn()
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
    override fun changeDownloadButtonStatus() {
        if (book_cover_download == null || bookVo == null) {
            return
        }
        var book: Book? = null
        book = mCoverPagePresenter?.getBook()
        if (book != null && book_cover_download != null) {
            val status = CacheManager.getBookStatus(book)
            if (status == DownloadState.FINISH) {
                book_cover_download.setText(R.string.download_status_complete)
            } else if (status == DownloadState
                    .WAITTING || status == DownloadState.DOWNLOADING) {
                book_cover_download.setText(R.string.download_status_underway)
            } else {
                book_cover_download.setText(R.string.download_status_total)
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
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.BACK, data)
                finish()
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
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.CATALOG)
                if (mCoverPagePresenter != null) {
                    mCoverPagePresenter!!.goToCataloguesAct(false)
                }
            }
            R.id.tv_wonderful_comment ->{
                if (mCoverPagePresenter != null) {
                    val data3 = HashMap<String, String>()
                    data3.put("bookId", requestItem?.book_id+"")
                    data3.put("type", "2")
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.CLICK, data3)
                    mCoverPagePresenter!!.goToCommentAct()
                }
            }
        }
    }


    override fun onDestroy() {
        try {
            setContentView(R.layout.empty)
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }

        AppLog.e("ondestory","destory")
        if (mCoverPagePresenter != null) {
            mCoverPagePresenter!!.destory()
        }
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        isShouldReceiver = false
        AppLog.e("onPause","onPause")
    }

    override fun onTaskStatusChange() {
        super.onTaskStatusChange()
        changeDownloadButtonStatus()
    }

    override fun onScrollChanged(top: Int, oldTop: Int) {
        if (AppUtils.px2dip(this, top.toFloat()) > 34) {
            if (tv_title != null && bookVo != null && !TextUtils.isEmpty(bookVo!!.name) && !isScrool) {
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
        tv_title.text = "书籍详情"
        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.RECOMMENDEDBOOK, data)
        BookHelper.goToCoverOrRead(this, this, book, 2)

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        isScrool = true;
        isShouldReceiver = false
        if(book_cover_bookshelf != null){
            book_cover_bookshelf!!.setClickable(true)
            book_cover_bookshelf.setText(R.string.book_cover_add_bookshelf)
            setAddBtn()
        }
        mComments.clear()
        coverCommentAdapter?.notifyDataSetChanged()
        initData(intent)
        initListener()
    }
}
