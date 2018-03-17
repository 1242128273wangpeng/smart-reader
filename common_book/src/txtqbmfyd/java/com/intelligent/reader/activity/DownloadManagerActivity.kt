package com.intelligent.reader.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import com.intelligent.reader.R
import com.intelligent.reader.adapter.DownloadManagerAdapter
import com.intelligent.reader.event.DownloadManagerToHome
import com.intelligent.reader.presenter.downloadmanager.DownloadManagerPresenter
import com.intelligent.reader.presenter.downloadmanager.DownloadManagerView
import com.intelligent.reader.read.help.BookHelper
import com.intelligent.reader.util.DownloadManagerRemoveHelper
import com.intelligent.reader.view.DownloadDeleteLoadingDialog
import com.intelligent.reader.widget.DownloadManagerMenuPopup
import de.greenrobot.event.EventBus
import kotlinx.android.synthetic.txtqbmfyd.download_manager.*
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.CallBackDownload
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.SettingItemsHelper
import net.lzbook.kit.utils.uiThread
import java.util.*

/**
 * Created by qiantao on 2017/11/22 0022
 */
class DownloadManagerActivity : BaseCacheableActivity(), CallBackDownload, DownloadManagerView {

    val presenter by lazy { DownloadManagerPresenter(this) }

    private val downloadAdapter: DownloadManagerAdapter by lazy {
        DownloadManagerAdapter(this, presenter.downloadBooks)
    }

    private var isDeleteBookOfShelf = false

    private var isShowing: Boolean = false

    private var time = System.currentTimeMillis()

    private var lastShowTime = 0L

    private val deleteLoadingDialog: DownloadDeleteLoadingDialog by lazy {
        DownloadDeleteLoadingDialog(this)
    }

    private val removeHelper: DownloadManagerRemoveHelper by lazy {
        val helper = DownloadManagerRemoveHelper(this, downloadAdapter, download_manager_list)
//        helper.setListView(download_manager_list)
        helper.setOnMenuDeleteListener { books ->
            presenter.uploadDeleteLog()
            isDeleteBookOfShelf = false
            if (books.isNotEmpty()) {
                deleteLoadingDialog.show()
                presenter.deleteDownload(books)
                presenter.uploadDialogConfirmLog(books)
            }
        }
        helper.setOnMenuStateListener(object : DownloadManagerRemoveHelper.OnMenuStateListener {
            override fun onMenuStateChanged(isShown: Boolean) {
                isShowing = isShown
                showSelectAllText(isShown)
                img_head_menu.visibility = if (isShown) View.GONE else View.VISIBLE
                txt_head_back.visibility = if (isShown) View.GONE else View.VISIBLE
                txt_head_title.text =
                        if (isShown) getString(R.string.download_manager_editor_title)
                        else getString(R.string.download_manager)
            }

            override fun onAllCheckedStateChanged(isAllChecked: Boolean) {}
        })
        helper
    }

    private val menuPopup: DownloadManagerMenuPopup by lazy {
        val popup = DownloadManagerMenuPopup(this)
        val settingItemsHelper = SettingItemsHelper.getSettingHelper(applicationContext)
        popup.setOnEditClickListener {
            removeHelper.showRemoveMenu(root)
            presenter.uploadEditLog()
        }
        popup.setOnTimeSortingClickListener {
            settingItemsHelper.putInt(settingItemsHelper.booklistSortType, 1)
            Constants.book_list_sort_type = 1
            presenter.queryDownloadBooks(false)
            presenter.uploadTimeSortingLog()
        }
        popup.setOnRecentReadSortingClickListener {
            settingItemsHelper.putInt(settingItemsHelper.booklistSortType, 0)
            Constants.book_list_sort_type = 0
            presenter.queryDownloadBooks(false)
            presenter.uploadRecentReadSortingLog()
        }
        popup
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.download_manager)
        initView()
        CacheManager.listeners.add(this)
    }

    override fun onResume() {
        super.onResume()
        presenter.queryDownloadBooks(false)
    }

    private fun initView() {
        txt_head_back.setOnClickListener {
            presenter.uploadBackLog()
            finish()
        }
        txt_select_all.setOnClickListener {
            if (presenter.isDoubleClick(System.currentTimeMillis())) {
                return@setOnClickListener
            }
            if (txt_select_all.text == getString(R.string.select_all)) {
                txt_select_all.text = getString(R.string.select_all_cancel)
                removeHelper.selectAll(true)
                presenter.uploadRemoveSelectAllLog(true)
            } else {
                txt_select_all.text = getString(R.string.select_all)
                removeHelper.selectAll(false)
                presenter.uploadRemoveSelectAllLog(false)
            }
        }
        txt_head_title.setOnClickListener {
            finish()
        }

        img_head_menu.setOnClickListener {
            menuPopup.show(img_head_menu)
        }

        txt_no_book_goto.setOnClickListener {
            EventBus.getDefault().post(DownloadManagerToHome(1))
            finish()
        }

        download_manager_list.adapter = downloadAdapter
        download_manager_list.topShadow = img_head_shadow
        download_manager_list.setOnItemClickListener { parent, view, position, id ->
            if (position < 0) return@setOnItemClickListener
            if (!removeHelper.isRemoveMode && !isShowing) {
                val book = downloadAdapter.getItem(position)
                if (book != null) {
                    presenter.uploadBookClickLog(book)
                    BookHelper.goToCoverOrRead(applicationContext, this@DownloadManagerActivity, book, 1)
                }
            } else {
                removeHelper.setCheckPosition(position)
            }
        }
        download_manager_list.setOnItemLongClickListener { parent, view, position, id ->
            if (AppUtils.getPackageName() != "cc.kdqbxs.reader") {
                if (!removeHelper.isRemoveMode) {
                    removeHelper.showRemoveMenu(root)
                }
            }
            false
        }
    }

    override fun onDownloadBookQuery(bookList: ArrayList<Book>, hasDeleted: Boolean) {
        if (bookList.size == 0) {
            download_manager_list.visibility = View.GONE
            ll_no_book.visibility = View.VISIBLE
        } else {
            ll_no_book.visibility = View.GONE
            download_manager_list.visibility = View.VISIBLE
            downloadAdapter.notifyDataSetChanged()
        }
    }

    override fun onDownloadDelete() {
        presenter.queryDownloadBooks(true)
        downloadAdapter.notifyDataSetChanged()
        removeHelper.dismissRemoveMenu()
        deleteLoadingDialog.dismiss()
    }

//    fun stopDownloadBook(book_id: String) {
//        CacheManager.stop(book_id)
//    }

    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        removeHelper.showRemoveMenu(root)
        return false
    }

    override fun onBackPressed() {
        if (removeHelper.isRemoveMode) {
            removeHelper.dismissRemoveMenu()
            showSelectAllText(false)
        } else {
            //如果是从通知栏过来, 且已经退出到home了, 要回到应用中
            if (isTaskRoot) {
                val intent = Intent(this, SplashActivity::class.java)
                startActivity(intent)
            }
            super.onBackPressed()
        }
    }

    override fun onTaskFinish(book_id: String?) {
        AppLog.e("tag", "onTaskFinish")
        val book = presenter.bookDaoHelper.getBook(book_id, 0)
        if (CacheManager.getBookStatus(book) == DownloadState.FINISH) {
            val data = presenter.downloadBooks
            for (b in data) {
                if (b.book_id != null && book.book_id != null && b.book_id == book.book_id) {
                    data.remove(b)
                    break
                }
            }
        }
        presenter.queryDownloadBooks(false)
    }


    override fun onTaskStatusChange(book_id: String?) {
        downloadAdapter.notifyDataSetChanged()
    }

    override fun onTaskFailed(book_id: String?, t: Throwable?) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastShowTime > 4000) {
            lastShowTime = currentTime
        }
        downloadAdapter.notifyDataSetChanged()
    }

    override fun onTaskProgressUpdate(book_id: String?) {
        if (System.currentTimeMillis() - time > 500) {
            time = System.currentTimeMillis()
            uiThread { downloadAdapter.notifyDataSetChanged() }
        }
    }

    override fun supportSlideBack(): Boolean {
        return !isTaskRoot && !removeHelper.isRemoveMode
    }

    override fun onDestroy() {
        super.onDestroy()
        CacheManager.listeners.remove(this)
    }

    private fun showSelectAllText(isShow: Boolean) {
        if (isShow) {
            txt_select_all.text = getString(R.string.select_all)
            txt_select_all.visibility = View.VISIBLE
        } else {
            txt_select_all.text = getString(R.string.select_all)
            txt_select_all.visibility = View.GONE
        }
    }
}