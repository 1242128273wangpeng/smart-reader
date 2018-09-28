package com.intelligent.reader.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.AbsListView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI.activity
import com.baidu.mobstat.StatService
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.Book
import com.ding.basic.bean.Bookmark
import com.ding.basic.bean.Chapter
import com.intelligent.reader.R
import com.intelligent.reader.adapter.CataloguesAdapter
import com.intelligent.reader.view.TransformReadDialog
import kotlinx.android.synthetic.mfqbxssc.act_catalog.*
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.bean.OfflineDownloadEvent
import net.lzbook.kit.view.CataloguesContract
import net.lzbook.kit.presenter.CataloguesPresenter
import net.lzbook.kit.receiver.OffLineDownLoadReceiver
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.book.RepairHelp
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.utils.download.CallBackDownload
import net.lzbook.kit.utils.download.DownloadState
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.MessageFormat
import java.util.*
import java.util.concurrent.Callable
import kotlin.collections.ArrayList

/**
 * CataloguesActivity
 * 小说目录
 */
@Route(path = RouterConfig.CATALOGUES_ACTIVITY)
class CataloguesActivity : BaseCacheableActivity(), View.OnClickListener, CataloguesContract, CallBackDownload {

    //是否是最后一页
    private var is_last_chapter: Boolean = false
    //是否来源于封面页
    private var fromCover: Boolean = false
    //是否来源于完结页
    private var fromEnd: Boolean = false
    //加载页
    private var loadingPage: LoadingPage? = null
    private var sequence: Int = 0
    //小说
    private var book: Book? = null
    //小说帮助类
    private var chapterList: ArrayList<Chapter> = ArrayList()

    private var isPositive = true

    private lateinit var cataloguesAdapter: CataloguesAdapter

    //是否换源
    private var changeSource: Boolean = false

    private var downLoadReceiver: OffLineDownLoadReceiver? = null

    private var cataloguesPresenter: CataloguesPresenter? = null

    private var transformReadDialog: TransformReadDialog?=null

    private var bookDownloadState: DownloadState = DownloadState.NOSTART

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.act_catalog)

        initUI()

        val bundle = intent.extras ?: return
        initData(bundle)

        initCatalog()

        if (fromEnd) {
            isPositive = false
            changeSortState(isPositive)
        }
        EventBus.getDefault().register(this)
    }

    private fun initUI() {
        recl_catalog_content.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        reclfs_catalog_scroll.setRecyclerView(recl_catalog_content)
        reclfs_catalog_scroll.setViewsToUse(R.layout.catalog_recyclerview_fast_scroller, R.id.img_recycler_view_scroller)

        img_catalog_back.setOnClickListener(this)
        img_catalog_sort.setOnClickListener(this)

        txt_catalog_shelf.setOnClickListener(this)
        txt_catalog_read.setOnClickListener(this)
        txt_catalog_cache.setOnClickListener(this)

        char_hint!!.visibility = View.INVISIBLE

        iv_fixbook.setOnClickListener {
            cataloguesPresenter?.fixBook()
        }

        changeSortState(isPositive)
    }

    private var scrollState: Int = 0

    private fun initData(bundle: Bundle) {

        cataloguesAdapter = CataloguesAdapter()

        recl_catalog_content.adapter = cataloguesAdapter

        recl_catalog_content.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {

                scrollState = newState
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    if (cataloguesPresenter != null) {
                        cataloguesPresenter!!.delayOverLayHandler()
                    }
                } else {
                    if (char_hint != null) {
                        char_hint!!.visibility = View.VISIBLE
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (!chapterList.isEmpty()) {
                    val manager = recl_catalog_content.layoutManager
                    if (manager is LinearLayoutManager) {

                        char_hint!!.text = String.format(getString(R.string.chapter_sort), chapterList[manager.findFirstVisibleItemPosition()].sequence + 1)
                    }

                }
            }
        })

        cataloguesAdapter.insertChapterItemClickListener(object : CataloguesAdapter.ChapterItemClickListener {
            override fun clickedChapter(position: Int, chapter: Chapter) {
                if (cataloguesPresenter != null) {
                    cataloguesPresenter?.catalogToReading(position, true)
                }
            }
        })

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

        if (book != null) {
            catalog_novel_name!!.text = book!!.name

            if (RepairHelp.isShowFixBtn(this, book!!.book_id)) {
                iv_fixbook!!.visibility = View.VISIBLE
            } else {
                iv_fixbook!!.visibility = View.GONE
            }
        }

        if (book != null) {
            cataloguesPresenter = CataloguesPresenter(this, book!!, this, this, fromCover)
            transformReadDialog=TransformReadDialog(this)

            transformReadDialog?.insertContinueListener {
                val data = HashMap<String, String>()
                data["type"] = "1"

                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEPOPUP, data)

                intoReadingActivity()

                if (!this.isFinishing) {
                    transformReadDialog?.dismiss()
                }
            }

            transformReadDialog?.insertCancelListener {
                val data = HashMap<String, String>()
                data["type"] = "2"

                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEPOPUP, data)

                if (!this.isFinishing) {
                    transformReadDialog?.dismiss()
                }
            }
        }

        cataloguesPresenter?.registerRec()

        getChapterData()

        if (cataloguesPresenter != null) {
            cataloguesPresenter!!.loadBookMark()
        }
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

        RouterUtil.navigation(activity, RouterConfig.READER_ACTIVITY, bundle, flags)
    }

    override fun showReadDialog(){
        if (!this.isFinishing) {
            if (!transformReadDialog!!.isShow()) {
                transformReadDialog!!.show()
            }
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOKCATALOG, StartLogClickUtil.CATALOG_TRANSCODEREAD)
        }
    }

    private fun getChapterData() {
        if (book != null) {

            if (loadingPage != null) {
                loadingPage?.onSuccess()
            }

            loadingPage = LoadingPage(this, LoadingPage.setting_result)

            if (!fromCover) {
                loadingPage?.setCustomBackgroud()
            }

            if (cataloguesPresenter != null) {
                cataloguesPresenter?.requestCatalogList(changeSource)
            }

            if (loadingPage != null) {
                loadingPage?.isCategory = true

                loadingPage?.setReloadAction(Callable<Void> {
                    if (cataloguesPresenter != null) {
                        cataloguesPresenter?.requestCatalogList(changeSource)
                    }
                    null
                })
            }
        }
    }

    private fun dataError() {
        if (loadingPage != null) {
            loadingPage?.onError()
        }
    }

    override fun onResume() {
        super.onResume()
        StatService.onResume(this)

        if (cataloguesPresenter != null) {
            cataloguesPresenter?.refreshNavigationState()
        }
        CacheManager.listeners.add(this)
    }

    override fun onPause() {
        super.onPause()
        StatService.onPause(this)

        CacheManager.listeners.remove(this)
    }

    private fun initCatalog() {
        cataloguesAdapter.insertCatalog(chapterList)

        if (is_last_chapter) {
            cataloguesAdapter.setSelectedItem(chapterList.size - 1)
            recl_catalog_content.scrollToPosition(chapterList.size - 1)
        } else {
            cataloguesAdapter.setSelectedItem(sequence)
            recl_catalog_content.scrollToPosition(sequence)
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
            cataloguesPresenter?.removeHandler()
            cataloguesPresenter?.unRegisterRec()
        }
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.img_catalog_back -> {
                val data = HashMap<String, String>()
                data["type"] = "1"
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.CATALOG, StartLogClickUtil.BACK, data)
                if (!fromCover) {
                    if (cataloguesPresenter != null) {
                        sequence = Math.min(sequence, chapterList.size - 1)
                        cataloguesPresenter?.activityResult(sequence)
                    }
                }
                finish()
            }

            R.id.img_catalog_sort ->
                if (chapterList.isNotEmpty()) {

                    //书签点击的统计
                    isPositive = !isPositive
                    chapterList.reverse()

                    cataloguesAdapter.insertCatalog(chapterList)
                    cataloguesAdapter.notifyDataSetChanged()

                    changeSortState(isPositive)

                    recl_catalog_content.scrollToPosition(0)
                }
            R.id.img_fix_book -> if (cataloguesPresenter != null) {
                cataloguesPresenter?.fixBook()
            }

            R.id.txt_catalog_shelf -> {
                cataloguesPresenter?.handleBookShelfAction(false)
            }

            R.id.txt_catalog_read -> {
                //转码阅读点击的统计
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.CATALOG, StartLogClickUtil.TRANSCODEREAD)
                if (cataloguesPresenter != null) {
                    cataloguesPresenter?.handleReadingAction()
                }
            }

            R.id.txt_catalog_cache -> {
                if (book != null && !TextUtils.isEmpty(book?.book_id)) {
                    book?.book_id.let {
                        val dataDownload = HashMap<String, String>()
                        dataDownload["bookId"] = book?.book_id!!

                        if (cataloguesPresenter != null) {
                            requestBookDownloadState(book?.book_id)

                            if (bookDownloadState == DownloadState.DOWNLOADING) {
                                CacheManager.stop(book?.book_id!!)
                            } else {
                                cataloguesPresenter?.handleDownloadAction()
                                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.CATALOG, StartLogClickUtil.CASHEALL, dataDownload)
                            }
                        }
                    }
                }
            }
            else -> {

            }
        }
    }

    private fun changeSortState(negative: Boolean) {
        if (negative) {
            img_catalog_sort.setImageResource(R.drawable.icon_catalog_daoxu)
            StatServiceUtils.statAppBtnClick(this, StatServiceUtils.rb_catalog_click_zx_btn)
        } else {
            img_catalog_sort.setImageResource(R.drawable.icon_catalog_zhengxu)
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

    override fun requestCatalogSuccess(chapterList: ArrayList<Chapter>) {
        this.chapterList = chapterList

        if (loadingPage != null) {
            loadingPage!!.onSuccess()
        }

        catalog_chapter_count?.text = MessageFormat.format("共{0}章", chapterList.size)

        if (fromEnd) {
            isPositive = false
            chapterList.reverse()
        }

        cataloguesAdapter.insertCatalog(chapterList)

        //设置选中的条目
        val position = if (is_last_chapter) {
            chapterList.size
        } else {
            sequence
        }

        if (fromEnd) {
            recl_catalog_content.scrollToPosition(0)
        }
        cataloguesAdapter.setSelectedItem(position)
    }

    override fun requestCatalogError() {
        dataError()
    }

    override fun handOverLay() {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && char_hint != null) {
            char_hint!!.visibility = View.INVISIBLE
        }
    }

    override fun deleteBookmarks(deleteList: ArrayList<Int>) {

    }

    override fun notifyDataChange(isCatalog: Boolean, bookmarkList: ArrayList<Bookmark>) {

    }

    override fun successAddIntoShelf(isAddIntoShelf: Boolean) {

    }

    override fun changeDownloadButtonStatus() {
        if (book == null) {
            return
        }

        val status = CacheManager.getBookStatus(book!!)

        bookDownloadState = status

        cataloguesPresenter?.let {
            when (status) {
                DownloadState.FINISH -> txt_catalog_cache.text = "缓存完成"
                DownloadState.PAUSEED -> txt_catalog_cache.text = "缓存已暂停"
                DownloadState.NOSTART -> txt_catalog_cache.text = "全本缓存"
                DownloadState.DOWNLOADING -> txt_catalog_cache.text = "缓存中"
                else -> {

                }
            }
        }

        if (!cataloguesPresenter!!.checkBookSubscribe()) {
            txt_catalog_cache.text = "全本缓存"
        }
    }

    override fun insertBookShelfResult(result: Boolean) {
        if (result) {
            txt_catalog_shelf?.text = "已在书架"
            txt_catalog_shelf.setTextColor(Color.parseColor("#b5b5b5"))
        } else {
            txt_catalog_shelf.setTextColor(Color.parseColor("#252B35"))
            txt_catalog_shelf?.text = "加入书架"
        }
    }

    override fun changeShelfButtonClickable(clickable: Boolean) {
        if (txt_catalog_shelf != null) {
            txt_catalog_shelf.isClickable = clickable
        }
    }

    override fun bookSubscribeState(subscribe: Boolean) {
        if (subscribe) {
            txt_catalog_shelf?.text = "已在书架"
            txt_catalog_shelf.setTextColor(Color.parseColor("#b5b5b5"))
        }
    }

    /***
     * 获取下载状态
     * **/
    private fun requestBookDownloadState(book_id: String?) {
        if (!TextUtils.isEmpty(book_id)) {

            txt_catalog_cache.visibility = View.VISIBLE

            book_id?.let {
                val book = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(it)

                if (book != null) {

                    val downloadState = CacheManager.getBookStatus(book)

                    bookDownloadState = downloadState

                    when (downloadState) {
                        DownloadState.FINISH -> txt_catalog_cache.text = "缓存完成"
                        DownloadState.PAUSEED -> txt_catalog_cache.text = "缓存已暂停"
                        DownloadState.NOSTART -> txt_catalog_cache.text = "全本缓存"
                        DownloadState.DOWNLOADING -> txt_catalog_cache.text = "缓存中"
                        else -> {

                        }
                    }
                } else {
                    txt_catalog_cache.text = "全本缓存"
                }
            }
        } else {
            txt_catalog_cache.visibility = View.GONE
        }
    }

    override fun onTaskStatusChange(book_id: String?) {
        requestBookDownloadState(book_id)
    }

    override fun onTaskFinish(book_id: String?) {
        requestBookDownloadState(book_id)
    }

    override fun onTaskFailed(book_id: String?, t: Throwable?) {
    }

    override fun onTaskProgressUpdate(book_id: String?) {
    }

    override fun supportSlideBack(): Boolean {
        return !isTaskRoot
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun notifyChangeDownLoad(event: OfflineDownloadEvent){

    }
}