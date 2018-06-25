package com.dingyue.downloadmanager

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ding.basic.bean.Book
import com.dingyue.contract.CommonContract
import com.dingyue.contract.router.BookRouter
import com.dingyue.downloadmanager.contract.CacheManagerContract
import com.dingyue.downloadmanager.recl.DownloadManagerAdapter
import kotlinx.android.synthetic.qbmfrmxs.frag_download_manager.*
import net.lzbook.kit.book.download.DownloadState

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/6/20 14:09
 */
class DownloadManagerFragment: Fragment(), DownloadManagerAdapter.DownloadManagerItemListener, MenuManager {

    private var title: String? = null

    private var downloadBooks: ArrayList<Book> = ArrayList()

    private var downloadManagerListener: DownloadManagerListener? = null

    private lateinit var downloadManagerViewModel: DownloadManagerViewModel

    private val downloadManagerAdapter: DownloadManagerAdapter by lazy {
        DownloadManagerAdapter(requireContext(), this, downloadBooks)
    }

    private val popupHeight by lazy {
        resources.getDimensionPixelSize(R.dimen.download_manager_popup_height)
    }

    private val removeMenuPopup: RemoveMenuPopup by lazy {
        val popup = RemoveMenuPopup(requireContext())
        popup.setOnDeletedClickListener {
            deleteCache(downloadManagerAdapter.checkedBooks)
        }
        popup
    }

    private val managerDeleteDialog: DownloadManagerDeleteDialog by lazy {
        DownloadManagerDeleteDialog(requireActivity())
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            downloadManagerListener = context as DownloadManagerListener
        } catch (classCastException: ClassCastException) {
            throw ClassCastException(context.toString() + " must implement DownloadManagerListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = this.arguments

        if (bundle != null) {
            this.title = bundle.getString("title")
        }

        downloadManagerViewModel = ViewModelProviders.of(requireActivity()).get(DownloadManagerViewModel::class.java)

        observeViewModel(downloadManagerViewModel)

        downloadManagerViewModel.refreshBooks()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_download_manager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txt_empty_find.setOnClickListener {
            downloadManagerListener?.navigationBookStore()
        }

        recl_content.adapter = downloadManagerAdapter
        recl_content.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        //解决RecyclerView刷新列表时，图片的抖动问题
        (recl_content.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    override fun onResume() {
        super.onResume()
        downloadManagerViewModel.refreshBooks()
    }

    private fun observeViewModel(viewModel: DownloadManagerViewModel) {
        viewModel.loadBookListLiveData().observe(this, Observer<List<Book>> { books ->
            if (books != null) {
                downloadBooks.clear()

                if ("已缓存" == title) {
                    books.forEach {
                        if (CacheManagerContract.loadBookDownloadState(it) == DownloadState.FINISH) {
                            downloadBooks.add(it)
                        }
                    }
                } else if ("未缓存" == title) {
                    books.forEach {
                        if (CacheManagerContract.loadBookDownloadState(it) != DownloadState.FINISH) {
                            downloadBooks.add(it)
                        }
                    }
                }
            }
            onDownloadBookQuery()
        })

        viewModel.loadDeleteCacheLiveData().observe(this, Observer {
            onDownloadDelete()
        })
    }

    private fun onDownloadBookQuery() {
        if (downloadBooks.size == 0) {
            recl_content.visibility = View.GONE
            ll_empty.visibility = View.VISIBLE
        } else {
            ll_empty.visibility = View.GONE
            recl_content.visibility = View.VISIBLE
            downloadManagerAdapter.notifyDataSetChanged()
        }
    }

    private fun onDownloadDelete() {
        downloadManagerViewModel.refreshBooks()
        dismissMenu()
        managerDeleteDialog.dismiss()
    }

    fun loadRemoveState(): Boolean {
        return downloadManagerAdapter.remove
    }

    fun refreshData() {
        downloadManagerAdapter.notifyDataSetChanged()
    }

    fun refreshBookState(book: Book) {
        if (CacheManagerContract.loadBookDownloadState(book) == DownloadState.FINISH) {
            val data = downloadBooks
            for (b in data) {
                if (b.book_id.isNotEmpty() && book.book_id.isNotEmpty() && b.book_id == book.book_id) {
                    data.remove(b)
                    break
                }
            }
        }
        downloadManagerViewModel.refreshBooks()
    }

    override fun clickedDownloadItem(book: Book?, position: Int) {
        if (position < 0) {
            return
        }

        if (!downloadManagerAdapter.remove) {
            if (book != null) {
                DownloadManagerLogger.uploadCacheManagerBookClick(book)
                BookRouter.navigateCoverOrRead(requireActivity(), book, BookRouter.NAVIGATE_TYPE_DOWNLOAD)
            }
        } else {
            downloadManagerAdapter.insertCheckedPosition(position)
            removeMenuPopup.setSelectedNum(downloadManagerAdapter.checkedBooks.size)
            downloadManagerListener?.changeSelectAllContent(if (downloadManagerAdapter.isCheckAll()) getString(R.string.select_all_cancel) else getString(R.string.select_all))
        }
    }

    override fun longClickedDownloadItem(): Boolean {
        if (!downloadManagerAdapter.remove) {
            showMenu()
        }
        return false
    }

    override fun showMenu() {
        downloadManagerAdapter.insertRemoveState(true)
        removeMenuPopup.show(rl_root)

        recl_content.setPadding(0, recl_content.paddingTop, 0, popupHeight)

        downloadManagerListener?.changeRemoveViewState(true)

        DownloadManagerLogger.uploadCacheManagerEdit()
    }

    override fun dismissMenu() {
        downloadManagerAdapter.insertRemoveState(false)
        removeMenuPopup.dismiss()

        recl_content.setPadding(0, recl_content.paddingTop, 0, 0)

        downloadManagerListener?.changeRemoveViewState(false)

        DownloadManagerLogger.uploadCacheMangerEditCancel()
    }

    override fun checkAll(isAll: Boolean) {
        downloadManagerAdapter.insertSelectAllState(isAll)
        removeMenuPopup.setSelectedNum(downloadManagerAdapter.checkedBooks.size)
        DownloadManagerLogger.uploadCacheManagerEditSelectAll(isAll)
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