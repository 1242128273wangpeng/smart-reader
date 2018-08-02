package com.intelligent.reader.activity

import com.baidu.mobstat.StatService
import com.intelligent.reader.R
import com.intelligent.reader.adapter.CatalogAdapter
import com.intelligent.reader.presenter.catalogues.CataloguesContract
import com.intelligent.reader.presenter.catalogues.CataloguesPresenter
import com.intelligent.reader.receiver.OffLineDownLoadReceiver

import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.Bookmark
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.EventBookmark
import net.lzbook.kit.data.bean.RequestItem
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.repair_books.RepairHelp
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.StatServiceUtils

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.View.OnClickListener
import android.widget.AbsListView
import android.widget.AbsListView.OnScrollListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener

import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.concurrent.Callable

import de.greenrobot.event.EventBus
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.mfqbxssc.act_catalog.*
import net.lzbook.kit.book.download.CacheManager

/**
 * CataloguesActivity
 * 小说目录
 */
class CataloguesActivity : BaseCacheableActivity(), OnClickListener, OnScrollListener, OnItemClickListener, CataloguesContract {
    var type = 2
    internal var colorSelected: Int = 0
    internal var colorNormal: Int = 0
    internal var sortIcon = 0//背景色
    //是否是最后一页
    private var is_last_chapter: Boolean = false
    //是否来源于封面页
    private var fromCover: Boolean = false
    //是否来源于完结页
    private var fromEnd: Boolean = false
    //加载页
    private var loadingPage: LoadingPage? = null
    private var sequence: Int = 0
    //小说ID
    private val nid: Int = 0
    //小说
    private var book: Book? = null
    //小说帮助类
    private var mBookDaoHelper: BookDaoHelper? = null
    private var mCatalogAdapter: CatalogAdapter? = null
    private var chapterList: ArrayList<Chapter>? = ArrayList()
    private var isPositive = true
    /**
     * 标识List的滚动状态。
     */
    private var scrollState: Int = 0
    private var downLoadReceiver: OffLineDownLoadReceiver? = null
    private var requestItem: RequestItem? = null
    private var mTextColor = 0
    private var mCataloguesPresenter: CataloguesPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.act_catalog)
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }

        colorSelected = resources.getColor(R.color.theme_primary_ffffff)
        colorNormal = resources.getColor(R.color.theme_primary)
        initUI()
        initListener()

        val bundle = intent.extras ?: return

        initData(bundle)
        initCatalogAndBookmark()
        if (fromEnd) {
            isPositive = false
            changeSortState(isPositive)
        }
        EventBus.getDefault().register(this)

    }

    private fun initUI() {

        catalog_novel_close!!.setOnClickListener(this)

        book_catalog_download!!.setOnClickListener(this)
        book_catalog_reading!!.setOnClickListener(this)
        book_catalog_bookshelf!!.setOnClickListener(this)
        iv_catalog_novel_sort!!.setOnClickListener(this)
        char_hint!!.visibility = View.INVISIBLE

        iv_back_reading!!.setOnClickListener(this)
        changeSortState(isPositive)
    }

    fun notifyChangeDownLoad() {
        if (mCatalogAdapter != null) {
            mCatalogAdapter!!.notifyDataSetChanged()
        }
    }

    private fun initListener() {
        if (catalog_main != null) {
            catalog_main!!.onItemClickListener = this
            catalog_main!!.setOnScrollListener(this)
        }


        if (catalog_empty_refresh != null) {
            catalog_empty_refresh!!.setOnClickListener(this)
        }

        if (iv_fixbook != null) {
            iv_fixbook!!.setOnClickListener(this)
        }
    }


    private fun initData(bundle: Bundle) {

        requestItem = bundle.getSerializable(Constants.REQUEST_ITEM) as RequestItem

        if (requestItem == null || requestItem?.book_id == null || requestItem?.host == null) {
            exitAndUpdate()
            return
        }

        sequence = Math.max(bundle.getInt("sequence"), 0)
        AppLog.e(TAG, "CataloguesActivity: " + sequence)
        is_last_chapter = bundle.getBoolean("is_last_chapter", false)
        fromCover = bundle.getBoolean("fromCover", true)
        fromEnd = bundle.getBoolean("fromEnd", false)
        book = bundle.getSerializable("cover") as Book
        if (book != null) {
            catalog_novel_name!!.text = book!!.name
            if (RepairHelp.isShowFixBtn(this, book!!.book_id)) {
                iv_fixbook!!.visibility = View.VISIBLE
            } else {
                iv_fixbook!!.visibility = View.GONE
            }

        }

        if (mBookDaoHelper == null)
            mBookDaoHelper = BookDaoHelper.getInstance()

        if (requestItem != null && book != null) {
            mCataloguesPresenter = CataloguesPresenter(this, book!!, requestItem!!, this, this, fromCover)
        }
        getChapterData()

    }

    fun onEvent(eventBookmark: EventBookmark) {
        if (eventBookmark.type == EventBookmark.type_delete) {
            AppLog.e(TAG, "eventBookmark:" + eventBookmark.bookmark.id + " name:" + eventBookmark.bookmark.chapter_name)
            val bookmark = eventBookmark.bookmark
            if (mCataloguesPresenter != null) {
                mCataloguesPresenter!!.onEventReceive(bookmark)
            }

        }
    }

    private fun getChapterData() {
        if (book != null) {
            if (loadingPage != null) {
                loadingPage!!.onSuccess()
            }
            loadingPage = LoadingPage(this, LoadingPage.setting_result)
            loadingPage!!.setCustomBackgroud()
            if (mCataloguesPresenter != null) {
                mCataloguesPresenter!!.requestCatalogList()
            }


            if (loadingPage != null) {
                loadingPage!!.isCategory = true
                loadingPage!!.setReloadAction(Callable<Void> {
                    if (mCataloguesPresenter != null) {
                        mCataloguesPresenter!!.getRequest()
                    }
                    null
                })
            }
        }
    }


    private fun dataError() {
        if (loadingPage != null) {
            loadingPage!!.onError()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        if (isModeChange) {
            setMode()
        }
        if (mBookDaoHelper == null || requestItem == null)
            return

        changeDownloadButtonStatus()
        if (mBookDaoHelper!!.isBookSubed(requestItem!!.book_id)) {
            if (book_catalog_bookshelf != null) {
                book_catalog_bookshelf!!.setText(R.string.book_cover_havein_bookshelf)
                setRemoveBtn()
            }
        }

        StatService.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        StatService.onPause(this)
    }

    private fun initCatalogAndBookmark() {
        mCatalogAdapter = CatalogAdapter(this, chapterList, requestItem!!.host)
        catalog_main!!.adapter = mCatalogAdapter
        if (!chapterList!!.isEmpty()) {
            sequence = Math.min(chapterList!!.size - 1, sequence)
        }
        if (is_last_chapter) {
            mCatalogAdapter!!.setSelectedItem(chapterList!!.size)
            catalog_main!!.setSelection(chapterList!!.size)
        } else {
            mCatalogAdapter!!.setSelectedItem(sequence)
            catalog_main!!.setSelection(sequence)
        }
    }


    override fun onStart() {
        super.onStart()
        if (downLoadReceiver == null) {
            downLoadReceiver = OffLineDownLoadReceiver(this)
        }
        downLoadReceiver!!.registerAction()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        if (downLoadReceiver != null) {
            try {
                unregisterReceiver(downLoadReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }

        }
        try {
            EventBus.getDefault().unregister(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (chapterList != null) {
            chapterList!!.clear()
        }
        if (mCataloguesPresenter != null) {
            mCataloguesPresenter!!.removeHandler()
            mCataloguesPresenter!!.unRegisterRec()
        }

        super.onDestroy()
    }


    // 目录
    override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
        if (chapterList != null && !chapterList!!.isEmpty()) {
            char_hint!!.text = String.format(getString(R.string.chapter_sort), chapterList!![firstVisibleItem].sequence + 1)
        }
    }

    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
        this.scrollState = scrollState
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE || scrollState == OnScrollListener.SCROLL_STATE_FLING) {
            if (mCataloguesPresenter != null) {
                mCataloguesPresenter!!.delayOverLayHandler()
            }
        } else {
            if (char_hint != null) {
                char_hint!!.visibility = View.VISIBLE
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.catalog_novel_close -> {
                val data = HashMap<String, String>()
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOKCATALOG, StartLogClickUtil.BACK, data)
                if (!fromCover  && mCataloguesPresenter != null) {
                    if(!chapterList!!.isEmpty()){
                        sequence = Math.min(sequence, (chapterList?.size ?: 1) - 1)
                        mCataloguesPresenter!!.activityResult(sequence,true)
                    }else{
                        mCataloguesPresenter!!.activityResult(sequence,false)
                    }
                }
                finish()
            }
            R.id.iv_back_reading -> finish()
            R.id.catalog_empty_refresh -> getChapterData()
            R.id.book_catalog_download -> {
                val data1 = HashMap<String, String>()
                if (requestItem != null && requestItem!!.book_id != null) {
                    data1.put("bookid", requestItem!!.book_id)
                }
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOKCATALOG, StartLogClickUtil.CATALOG_CASHEALL, data1)
                if (mCataloguesPresenter != null) {
                    mCataloguesPresenter!!.startDownLoader()
                }
            }
            R.id.book_catalog_reading -> {

                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOKCATALOG, StartLogClickUtil.TRANSCODEREAD)
                if (mCataloguesPresenter != null) {
                    if (!fromCover  && mCataloguesPresenter != null) {
                        if(!chapterList!!.isEmpty()){
                            sequence = Math.min(sequence, (chapterList?.size ?: 1) - 1)
                        }
                    }
                    mCataloguesPresenter!!.showReadingSourceDialog(fromCover,sequence)
                }
            }
            R.id.book_catalog_bookshelf -> if (mCataloguesPresenter != null) {
                mCataloguesPresenter!!.addBookIntoShelf()
            }

            R.id.iv_catalog_novel_sort//正序、逆序
            -> if (chapterList != null && !chapterList!!.isEmpty()) {
                //书签点击的统计
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.rb_catalog_click_book_mark)
                isPositive = !isPositive
                Collections.reverse(chapterList!!)
                mCatalogAdapter!!.list = chapterList
                mCatalogAdapter!!.notifyDataSetChanged()
                changeSortState(isPositive)
            }
            R.id.iv_fixbook -> if (mCataloguesPresenter != null) {
                mCataloguesPresenter!!.fixBook()
            }
            else -> {
            }
        }
    }


    private fun setRemoveBtn() {
        mTextColor = R.color.home_title_search_text
        book_catalog_bookshelf!!.setTextColor(resources.getColor(mTextColor))
    }

    //排序
    private fun changeSortState(b: Boolean) {
        if (iv_catalog_novel_sort != null) {
            if (b) {
                sortIcon = R.drawable.icon_catalog_daoxu
                //正序的统计
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.rb_catalog_click_zx_btn)

                iv_catalog_novel_sort!!.setImageResource(sortIcon)
            } else {
                sortIcon = R.drawable.icon_catalog_zhengxu
                iv_catalog_novel_sort!!.setImageResource(sortIcon)
                //倒序的统计
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.rb_catalog_click_dx_btn)
            }
        }
    }

    override fun onTaskStatusChange() {
        super.onTaskStatusChange()
        changeDownloadButtonStatus()
    }

    override fun onBackPressed() {
        if (!fromCover  && mCataloguesPresenter != null) {
            if(!chapterList!!.isEmpty()){
                sequence = Math.min(sequence, (chapterList?.size ?: 1) - 1)
                mCataloguesPresenter!!.activityResult(sequence,true)
            }else{
                mCataloguesPresenter!!.activityResult(sequence,false)
            }
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

    private fun exit() {
        if (!this.isFinishing()) {
            finish()
        }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (parent === catalog_main) {
            if (mCataloguesPresenter != null) {
                mCataloguesPresenter!!.catalogToReading(position, true)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add("menu")
        return super.onCreateOptionsMenu(menu)
    }

    override fun requestCatalogSuccess(chapterList: ArrayList<Chapter>) {
        this.chapterList = chapterList
        if (loadingPage != null) {
            loadingPage!!.onSuccess()
        }
        catalog_chapter_count!!.text = "共" + chapterList.size + "章"
        if (mCatalogAdapter != null) {
            if (fromEnd) {
                isPositive = false
                Collections.reverse(chapterList)
            }

            //更新书籍缓存状态啊
            Observable.create(object : ObservableOnSubscribe<Boolean> {
                @Throws(Exception::class)
                override fun subscribe(e: ObservableEmitter<Boolean>) {
                    CacheManager.freshBook(book!!.book_id, false)
                    e.onNext(true)
                    e.onComplete()
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object :Consumer<Boolean> {
                        @Throws(Exception::class)
                        override fun accept(flag : Boolean) {
                            mCatalogAdapter?.setList(chapterList)
                            mCatalogAdapter?.notifyDataSetChanged()
                            sequence = Math.min(chapterList.size - 1, sequence)
                            if (is_last_chapter) {
                                mCatalogAdapter?.setSelectedItem(chapterList.size)
                                catalog_main.setSelection(chapterList.size)
                            } else {
                                mCatalogAdapter?.setSelectedItem(sequence)
                                catalog_main.setSelection(sequence)
                            }
                        }
                    })
        }
    }


    override fun requestCatalogError() {
        dataError()
    }

    override fun notifyDataChange(isCatalog: Boolean, bookmarkList: ArrayList<Bookmark>) {

    }

    override fun deleteBookmarks(deleteList: ArrayList<Int>) {

    }

    override fun handOverLay() {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE || scrollState == OnScrollListener.SCROLL_STATE_FLING && char_hint != null) {
            char_hint!!.visibility = View.INVISIBLE
        }
    }


    override fun changeDownloadButtonStatus() {
        if (book_catalog_download == null) {
            return
        }
        if (book != null && book_catalog_download != null) {
            var status = CacheManager.getBookStatus(book!!)
            if (status == DownloadState.FINISH) {
                book_catalog_download.setText(R.string.download_status_complete)
            } else if (status == DownloadState
                    .WAITTING || status == DownloadState.DOWNLOADING) {
                book_catalog_download.setText(R.string.download_status_underway)
            } else {
                book_catalog_download.setText(R.string.download_status_total)
            }
        }
    }


    override fun successAddIntoShelf(isAddIntoShelf: Boolean) {
        if (isAddIntoShelf && book_catalog_bookshelf != null) {
            book_catalog_bookshelf!!.setText(R.string.book_cover_havein_bookshelf)
            setRemoveBtn()
        }
    }

}