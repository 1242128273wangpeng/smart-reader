package com.dingyue.downloadmanager

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.Menu
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.ding.basic.bean.Book
import com.dingyue.downloadmanager.contract.BookHelperContract
import com.dingyue.downloadmanager.contract.CacheManagerContract
import com.dingyue.downloadmanager.recl.DownloadItemDecoration
import com.dingyue.downloadmanager.recl.DownloadManagerAdapter
import kotlinx.android.synthetic.mfqbxssc.act_download_manager.*
import kotlinx.android.synthetic.mfqbxssc.item_download_manager_task_header.view.*
import net.lzbook.kit.base.activity.BaseCacheableActivity
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.book.CommonContract
import net.lzbook.kit.utils.download.CallBackDownload
import net.lzbook.kit.utils.download.DownloadState
import net.lzbook.kit.utils.router.BookRouter
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.toast.showToastMessage
import net.lzbook.kit.utils.uiThread
import java.util.*

/**
 * Desc 下载管理页面
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/4/28 0025 11:39
 */
@Route(path = RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
class DownloadManagerActivity : BaseCacheableActivity(), CallBackDownload,
        DownloadManagerAdapter.DownloadManagerItemListener, MenuManager {

    private val firstHeaderHeight by lazy {
        resources.getDimensionPixelSize(R.dimen.download_manager_item_first_header)
    }

    private val headerHeight by lazy {
        resources.getDimensionPixelSize(R.dimen.download_manager_item_header)
    }

    private val popupHeight by lazy {
        resources.getDimensionPixelSize(R.dimen.download_manager_popup_height)
    }

    var downloadBooks: ArrayList<Book> = ArrayList()

    private val downloadManagerAdapter: DownloadManagerAdapter by lazy {
        DownloadManagerAdapter(this, this, downloadBooks)
    }

    private var time = System.currentTimeMillis()

    private var lastShowTime = 0L


    private lateinit var downloadManagerViewModel: DownloadManagerViewModel

    private val managerDeleteDialog: DownloadManagerDeleteDialog by lazy {
        DownloadManagerDeleteDialog(this)
    }

    private val removeMenuPopup: RemoveMenuPopup by lazy {
        val popup = RemoveMenuPopup(this)
        popup.setOnDeletedClickListener {
            deleteCache(downloadManagerAdapter.checkedBooks)
        }
        popup
    }

    private val topMenuPopup: TopMenuPopup by lazy {
        val popup = TopMenuPopup(this)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_download_manager)
        initView()

        CacheManagerContract.insertDownloadCallBack(this)

        Constants.isDownloadManagerActivity = true

        downloadManagerViewModel = ViewModelProviders.of(this).get(DownloadManagerViewModel::class.java)

        observeViewModel(downloadManagerViewModel)

        downloadManagerViewModel.refreshBooks()
    }

    private fun observeViewModel(viewModel: DownloadManagerViewModel) {
        viewModel.loadBookListLiveData().observe(this, Observer<List<Book>> { books ->
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
        img_head_back.setOnClickListener {
            DownloadManagerLogger.uploadCacheManagerBack()
            finish()
        }
        txt_head_cancel.setOnClickListener {
            dismissMenu()
        }
        txt_head_select_all.setOnClickListener {
            if (CommonContract.isDoubleClick(System.currentTimeMillis())) {
                return@setOnClickListener
            }
            if (txt_head_select_all.text == getString(R.string.select_all)) {
                txt_head_select_all.text = getString(R.string.select_all_cancel)
                checkAll(true)
            } else {
                txt_head_select_all.text = getString(R.string.select_all)
                checkAll(false)
            }
        }
        txt_head_title.setOnClickListener {
            finish()
        }

        img_head_more.setOnClickListener {
            DownloadManagerLogger.uploadCacheManagerMore()
            topMenuPopup.show(img_head_more)
        }

        txt_empty_find.setOnClickListener {

            DownloadManagerLogger.uploadCacheManagerBookCity()

            val bundle = Bundle()
            bundle.putInt("position", 1)
            RouterUtil.navigation(this, RouterConfig.HOME_ACTIVITY, bundle)

            finish()
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
                        view.txt_state.text = "已缓存"
                    } else {
                        view.txt_state.text = "未缓存"
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

    private fun onDownloadDelete() {
        downloadManagerViewModel.refreshBooks()
        dismissMenu()
        managerDeleteDialog.dismiss()
    }

    private fun onDownloadBookQuery() {
        if (downloadBooks.size == 0) {
            recl_content.visibility = View.GONE
            ll_empty.visibility = View.VISIBLE
            img_head_more.visibility = View.GONE
        } else {
            ll_empty.visibility = View.GONE
            recl_content.visibility = View.VISIBLE
            downloadManagerAdapter.notifyDataSetChanged()
        }
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

    override fun onTaskStatusChange(book_id: String?) {
        downloadManagerAdapter.notifyDataSetChanged()
    }

    override fun onTaskFinish(book_id: String?) {

        book_id?.let {
            val book = BookHelperContract.loadLocalBook(book_id)

            if (book != null && CacheManagerContract.loadBookDownloadState(book) == DownloadState.FINISH) {
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
    }

    override fun onTaskFailed(book_id: String?, t: Throwable?) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastShowTime > 4000) {
            lastShowTime = currentTime
        }
        downloadManagerAdapter.notifyDataSetChanged()
    }

    override fun onTaskProgressUpdate(book_id: String?) {
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
        img_head_back.visibility = View.GONE
        txt_head_title.text = getString(R.string.edit_cache)
        txt_head_select_all.text = getString(R.string.select_all)
        txt_head_select_all.visibility = View.VISIBLE
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
        txt_head_select_all.text = getString(R.string.select_all)
        txt_head_select_all.visibility = View.GONE
        txt_head_cancel.visibility = View.GONE

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
        } else {
            this.showToastMessage(R.string.download_manager_delete_empty)
        }
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
            txt_head_select_all.text = if (downloadManagerAdapter.isCheckAll()) getString(R.string.select_all_cancel) else getString(R.string.select_all)
        }
    }

    override fun longClickedDownloadItem(): Boolean {
        if (!downloadManagerAdapter.remove) {
            showMenu()
        }
        return false
    }
}