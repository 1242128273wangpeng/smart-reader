
package com.intelligent.reader.activity

import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.widget.AbsListView
import android.widget.AbsListView.OnScrollListener
import android.widget.TextView
import com.baidu.mobstat.StatService
import com.ding.basic.bean.Book
import com.ding.basic.bean.Bookmark
import com.ding.basic.bean.Chapter
import com.ding.basic.repository.RequestRepositoryFactory
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.util.showToastMessage
import com.intelligent.reader.adapter.CatalogAdapter
import com.intelligent.reader.presenter.catalogues.CataloguesContract
import com.intelligent.reader.presenter.catalogues.CataloguesPresenter
import com.intelligent.reader.receiver.OffLineDownLoadReceiver
import de.greenrobot.event.EventBus
import iyouqu.theme.BaseCacheableActivity
import kotlinx.android.synthetic.qbmfkdxs.act_catalog.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.book.view.LoadingPage
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.data.bean.EventBookmark
import net.lzbook.kit.repair_books.RepairHelp
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.BookCoverUtil
import net.lzbook.kit.utils.StatServiceUtils
import java.util.*
import java.util.concurrent.Callable
import kotlin.collections.ArrayList

/**
 * CataloguesActivity
 * 小说目录
 */
class CataloguesActivity : BaseCacheableActivity(), CataloguesContract {
    //是否是最后一页
    private var isLastChapter: Boolean = false
    //是否来源于封面页
    private var isFromCover: Boolean = false
    //是否来源于完结页
    private var isFromEnd: Boolean = false
    //加载页
    private val loadingPage: LoadingPage by lazy {
        val page = LoadingPage(this, LoadingPage.setting_result)
        page.setCustomBackgroud()
        page
    }
    private var sequence: Int = 0
    //小说
    private var book: Book? = null
    //小说帮助类
    private val catalogAdapter: CatalogAdapter by lazy {
        CatalogAdapter(this, chapterList, "")
    }
    private var chapterList: ArrayList<Chapter> = ArrayList()
    private var isPositive = true

    private var isChangeSource: Boolean = false

    private var scrollState: Int = 0
    private var downLoadReceiver: OffLineDownLoadReceiver? = null
    private var readingSourceDialog: MyDialog? = null
    private var bookCoverUtil: BookCoverUtil? = null

    private var presenter: CataloguesPresenter? = null

    private val requestFactory by lazy {
        RequestRepositoryFactory
                .loadRequestRepositoryFactory(this.applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_catalog)

        initView()
        initData()
        initCatalogAndBookmark()
        if (isFromEnd) {
            isPositive = false
            changeSortState(isPositive)
        }
        EventBus.getDefault().register(this)

    }

    private fun initView() {

        char_hint.visibility = View.INVISIBLE
        changeSortState(isPositive)

        catalog_novel_close.setOnClickListener {
            val data = HashMap<String, String>()
            data.put("type", "1")
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOKCATALOG,
                    StartLogClickUtil.BACK, data)
            onBackPressed()
        }

        book_catalog_download.setOnClickListener {
            book?.let {
                val downloadState = CacheManager.getBookStatus(it)
                if (downloadState != DownloadState.FINISH
                        && downloadState != DownloadState.WAITTING
                        && downloadState != DownloadState.DOWNLOADING) {
                    showToastMessage("正在缓存中。。。")
                }
                presenter?.startDownload()
            }
        }

        book_catalog_reading.setOnClickListener {
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOKCATALOG,
                    StartLogClickUtil.TRANSCODEREAD)
            showReadingSourceDialog()
        }

        book_catalog_bookshelf.setOnClickListener {
            presenter?.addToBookShelf()
        }

        iv_catalog_novel_sort.setOnClickListener {
            if (chapterList.isNotEmpty()) {
                //书签点击的统计
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.rb_catalog_click_book_mark)
                isPositive = !isPositive
                Collections.reverse(chapterList)
                catalogAdapter.list = chapterList
                catalogAdapter.notifyDataSetChanged()
                changeSortState(isPositive)
            }
        }

        iv_back_reading.setOnClickListener {
            finish()
        }

        catalog_empty_refresh.setOnClickListener {
            getChapterData()
        }

        iv_fixbook.setOnClickListener {
            presenter?.fixBook()
        }

        catalog_main.setOnItemClickListener { parent, view, position, id ->
            val isCatalog: Boolean = parent == catalog_main
            presenter?.catalogToReading(position, isCatalog)
        }

        catalog_main.setOnScrollListener(object : OnScrollListener {
            override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                if (chapterList.isNotEmpty()) {
                    char_hint.text = String.format(getString(R.string.chapter_sort),
                            chapterList[firstVisibleItem].sequence + 1)
                }
            }

            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
                this@CataloguesActivity.scrollState = scrollState
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE || scrollState == OnScrollListener.SCROLL_STATE_FLING) {
                    presenter?.delayOverLayHandler()
                } else {
                    char_hint?.visibility = View.VISIBLE
                }
            }

        })

    }

    private fun initData() {
        val bundle = intent.extras ?: return

        book = bundle.getSerializable("cover") as Book
        if (book == null || book?.book_id == null || book?.host == null) {
            exitAndUpdate()
            return
        }

        sequence = Math.max(bundle.getInt("sequence"), 0)
        AppLog.e(TAG, "CataloguesActivity: " + sequence)
        isLastChapter = bundle.getBoolean("isLastChapter", false)
        isFromCover = bundle.getBoolean("isFromCover", true)
        isFromEnd = bundle.getBoolean("isFromEnd", false)
        isChangeSource = bundle.getBoolean("changeSource", false)
        book?.let {
            catalog_novel_name.text = it.name
            if (RepairHelp.isShowFixBtn(this, it.book_id)) {
                iv_fixbook.visibility = View.VISIBLE
            } else {
                iv_fixbook.visibility = View.GONE
            }

            presenter = CataloguesPresenter(this, it, this,
                    null, isFromCover)
        }

        getChapterData()

//        presenter?.loadBookMark()

    }

    fun onEvent(eventBookmark: EventBookmark) {
        if (eventBookmark.type == EventBookmark.type_delete) {
            AppLog.e(TAG, "eventBookmark:" + eventBookmark.bookmark.id + " name:" + eventBookmark.bookmark.chapter_name)
            val bookmark = eventBookmark.bookmark
            if (bookmark != null) {
                val deleteList = ArrayList<Int>()
                deleteList.add(bookmark.id)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isModeChange) {
            setMode()
        }
        changeDownloadButtonStatus()
        book?.let {
            val subscribedBook = requestFactory.checkBookSubscribe(it.book_id)
            if (subscribedBook != null) {
                setRemoveBtn()
            }
        }
        StatService.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        StatService.onPause(this)
    }

    private fun getChapterData() {
        if (book != null) {
            loadingPage.onSuccess()

            presenter?.requestCatalogList(isChangeSource)

            loadingPage.isCategory = true
            loadingPage.setReloadAction(Callable<Void> {
                presenter?.requestCatalogList(isChangeSource)
                null
            })
        }
    }

    /**
     * 改变缓存状态值
     */
    override fun changeDownloadButtonStatus() {
        if (book != null) {
            val status = CacheManager.getBookStatus(book!!)
            if (status == DownloadState.FINISH) {
                book_catalog_download!!.setText(R.string.download_status_complete)
            } else if (status == DownloadState
                    .WAITTING || status == DownloadState.DOWNLOADING) {
                book_catalog_download!!.setText(R.string.download_status_underway)
            } else {
                book_catalog_download!!.setText(R.string.download_status_total)
            }
        }
    }

    private fun initCatalogAndBookmark() {
        catalog_main.adapter = catalogAdapter
        if (chapterList.isNotEmpty()) {
            sequence = Math.min(chapterList.size - 1, sequence)
        }
        if (isLastChapter) {
            catalogAdapter.setSelectedItem(chapterList.size)
            catalog_main.setSelection(chapterList.size)
        } else {
            catalogAdapter.setSelectedItem(sequence)
            catalog_main.setSelection(sequence)
        }


    }

    override fun onStart() {
        super.onStart()
        if (downLoadReceiver == null) {
            downLoadReceiver = OffLineDownLoadReceiver(this)
        }
        downLoadReceiver?.registerAction()
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

        presenter?.removeHandler()
        presenter?.unRegisterRec()
        super.onDestroy()
    }

    fun notifyChangeDownLoad() {
        catalogAdapter.notifyDataSetChanged()
    }


    override fun onTaskStatusChange() {
        super.onTaskStatusChange()
        changeDownloadButtonStatus()
    }

    private fun setRemoveBtn() {
        book_catalog_bookshelf.setText(R.string.book_cover_havein_bookshelf)
        book_catalog_bookshelf.setTextColor(resources.getColor(R.color.home_title_search_text))
    }

    private fun showReadingSourceDialog() {
        if (readingSourceDialog == null) {
            readingSourceDialog = MyDialog(this@CataloguesActivity, R.layout
                    .dialog_read_source, Gravity.CENTER)
            readingSourceDialog!!.setCanceledOnTouchOutside(true)
            val title = readingSourceDialog!!.findViewById<View>(R.id.dialog_top_title) as TextView
            title.text = "转码"

            val cancel = readingSourceDialog!!.findViewById<View>(R.id.change_source_original_web) as TextView
            cancel.setText(R.string.cancel)
            val continueRead = readingSourceDialog!!.findViewById<View>(R.id.change_source_continue) as TextView

            cancel.setOnClickListener {
                val data1 = HashMap<String, String>()
                data1.put("type", "2")
                StartLogClickUtil.upLoadEventLog(this@CataloguesActivity, StartLogClickUtil.BOOKCATALOG, StartLogClickUtil.CATALOG_TRANSCODEPOPUP, data1)
                readingSourceDialog!!.dismiss()
            }
            continueRead.setOnClickListener {
                val data1 = HashMap<String, String>()
                data1.put("type", "1")
                StartLogClickUtil.upLoadEventLog(this@CataloguesActivity, StartLogClickUtil.BOOKCATALOG, StartLogClickUtil.CATALOG_TRANSCODEPOPUP, data1)
                presenter?.continueReading()
                if (readingSourceDialog!!.isShowing) {
                    readingSourceDialog!!.dismiss()
                }
            }
        }
        if (!readingSourceDialog!!.isShowing) {
            try {
                readingSourceDialog!!.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }


    //排序
    private fun changeSortState(b: Boolean) {
        if (iv_catalog_novel_sort != null) {
            if (b) {
                val sortIcon = R.drawable.icon_catalog_daoxu
                //正序的统计
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.rb_catalog_click_zx_btn)

                iv_catalog_novel_sort!!.setImageResource(sortIcon)
            } else {
                val sortIcon = R.drawable.icon_catalog_zhengxu
                iv_catalog_novel_sort!!.setImageResource(sortIcon)
                //倒序的统计
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.rb_catalog_click_dx_btn)
            }
        }
    }


    override fun onBackPressed() {
        if (!isFromCover) {
            if (chapterList.isNotEmpty()) {
                sequence = Math.min(sequence, chapterList.size - 1)
            }
            presenter?.activityResult(sequence)
        }
        exitAndUpdate()
    }

    private fun exitAndUpdate() {
        //如果是从通知栏过来, 且已经退出到home了, 要回到应用中
        if (isTaskRoot) {
            RouterUtil.navigation(this, RouterConfig.SPLASH_ACTIVITY)
        }
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add("menu")
        return super.onCreateOptionsMenu(menu)
    }

    override fun requestCatalogSuccess(chapterList: ArrayList<Chapter>) {
        this.chapterList.clear()
        this.chapterList.addAll(chapterList)
        loadingPage.onSuccess()
        val text = "共" + chapterList.size + "章"
        catalog_chapter_count.text = text
        if (isFromEnd) {
            isPositive = false
            Collections.reverse(chapterList)
        }
        catalogAdapter.list = chapterList
        catalogAdapter.notifyDataSetChanged()

        //设置选中的条目
        val position: Int = if (isLastChapter) {
            chapterList.size
        } else {
            sequence
        }

        catalog_main.setSelection(position)

        if (isFromEnd) {
            catalog_main!!.setSelection(0)
        }

        catalogAdapter.setSelectedItem(position)
    }

    override fun requestCatalogError() {
        loadingPage.onError()
    }

    override fun supportSlideBack(): Boolean {
        return !isTaskRoot
    }

    override fun notifyDataChange(isCatalog: Boolean, bookmarkList: ArrayList<Bookmark>) {
        //刷新书签
    }

    override fun deleteBookmarks(deleteList: ArrayList<Int>) {
        //删除书签
    }

    override fun handOverLay() {

    }

    override fun successAddIntoShelf(isAddIntoShelf: Boolean) {
        if (isAddIntoShelf) {
            setRemoveBtn()
            showToastMessage(R.string.succeed_add)
        } else {
            showToastMessage(R.string.have_add)
        }
    }

}