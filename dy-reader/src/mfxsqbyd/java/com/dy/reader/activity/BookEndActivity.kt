package com.dy.reader.activity

import android.content.res.Resources
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.ding.basic.bean.Book
import com.ding.basic.bean.RecommendBean
import com.ding.basic.bean.RecommendBooksEndResp
import com.ding.basic.bean.Source

import com.dy.media.MediaControl
import com.dy.media.MediaLifecycle
import com.dy.reader.R
import com.dy.reader.R.id.*
import com.dy.reader.adapter.SourceAdapter
import com.dy.reader.listener.SourceClickListener
import com.dy.reader.presenter.BookEndContract
import com.dy.reader.presenter.BookEndPresenter
import com.dy.reader.setting.ReaderStatus

import kotlinx.android.synthetic.mfxsqbyd.act_book_end.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity

import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.ui.widget.LoadingPage
import org.antlr.v4.runtime.misc.MurmurHash.finish
import java.util.*
import java.util.concurrent.Callable

@Route(path = RouterConfig.BOOK_END_ACTIVITY)
class BookEndActivity : BaseCacheableActivity(), BookEndContract, SourceClickListener {
    override fun showRecommendV4(one: Boolean, two: Boolean, recommendRes: RecommendBooksEndResp) {
    }

    private var book: Book? = null
    private var book_id: String? = null
    private var book_name: String? = null
    //TODO 打点使用，目前完结页打点缺失，后期需要补充
    private var chapter_id: String? = null

    private var loadingPage: LoadingPage? = null

    private var sourceAdapter: SourceAdapter? = null

    private var bookEndPresenter: BookEndPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_book_end)

        initListener()

        initIntent()

        bookEndPresenter = BookEndPresenter(this, this)
        bookEndPresenter?.uploadLog(book,StartLogClickUtil.ENTER)

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

        txt_prompt.text = Html.fromHtml(resources.getString(R.string.book_end_prompt))

        txt_bookshelf.setOnClickListener {
            if (bookEndPresenter != null) {
                bookEndPresenter?.startBookShelf()
                bookEndPresenter?.uploadLog(book,StartLogClickUtil.TOSHELF)
            }
            finish()
        }

        txt_bookstore.setOnClickListener {
            if (bookEndPresenter != null) {
                bookEndPresenter?.startBookStore()
                bookEndPresenter?.uploadLog(book,StartLogClickUtil.TOBOOKSTORE)
            }
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
        loadingPage = LoadingPage(this, LoadingPage.setting_result)

        if (book != null && bookEndPresenter != null) {
            bookEndPresenter?.requestBookSource(book!!)
        }

        loadingPage?.setReloadAction(Callable<Void> {
            if (bookEndPresenter != null && book != null) {
                bookEndPresenter!!.requestBookSource(book!!)
            }
            null
        })
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

    /***
     * 隐藏LoadingPage
     * **/
    private fun dismissLoading() {
        if (loadingPage != null) {
            loadingPage?.onSuccess()
        }
    }

    override fun onDestroy() {

        if (loadingPage != null) {
            loadingPage = null
        }
        if (bookEndPresenter != null) {
            bookEndPresenter = null
        }
        try {
            setContentView(R.layout.common_empty_view)
        } catch (exception: Resources.NotFoundException) {
            exception.printStackTrace()
        }
        MediaLifecycle.onDestroy()
        super.onDestroy()
    }


    /***
     * 展示来源信息
     * **/
    override fun showSourceList(sourceList: ArrayList<Source>) {
        if (sourceList.isNotEmpty()) {
            rl_source.visibility = View.VISIBLE

            sourceAdapter = SourceAdapter(sourceList, this)

            val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

            rl_source.adapter = sourceAdapter

            rl_source.layoutManager = linearLayoutManager

            rl_source.layoutParams.height = sourceList.size * resources.getDimensionPixelOffset(R.dimen.source_item_height)
        } else {
            rl_source.visibility = View.GONE
        }

        dismissLoading()
    }

    override fun showRecommend(recommends: ArrayList<RecommendBean>?) {

    }

    override fun clickedSource(source: Source) {
        if (bookEndPresenter != null) {
            bookEndPresenter?.clickedBookSource(source)
        }
    }
}