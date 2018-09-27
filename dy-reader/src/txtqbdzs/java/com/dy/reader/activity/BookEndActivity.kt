package com.dy.reader.activity

import android.graphics.Rect
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.ding.basic.bean.Book
import com.ding.basic.bean.RecommendBean
import com.ding.basic.bean.RecommendBooksEndResp
import com.ding.basic.bean.Source
import com.dy.media.MediaControl
import com.dy.media.MediaLifecycle
import com.dy.reader.R
import com.dy.reader.adapter.BookEndRecommendAdapter
import com.dy.reader.dialog.BookEndChangeSourceDialog
import com.dy.reader.listener.SourceClickListener
import com.dy.reader.presenter.BookEndContract
import com.dy.reader.presenter.BookEndPresenter
import com.dy.reader.setting.ReaderStatus
import kotlinx.android.synthetic.txtqbdzs.act_book_end.*
import kotlinx.android.synthetic.txtqbdzs.bookend_recommend_books_layout.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.ui.widget.LoadingPage
import java.util.*
import java.util.concurrent.Callable
import kotlin.collections.ArrayList

@Route(path = RouterConfig.BOOK_END_ACTIVITY)
class BookEndActivity : BaseCacheableActivity(), BookEndContract {

    override fun showRecommend(recommends: java.util.ArrayList<RecommendBean>?) {
        if (recommends == null) {
            loadingPage.onError()
        } else {
            loadingPage.onSuccess()
            bookEndAdapter.setBooks(recommends)


        }

    }

    override fun showRecommendV4(one: Boolean, two: Boolean, recommendRes: RecommendBooksEndResp) {

    }


    private var book: Book? = null
    private var bookId: String? = null
    private var bookName: String? = null
    private var bookChapterId: String? = null

    private var sourceList: ArrayList<Source> = ArrayList()
    private val changeSourceDialog by lazy {
        BookEndChangeSourceDialog(this, object : SourceClickListener {
            override fun clickedSource(source: Source) {
                bookEndPresenter.clickedBookSource(source)
            }

        })
    }

    private val bookEndAdapter: BookEndRecommendAdapter by lazy {
        BookEndRecommendAdapter(this)
    }

    private val loadingPage by lazy {
        LoadingPage(this, fl_bookend_content, LoadingPage.setting_result)
    }

    private val bookEndPresenter: BookEndPresenter by lazy {
        BookEndPresenter(this, this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_book_end)

        initView()
        initListener()

        initIntent()

        loadBookSource()
        bookEndPresenter?.uploadLog(book,StartLogClickUtil.ENTER)
        if (!Constants.isHideAD) {
            initBookEndAD()
        }
    }

    private fun initView() {
        recl_recommend_book.layoutManager = GridLayoutManager(this, 3)
        recl_recommend_book.adapter = bookEndAdapter
        recl_recommend_book.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
                val spaceLR = AppUtils.dip2px(view!!.context, 15f)
                val spaceTB = AppUtils.dip2px(view!!.context, 16f)
                outRect!!.top = spaceTB
                outRect!!.left = spaceLR
                outRect.right = spaceLR

            }
        })
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
                ToastUtil.showToastMessage("本书暂无其他来源")
            } else {
                changeSourceDialog.show(sourceList)
            }
            val map = HashMap<String, String>()
            bookId?.let { map.put("bookid", it) }
            bookChapterId?.let { map.put("chapterid", it) }
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.READFINISH, StartLogClickUtil.SOURCECHANGE, map)

        }
        /**
         * 喜欢这本书的人还喜欢（换一换）
         */
        tv_more_refresh.setOnClickListener {
            refreshRecommendBooks()
        }
//        /**
//         * 新锐好书抢先看（换一换）
//         */
//        tv_new_refresh.setOnClickListener {
//            refreshNewBooks()
//        }
        /**
         * 我的书架
         */
        txt_bookshelf.setOnClickListener {
            bookEndPresenter.startBookShelf()
            bookEndPresenter?.uploadLog(book,StartLogClickUtil.TOSHELF)
            finish()
        }
        /**
         * 去书城
         */
        txt_bookstore.setOnClickListener {
            bookEndPresenter.startBookStore()
            bookEndPresenter?.uploadLog(book,StartLogClickUtil.TOBOOKSTORE)
            finish()
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


    private fun loadBookSource() {
        book?.let {
            bookEndPresenter.requestBookSource(it)
            bookEndPresenter.requestRecommend(it.book_id)
            loadingPage?.setReloadAction(Callable<Void> {
                bookEndPresenter.requestBookSource(it)
                bookEndPresenter.requestRecommend(it.book_id)
                null
            })
        }

    }


    private fun initBookEndAD() {
        MediaControl.loadBookEndMedia(this) { view, isSuccess ->
            if (isSuccess) {
                rl_ad_view.visibility = View.VISIBLE
                rl_ad_view.addView(view)
            } else {
                rl_ad_view.visibility = View.GONE
            }
        }
    }

    private fun refreshRecommendBooks() {
        bookEndPresenter.changeRecommendBooks()
        val refresh = HashMap<String, String>()
        refresh.put("module", "1")
        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOKENDPAGE_PAGE,
                StartLogClickUtil.REPLACE, refresh)
    }

    private fun refreshNewBooks() {
        bookEndPresenter.changeRecommendBooks()

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
        if (sourceList.isNotEmpty()) {
            this.sourceList.clear()
            this.sourceList.addAll(sourceList)
        }
    }

}