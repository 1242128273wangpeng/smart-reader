package com.dingyue.downloadmanager

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.ding.basic.bean.Book
import com.dingyue.downloadmanager.contract.BookHelperContract
import com.dingyue.downloadmanager.contract.CacheManagerContract
import kotlinx.android.synthetic.qbmfrmxs.act_download_manager.*
import net.lzbook.kit.base.activity.BaseCacheableActivity
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.download.CallBackDownload
import net.lzbook.kit.utils.oneclick.OneClickUtil
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.utils.uiThread
import java.util.*

/**
 * Created by qiantao on 2017/11/22 0022
 */
@Route(path = RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)
class DownloadManagerActivity : BaseCacheableActivity(), CallBackDownload, DownloadManagerListener {

    private var downloadManagerAdapter: DownloadManagerAdapter? = null

    private var time = System.currentTimeMillis()

    private var lastShowTime = 0L

    private val titles = object : ArrayList<String>() {
        init {
            add("未缓存")
            add("已缓存")
        }
    }

    private val cacheFragment: DownloadManagerFragment by lazy {
        val fragment = DownloadManagerFragment()
        val bundle = Bundle()
        bundle.putString("title", titles[0])
        fragment.arguments = bundle
        fragment
    }

    private val cachedFragment: DownloadManagerFragment by lazy {
        val fragment = DownloadManagerFragment()
        val bundle = Bundle()
        bundle.putString("title", titles[1])
        fragment.arguments = bundle
        fragment
    }

    private val topMenuPopup: DownloadManagerMenuPopup by lazy {
        val popup = DownloadManagerMenuPopup(this)
        popup.setOnEditClickListener {
            if (nbs_navigation.checkScrollState()) {
                ToastUtil.showToastMessage("当前页面位置不正确！")
            } else {
                showMenu()
            }
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

        txt_head_select_all.setOnClickListener {
            if (OneClickUtil.isDoubleClick(System.currentTimeMillis())) {
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
            topMenuPopup.show(img_head_more)
            DownloadManagerLogger.uploadCacheManagerMore()
        }

        txt_head_complete.setOnClickListener {
            dismissMenu()
        }

        downloadManagerAdapter = DownloadManagerAdapter(supportFragmentManager, nbs_navigation, vp_result)

        downloadManagerAdapter?.insertNavigationTable(titles[0])

        downloadManagerAdapter?.insertNavigationTable(titles[1])
    }

    override fun onBackPressed() {
        if (checkRemoveState()) {
            dismissMenu()
        } else {
            //如果是从通知栏过来, 且已经退出到home了, 要回到应用中
            if (isTaskRoot) {
                RouterUtil.navigation(this, RouterConfig.SPLASH_ACTIVITY)
            }
            super.onBackPressed()
        }
    }

    override fun onTaskFinish(book_id: String) {
        val book = BookHelperContract.loadLocalBook(book_id)

        if (book != null) {
            refreshBookState(book)
        }
    }


    override fun onTaskStatusChange(book_id: String?) {
        refreshData()
    }

    override fun onTaskFailed(book_id: String?, t: Throwable?) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastShowTime > 4000) {
            lastShowTime = currentTime
        }
        refreshData()
    }

    override fun onTaskProgressUpdate(book_id: String?) {
        if (System.currentTimeMillis() - time > 500) {
            time = System.currentTimeMillis()
            uiThread { refreshData() }
        }
    }

    override fun supportSlideBack(): Boolean {
        return false
    }

    override fun navigationBookStore() {
        DownloadManagerLogger.uploadCacheManagerBookCity()

        val bundle = Bundle()
        bundle.putInt("position", 1)
        RouterUtil.navigation(this, RouterConfig.HOME_ACTIVITY, bundle)
        finish()
    }

    override fun changeRemoveViewState(show: Boolean) {
        img_head_more.visibility = if (show) View.GONE else View.VISIBLE
        img_head_back.visibility = if (show) View.GONE else View.VISIBLE

        txt_head_title.text = if (show) getString(R.string.edit_cache) else getString(R.string.download_manager)

        txt_head_complete.visibility = if (show) View.VISIBLE else View.GONE

        txt_head_select_all.text = getString(R.string.select_all)
        txt_head_select_all.visibility = if (show) View.VISIBLE else View.GONE

        //允许、禁止ViewPager滑动
        vp_result.insertRemoveAble(!show)

        nbs_navigation.insertClickAble(!show)
    }

    override fun changeSelectAllContent(content: String) {
        txt_head_select_all.text = content
    }

    private fun showMenu() {
        val position = vp_result.currentItem

        if (position == 0) {
            cacheFragment.showMenu()
        } else if (position == 1) {
            cachedFragment.showMenu()
        }
    }

    private fun dismissMenu() {
        val position = vp_result.currentItem

        if (position == 0) {
            cacheFragment.dismissMenu()
        } else if (position == 1) {
            cachedFragment.dismissMenu()
        }
    }

    private fun checkAll(isAll: Boolean) {
        val position = vp_result.currentItem

        if (position == 0) {
            cacheFragment.checkAll(isAll)
        } else if (position == 1) {
            cachedFragment.checkAll(isAll)
        }
    }

    private fun sortBooks(type: Int) {
        val position = vp_result.currentItem

        if (position == 0) {
            cacheFragment.sortBooks(type)
        } else if (position == 1) {
            cachedFragment.sortBooks(type)
        }
    }

    private fun refreshData() {
        val position = vp_result.currentItem

        if (position == 0) {
            cacheFragment.refreshData()
        } else if (position == 1) {
            cachedFragment.refreshData()
        }
    }

    private fun refreshBookState(book: Book) {
        val position = vp_result.currentItem

        if (position == 0) {
            cacheFragment.refreshBookState(book)
        } else if (position == 1) {
            cachedFragment.refreshBookState(book)
        }
    }

    private fun checkRemoveState(): Boolean {
        val position = vp_result.currentItem

        return when (position) {
            0 -> cacheFragment.loadRemoveState()
            1 -> cachedFragment.loadRemoveState()
            else -> false
        }
    }

    inner class DownloadManagerAdapter(fragmentManager: FragmentManager, private var navigationBarStrip: NavigationBarStrip, private var viewPager: ViewPager) : FragmentStatePagerAdapter(fragmentManager) {

        init {
            this.viewPager.adapter = this
            navigationBarStrip.insertViewPager(viewPager)
        }

        @JvmOverloads
        fun remove(index: Int = 0) {
            var removeIndex = index

            if (title.isEmpty()) {
                return
            }

            if (index < 0) {
                removeIndex = 0
            }

            if (index >= titles.size) {
                removeIndex = titles.size - 1
            }

            titles.removeAt(removeIndex)
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return titles.size
        }

        override fun getItemPosition(any: Any): Int {
            return PagerAdapter.POSITION_NONE
        }

        override fun getItem(position: Int): Fragment? {
            return when (position) {
                0 -> cacheFragment
                1 -> cachedFragment
                else -> null
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }

        fun insertNavigationTable(title: String) {
            if (TextUtils.isEmpty(title)) {
                return
            }

            navigationBarStrip.insertTitle(title)

            notifyDataSetChanged()
        }

        fun recycle() {
            viewPager.removeAllViews()
        }
    }
}