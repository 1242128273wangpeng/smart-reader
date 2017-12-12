package com.intelligent.reader.activity

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import com.intelligent.reader.R
import com.intelligent.reader.adapter.DownloadManagerAdapter
import com.intelligent.reader.presenter.downloadmanager.DownloadManagerPresenter
import com.intelligent.reader.presenter.downloadmanager.DownloadManagerView
import com.intelligent.reader.read.help.BookHelper
import com.intelligent.reader.receiver.DownBookClickReceiver
import com.intelligent.reader.view.DownloadDeleteDialog
import kotlinx.android.synthetic.txtqbdzs.download_manager.*
import kotlinx.android.synthetic.txtqbdzs.download_manager_pager.*
import net.lzbook.kit.book.component.service.DownloadService
import net.lzbook.kit.book.download.CallBackDownload
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.utils.*

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

    private val deleteDialog: DownloadDeleteDialog by lazy {
        val dialog = DownloadDeleteDialog(this)
        dialog.setCheckListener {
            presenter.uploadDialogCheckLog()
        }
        dialog.setCancelListener {
            presenter.uploadDialogCancelLog()
        }
        dialog.setConfirmListener { books, isDeleteBookOfShelf ->
            if (isDeleteBookOfShelf) presenter.deleteBooksOfShelf(books)
            presenter.uploadDialogConfirmLog(books?.size)
            presenter.deleteDownload(books, isDeleteBookOfShelf)
        }
        dialog
    }

    private val removeHelper: RemoveAdapterHelper by lazy {
        val helper = RemoveAdapterHelper(this, downloadAdapter,
                RemoveAdapterHelper.popup_type_download)
        helper.setListView(download_manager_list)
        helper.setOnMenuDeleteListener { checkStates ->
            presenter.uploadDeleteLog()
            isDeleteBookOfShelf = false
            val deleteBooks = presenter.getDeleteBooks(checkStates)
            if (deleteBooks.isEmpty()) {
                toastShort(net.lzbook.kit.R.string.mian_delete_cache_no_choose)
            } else {
                deleteDialog.show(deleteBooks)
            }
        }
        helper.setOnSelectAllListener { checkedAll ->
            presenter.uploadRemoveSelectAllLog(checkedAll)
        }
        helper.setOnMenuStateListener(object : RemoveAdapterHelper.OnMenuStateListener {
            override fun getMenuShownState(isShown: Boolean) {
                isShowing = isShown
            }

            override fun getAllCheckedState(isAll: Boolean) {}
        })
        helper
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.download_manager)
        initView()
        startDownloadService()
    }

    override fun onResume() {
        super.onResume()
        presenter.queryDownloadBooks(false)
    }

    private fun initView() {
        title_back_btn.setOnClickListener {
            presenter.uploadBackLog()
            finish()
        }
        btn_edit.setOnClickListener {
            if (presenter.isDoubleClick(System.currentTimeMillis())) {
                return@setOnClickListener
            }
            if ("编辑" == btn_edit.text) {
                removeHelper.showRemoveMenu(root)
                btn_edit.text = "取消"
                presenter.uploadEditLog()
            } else {
                removeHelper.dismissRemoveMenu()
                btn_edit.text = "编辑"
                presenter.uploadCancelLog()
            }
        }
        title_name_btn.setOnClickListener {
            finish()
        }
        download_manager_list.adapter = downloadAdapter
        download_manager_list.setOnItemClickListener { parent, view, position, id ->
            if (position < 0 || position > presenter.downloadBooks.size) return@setOnItemClickListener
            if (!removeHelper.isRemoveMode && !isShowing) {
                val book = presenter.downloadBooks[position]
                val b = presenter.bookDaoHelper.getBook(book.book_id, 0) as Book
                presenter.uploadBookClickLog(b)
                BookHelper.goToCoverOrRead(applicationContext, this@DownloadManagerActivity, b, 1)
            } else {
                removeHelper.setCheckPosition(position)
            }
        }
        download_manager_list.setOnItemLongClickListener { parent, view, position, id ->
            if (AppUtils.getPackageName() != "cc.kdqbxs.reader") {
                if (!removeHelper.isRemoveMode) {
                    removeHelper.showRemoveMenu(root)
                    btn_edit.text = "取消"
                }
            }
            false
        }
    }

    fun startDownloadService() {
        if (presenter.downloadService != null) {//downloadService已经启用
            presenter.setDownloadService()
        } else {
            val intent = Intent()
            intent.setClass(this, DownloadService::class.java)
            startService(intent)
            bindService(intent, presenter.serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDownloadBookQuery(bookList: ArrayList<Book>, hasDeleted: Boolean) {
        if (bookList.size == 0) {
            btn_edit.visibility = View.GONE
        } else {
            if (AppUtils.getPackageName() == "cc.kdqbxs.reader") {
                btn_edit.visibility = View.GONE
            } else {
                btn_edit.visibility = View.VISIBLE
                if (hasDeleted) {
                    btn_edit.text = "编辑"
                }
            }
        }
        if (bookList.size == 0) {
            download_manager_list.visibility = View.GONE
            empty_bookshelf.visibility = View.VISIBLE
        } else {
            empty_bookshelf.visibility = View.GONE
            download_manager_list.visibility = View.VISIBLE
            presenter.downloadService?.replaceOffLineCallBack(this)
            downloadAdapter.notifyDataSetChanged()
        }
    }

    override fun onDownloadDelete(isDeleteOfShelf: Boolean) {
        if (isDeleteOfShelf) {
            presenter.queryDownloadBooks(true)// FIXME
        } else {
            downloadAdapter.notifyDataSetChanged()
        }
        removeHelper.dismissRemoveMenu()
        if (presenter.downloadBooks.isEmpty()) {
            btn_edit.visibility = View.GONE
        } else {
            btn_edit.text = "编辑"
        }
    }

    fun addBookToService(books: ArrayList<Book>) {
        books.forEach {
            BookHelper.addDownBookTask(this@DownloadManagerActivity, it,
                    this, true)
        }
    }

    fun pendingIntent(bookId: String?): PendingIntent {
        val pending: PendingIntent
        val intent: Intent
        if (bookId != (-1).toString() + "") {
            intent = Intent(this, DownBookClickReceiver::class.java)
            intent.action = DownBookClickReceiver.action
            intent.putExtra("book_id", bookId)
            pending = PendingIntent.getBroadcast(applicationContext, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            intent = Intent(this,
                    DownloadManagerActivity::class.java)
            pending = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        return pending
    }

    fun stopDownloadBook(book_id: String) {
        if (presenter.downloadService != null) {
            presenter.downloadService?.cancelTask(book_id)
        }
    }

    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        removeHelper.showRemoveMenu(root)
        return false
    }

    override fun onBackPressed() {
        if (removeHelper.isRemoveMode) {
            removeHelper.dismissRemoveMenu()
            btn_edit.text = "编辑"
        } else {
            //如果是从通知栏过来, 且已经退出到home了, 要回到应用中
            if (isTaskRoot) {
                val intent = Intent(this, SplashActivity::class.java)
                startActivity(intent)
            }
            super.onBackPressed()
        }
    }

    override fun onTaskStart(book_id: String?) {
        downloadAdapter.notifyDataSetChanged()
    }

    override fun onChapterDownStart(book_id: String?, sequence: Int) {}

    override fun onChapterDownFinish(book_id: String?, sequence: Int) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - time > 1000) {
            time = System.currentTimeMillis()
            downloadAdapter.notifyDataSetChanged()
        }
    }

    override fun onChapterDownFailed(book_id: String?, sequence: Int, msg: String?) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastShowTime > 4000) {
            lastShowTime = currentTime
            toastLong(msg)
        }
        downloadAdapter.notifyDataSetChanged()
    }

    override fun onChapterDownFailedNeedLogin() {}

    override fun onChapterDownFailedNeedPay(book_id: String?, nid: Int, sequence: Int) {}

    override fun onTaskFinish(book_id: String?) {
        val book = presenter.bookDaoHelper.getBook(book_id, 0)
        toastShort( "${book.name}缓存完成")
        if (presenter.downloadService?.getDownBookTask(book_id) != null
                && presenter.downloadService?.getDownBookTask(book_id)?.state == DownloadState.FINISH) {
            val data = presenter.downloadBooks
            for (b in data) {
                if (b.book_id != null && book.book_id != null && b.book_id == book.book_id) {
                    data.remove(b)
                    break
                }
            }
        }
        presenter.queryDownloadBooks(true)
    }

    override fun onProgressUpdate(book_id: String?, progress: Int) {
        if (System.currentTimeMillis() - time > 500) {
            time = System.currentTimeMillis()
            runOnMain { downloadAdapter.notifyDataSetChanged() }
        }
    }

    override fun onOffLineFinish() {
        AppLog.d(TAG, "onOffLineFinish")
    }

}