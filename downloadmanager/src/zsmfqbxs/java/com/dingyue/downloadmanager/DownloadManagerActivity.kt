package com.dingyue.downloadmanager

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.Menu
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.dingyue.contract.CommonContract
import com.dingyue.downloadmanager.contract.BookHelperContract
import com.dingyue.downloadmanager.contract.CacheManagerContract
import com.dingyue.downloadmanager.recl.DownloadItemDecoration
import com.dingyue.downloadmanager.recl.DownloadManagerAdapter
import iyouqu.theme.BaseCacheableActivity
import kotlinx.android.synthetic.zsmfqbxs.act_download_manager.*
import kotlinx.android.synthetic.zsmfqbxs.item_download_manager_task_header.view.*
import net.lzbook.kit.book.download.CallBackDownload
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.router.BookRouter
import net.lzbook.kit.router.RouterConfig
import net.lzbook.kit.router.RouterUtil
import net.lzbook.kit.utils.loge
import net.lzbook.kit.utils.logi
import net.lzbook.kit.utils.uiThread

/**
 * Desc 下载管理页面
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/4/28 0025 11:39
 */
@Route(path = RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
class DownloadManagerActivity : BaseCacheableActivity(),
        DownloadManagerAdapter.DownloadManagerItemListener, CallBackDownload, MenuManager {

    private val firstHeaderHeight by lazy {
        resources.getDimensionPixelSize(R.dimen.download_manager_item_first_header)
    }

    private val headerHeight by lazy {
        resources.getDimensionPixelSize(R.dimen.download_manager_item_header)
    }

    private val popupHeight by lazy {
        resources.getDimensionPixelSize(R.dimen.download_manager_popup_height)
    }

    private var downloadBooks: ArrayList<Book> = ArrayList()

    private val downloadManagerAdapter: DownloadManagerAdapter by lazy {
        DownloadManagerAdapter(this, this, downloadBooks)
    }


    private var lastShowTime = 0L

    private var time = System.currentTimeMillis()

    private lateinit var downloadManagerViewModel: DownloadManagerViewModel

    private val topMenuPopup: DownloadManagerMenuPopup by lazy {
        val popup = DownloadManagerMenuPopup(this)
        popup.setOnEditClickListener {
            showMenu()
        }
        popup.setOnTimeSortingClickListener {
            sortBooks(1)
        }
        popup.setOnRecentReadSortingClickListener {
            sortBooks(0)
        }
        popup
    }

    private val removeMenuPopup: RemoveMenuPopup by lazy {
        val popup = RemoveMenuPopup(this)
        popup.setOnDeletedClickListener {
            deleteCache(downloadManagerAdapter.checkedBooks)
        }

        popup.setOnSelectAllClickListener { isSelectAll ->
            checkAll(isSelectAll)
        }

        popup
    }

    private val managerDeleteDialog: DownloadManagerDeleteDialog by lazy {
        DownloadManagerDeleteDialog(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_download_manager)
        initView()

        CacheManagerContract.insertDownloadCallBack(this)

        Constants.isDownloadManagerActivity = true
        Constants.hadShownMobilNetworkConfirm = false

        downloadManagerViewModel = ViewModelProviders.of(this).get(DownloadManagerViewModel::class.java)

        observeViewModel(downloadManagerViewModel)

        downloadManagerViewModel.refreshBooks()
    }

    private fun observeViewModel(viewModel: DownloadManagerViewModel) {
        viewModel.loadBookListLiveData().observe(this, Observer { books ->
            if (books != null) {
                downloadBooks.clear()
                downloadBooks.addAll(books)
            }
            onDownloadBookQuery()
        })

        viewModel.loadDeleteCacheLiveData().observe(this, Observer {
            onDownloadDelete()
        })
    }

    override fun onResume() {
        super.onResume()
        downloadManagerViewModel.refreshBooks()
    }

    override fun onDestroy() {
        super.onDestroy()
        CacheManagerContract.removeDownloadCallBack(this)
        Constants.isDownloadManagerActivity = false
    }

    private fun initView() {
        txt_head_cancel.setOnClickListener {
            dismissMenu()
        }

        img_head_back.setOnClickListener {
            DownloadManagerLogger.uploadCacheManagerBack()
            finish()
        }

        txt_head_title.setOnClickListener {
            finish()
        }

        img_head_more.setOnClickListener {
            topMenuPopup.show(img_head_more)
            DownloadManagerLogger.uploadCacheManagerMore()
        }

        recl_content.addItemDecoration(DownloadItemDecoration(object : DownloadItemDecoration.DownloadHeaderInterface {
            override fun requestItemCacheState(position: Int): Boolean? {
                return if (position > -1 && downloadBooks.size > position) {
                    CacheManagerContract.loadBookDownloadState(downloadBooks[position]) != DownloadState.FINISH
                } else {
                    null
                }
            }

            override fun requestItemHeaderView(position: Int): View? {
                return if (position > -1 && downloadBooks.size > position) {
                    val view = layoutInflater.inflate(R.layout.item_download_manager_task_header, null, false)

                    val finish = CacheManagerContract.loadBookDownloadState(downloadBooks[position]) == DownloadState.FINISH

                    if (finish) {
                        view.img_state.setImageResource(R.drawable.download_manager_item_header_cache_icon)
                    } else {
                        view.img_state.setImageResource(R.drawable.download_manager_item_header_nocache_icon)
                    }
                    view
                } else {
                    null
                }
            }
        }, firstHeaderHeight, headerHeight))

        recl_content.adapter = downloadManagerAdapter
        recl_content.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        //解决RecyclerView刷新列表时，图片的抖动问题
        (recl_content.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }


    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        showMenu()
        return false
    }

    override fun onBackPressed() {
        if (downloadManagerAdapter.remove) {
            dismissMenu()
        } else {
            //如果是从通知栏过来, 且已经退出到home了, 要回到应用中
            if (isTaskRoot) {
                RouterUtil.navigation(this, RouterConfig.SPLASH_ACTIVITY)
            }
            super.onBackPressed()
        }
    }

    override fun supportSlideBack(): Boolean {
        return !isTaskRoot && !downloadManagerAdapter.remove
    }

    fun onDownloadBookQuery() {
        if (downloadBooks.size == 0) {
            recl_content.visibility = View.GONE
            rl_empty.visibility = View.VISIBLE
        } else {
            rl_empty.visibility = View.GONE
            recl_content.visibility = View.VISIBLE
            downloadManagerAdapter.notifyDataSetChanged()
        }
    }

    private fun onDownloadDelete() {
        downloadManagerViewModel.refreshBooks()
        dismissMenu()
        managerDeleteDialog.dismiss()
    }


    override fun clickedDownloadItem(book: Book?, position: Int) {
        if (position < 0) {
            return
        }

        if (!downloadManagerAdapter.remove) {
            if (book != null) {
                DownloadManagerLogger.uploadCacheManagerBookClick(book)
                BookRouter.navigateCoverOrRead(this, book, BookRouter.NAVIGATE_TYPE_DOWNLOAD)
            }
        } else {
            downloadManagerAdapter.insertCheckedPosition(position)
            removeMenuPopup.setSelectedNum(downloadManagerAdapter.checkedBooks.size)
            val allText = if (downloadManagerAdapter.isCheckAll()) getString(R.string.select_all_cancel) else getString(R.string.select_all)
            removeMenuPopup.setSelectAllText(allText)
        }
    }

    override fun longClickedDownloadItem(): Boolean {
        if (!downloadManagerAdapter.remove) {
            showMenu()
        }
        return false
    }


    override fun onTaskStatusChange(book_id: String?) {
        logi("onTaskStatusChange")
        downloadManagerAdapter.notifyDataSetChanged()
    }

    override fun onTaskFinish(book_id: String?) {
        val book = BookHelperContract.loadLocalBook(book_id)

        if (CacheManagerContract.loadBookDownloadState(book) == DownloadState.FINISH) {
            val data = downloadBooks
            for (b in data) {
                if (b.book_id != null && book.book_id != null && b.book_id == book.book_id) {
                    data.remove(b)
                    break
                }
            }
        }
        downloadManagerViewModel.refreshBooks()
    }

    override fun onTaskFailed(book_id: String?, t: Throwable?) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastShowTime > 4000) {
            lastShowTime = currentTime
        }
        downloadManagerAdapter.notifyDataSetChanged()
    }

    override fun onTaskProgressUpdate(book_id: String?) {
        loge("onTaskProgressUpdate")
        if (System.currentTimeMillis() - time > 500) {
            time = System.currentTimeMillis()
            uiThread { downloadManagerAdapter.notifyDataSetChanged() }
        }
    }


    override fun showMenu() {
        downloadManagerAdapter.insertRemoveState(true)
        removeMenuPopup.show(rl_root)

        recl_content.setPadding(0, recl_content.paddingTop, 0, popupHeight)

        img_head_more.visibility = View.GONE

        txt_head_title.text = getString(R.string.edit_cache)
        txt_head_cancel.text = getString(R.string.cancel)

        txt_head_cancel.visibility = View.VISIBLE

        DownloadManagerLogger.uploadCacheManagerEdit()
    }

    override fun dismissMenu() {
        downloadManagerAdapter.insertRemoveState(false)
        removeMenuPopup.dismiss()

        recl_content.setPadding(0, recl_content.paddingTop, 0, 0)

        img_head_more.visibility = View.VISIBLE
        img_head_back.visibility = View.VISIBLE
        txt_head_title.text = getString(R.string.download_manager)
        txt_head_cancel.visibility = View.GONE
        img_head_more.visibility = View.VISIBLE
        DownloadManagerLogger.uploadCacheMangerEditCancel()
    }

    override fun checkAll(all: Boolean) {
        downloadManagerAdapter.insertSelectAllState(all)
        removeMenuPopup.setSelectedNum(downloadManagerAdapter.checkedBooks.size)
        DownloadManagerLogger.uploadCacheManagerEditSelectAll(all)
    }

    override fun sortBooks(type: Int) {
        CommonContract.insertShelfSortType(type)
        downloadManagerViewModel.refreshBooks()
        DownloadManagerLogger.uploadCacheManagerSort(type)
    }

    override fun deleteCache(books: ArrayList<Book>) {
        DownloadManagerLogger.uploadCacheManagerEditDeleteLog()
        if (books.isNotEmpty()) {
            managerDeleteDialog.show()
            downloadManagerViewModel.deleteCache(books)
            DownloadManagerLogger.uploadCacheManagerEditDelete(books)
        }
    }
}