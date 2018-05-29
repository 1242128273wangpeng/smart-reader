package com.dy.reader.activity

import android.content.res.Resources
import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.ding.basic.bean.Book
import com.ding.basic.bean.RecommendBooksEndResp
import com.ding.basic.bean.Source
import com.dingyue.contract.router.RouterConfig
import com.dy.reader.R
import com.dy.reader.presenter.BookEndContract
import com.dy.reader.presenter.BookEndPresenter
import com.dy.reader.setting.ReaderStatus
import com.dycm_adsdk.PlatformSDK
import com.dycm_adsdk.callback.AbstractCallback
import com.dycm_adsdk.callback.ResultCode
import iyouqu.theme.BaseCacheableActivity
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.adapter.SourceAdapter
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.constants.Constants
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.Callable

@Route(path = RouterConfig.BOOK_END_ACTIVITY)
class BookEndActivity : BaseCacheableActivity(), View.OnClickListener, BookEndContract {
    private var iv_back_bookstore: View? = null
    private var iv_back: View? = null
    private var iv_title_right: View? = null

    private var textView_endInfo: TextView? = null
    private var book: Book? = null
    private var bookName: String? = null
    private var loadingPage: LoadingPage? = null
    private var name_bookend: TextView? = null

    private var category: String? = null
    private var book_id: String? = null

    private var sourceAdapter: SourceAdapter? = null
    private var sourceListView: ListView? = null
    private var mBookEndPresenter: BookEndPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_book_end)
        initListener()

        initData()

        mBookEndPresenter = BookEndPresenter(this, this, category)

        loadSource()

        if (!Constants.isHideAD) {
            initAD()
        }
    }

    private fun initListener() {
        sourceListView = findViewById(R.id.sourcelist_bookend)
        iv_title_right = findViewById(R.id.iv_title_right)
        iv_back = findViewById(R.id.iv_back)
        iv_back_bookstore = findViewById(R.id.iv_back_bookstore)
        textView_endInfo = findViewById(R.id.textView_endInfo)
        textView_endInfo!!.text = Html.fromHtml(resources.getString(R.string.book_end_info))
        name_bookend = findViewById(R.id.name_bookend)

        iv_title_right!!.setOnClickListener(this)
        iv_back!!.setOnClickListener(this)
        iv_back_bookstore!!.setOnClickListener(this)
    }

    private fun initData() {
        if (intent != null) {
            book = intent.getSerializableExtra("book") as Book
            bookName = intent.getStringExtra("bookName")
            category = intent.getStringExtra("book_category")
            book_id = intent.getStringExtra("book_id")
            name_bookend!!.text = bookName

            ReaderStatus.book = book!!
        }

        if (book == null) {
            finish()
        }
    }


    private fun loadSource() {

        if (!Constants.isHideAD) {
            if (mBookEndPresenter == null) {
                mBookEndPresenter = BookEndPresenter(this, this, category)
            }
        }

        loadingPage = LoadingPage(this, LoadingPage.setting_result)

        if (mBookEndPresenter != null && book != null) {
            mBookEndPresenter!!.getBookSource(book!!)
        }

        loadingPage!!.setReloadAction(Callable<Void> {
            if (mBookEndPresenter != null && book != null) {
                mBookEndPresenter!!.getBookSource(book!!)
            }
            null
        })
    }


    private fun initAD() {
        val adview = findViewById<RelativeLayout>(R.id.ad_view)
        PlatformSDK.adapp().dycmNativeAd(this, "9-1", null, object : AbstractCallback() {
            override fun onResult(adswitch: Boolean, views: List<ViewGroup>, jsonResult: String?) {
                super.onResult(adswitch, views, jsonResult)
                if (!adswitch) {
                    return
                }
                try {
                    val jsonObject = JSONObject(jsonResult)
                    if (jsonObject.has("state_code")) {
                        when (ResultCode.parser(jsonObject.getInt("state_code"))) {
                            ResultCode.AD_REQ_SUCCESS
                            -> {
                                adview.visibility = View.VISIBLE
                                adview.addView(views[0])
                            }
                            else -> {
                                adview.visibility = View.GONE
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }

    override fun onClick(v: View) {
        if (mBookEndPresenter != null) {
            mBookEndPresenter!!.goToBookSearchActivity(v)
        }
        when (v.id) {
            R.id.iv_back_bookstore -> {
                if (mBookEndPresenter != null) {
                    mBookEndPresenter!!.goToBookStore()
                }
                finish()
            }
            R.id.iv_title_right -> {
                if (mBookEndPresenter != null) {
                    mBookEndPresenter!!.goToShelf()
                }
                finish()
            }
            R.id.iv_back -> {
                val data = HashMap<String, String>()
                data.put("type", "1")
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOKENDPAGE_PAGE, StartLogClickUtil.BACK, data)
                finish()
            }
        }
    }

    /**
     * 展示换源
     */
    override fun showSource(hasSource: Boolean, sourceList: ArrayList<Source>) {
        if (hasSource) {
            sourceListView!!.visibility = View.VISIBLE
            sourceAdapter = SourceAdapter(this, sourceList)
            sourceListView!!.adapter = sourceAdapter
            sourceListView!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                val source = sourceList[position]
                if (mBookEndPresenter != null) {
                    mBookEndPresenter!!.itemClick(source)
                }
            }
            sourceListView!!.layoutParams.height = sourceList.size * resources.getDimensionPixelOffset(R
                    .dimen.dimen_view_height_70)
        } else {
            sourceListView!!.visibility = View.GONE
        }
        disLoading()
    }

    fun disLoading() {
        if (loadingPage != null) {
            loadingPage!!.onSuccess()
        }
    }


    override fun onDestroy() {

        if (loadingPage != null) {
            loadingPage = null
        }
        if (mBookEndPresenter != null) {
            mBookEndPresenter = null
        }
        try {
            setContentView(R.layout.empty)
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }
        PlatformSDK.lifecycle()?.onDestroy()
        super.onDestroy()
    }

    override fun showRecommend(one: Boolean, two: Boolean, recommendRes: RecommendBooksEndResp) {

    }
}