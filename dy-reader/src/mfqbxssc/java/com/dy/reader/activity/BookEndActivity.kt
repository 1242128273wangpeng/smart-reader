package com.dy.reader.activity

import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.baidu.mobstat.StatService
import com.ding.basic.bean.Book
import com.ding.basic.bean.RecommendBean
import com.ding.basic.bean.RecommendBooksEndResp
import com.ding.basic.bean.Source
import com.dy.media.MediaControl
import com.dy.media.MediaLifecycle
import com.dy.reader.R
import com.dy.reader.adapter.BookEndAdapter
import com.dy.reader.dialog.BookEndChangeSourceDialog
import com.dy.reader.listener.SourceClickListener
import com.dy.reader.presenter.BookEndContract
import com.dy.reader.presenter.BookEndPresenter
import com.dy.reader.setting.ReaderStatus
import kotlinx.android.synthetic.mfqbxssc.act_book_end.*
import kotlinx.android.synthetic.mfqbxssc.layout_book_end_new_books.*
import kotlinx.android.synthetic.mfqbxssc.layout_book_end_recommend_books.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.base.activity.BaseCacheableActivity
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.user.BookRecommender
import net.lzbook.kit.utils.logger.AppLog
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.toast.showToastMessage
import net.lzbook.kit.widget.LoadingPage
import java.util.*
import java.util.concurrent.Callable
import kotlin.collections.ArrayList

@Route(path = RouterConfig.BOOK_END_ACTIVITY)
class BookEndActivity : BaseCacheableActivity(), BookEndContract, SourceClickListener {

    private var book: Book? = null
    private var bookId: String? = null
    private var bookName: String? = null
    //TODO 打点使用，目前完结页打点缺失，后期需要补充
    private var bookChapterId: String? = null

    private var loadingPage: LoadingPage? = null
    private val sourceList: ArrayList<Source> = ArrayList()
    private var bookRecommendsList: ArrayList<Book>? = null

    private var mRecommendBookAdapter: BookEndAdapter? = null
    private var mNewBookAdapter: BookEndAdapter? = null

    /**
     * 推荐书籍管理类：负责书架和书末页的推荐书籍管理
     */
    private var recommender: BookRecommender? = null

    private val bookEndPresenter: BookEndPresenter by lazy {
        BookEndPresenter(this, this)
    }

    private val changeSourceDialog: BookEndChangeSourceDialog by lazy {
        BookEndChangeSourceDialog(this, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_book_end)

        initListener()
        initIntent()
        initData()
        bookEndPresenter?.uploadLog(book,StartLogClickUtil.ENTER)
        if (!Constants.isHideAD && !AppUtils.isNeedAdControl(Constants.ad_control_other)) {
            initBookEndAD()
        }
    }

    override fun onResume() {
        super.onResume()
        StatService.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        StatService.onPause(this)
    }
    private fun initListener() {

        iv_back.setOnClickListener {
            val map = HashMap<String, String>()
            map["type"] = "1"
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOKENDPAGE_PAGE, StartLogClickUtil.BACK, map)
            finish()
        }

        // 右上角换源按钮
        txt_change_source.setOnClickListener {
            if (sourceList.isEmpty()) {
                showToastMessage("本书暂无其他来源")
            } else {
                changeSourceDialog.show(sourceList)
            }
            val map = HashMap<String, String>()
            bookId?.let { map.put("bookid", it) }
            bookChapterId?.let { map.put("chapterid", it) }
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.READFINISH, StartLogClickUtil.SOURCECHANGE, map)

        }


//        txt_prompt.text = Html.fromHtml(resources.getString(R.string.book_end_prompt))
        // 我的书架
        txt_bookshelf.setOnClickListener {
            bookEndPresenter.startBookShelf()
            bookEndPresenter?.uploadLog(book,StartLogClickUtil.TOSHELF)
            finish()
        }

        //去书城看一看
        txt_bookstore.setOnClickListener {
            bookEndPresenter.startBookStore()
            bookEndPresenter?.uploadLog(book,StartLogClickUtil.TOBOOKSTORE)
            finish()
        }
        //喜欢这本书的人还喜欢（换一换）
        txt_more_refresh.setOnClickListener {
            refreshBooks1()
            val refresh = java.util.HashMap<String, String>()
            refresh.put("module", "1")
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.READFINISH, StartLogClickUtil.REPLACE, refresh)
        }

        //新锐好书抢先看（换一换）
        txt_new_refresh.setOnClickListener {
            refreshBooks2()
            val refresh = java.util.HashMap<String, String>()
            refresh.put("module", "2")
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.READFINISH, StartLogClickUtil.REPLACE, refresh)
        }


    }

    private fun initIntent() {
        if (intent != null) {
            book = intent.getSerializableExtra("book") as Book
            bookId = intent.getStringExtra("book_id")
            bookName = intent.getStringExtra("book_name")
            bookChapterId = intent.getStringExtra("chapter_id")

            txt_title.text = bookName

            if (book != null) {
                ReaderStatus.book = book!!
            }
        }

        if (book == null) {
            finish()
        }
    }


    private fun initData() {
        loadingPage = LoadingPage(this, LoadingPage.setting_result)

        book?.let {
            bookEndPresenter.requestBookSource(it)
            bookEndPresenter.requestRecommendV4(true, true, it.book_id)

            loadingPage?.setReloadAction(Callable<Void> {
                bookEndPresenter.requestBookSource(it)
                bookEndPresenter.requestRecommendV4(true, true, it.book_id)
                null
            })
        }

    }


    private fun initBookEndAD() {
        MediaControl.loadBookEndMedia(this) { view, isSuccess ->
            if (isSuccess) {
                rl_book_end_ad.visibility = View.VISIBLE
                rl_book_end_ad.addView(view)
            } else {
                rl_book_end_ad.visibility = View.GONE
            }
        }
    }


    private fun refreshBooks1() {
        if (recommender != null) {
            val recommendBookendBooks1 = recommender!!.recommendBookendBooks1
            if (recommendBookendBooks1 != null) {
                mRecommendBookAdapter?.setBooks(recommendBookendBooks1)
                mRecommendBookAdapter?.notifyDataSetChanged()
            } else {
                AppLog.e("test", "书籍已拿完，重新从后端获取")
                bookEndPresenter.requestRecommendV4(true, false, bookId!!)
            }
        }
    }

    private fun refreshBooks2() {
        if (recommender != null) {
            val recommendBookendBooks2 = recommender!!.recommendBookendBooks2
            if (recommendBookendBooks2 != null) {
                mNewBookAdapter?.setBooks(recommendBookendBooks2)
                mNewBookAdapter?.notifyDataSetChanged()
            } else {
                bookEndPresenter.requestRecommendV4(false, true, bookId!!)
            }
        }
    }


    /***
     * 隐藏LoadingPage
     * **/
    private fun dismissLoading() {
        loadingPage?.onSuccess()
    }

    override fun onDestroy() {
        MediaLifecycle.onDestroy()
        super.onDestroy()
    }


    /***
     * 展示来源信息
     * **/
    override fun showSourceList(sourceList: ArrayList<Source>) {
        this.sourceList.clear()
        this.sourceList.addAll(sourceList)
        dismissLoading()
    }

    override fun showRecommend(recommends: ArrayList<RecommendBean>?) {

    }

    override fun showRecommendV4(one: Boolean, two: Boolean, recommendRes: RecommendBooksEndResp) {
        recommender = BookRecommender(recommendRes, Constants.sRecommendRateForBookend)
        if (one) {
            if (mRecommendBookAdapter == null) {
                mRecommendBookAdapter = BookEndAdapter(this)
            }
            mRecommendBookAdapter?.setBooks(recommender!!.recommendBookendBooks1)
            gv_recommend_book.adapter = mRecommendBookAdapter
        }
        if (two) {
            if (mNewBookAdapter == null) {
                mNewBookAdapter = BookEndAdapter(this)
            }
            mNewBookAdapter?.setBooks(recommender!!.recommendBookendBooks2)
            gv_new_book.adapter = mNewBookAdapter
        }
        dismissLoading()
    }

    override fun clickedSource(source: Source) {
        bookEndPresenter.clickedBookSource(source)
    }


}