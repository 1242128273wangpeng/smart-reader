package com.intelligent.reader.activity

import com.dingyueads.sdk.NativeInit
import com.intelligent.reader.R
import com.intelligent.reader.adapter.SourceAdapter
import com.intelligent.reader.presenter.bookEnd.BookEndContract
import com.intelligent.reader.presenter.bookEnd.BookEndPresenter

import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.EventNativeType
import net.lzbook.kit.data.bean.ReadStatus
import net.lzbook.kit.data.bean.RequestItem
import net.lzbook.kit.data.bean.Source
import net.lzbook.kit.utils.ResourceUtil
import net.lzbook.kit.utils.StatisticManager

import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

import java.util.ArrayList
import java.util.concurrent.Callable

import de.greenrobot.event.EventBus
import kotlinx.android.synthetic.kdqbdzs.act_book_end.*

class BookEndActivity : BaseCacheableActivity(), View.OnClickListener, BookEndContract {
    private var book: Book? = null
    private var bookName: String? = null
    private var loadingPage: LoadingPage? = null

    private var requestItem: RequestItem? = null
    private var category: String? = null
    private var book_id: String? = null
    private var readStatus: ReadStatus? = null

    private var sourceAdapter: SourceAdapter? = null
    private var mBookEndPresenter: BookEndPresenter? = null
    private var thememode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_book_end)
        EventBus.getDefault().register(this)
        initListener()
        readStatus = ReadStatus(applicationContext)
        initData()
        if (requestItem != null && readStatus != null) {
            mBookEndPresenter = BookEndPresenter(this, this, requestItem!!, readStatus!!, bookName!!, book_id!!, category!!)
        }
        loadSource()

    }

    private fun loadSource() {

        if (!Constants.isHideAD) {
            if (mBookEndPresenter == null) {
                mBookEndPresenter = BookEndPresenter(this, this, requestItem!!, readStatus!!, bookName!!, book_id!!, category!!)
            }
            mBookEndPresenter!!.initAD()
        }
        loadingPage = LoadingPage(this, LoadingPage.setting_result)
        if (mBookEndPresenter != null) {
            mBookEndPresenter!!.getBookSource()
        }
        loadingPage!!.setReloadAction(Callable<Void> {
            if (mBookEndPresenter != null) {
                mBookEndPresenter!!.getBookSource()
                mBookEndPresenter!!.setADItem()
            }
            null
        })
    }


    private fun initListener() {
        iv_title_right!!.setOnClickListener(this)
        iv_back!!.setOnClickListener(this)
        iv_back_bookstore!!.setOnClickListener(this)
        ad_view!!.setOnClickListener(this)
        ad_view_logo!!.visibility = View.GONE
        textView_endInfo!!.text = Html.fromHtml(resources.getString(R.string.book_end_info))

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


    fun onEvent(eventNativeType: EventNativeType) {
        if (NativeInit.CustomPositionName.BOOK_END_POSITION.toString() == eventNativeType.type_ad) {
            if (!isFinishing) {
                if (mBookEndPresenter != null) {
                    mBookEndPresenter!!.setADItem()
                }
            }
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
            R.id.ad_view -> {
                if (mBookEndPresenter != null) {
                    mBookEndPresenter!!.adSchedulingRequest(v, StatisticManager.TYPE_CLICK, false)
                }
                if (Constants.DEVELOPER_MODE) {
                    Toast.makeText(this, "你点击了广告", Toast.LENGTH_SHORT).show()
                }
            }
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
            sourcelist_bookend!!.visibility = View.VISIBLE
            sourceAdapter = SourceAdapter(this, sourceList)
            sourcelist_bookend!!.adapter = sourceAdapter
            sourcelist_bookend!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                val source = sourceList[position]
                if (mBookEndPresenter != null) {
                    mBookEndPresenter!!.itemClick(source)
                }
            }
            sourcelist_bookend!!.layoutParams.height = sourceList.size * resources.getDimensionPixelOffset(R
                    .dimen.dimen_view_height_70)
        } else {
            sourcelist_bookend!!.visibility = View.GONE
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
        if ("广点通" == rationName) {
            ad_view_logo!!.setImageResource(R.drawable.icon_ad_gdt)
        } else if ("百度" == rationName) {
            ad_view_logo!!.setImageResource(R.drawable.icon_ad_bd)
        } else if ("360" == rationName) {
            ad_view_logo!!.setImageResource(R.drawable.icon_ad_360)
        } else {
            ad_view_logo!!.setImageResource(R.drawable.icon_ad_default)
        }

    }

    override fun showAdImgSuccess(bitmap: Bitmap) {
        ad_view!!.visibility = View.VISIBLE
        ad_view!!.setImageBitmap(bitmap)
        if ("night" == ResourceUtil.mode) {
            ad_view!!.setAlpha(80)
        }

        ad_view_logo!!.visibility = View.VISIBLE

        if (mBookEndPresenter != null) {
            mBookEndPresenter!!.adSchedulingRequest(ad_view!!, StatisticManager.TYPE_SHOW, false)
        }
    }


    override fun showAdImgError() {
        ad_view!!.visibility = View.GONE
        if (mBookEndPresenter != null) {
            mBookEndPresenter!!.adSchedulingRequest(ad_view!!, StatisticManager.TYPE_SHOW, false)
        }
    }

    /*****************************以上广告相关 */
    override fun onDestroy() {

        if (ad_view != null && mBookEndPresenter != null) {
            mBookEndPresenter!!.adSchedulingRequest(ad_view!!, StatisticManager.TYPE_END, true)
        }

        try {
            EventBus.getDefault().unregister(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

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

}