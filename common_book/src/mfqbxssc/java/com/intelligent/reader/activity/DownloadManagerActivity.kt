package com.intelligent.reader.activity

import com.intelligent.reader.R
import com.intelligent.reader.read.help.BookHelper
import com.intelligent.reader.view.DownloadPager
import com.intelligent.reader.view.DownloadPager.DeleteItemListener

import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.utils.FrameBookHelper.CachedComparator
import net.lzbook.kit.utils.FrameBookHelper.MultiComparator
import net.lzbook.kit.utils.SettingItemsHelper
import net.lzbook.kit.utils.StatServiceUtils

import android.content.Intent
import android.content.res.Resources.NotFoundException
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.view.KeyEvent
import android.view.Menu
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import net.lzbook.kit.book.download.CallBackDownload

import java.util.ArrayList
import java.util.Collections
import java.util.HashMap

class DownloadManagerActivity : BaseCacheableActivity(), OnClickListener, OnItemClickListener, OnItemLongClickListener {
    private var alertDialog: MyDialog? = null
    private var back_btn: ImageView? = null
    private var content_layout: ViewGroup? = null
    private var downloadingBooks: ArrayList<Book>? = null
    var lastClickTime: Long = 0
    private var mBookDaoHelper: BookDaoHelper? = null
    private var more: ImageView? = null
    private var title_name_btn: TextView? = null
    private var editCancel: TextView? = null
    private var editSelectAll: TextView? = null
    var views: DownloadPager? = null

    override
    fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        try {
            setContentView(R.layout.download_manager)
        } catch (e: NotFoundException) {
            e.printStackTrace()
        }

        initView()
        initData()
        initListener()
        Constants.isDownloadManagerActivity = true
        Constants.hadShownMobilNetworkConfirm = false
    }

    override
    protected fun onStart() {
        super.onStart()
        initService()
    }

    override
    protected fun onStop() {
        super.onStop()
    }

    override
    protected fun onDestroy() {
        Constants.isDownloadManagerActivity = false
        Constants.hadShownMobilNetworkConfirm = false
        CacheManager.listeners.remove(this.views as CallBackDownload)
        if (this.alertDialog != null) {
            this.alertDialog = null
        }
        try {
            setContentView(R.layout.empty)
        } catch (e: NotFoundException) {
            e.printStackTrace()
        }

        super.onDestroy()
    }

    override
    protected fun onResume() {
        try {
            super.onResume()
            freshBooks(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun initService() {}

    fun stopDownloadbook(book_id: String) {
        CacheManager.stop(book_id)
    }

    private fun initView() {
        this.back_btn = findViewById(R.id.title_back_btn) as ImageView
        this.more = findViewById(R.id.ibtn_more) as ImageView
        this.content_layout = findViewById(R.id.content_layout) as ViewGroup
        this.title_name_btn = findViewById(R.id.title_name_btn) as TextView
        this.editCancel = findViewById(R.id.edit_cancel) as TextView
        this.editSelectAll = findViewById(R.id.edit_select_all) as TextView
    }

    private fun initListener() {
        this.back_btn!!.setOnClickListener(this)
        if (this.more != null) {
            this.more!!.setOnClickListener(this)
        }
        if (this.editCancel != null) {
            this.editCancel!!.setOnClickListener(this)
        }
        if (this.editSelectAll != null) {
            this.editSelectAll!!.setOnClickListener(this)
        }
        this.title_name_btn!!.setOnClickListener(this)
        this.views!!.setDeleteItemListener(object :DeleteItemListener {
            override
            fun onSuccess() {
                this@DownloadManagerActivity.editCancel!!.setVisibility(View.GONE)
                this@DownloadManagerActivity.editSelectAll!!.setVisibility(View.GONE)
                this@DownloadManagerActivity.more!!.setVisibility(View.VISIBLE)
                this@DownloadManagerActivity.back_btn!!.setVisibility(View.VISIBLE)
            }

            override
            fun onFailed() {
                this@DownloadManagerActivity.editCancel!!.setVisibility(View.GONE)
                this@DownloadManagerActivity.editSelectAll!!.setVisibility(View.GONE)
                this@DownloadManagerActivity.more!!.setVisibility(View.VISIBLE)
                this@DownloadManagerActivity.back_btn!!.setVisibility(View.VISIBLE)
            }
        })
        this.views!!.getListView().setOnItemClickListener(this)
        this.views!!.getListView().setOnItemLongClickListener(this)
    }

    private fun initData() {
        this.mBookDaoHelper = BookDaoHelper.getInstance()
        this.downloadingBooks = ArrayList()
        this.views = DownloadPager(getApplicationContext(), this, this.downloadingBooks)
        CacheManager.listeners.add(this.views as CallBackDownload)
        this.content_layout!!.addView(this.views)
    }

    fun freshBooks(hasDeleted: Boolean) {
        CacheManager.freshBooks(false)
        getDownLoadBookList(hasDeleted)
    }

    private fun getDownLoadBookList(hasDeleted: Boolean) {
        if (this.mBookDaoHelper == null) {
            this.mBookDaoHelper = BookDaoHelper.getInstance()
        }
        val books = this.mBookDaoHelper!!.getBooksOnLineList()
        if (this.downloadingBooks != null && this.views != null) {
            this.downloadingBooks!!.clear()
            this.downloadingBooks!!.addAll(books)
            Collections.sort(this.downloadingBooks)
            Collections.sort(this.downloadingBooks, MultiComparator())
            Collections.sort(this.downloadingBooks, CachedComparator())
            if (downloadingBooks!!.isEmpty()) {
                this.more!!.setVisibility(View.GONE)
            }
            this.views!!.freshBookList(this.downloadingBooks)
        }
    }

    fun checkSelectAll(isAll: Boolean) {
        if (isAll) {
            this.editSelectAll!!.setText("取消全选")
        } else {
            this.editSelectAll!!.setText("全选")
        }
    }

    override
    fun onClick(v: View) {

        val data = HashMap<String, String>()
        when (v.getId()) {
        //            case R.id.btn_edit:
        //                this.views.dissmissremoveMenu();
        //                this.editCancel.setVisibility(View.GONE);
        //                this.editSelectAll.setVisibility(View.GONE);
        //                this.more.setVisibility(View.VISIBLE);
        //                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.bs_down_m_click_cancel);
        //                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.CACHEMANAGE_PAGE, StartLogClickUtil.CANCLE);
        //                return;
            R.id.edit_cancel -> {
                if (!isDoubleClick(System.currentTimeMillis())) {
                    this.views!!.removehelper.dismissRemoveMenu()
                    this.editCancel!!.setVisibility(View.GONE)
                    this.editSelectAll!!.setVisibility(View.GONE)
                    this.more!!.setVisibility(View.VISIBLE)
                    this.back_btn!!.setVisibility(View.VISIBLE)
                }
                return
            }
            R.id.edit_select_all -> {
                val isAll = "全选".equals(this.editSelectAll!!.getText())
                if (isAll) {
                    this.editSelectAll!!.setText("取消全选")
                } else {
                    this.editSelectAll!!.setText("全选")
                }
                views!!.setSelectAll(isAll)
                return
            }
            R.id.title_back_btn -> {
                data.put("type", "1")
                StartLogClickUtil.upLoadEventLog(this, "CACHEMANAGE", StartLogClickUtil.BACK, data)
                finish()
                return
            }
            R.id.title_name_btn -> {
                finish()
                return
            }
        //            case R.id.btn_edit:
        //                if (!isDoubleClick(System.currentTimeMillis())) {
        //                    if ("编辑".equals(this.editBtn.getText())) {
        //                        this.views.showRemoveMenu(this.views);
        //                        this.editBtn.setText("取消");
        //                        StatServiceUtils.statAppBtnClick(this, StatServiceUtils.bs_down_m_click_edit);
        //                        StartLogClickUtil.upLoadEventLog(this, "CACHEMANAGE", "CACHEEDIT");
        //                        return;
        //                    }
        //                    this.views.dissmissremoveMenu();
        //                    this.editBtn.setText("编辑");
        //                    StatServiceUtils.statAppBtnClick(this, StatServiceUtils.bs_down_m_click_cancel);
        //                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.CHCHEEDIT_PAGE, "CANCLE");
        //                    return;
        //                }
        //                return;
            R.id.ibtn_more -> {

                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.CACHEMANAGE_PAGE, StartLogClickUtil.MORE)


                if (!isDoubleClick(System.currentTimeMillis())) {
                    val inflate = getLayoutInflater().inflate(R.layout.download_head_more_pop, null)
                    inflate.measure(0, 0)
                    val popupWindow = PopupWindow(inflate, -2, -2)
                    popupWindow.setBackgroundDrawable(ColorDrawable(ViewCompat.MEASURED_SIZE_MASK))
                    popupWindow.setFocusable(true)
                    popupWindow.setTouchable(true)
                    popupWindow.setOutsideTouchable(false)
                    this.more!!.getLocationOnScreen(IntArray(2))
                    popupWindow.showAsDropDown(this.more, 0, -this.more!!.getHeight())
                    val settingItemsHelper = SettingItemsHelper.getSettingHelper(getApplicationContext())
                    val txt_read = inflate.findViewById(R.id.download_head_pop_sort_read) as TextView
                    val txt_time = inflate.findViewById(R.id.download_head_pop_sort_time) as TextView
                    if (settingItemsHelper.getValues().booklist_sort_type === 0) {
                        txt_read.setTextColor(getResources().getColor(R.color.color_primary))
                    } else {
                        txt_time.setTextColor(getResources().getColor(R.color.color_primary))
                    }
                    txt_read.setOnClickListener(object : OnClickListener {
                        override
                        fun onClick(v: View) {
                            settingItemsHelper.putInt(settingItemsHelper.booklistSortType, 0)
                            Constants.book_list_sort_type = 0
                            popupWindow.dismiss()
                            this@DownloadManagerActivity.freshBooks(false)
                            data.put("type", "0")
                            StartLogClickUtil.upLoadEventLog(this@DownloadManagerActivity, StartLogClickUtil.CACHEMANAGE_PAGE, StartLogClickUtil.SORT, data)
                        }
                    })
                    txt_time.setOnClickListener(object : OnClickListener {
                        override
                        fun onClick(v: View) {
                            settingItemsHelper.putInt(settingItemsHelper.booklistSortType, 1)
                            Constants.book_list_sort_type = 1
                            popupWindow.dismiss()
                            this@DownloadManagerActivity.freshBooks(false)
                            data.put("type", "1")
                            StartLogClickUtil.upLoadEventLog(this@DownloadManagerActivity, StartLogClickUtil.CACHEMANAGE_PAGE, StartLogClickUtil.SORT, data)

                        }
                    })
                    inflate.findViewById(R.id.download_head_pop_edit).setOnClickListener(object : OnClickListener {
                        override
                        fun onClick(v: View) {
                            this@DownloadManagerActivity.editCancel!!.setVisibility(View.VISIBLE)
                            this@DownloadManagerActivity.editSelectAll!!.setVisibility(View.VISIBLE)
                            this@DownloadManagerActivity.more!!.setVisibility(View.GONE)
                            this@DownloadManagerActivity.back_btn!!.setVisibility(View.GONE)
                            this@DownloadManagerActivity.views!!.showRemoveMenu(this@DownloadManagerActivity.views)
                            StatServiceUtils.statAppBtnClick(this@DownloadManagerActivity, StatServiceUtils.bs_down_m_click_edit)
                            StartLogClickUtil.upLoadEventLog(this@DownloadManagerActivity, StartLogClickUtil.CACHEMANAGE_PAGE, StartLogClickUtil.CACHEEDIT)
                            popupWindow.dismiss()
                        }
                    })
                    return
                }
                return
            }
            else -> return
        }
    }

    override
    fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        this.editCancel!!.setVisibility(View.VISIBLE)
        this.editSelectAll!!.setVisibility(View.VISIBLE)
        this.more!!.setVisibility(View.GONE)
        this.back_btn!!.setVisibility(View.GONE)
        this.views!!.showRemoveMenu(this.views)
        return false
    }

    override
    fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override
    fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode != 4 || this.alertDialog == null || !this.alertDialog!!.isShowing()) {
            return super.onKeyDown(keyCode, event)
        }
        this.alertDialog!!.cancel()
        return true
    }

    override
    fun onItemLongClick(adapterView: AdapterView<*>, view: View, position: Int, id: Long): Boolean {
        if (!this.views!!.isRemoveMode()) {
            this.editCancel!!.setVisibility(View.VISIBLE)
            this.editSelectAll!!.setVisibility(View.VISIBLE)
            this.more!!.setVisibility(View.GONE)
            this.back_btn!!.setVisibility(View.GONE)
            this.views!!.showRemoveMenu(findViewById(R.id.root))
        }
        return false
    }

    override
    fun onItemClick(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
        if (this.views!!.isRemoveMode() || this.views!!.isShowing) {
            this.views!!.setRemoveChecked(position)
            return
        }
        val book = this.views!!.getAdapter().getItem(position) as Book
        if (book != null) {
            val data = HashMap<String, String>()
            data.put("STATUS", if (CacheManager.getBookStatus(book) === DownloadState.FINISH) "1" else "0")
            StartLogClickUtil.upLoadEventLog(this, "CACHEMANAGE", "BOOKCLICK", data)
            BookHelper.goToCoverOrRead(getApplicationContext(), this, book, 1)
        }
    }

    override
    fun onBackPressed() {
        if (this.views!!.removehelper == null || !this.views!!.removehelper.isRemoveMode()) {
            if (isTaskRoot()) {
                startActivity(Intent(this, SplashActivity::class.java))
            }
            super.onBackPressed()
            return
        }
        this.views!!.removehelper.dismissRemoveMenu()
        this.editCancel!!.setVisibility(View.GONE)
        this.editSelectAll!!.setVisibility(View.GONE)
        this.more!!.setVisibility(View.VISIBLE)
        this.back_btn!!.setVisibility(View.VISIBLE)
    }

    fun isDoubleClick(time: Long): Boolean {
        if (time - this.lastClickTime <= 800) {
            return true
        }
        this.lastClickTime = time
        return false
    }

    override
    fun supportSlideBack(): Boolean {
        return if (isTaskRoot() || this.views!!.isRemoveMode()) false else true
    }

    companion object {
        private val TAG = "DownloadManagerActivity"
    }
}
