package com.dy.reader.activity

import android.content.res.Resources
import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.ding.basic.bean.Book
import com.ding.basic.bean.RecommendBean
import com.ding.basic.bean.Source
import com.dy.media.MediaLifecycle
import com.dy.reader.R
import com.dy.reader.adapter.BookRecommendAdapter
import com.dy.reader.dialog.BookEndChangeSourceDialog
import com.dy.reader.listener.SourceClickListener
import com.dy.reader.presenter.BookEndContract
import com.dy.reader.presenter.BookEndPresenter
import com.dy.reader.setting.ReaderStatus
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.qbmfrmxs.act_book_end.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.base.activity.BaseCacheableActivity
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.router.BookRouter
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.widget.LoadingPage
import java.util.*
import java.util.concurrent.Callable
import kotlin.collections.ArrayList

@Route(path = RouterConfig.BOOK_END_ACTIVITY)
class BookEndActivity : BaseCacheableActivity(), BookEndContract, SourceClickListener {

    private var book: Book? = null
    private var book_id: String? = null
    private var book_name: String? = null
    //TODO 打点使用，目前完结页打点缺失，后期需要补充
    private var chapter_id: String? = null

    private var loadingPage: LoadingPage? = null

    private val bookEndPresenter: BookEndPresenter by lazy {
        BookEndPresenter(this, this)
    }

    private val recommends: ArrayList<RecommendBean> = ArrayList()

    private val bookRecommendAdapter: BookRecommendAdapter by lazy {
        BookRecommendAdapter(recommends)
    }

    private val sourceList: ArrayList<Source> = ArrayList()

    private val changeSourceDialog: BookEndChangeSourceDialog by lazy {
        BookEndChangeSourceDialog(this, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_book_end)

        initView()

        initIntent()

        initData()
        bookEndPresenter?.uploadLog(book,StartLogClickUtil.ENTER)
        if (!Constants.isHideAD) {
            initBookEndAD()
        }
    }

    private fun initView() {
        iv_back.setOnClickListener {
            val data = HashMap<String, String>()
            data["type"] = "1"
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOKENDPAGE_PAGE, StartLogClickUtil.BACK, data)
            finish()
        }

//        txt_prompt.text = Html.fromHtml(resources.getString(R.string.book_end_prompt))

        txt_bookshelf.setOnClickListener {
            bookEndPresenter.startBookShelf()
            bookEndPresenter?.uploadLog(book,StartLogClickUtil.TOSHELF)
            finish()
        }

        txt_bookstore.setOnClickListener {
            bookEndPresenter.startBookStore()
            bookEndPresenter?.uploadLog(book,StartLogClickUtil.TOBOOKSTORE)
            finish()
        }

        gv_recommend.adapter = bookRecommendAdapter
        gv_recommend.setOnItemClickListener { _, _, position, _ ->
            val recommendBean = recommends[position]
            val data = HashMap<String, String>()

            if (book_id != null && book_id?.isNotEmpty() == true) {
                data["bookid"] = book_id!!
            }

            if (recommendBean.bookId?.isNotEmpty() == true) {
                data["Tbookid"] = recommendBean.bookId!!
            }
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE,
                    StartLogClickUtil.RECOMMENDEDBOOK, data)
            val book = Book()
            book.book_id = recommendBean.bookId ?: return@setOnItemClickListener
            book.book_source_id = recommendBean.id ?: return@setOnItemClickListener
            book.book_chapter_id = recommendBean.bookChapterId ?: return@setOnItemClickListener
            BookRouter.navigateCover(this, book)
        }

        txt_change_source.setOnClickListener {
            if (sourceList.isEmpty()) {
                ToastUtil.showToastMessage("本书暂无其他来源")
            } else {
                changeSourceDialog.show(sourceList)
            }
        }

        txt_recommend_change.setOnClickListener {
            bookEndPresenter.changeRecommendBooks()
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


    private fun initData() {
        loadingPage = LoadingPage(this, LoadingPage.setting_result)

        book?.let {
            bookEndPresenter.requestBookSource(it)
            loadingPage?.setReloadAction(Callable<Void> {
                bookEndPresenter.requestBookSource(it)
                null
            })
            bookEndPresenter.requestRecommend(it.book_id)
        }

    }


    private fun initBookEndAD() {
//        MediaControl.loadBookEndMedia(this) { view, isSuccess ->
//            if (isSuccess) {
//                rl_book_end_ad.visibility = View.VISIBLE
//                rl_book_end_ad.addView(view)
//            } else {
//                rl_book_end_ad.visibility = View.GONE
//            }
//        }
    }

    /***
     * 隐藏LoadingPage
     * **/
    private fun dismissLoading() {
        loadingPage?.onSuccess()
    }

    override fun onDestroy() {

        if (loadingPage != null) {
            loadingPage = null
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
        this.sourceList.clear()
        this.sourceList.addAll(sourceList)
        dismissLoading()
    }

    override fun showRecommend(recommends: ArrayList<RecommendBean>?) {
        Logger.e("recommends: $recommends")
        if (recommends == null) return

        if (recommends.size == 0) {
            rl_recommend.visibility = View.GONE
            gv_recommend.visibility = View.GONE
        } else {
            rl_recommend.visibility = View.VISIBLE
            gv_recommend.visibility = View.VISIBLE

            this.recommends.clear()
            this.recommends.addAll(recommends)
            bookRecommendAdapter.notifyDataSetChanged()
        }
    }

    override fun clickedSource(source: Source) {
        bookEndPresenter.clickedBookSource(source)
    }
}