package com.intelligent.reader.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.Menu
import android.view.View
import android.view.View.OnClickListener
import android.widget.AbsListView
import android.widget.AbsListView.OnScrollListener
import com.alibaba.android.arouter.facade.annotation.Route
import com.baidu.mobstat.StatService
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.Book
import com.ding.basic.bean.Bookmark
import com.ding.basic.bean.Chapter
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.dingyue.statistics.DyStatService
import com.intelligent.reader.R
import com.intelligent.reader.adapter.CataloguesAdapter
import com.intelligent.reader.view.TransformReadDialog
import kotlinx.android.synthetic.txtqbmfyd.act_catalog.*
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.bean.EventBookmark
import net.lzbook.kit.bean.OfflineDownloadEvent
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.view.CataloguesContract
import net.lzbook.kit.presenter.CataloguesPresenter
import net.lzbook.kit.receiver.OffLineDownLoadReceiver
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.book.RepairHelp
import net.lzbook.kit.utils.logger.AppLog
import net.lzbook.kit.utils.router.RouterUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.concurrent.Callable

/**
 * CataloguesActivity
 * 小说目录
 */

@Route(path = RouterConfig.CATALOGUES_ACTIVITY)
class CataloguesActivity : BaseCacheableActivity(), OnClickListener,
        OnScrollListener, CataloguesContract {

    private var colorSelected: Int = 0
    private var colorNormal: Int = 0
    private var sortIcon = 0//背景色

    //是否是最后一页
    private var is_last_chapter: Boolean = false
    //是否来源于封面页
    private var fromCover: Boolean = false
    //是否来源于完结页
    private var fromEnd: Boolean = false
    //加载页
    private val loadingPage: LoadingPage by lazy {
        LoadingPage(this, LoadingPage.setting_result)
    }
    private var sequence: Int = 0
    //小说
    private var book: Book? = null
    //小说帮助类
    private var chapterList: ArrayList<Chapter> = ArrayList()
    private var isPositive = true

    private val cataloguesAdapter: CataloguesAdapter by lazy {
        val adapter = CataloguesAdapter(applicationContext)
        adapter.setOnChapterItemClickListener { position, _ ->
            cataloguesPresenter?.catalogToReading(position, true)
        }
        adapter
    }

    //是否换源
    private var changeSource: Boolean = false

    /**
     * 标识List的滚动状态。
     */
    private var scrollState: Int = 0
    private var downLoadReceiver: OffLineDownLoadReceiver? = null
    private var cataloguesPresenter: CataloguesPresenter? = null
    private var transformReadDialog: TransformReadDialog?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_catalog)

        colorSelected = resources.getColor(R.color.theme_primary_ffffff)
        colorNormal = resources.getColor(R.color.theme_primary)
        initUI()
        initListener()

        val bundle = intent.extras ?: return
        initData(bundle)

        if (fromEnd) {
            isPositive = false
            changeSortState(isPositive)
        }
        EventBus.getDefault().register(this)
    }

    private fun initUI() {
        catalog_recyceler_main.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        catalog_fastscroller.setRecyclerView(catalog_recyceler_main)
        catalog_fastscroller.setViewsToUse(R.layout.catalog_recyclerview_fast_scroller, R.id.img_recycler_view_scroller)

        backIv.setOnClickListener(this)
        tv_catalog_novel_sort.setOnClickListener(this)
        changeSortState(isPositive)
    }

    private fun initListener() {
        catalog_empty_refresh.setOnClickListener(this)
    }

    private fun initData(bundle: Bundle) {

        catalog_recyceler_main.adapter = cataloguesAdapter

        if (bundle.containsKey("cover")) {
            book = bundle.getSerializable("cover") as Book
        }

        if (book == null || TextUtils.isEmpty(book?.book_id)) {
            exitAndUpdate()
            return
        }

        sequence = Math.max(bundle.getInt("sequence"), 0)
        is_last_chapter = bundle.getBoolean("is_last_chapter", false)
        fromCover = bundle.getBoolean("fromCover", true)
        fromEnd = bundle.getBoolean("fromEnd", false)
        changeSource = bundle.getBoolean("changeSource", false)

        book?.let {
            catalog_novel_name.text = it.name
            if (RepairHelp.isShowFixBtn(this, it.book_id)) {
                iv_fixbook.visibility = View.VISIBLE
            } else {
                iv_fixbook.visibility = View.GONE
            }
            cataloguesPresenter = CataloguesPresenter(this, it, this, this, fromCover)

            transformReadDialog=TransformReadDialog(this)

            transformReadDialog?.insertContinueListener {
                DyStatService.onEvent(EventPoint.BOOOKDETAIL_TRANSCODEPOPUP, mapOf("type" to "1"))

                intoReadingActivity()

                if (!this.isFinishing) {
                    transformReadDialog?.dismiss()
                }
            }

            transformReadDialog?.insertCancelListener {
                DyStatService.onEvent(EventPoint.BOOOKDETAIL_TRANSCODEPOPUP, mapOf("type" to "2"))

                if (!this.isFinishing) {
                    transformReadDialog?.dismiss()
                }
            }
        }

        getChapterData()
        cataloguesPresenter?.loadBookMark()
    }

    /***
     * 进入阅读页
     * **/
    private fun intoReadingActivity() {
        if (TextUtils.isEmpty(book!!.book_id)) {
            return
        }

        val bundle = Bundle()

        val flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val localBook = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(book!!.book_id)

        if (localBook != null) {
            if (book!!.sequence != -2) {
                bundle.putInt("sequence", localBook.sequence)
                bundle.putInt("offset", localBook.offset)
            } else {
                bundle.putInt("sequence", -1)
                bundle.putInt("offset", 0)
            }

            bundle.putSerializable("book", localBook)
        } else {
            bundle.putSerializable("book", book)
        }

        RouterUtil.navigation(this, RouterConfig.READER_ACTIVITY, bundle, flags)
    }

    override fun showReadDialog(){
        if (!this.isFinishing) {
            if (!transformReadDialog!!.isShow()) {
                val isChecked = SPUtils.getDefaultSharedBoolean(SPKey.NOT_SHOW_NEXT_TIME, false)
                if (isChecked) {
                    intoReadingActivity()
                } else {
                    transformReadDialog?.show()
                }
            }
        }
    }

    private fun getChapterData() {
        if (book != null) {
            cataloguesPresenter?.requestCatalogList(changeSource)

            loadingPage.isCategory = true
            loadingPage.setReloadAction(Callable<Void> {
                cataloguesPresenter?.requestCatalogList(changeSource)
                null
            })
        }
    }


    private fun dataError() {
        loadingPage.onError()
    }

    fun onEvent(eventBookmark: EventBookmark) {
        if (eventBookmark.type == EventBookmark.type_delete) {
            AppLog.e(TAG, "eventBookmark:" + eventBookmark.bookmark.id + " name:" + eventBookmark.bookmark.chapter_name)
            val bookmark = eventBookmark.bookmark
            cataloguesPresenter?.onEventReceive(bookmark)
        }
    }


    override fun onResume() {
        super.onResume()
        if (isModeChange) {
            setMode()
        }
        StatService.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        StatService.onPause(this)
    }

    private fun initCatalogAndBookmark() {

        cataloguesAdapter.setData(chapterList)

        if (is_last_chapter) {
            cataloguesAdapter.setSelectedItem(chapterList.size - 1)
            catalog_recyceler_main.scrollToPosition(chapterList.size - 1)
        } else {
            cataloguesAdapter.setSelectedItem(sequence)
            catalog_recyceler_main.scrollToPosition(sequence)
        }
    }

    override fun onStart() {
        super.onStart()
        if (downLoadReceiver == null) {
            downLoadReceiver = OffLineDownLoadReceiver(this)
        }
        downLoadReceiver!!.registerAction()
    }

    override fun onDestroy() {
        if (downLoadReceiver != null) {
            try {
                unregisterReceiver(downLoadReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }

        }
        chapterList.clear()

        if (cataloguesPresenter != null) {
            cataloguesPresenter!!.removeHandler()
            cataloguesPresenter!!.unRegisterRec()
        }
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun notifyChangeDownLoad(event: OfflineDownloadEvent) {
        cataloguesAdapter.notifyDataSetChanged()
    }

    // 目录
    override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
        if (chapterList.isNotEmpty()) {
//            char_hint.text = String.format(getString(R.string.chapter_sort), chapterList!![firstVisibleItem].sequence + 1)
        }
    }

    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
        this.scrollState = scrollState
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE || scrollState == OnScrollListener.SCROLL_STATE_FLING) {
            if (cataloguesPresenter != null) {
                cataloguesPresenter!!.delayOverLayHandler()
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.backIv -> {
                if (!fromCover) {
                    if (cataloguesPresenter != null) {
                        sequence = Math.min(sequence, (chapterList.size ?: 1) - 1)
                        cataloguesPresenter!!.activityResult(sequence)
                    }
                }
                DyStatService.onEvent(EventPoint.BOOKCATALOG_BACK, mapOf("type" to "1"))
                finish()
            }
            R.id.catalog_empty_refresh -> getChapterData()
            R.id.tv_catalog_novel_sort -> {//正序、逆序
                if (chapterList.isNotEmpty()) {
                    StatServiceUtils.statAppBtnClick(this, StatServiceUtils.rb_catalog_click_book_mark)
                    isPositive = !isPositive

                    if (!is_last_chapter) {
                        Collections.reverse(chapterList)
                    } else {
                        is_last_chapter = false
                    }

                    cataloguesAdapter.setData(chapterList)
                    cataloguesAdapter.notifyDataSetChanged()
                    changeSortState(isPositive)
                    catalog_recyceler_main.scrollToPosition(0)
                }
            }
            R.id.iv_fixbook -> cataloguesPresenter?.fixBook()
            else -> {
            }
        }
    }

    private fun changeSortState(b: Boolean) {
        if (b) {
            tv_catalog_novel_sort.setText(R.string.catalog_negative)
            sortIcon = R.drawable.dir_sort_negative
            //正序的统计
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.rb_catalog_click_zx_btn)

//                iv_catalog_novel_sort!!.setImageResource(sortIcon)

        } else {
            tv_catalog_novel_sort.setText(R.string.catalog_positive)
            sortIcon = R.drawable.dir_sort_positive
//                iv_catalog_novel_sort!!.setImageResource(sortIcon)
            //倒序的统计
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.rb_catalog_click_dx_btn)
        }
    }

    override fun onBackPressed() {
        if (cataloguesPresenter != null) {
            sequence = Math.min(sequence, chapterList.size - 1)
            cataloguesPresenter!!.activityResult(sequence)
        }
        exitAndUpdate()
    }

    private fun exitAndUpdate() {
        //如果是从通知栏过来, 且已经退出到home了, 要回到应用中
        if (isTaskRoot) {
            val intent = Intent(this, SplashActivity::class.java)
            startActivity(intent)
        }
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add("menu")
        return super.onCreateOptionsMenu(menu)
    }

    override fun requestCatalogSuccess(chapterList: ArrayList<Chapter>) {
        this.chapterList = chapterList
        loadingPage.onSuccess()

        initCatalogAndBookmark()

        catalog_chapter_count!!.text = "共" + chapterList.size + "章"

        if (fromEnd) {
            isPositive = false
            Collections.reverse(chapterList)
        }
    }

    override fun requestCatalogError() {
        dataError()
    }

    override fun handOverLay() {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE || scrollState == OnScrollListener.SCROLL_STATE_FLING) {
//            char_hint.visibility = View.INVISIBLE
        }
    }

    override fun deleteBookmarks(deleteList: ArrayList<Int>) {
//        startDeleteBookmarks(tab_bookmark!!, deleteList)
    }

    override fun notifyDataChange(isCatalog: Boolean, bookmarkList: ArrayList<Bookmark>) {

    }


    //目前只有免费小说书城有用到 其他几个壳没有
    override fun changeDownloadButtonStatus() {}

    override fun successAddIntoShelf(isAddIntoShelf: Boolean) {}

    override fun insertBookShelfResult(result: Boolean) {}

    override fun changeShelfButtonClickable(clickable: Boolean) {}

    override fun bookSubscribeState(subscribe: Boolean) {}
}