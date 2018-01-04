package com.intelligent.reader.activity

import com.intelligent.reader.R
import com.intelligent.reader.adapter.SourceAdapter
import com.intelligent.reader.presenter.bookEnd.BookEndContract
import com.intelligent.reader.presenter.bookEnd.BookEndPresenter

import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.ReadStatus
import net.lzbook.kit.data.bean.RequestItem
import net.lzbook.kit.data.bean.Source

import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.dycm_adsdk.PlatformSDK
import com.dycm_adsdk.callback.AbstractCallback
import com.dycm_adsdk.callback.ResultCode
import org.json.JSONException
import org.json.JSONObject

import java.util.ArrayList
import java.util.concurrent.Callable

class BookEndActivity : BaseCacheableActivity(), View.OnClickListener, BookEndContract {
    private var iv_back_bookstore: View? = null
    private var iv_back: View? = null
    private var iv_title_right: View? = null
//    private var ad_view: ImageView? = null
//    private var ad_view_logo: ImageView? = null
    private var textView_endInfo: TextView? = null
    private var book: Book? = null
    private var bookName: String? = null
    private var loadingPage: LoadingPage? = null
    private var name_bookend: TextView? = null

    private var requestItem: RequestItem? = null
    private var category: String? = null
    private var book_id: String? = null
    private var readStatus: ReadStatus? = null

    private var sourceAdapter: SourceAdapter? = null
    private var sourceListView: ListView? = null
    private var mBookEndPresenter: BookEndPresenter? = null
    private var thememode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_book_end)
        initListener()
        readStatus = ReadStatus(applicationContext)
        initData()
        if (requestItem != null && readStatus != null) {
            mBookEndPresenter = BookEndPresenter(this, this, requestItem!!, readStatus!!, bookName!!, book_id!!, category!!)
        }
        loadSource()
        initAD()
    }

    private fun initAD() {
        val adview = findViewById(R.id.ad_view) as RelativeLayout
        PlatformSDK.adapp().dycmNativeAd(this, "9-1",null, object : AbstractCallback() {
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
                                adview.addView(views[0])
                            }
                            else -> {
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun loadSource() {

        if (!Constants.isHideAD) {
            if (mBookEndPresenter == null) {
                mBookEndPresenter = BookEndPresenter(this, this, requestItem!!, readStatus!!, bookName!!, book_id!!, category!!)
            }
        }
        loadingPage = LoadingPage(this, LoadingPage.setting_result)
        if (mBookEndPresenter != null) {
            mBookEndPresenter!!.getBookSource()
        }
        loadingPage!!.setReloadAction(Callable<Void> {
            if (mBookEndPresenter != null) {
                mBookEndPresenter!!.getBookSource()
            }
            null
        })
    }


    private fun initListener() {
        sourceListView = findViewById(R.id.sourcelist_bookend) as ListView
        iv_title_right = findViewById(R.id.iv_title_right)
        iv_back = findViewById(R.id.iv_back)
        iv_back_bookstore = findViewById(R.id.iv_back_bookstore)
//        ad_view = findViewById(R.id.ad_view) as ImageView
//        ad_view_logo = findViewById(R.id.ad_view_logo) as ImageView
//        ad_view_logo!!.visibility = View.GONE
        textView_endInfo = findViewById(R.id.textView_endInfo) as TextView
        textView_endInfo!!.text = Html.fromHtml(resources.getString(R.string.book_end_info))
        name_bookend = findViewById(R.id.name_bookend) as TextView

        iv_title_right!!.setOnClickListener(this)
        iv_back!!.setOnClickListener(this)
        iv_back_bookstore!!.setOnClickListener(this)
//        ad_view!!.setOnClickListener(this)


    }

    private fun initData() {
        if (intent != null) {
            bookName = intent.getStringExtra("bookName")
            book = intent.getSerializableExtra("book") as Book
            requestItem = intent.getSerializableExtra(Constants.REQUEST_ITEM) as RequestItem
            category = intent.getStringExtra("book_category")
            book_id = intent.getStringExtra("book_id")
            name_bookend!!.text = bookName
            readStatus!!.sequence = intent.getIntExtra("sequence", 0)
            readStatus!!.offset = intent.getIntExtra("offset", 0)
            thememode = intent.getStringExtra("thememode")
            if (requestItem != null) {
                readStatus!!.requestItem = requestItem
            }
            readStatus!!.book = book
            readStatus!!.book_id = book_id
        }
        if (requestItem == null) {
            finish()
        }
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
            R.id.iv_back -> finish()
//            R.id.ad_view -> {
//                if (Constants.DEVELOPER_MODE) {
//                    Toast.makeText(this@BookEndActivity, "你点击了广告", Toast.LENGTH_SHORT).show()
//                }
//            }
        }
    }

    override fun onResume() {
        super.onResume()
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

    /*****************************以下广告相关 */
    override fun showAdViewLogo(rationName: String) {
//        if ("广点通" == rationName) {
//            ad_view_logo!!.setImageResource(R.drawable.icon_ad_gdt)
//        } else if ("百度" == rationName) {
//            ad_view_logo!!.setImageResource(R.drawable.icon_ad_bd)
//        } else if ("360" == rationName) {
//            ad_view_logo!!.setImageResource(R.drawable.icon_ad_360)
//        } else {
//            ad_view_logo!!.setImageResource(R.drawable.icon_ad_default)
//        }

    }

    override fun showAdImgSuccess(bitmap: Bitmap) {
//        ad_view!!.visibility = View.VISIBLE
//        ad_view!!.setImageBitmap(bitmap)
//        if ("night" == ResourceUtil.mode) {
//            ad_view!!.setAlpha(80)
//        }

//        ad_view_logo!!.visibility = View.VISIBLE

    }


    override fun showAdImgError() {
//        ad_view!!.visibility = View.GONE
    }

    /*****************************以上广告相关 */
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

        super.onDestroy()
    }

    companion object {
        private val TAG = "BookEndActivity"
    }

}