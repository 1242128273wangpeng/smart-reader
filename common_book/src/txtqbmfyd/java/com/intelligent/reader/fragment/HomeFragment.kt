package com.intelligent.reader.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.FrameLayout
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI
import com.baidu.mobstat.StatService
import com.intelligent.reader.BuildConfig
import com.intelligent.reader.R
import com.intelligent.reader.activity.*
import com.intelligent.reader.app.BookApplication
import com.intelligent.reader.presenter.home.HomePresenter
import com.intelligent.reader.presenter.home.HomeView
import com.intelligent.reader.widget.BookSortingDialog
import com.intelligent.reader.widget.ClearCacheDialog
import com.intelligent.reader.widget.HomeMenuPopup
import com.intelligent.reader.widget.drawer.DrawerLayout
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import iyouqu.theme.ThemeMode
import kotlinx.android.synthetic.txtqbmfyd.content_view.*
import kotlinx.android.synthetic.txtqbmfyd.content_view_main.*
import kotlinx.android.synthetic.txtqbmfyd.content_view_menu.*
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.view.NonSwipeViewPager
import net.lzbook.kit.cache.DataCleanManager
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.constants.SPKeys
import net.lzbook.kit.data.bean.ReadConfig
import net.lzbook.kit.encrypt.URLBuilderIntterface
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.*
import net.lzbook.kit.utils.update.ApkUpdateUtils
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 主页面
 */
class HomeFragment : BaseFragment(), FrameBookHelper.SearchUpdateBook, HomeView {

    private val TAG = HomeFragment::class.java.simpleName

    private val presenter by lazy { HomePresenter(this) }

    private var bookShelfFragment: BookShelfFragment? = null

    private lateinit var apkUpdateUtils: ApkUpdateUtils

    private lateinit var parent: HomeActivity

    private val recommendFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "recommend")
        val uri = URLBuilderIntterface.WEB_RECOMMEND.replace("{packageName}", AppUtils.getPackageName())
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }

    private val rankingFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "rank")
        val uri = URLBuilderIntterface.WEB_RANK.replace("{packageName}", AppUtils.getPackageName())
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }

    private val categoryFragment: CategoryFragment by lazy {
        val fragment = CategoryFragment()
        val bundle = Bundle()
//        bundle.putString("type", "category")
//        val uri = URLBuilderIntterface.WEB_CATEGORY.replace("{packageName}", AppUtils.getPackageName())
//        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
//        fragment.arguments = bundle
        fragment
    }

    private val clearCacheDialog: ClearCacheDialog by lazy {
        val dialog = ClearCacheDialog(activity)
        dialog.setOnConfirmListener {
            dialog.showLoading()
            activity.doAsync {
                CacheManager.removeAll()
                UIHelper.clearAppCache()
                DataCleanManager.clearAllCache(activity.applicationContext)
                Thread.sleep(1000)
                uiThread {
                    dialog.dismiss()
                    txt_clear_cache_message.text = "0B"
                }
            }
        }
        dialog
    }

    private val homeMenuPopup: HomeMenuPopup by lazy {
        val popup = HomeMenuPopup(activity)
        popup.setOnDownloadClickListener {
            startActivity(Intent(activity, DownloadManagerActivity::class.java))
            presenter.uploadDownloadManagerLog()
        }
        popup.setOnSortingClickListener {
            bookSortingDialog.show()
            presenter.uploadBookSortingLog()
        }
        popup
    }

    private val settingItemsHelper by lazy { SettingItemsHelper.getSettingHelper(context) }

    private val bookSortingDialog: BookSortingDialog by lazy {
        val dialog = BookSortingDialog(activity)
        dialog.setOnRecentReadClickListener {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_cli_shelf_rak_time)
            settingItemsHelper.putInt(settingItemsHelper.booklistSortType, 0)
            Constants.book_list_sort_type = 0
            bookShelfFragment?.updateUI()
        }
        dialog.setOnUpdateTimeClickListener {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_cli_shelf_rak_time)
            settingItemsHelper.putInt(settingItemsHelper.booklistSortType, 1)
            Constants.book_list_sort_type = 1
            bookShelfFragment?.updateUI()
        }
        dialog
    }

    private var fManager: FragmentManager? = null
    private var adapter: MainAdapter? = null
    private var frameHelper: FrameBookHelper? = null
    private var currentTab = 0
    private var versionCode: Int = 0
    private val titles = arrayOf("书架", "推荐", "榜单", "分类")
    private var b: Boolean = true
    private var bottomType: Int = 0//青果打点搜索 2 推荐  3 榜单
    private lateinit var preferencesUtils: SharedPreferencesUtils
    lateinit var viewPager: NonSwipeViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesUtils = SharedPreferencesUtils(PreferenceManager.getDefaultSharedPreferences(context))
        versionCode = AppUtils.getVersionCode()
        parent = activity as HomeActivity
        apkUpdateUtils = ApkUpdateUtils(activity)
        try {
            fManager = this.childFragmentManager
        } catch (e: NoSuchMethodError) {
            e.printStackTrace()
        }
    }

    override fun getFrameView(inflater: LayoutInflater): View {
        try {
            mFrameView = inflater.inflate(R.layout.content_view, null)
            AppLog.e(TAG, "-->>HomeFragment")
        } catch (e: InflateException) {
            e.printStackTrace()
            //need restart app
            activity.finish()
            val intent = Intent(activity, SplashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            return FrameLayout(activity)
        }
        return mFrameView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initGuide()
        initView()
    }

    override fun setOnActivityCreate() {
        adapter?.notifyDataSetChanged()
        frameCallback?.frameHelper()
        if (frameHelper == null && actReference != null && actReference.get() != null) {
            frameHelper = (actReference.get() as HomeActivity).frameHelper
        }
        frameHelper?.setSearchUpdateUI(this)
        frameHelper?.registSearchUpdateReceiver()

        showCacheMessage()
    }

    private fun initGuide() {
        val key = versionCode.toString() + Constants.BOOKSHELF_GUIDE_TAG
        if (!preferencesUtils.getBoolean(key)) {
            fl_guide_layout.visibility = View.VISIBLE
            img_guide_remove.visibility = View.VISIBLE
            fl_guide_layout.setOnClickListener {
                if (b) {
                    img_guide_download.visibility = View.VISIBLE
                    img_guide_remove.visibility = View.GONE
                    b = false
                } else {
                    preferencesUtils.putBoolean(key, true)
                    img_guide_download.visibility = View.GONE
                    fl_guide_layout.visibility = View.GONE
                }
            }
        }
    }

    private fun initView() {
        //main
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                onTabSelected(position)
            }
        })
        view_pager.offscreenPageLimit = 3
        view_pager.isScrollable = false
        fManager?.let {
            adapter = MainAdapter(it)
        }
        view_pager.adapter = adapter
        viewPager = view_pager
        frameCallback?.getViewPager(view_pager)

        selectTab(currentTab)
        onTabSelected(currentTab)

        drawer_layout.setOnMenuStateChangeListener { state ->
            if (state == DrawerLayout.MenuState.MENU_OPENED) {
                showCacheMessage()
                val bookShelfRemoveHelper = bookShelfFragment?.bookShelfRemoveHelper
                if (bookShelfRemoveHelper?.isRemoveMode == true) {
                    bookShelfRemoveHelper.dismissRemoveMenu()
                }
            }
        }

        img_head_setting.setOnClickListener {
//            EventBus.getDefault().post(ConsumeEvent(R.id.red_point_head_setting))
            if (drawer_layout.isOpened) {
                drawer_layout.closeMenu()
            } else {
                drawer_layout.openMenu()
            }
            presenter.uploadHeadSettingLog()
        }

        img_head_search.setOnClickListener {
            AppLog.e(TAG, "SearchBookActivity -----> Start")
            startActivity(Intent(context, SearchBookActivity::class.java))
            presenter.uploadHeadSearchLog(bottomType)
        }

        rl_recommend_search.setOnClickListener {
            startActivity(Intent(context, SearchBookActivity::class.java))
        }

        img_ranking_search.setOnClickListener {
            startActivity(Intent(context, SearchBookActivity::class.java))
        }

        img_head_menu.setOnClickListener {
            homeMenuPopup.show(img_head_menu)
        }

        ll_bottom_tab_bookshelf.setOnClickListener {
            AppLog.e(TAG, "BookShelf Selected")
            selectTab(0)
            rl_head_bookshelf.visibility = View.VISIBLE
            rl_recommend_head.visibility = View.GONE
            rl_head_ranking.visibility = View.GONE
            presenter.uploadBookshelfSelectedLog()
        }

        ll_bottom_tab_recommend.setOnClickListener {
            AppLog.e(TAG, "Selection Selected")
            rl_head_bookshelf.visibility = View.INVISIBLE
            rl_recommend_head.visibility = View.VISIBLE
            rl_head_ranking.visibility = View.GONE
            selectTab(1)
            //双击回到顶部
            if (AppUtils.isDoubleClick(System.currentTimeMillis())) {
                if (view_pager.currentItem == 1) {
                    recommendFragment.loadWebData(recommendFragment.url)
                }
            }
            preferencesUtils.putString(Constants.FINDBOOK_SEARCH, "recommend")
            presenter.uploadRecommendSelectedLog()
        }

        ll_bottom_tab_ranking.setOnClickListener {
            AppLog.e(TAG, "Ranking Selected")
            rl_head_bookshelf.visibility = View.INVISIBLE
            rl_recommend_head.visibility = View.GONE
            rl_head_ranking.visibility = View.VISIBLE
            selectTab(2)
            preferencesUtils.putString(Constants.FINDBOOK_SEARCH, "top")
            presenter.uploadRankingSelectedLog()
        }

        ll_bottom_tab_category.setOnClickListener {
            AppLog.e(TAG, "Classify Selected")
            rl_head_bookshelf.visibility = View.GONE
            rl_recommend_head.visibility = View.GONE
            rl_head_ranking.visibility = View.GONE
            selectTab(3)
            preferencesUtils.putString(Constants.FINDBOOK_SEARCH, "class")
            presenter.uploadCategorySelectedLog()
        }

//        img_editor_back.setOnClickListener {
        //TODO move
//            bookShelfFragment?.bookShelfRemoveHelper?.dismissRemoveMenu()
//            presenter.uploadEditorBackLog()
//        }

        txt_editor_select_all.setOnClickListener {
            val bookShelfRemoveHelper = bookShelfFragment?.bookShelfRemoveHelper
            val isAllSelected = bookShelfRemoveHelper?.isAllChecked ?: false
            if (isAllSelected) {
                txt_editor_select_all.text = getString(R.string.select_all)
                bookShelfRemoveHelper?.selectAll(false)
            } else {
                txt_editor_select_all.text = getString(R.string.select_all_cancel)
                bookShelfRemoveHelper?.selectAll(true)
            }
            presenter.uploadEditorSelectAllLog(isAllSelected)
        }


        //menu
        val isNightMode = parent.mThemeHelper.isNight
        presenter.uploadCurModeLog(isNightMode)
        if (isNightMode) {
            tv_night_shift.setText(R.string.mode_day)
            bt_night_shift.isChecked = true
        } else {
            tv_night_shift.setText(R.string.mode_night)
            bt_night_shift.isChecked = false
        }
        bt_night_shift.setOnCheckedChangeListener { _, isChecked ->
            presenter.uploadModeChangeLog()
            if (isChecked) {
                tv_night_shift.setText(R.string.mode_day)
                preferencesUtils.putInt("current_light_mode", ReadConfig.MODE)
                ReadConfig.MODE = 61
                parent.mThemeHelper.setMode(ThemeMode.NIGHT)
            } else {
                tv_night_shift.setText(R.string.mode_night)
                preferencesUtils.putInt("current_night_mode", ReadConfig.MODE)
                ReadConfig.MODE = preferencesUtils.getInt("current_light_mode", 51)
                parent.mThemeHelper.setMode(ThemeMode.THEME1)
            }
            preferencesUtils.putInt("content_mode", ReadConfig.MODE)
            parent.nightShift(isChecked, true)
        }

        val isAutoDownload = PreferenceManager.getDefaultSharedPreferences(activity)
                .getBoolean(SPKeys.Setting.AUTO_UPDATE_CAHCE, true)
        btn_auto_download.isChecked = isAutoDownload
        btn_auto_download.setOnCheckedChangeListener { view, isChecked ->
            preferencesUtils.putBoolean(SPKeys.Setting.AUTO_UPDATE_CAHCE, isChecked)
            presenter.uploadAutoCacheLog(isChecked)
        }

        txt_push_setting.setOnClickListener {
            presenter.uploadPushSettingClickLog()
            startActivity(Intent(activity, SettingMoreActivity::class.java))
        }

        txt_feedback.setOnClickListener {
            presenter.uploadFeedbackClickLog()
            Observable.timer(500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        FeedbackAPI.openFeedbackActivity()
                    }
        }

        txt_mark.setOnClickListener {
            presenter.uploadMarkClickLog()
            try {
                val uri = Uri.parse("market://details?id=" + activity.packageName)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e: Exception) {
                activity.toastShort(R.string.menu_no_market)
            }

        }

        txt_disclaimer_statement.setOnClickListener {
            presenter.uploadDisclaimerClickLog()
            startActivity(Intent(activity, DisclaimerActivity::class.java))
        }

        val versionName = "V${AppUtils.getVersionName()}"
        txt_version_name.text = versionName
        rl_check_update.setOnClickListener {
            presenter.uploadCheckUpdateLog()
            try {
                apkUpdateUtils.getApkUpdateInfo(activity, null, "SettingActivity")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        rl_clear_cache.setOnClickListener {
            presenter.uploadClearCacheClickLog()
            clearCacheDialog.show()
        }

    }

    override fun searchUpdateBook() {

    }

    fun getDrawerLayout(): DrawerLayout = drawer_layout

    fun selectTab(position: Int) {
        if (currentTab != position) {
            AppLog.e(TAG, "position: " + position)
//            view_pager.currentItem = position
            view_pager.setCurrentItem(position,false)
        }
    }

    private fun onTabSelected(position: Int) {
        currentTab = position
        bottomType = position + 1
        if (currentTab != 0) {
            bookShelfFragment?.bookShelfRemoveHelper?.dismissRemoveMenu()
        }
        txt_head_title.text = titles[position]
        ll_bottom_tab_bookshelf.isSelected = position == 0
        ll_bottom_tab_recommend.isSelected = position == 1
        ll_bottom_tab_ranking.isSelected = position == 2
        ll_bottom_tab_category.isSelected = position == 3
    }

    override fun onResume() {
        super.onResume()
        AppLog.e(TAG, "onResume")
        bookShelfFragment?.bookShelfReAdapter?.notifyDataSetChanged()
        selectTab(currentTab)
        StatService.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        AppLog.e(TAG, "onPause currentItem:" + view_pager.currentItem)
        StatService.onPause(this)
    }

    override fun onDetach() {
        super.onDetach()
        AppLog.e(TAG, "onDetach")
        bookShelfFragment?.onRemoveModeAllCheckedListener = null
        try {
            val childFragmentManager = Fragment::class.java.getDeclaredField("mChildFragmentManager")
            childFragmentManager.isAccessible = true
            childFragmentManager.set(this, null)

        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (BuildConfig.DEBUG) {
            BookApplication.getRefWatcher().watch(this)
        }
    }

    fun onMenuShownState(state: Boolean) {
        if (state) {
            content_tab_selection.visibility = View.GONE
            img_bottom_shadow.visibility = View.GONE
            if (!rl_head_editor.isShown) {
                val showAnimation = AlphaAnimation(0.0f, 1.0f)
                showAnimation.duration = 200
                rl_head_editor.startAnimation(showAnimation)
                rl_head_editor.visibility = View.VISIBLE
            }
            AnimationHelper.smoothScrollTo(view_pager, 0)
        } else {
            if (rl_head_editor.isShown) {
                rl_head_editor.visibility = View.GONE
            }
            img_bottom_shadow.visibility = View.VISIBLE
            content_tab_selection.visibility = View.VISIBLE
            AnimationHelper.smoothScrollTo(view_pager, 0)
        }
    }


    private inner class MainAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getCount(): Int = 4

        override fun getItem(position: Int): Fragment? {
            AppLog.e(TAG, "position: " + position)
            return when (position) {
                0 -> {
                    if (bookShelfFragment == null) {
                        bookShelfFragment = BookShelfFragment()
                        bookShelfFragment?.onRemoveModeAllCheckedListener = { isAllChecked ->
                            AppLog.e(TAG, "isAllChecked: $isAllChecked")
                            if (isAllChecked) {
                                txt_editor_select_all.text = getString(R.string.select_all_cancel)
                            } else {
                                txt_editor_select_all.text = getString(R.string.select_all)
                            }
                        }
                    }
                    bookShelfFragment
                }
                1 -> recommendFragment
                2 -> rankingFragment
                3 -> categoryFragment
                else -> null
            }
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            return if (position == 0) {
                val bookShelfFragment = super.instantiateItem(container, position) as BookShelfFragment
                bookShelfFragment.doUpdateBook()
                if (view_pager != null && frameHelper != null) {
                    frameCallback?.getFrameBookRankView(bookShelfFragment)
                }
                bookShelfFragment
            } else {
                super.instantiateItem(container, position)
            }
        }

        override fun getItemPosition(`object`: Any?): Int = PagerAdapter.POSITION_NONE
    }

    private fun showCacheMessage() {
        activity.doAsync {
            var result = "0B"
            try {
                result = DataCleanManager.getTotalCacheSize(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            uiThread { txt_clear_cache_message.text = result }
        }
    }
}
