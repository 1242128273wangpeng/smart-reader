package com.dy.reader.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.ding.basic.bean.Book
import com.ding.basic.bean.RecommendBean
import com.ding.basic.bean.RecommendBooksEndResp
import com.ding.basic.bean.Source
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.util.showToastMessage
import com.dy.media.MediaLifecycle
import com.dy.reader.R
import com.dy.reader.adapter.BookEndAdapter
import com.dy.reader.dialog.BookEndChangeSourceDialog
import com.dy.reader.listener.SourceClickListener
import com.dy.reader.presenter.BookEndContract
import com.dy.reader.presenter.BookEndPresenter
import com.dy.reader.setting.ReaderStatus
import iyouqu.theme.BaseCacheableActivity
import kotlinx.android.synthetic.txtqbdzs.act_book_end.*
import kotlinx.android.synthetic.txtqbdzs.bookend_new_books_layout.*
import kotlinx.android.synthetic.txtqbdzs.bookend_recommend_books_layout.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.constants.Constants
import java.util.*
import java.util.concurrent.Callable
import kotlin.collections.ArrayList

@Route(path = RouterConfig.BOOK_END_ACTIVITY)
class BookEndActivity : BaseCacheableActivity(), BookEndContract {

    override fun showRecommend(recommends: java.util.ArrayList<RecommendBean>?) {
    }

    override fun showRecommendV4(one: Boolean, two: Boolean, recommendRes: RecommendBooksEndResp) {
    }

    private var book: Book? = null
    private var book_id: String? = null
    private var book_name: String? = null
    private var chapter_id: String? = null

    private var sourceList: ArrayList<Source> = ArrayList()

    private val changeSourceDialog by lazy {
        BookEndChangeSourceDialog(this, object : SourceClickListener {
            override fun clickedSource(source: Source) {
                bookEndPresenter.clickedBookSource(source)
            }

        })
    }

    private val loadingPage by lazy {
        LoadingPage(this, LoadingPage.setting_result)
    }

    private val bookEndPresenter: BookEndPresenter by lazy {
        BookEndPresenter(this, this)
    }

    private val recommendBookAdapter: BookEndAdapter by lazy {
        BookEndAdapter(this)
    }

    private val newBookAdapter: BookEndAdapter by lazy {
        BookEndAdapter(this)
    }

//    private var recommender: BookRecommender? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_book_end)

        initListener()

        initIntent()

        loadBookSource()

        if (!Constants.isHideAD) {
            initBookEndAD()
        }
    }

    private fun initListener() {
        iv_back.setOnClickListener {
            val data = HashMap<String, String>()
            data["type"] = "1"
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOKENDPAGE_PAGE, StartLogClickUtil.BACK, data)
            finish()
        }

//        txt_prompt.text = Html.fromHtml(resources.getString(R.string.book_end_prompt))

        txt_change_source.setOnClickListener {
            if (sourceList.isEmpty()) {
                showToastMessage("本书暂无其他来源")
            } else {
                changeSourceDialog.show(sourceList)
            }
        }

        tv_more_refresh.setOnClickListener {
            refreshRecommendBooks()
        }

        tv_new_refresh.setOnClickListener {
            refreshNewBooks()
        }

        txt_bookshelf.setOnClickListener {
            bookEndPresenter.startBookShelf()
            finish()
        }

        txt_bookstore.setOnClickListener {
            bookEndPresenter.startBookStore()
            finish()
        }


    }

    private fun initIntent() {
        if (intent != null) {
            book = intent.getSerializableExtra("book") as Book
            book_id = intent.getStringExtra("book_id")
            book_name = intent.getStringExtra("book_name")
            chapter_id = intent.getStringExtra("chapter_id")

            txt_title.text = book_name

            if (book != null) {
                ReaderStatus.book = book!!
            }
        }

        if (book == null) {
            finish()
        }
    }


    private fun loadBookSource() {

        book?.let {
            bookEndPresenter.requestBookSource(it)
            loadingPage.setReloadAction(Callable<Void> {
                bookEndPresenter.requestBookSource(it)
                null
            })
        }

    }


    private fun initBookEndAD() {
        //TODO 去 aar 添加新广告
//        MediaControl.loadBookEndMedia(this) { view, isSuccess ->
//            if (isSuccess) {
//                rl_book_end_ad.visibility = View.VISIBLE
//                rl_book_end_ad.addView(view)
//            } else {
//                rl_book_end_ad.visibility = View.GONE
//            }
//        }
    }

    private fun refreshRecommendBooks() {
//        if (recommender != null) {
//            val books = recommender!!.recommendBookendBooks1
//            if (books != null) {
//                recommendBookAdapter.setBooks(books)
//                recommendBookAdapter.notifyDataSetChanged()
//            } else {
//                AppLog.e("test", "书籍已拿完，重新从后端获取")
//                bookEndPresenter.loadRecommendBooks(true, false, bookId!!)
//            }
//        }
        val refresh = HashMap<String, String>()
        refresh.put("module", "1")
        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOKENDPAGE_PAGE, 
                StartLogClickUtil.REPLACE, refresh)
    }

    private fun refreshNewBooks() {
//        if (recommender != null) {
//            val books = recommender!!.recommendBookendBooks2
//            if (books != null) {
//                newBookAdapter.setBooks(books)
//                newBookAdapter.notifyDataSetChanged()
//            } else {
//                bookEndPresenter.loadRecommendBooks(false, true, bookId!!)
//            }
//        }
        val data = HashMap<String, String>()
        data.put("module", "2")
        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOKENDPAGE_PAGE, 
                StartLogClickUtil.REPLACE, data)
    }

    override fun onResume() {
        super.onResume()
        MediaLifecycle.onResume()
    }

    override fun onStart() {
        super.onStart()
        MediaLifecycle.onStart()
    }

    override fun onPause() {
        super.onPause()
        MediaLifecycle.onPause()
    }

    override fun onRestart() {
        super.onRestart()
        MediaLifecycle.onRestart()
    }

    override fun onStop() {
        super.onStop()
        MediaLifecycle.onStop()
    }


    override fun onDestroy() {
        super.onDestroy()
        MediaLifecycle.onDestroy()
    }


    /***
     * 展示来源信息
     * **/
    override fun showSourceList(sourceList: ArrayList<Source>) {
//        if (sourceList.isNotEmpty()) {
//            rl_source.visibility = View.VISIBLE
//
//            sourceAdapter = SourceAdapter(sourceList, this)
//
//            val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
//
//            rl_source.adapter = sourceAdapter
//
//            rl_source.layoutManager = linearLayoutManager
//
//            rl_source.layoutParams.height = sourceList.size * resources.getDimensionPixelOffset(R.dimen.source_item_height)
//        } else {
//            rl_source.visibility = View.GONE
//        }
        if (sourceList.isNotEmpty()) {
            this.sourceList.clear()
            this.sourceList.addAll(sourceList)
        }

        loadingPage.onSuccess()
    }

}