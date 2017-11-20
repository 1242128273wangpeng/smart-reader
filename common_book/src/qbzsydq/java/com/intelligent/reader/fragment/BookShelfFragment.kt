package com.intelligent.reader.fragment

import com.dingyueads.sdk.Native.YQNativeAdInfo
import com.dingyueads.sdk.NativeInit
import com.intelligent.reader.BuildConfig
import com.intelligent.reader.R
import com.intelligent.reader.activity.DownloadManagerActivity
import com.intelligent.reader.activity.HomeActivity
import com.intelligent.reader.adapter.BookShelfReAdapter
import com.intelligent.reader.app.BookApplication
import com.intelligent.reader.read.help.BookHelper
import com.intelligent.reader.util.BookShelfRemoveHelper
import com.intelligent.reader.util.ShelfGridLayoutManager

import net.lzbook.kit.ad.OwnNativeAdManager
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.component.service.CheckNovelUpdateService
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.UpdateCallBack
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.BookUpdate
import net.lzbook.kit.data.bean.BookUpdateResult
import net.lzbook.kit.data.bean.EventBookshelfAd
import net.lzbook.kit.data.bean.SensitiveWords
import net.lzbook.kit.data.bean.Source
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.FrameBookHelper
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.ToastUtils
import net.lzbook.kit.utils.Tools

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.text.TextUtils
import android.view.Gravity
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView

import java.lang.ref.WeakReference
import java.lang.reflect.Field
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedHashMap

import de.greenrobot.event.EventBus

/**
 * 书架页Fragment
 */
class BookShelfFragment : Fragment(), UpdateCallBack, FrameBookHelper.BookUpdateService, FrameBookHelper.DownLoadStateCallback, FrameBookHelper.DownLoadNotify, FrameBookHelper.NotificationCallback, BookShelfRemoveHelper.OnMenuDeleteClickListener, BookShelfRemoveHelper.OnMenuStateListener, FrameBookHelper.BookChanged, BookShelfReAdapter.ShelfItemClickListener, BookShelfReAdapter.ShelfItemLongClickListener {
    private val handler = UiHandler(this)
    var bookshelf_content: View? = null
    var bookshelf_main: RelativeLayout
    var bookShelfRemoveHelper: BookShelfRemoveHelper? = null
    var bookShelfReAdapter: BookShelfReAdapter? = null
    var iBookList: ArrayList<Book>? = ArrayList()
    var loading_progress_bar: ProgressBar
    var loading_message: TextView? = null
    var swipeRefreshLayout: SuperSwipeRefreshLayout? = null
    //书籍屏蔽相关字段
    protected var bookSensitiveWord: SensitiveWords? = null
    internal var download_bookshelf: ImageView? = null
    internal var isUpdateFinish = false
    internal var bookCollect_checked: ArrayList<Book>? = null
    private var weakReference: WeakReference<Activity>? = null
    private var mContext: Context? = null
    private var fragmentCallback: BaseFragment.FragmentCallback? = null
    private var versionCode: Int = 0
    private val bookSensitiveWords: List<String>? = null
    private val noBookSensitive = false
    //自有广告管理类
    private var ownNativeAdManager: OwnNativeAdManager? = null
    private var bookshelf_empty: LinearLayout? = null
    private var bookOnLines: ArrayList<Book>? = null
    private var update_table: ArrayList<String>? = null
    private var frameBookHelper: FrameBookHelper? = null
    private var bookDaoHelper: BookDaoHelper? = null
    private var isShowAD = false
    private var isGetAdEvent: Boolean = false
    private var bookrack_update_time: Long = 0
    private var load_data_finish_time: Long = 0
    private var deleteDialog: MyDialog? = null
    private val mDialog: MyDialog? = null
    private var sharedPreferences: SharedPreferences? = null
    private var esBookOnlineList = ArrayList<Book>()
    private var bookshelf_empty_btn: ImageView? = null
    private var head_pb_view: ProgressBar? = null
    private var head_text_view: TextView? = null
    private var head_image_view: ImageView? = null
    private var recyclerView: RecyclerView? = null
    private var layoutManager: ShelfGridLayoutManager? = null
    private var isList = true
    private val isShowDownloadBtn = false

    private val adInfoHashMap = HashMap<Int, YQNativeAdInfo>()

    /**
     * 从数据库中取书架中书本显示内容
     */
    private val bookListData: ArrayList<Book>?
        get() {

            val booksOnLine = bookDaoHelper!!.booksOnLineList
            if (bookOnLines == null)
                bookOnLines = ArrayList()

            if (bookOnLines != null) {
                bookOnLines!!.clear()
                bookOnLines!!.addAll(booksOnLine)

                setBookListHeadData(bookOnLines!!.size)
            }
            if (iBookList != null) {
                iBookList!!.clear()
                if (!booksOnLine.isEmpty()) {
                    Collections.sort(booksOnLine, FrameBookHelper.MultiComparator())
                    iBookList!!.addAll(booksOnLine)
                    if (Constants.dy_shelf_ad_switch && !Constants.isHideAD && ownNativeAdManager != null) {
                        setAdBook(booksOnLine)
                    }
                }
            }
            return iBookList
        }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        this.weakReference = WeakReference<Activity>(activity)
        fragmentCallback = activity as BaseFragment.FragmentCallback?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isList = true
        mContext = activity
        versionCode = AppUtils.getVersionCode()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        if (!Constants.isHideAD) {
            ownNativeAdManager = OwnNativeAdManager.getInstance(activity)
        }
        initData()

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return initView(inflater)
    }

    private fun initView(inflater: LayoutInflater?): View? {
        var inflater = inflater

        if (inflater == null) {
            inflater = mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }
        try {
            bookshelf_content = inflater.inflate(R.layout.fragment_bookshelf, null)
        } catch (e: InflateException) {
            e.printStackTrace()
        }

        if (bookshelf_content != null) {
            bookshelf_main = bookshelf_content!!.findViewById(R.id.bookshelf_main) as RelativeLayout

            bookshelf_empty = bookshelf_content!!.findViewById(R.id.bookshelf_empty) as LinearLayout
            bookshelf_empty_btn = bookshelf_content!!.findViewById(R.id.bookshelf_empty_btn) as ImageView
            bookshelf_empty!!.visibility = View.GONE

            bookrack_update_time = AppUtils.getLongPreferences(mContext, "bookrack_update_time", System.currentTimeMillis())

            //            book_shelf_loading = (RelativeLayout) bookshelf_content.findViewById(R.id.book_shelf_loading);
            //            book_shelf_loading.setVisibility(View.GONE);
            loading_progress_bar = bookshelf_content!!.findViewById(R.id.loading_progressbar) as ProgressBar
            //            download_bookshelf = (ImageView) bookshelf_content.findViewById(R.id.fab_goto_down_act);
            //           if(download_bookshelf.getVisibility()==View.VISIBLE){
            //               isShowDownloadBtn = true;
            //                download_bookshelf.setOnClickListener(new OnClickListener() {
            //                    @Override
            //                    public void onClick(View v) {
            //                        Intent intent = new Intent(mContext, DownloadManagerActivity.class);
            //                        startActivity(intent);
            //                    }
            //                });
            //            }
            //            loading_progress = (ProgressBar) bookshelf_content.findViewById(R.id.loading_progress);
            //            loading_message = (TextView) bookshelf_content.findViewById(R.id.loading_message);

            //初始化RecyclerView
            initRecyclerView()

            val activity = weakReference!!.get() ?: return null

            initRemoveHelper()
        }

        return bookshelf_content
    }


    override fun onItemClick(view: View, position: Int) {
        AppLog.e(TAG, "BookShelfItemClick")
        if (iBookList == null || position < 0 || position > iBookList!!.size) {
            return
        }
        if (bookShelfRemoveHelper!!.isRemoveMode) {
            bookShelfRemoveHelper!!.setCheckPosition(position)
        }
        if (!bookShelfRemoveHelper!!.isRemoveMode) {
            intoNovelContent(position)
        }
    }

    override fun onItemLongClick(view: View, position: Int) {
        if (!bookShelfRemoveHelper!!.isRemoveMode) {
            bookShelfRemoveHelper!!.showRemoveMenu(swipeRefreshLayout)
            StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.LONGTIMEBOOKSHELFEDIT)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initListener()

        if (fragmentCallback != null) {
            fragmentCallback!!.frameHelper()
        }

        val activity = weakReference!!.get() ?: return

        if (frameBookHelper == null) {
            if (activity is HomeActivity) {
                frameBookHelper = activity.frameHelper
            }
        }

        if (frameBookHelper != null) {
            frameBookHelper!!.setBookUpdate(this)
            frameBookHelper!!.setDownLoadState(this)
            frameBookHelper!!.setDownNotify(this)
            frameBookHelper!!.setNotification(this)
            frameBookHelper!!.initDownUpdateService()
            frameBookHelper!!.clickNotification(activity.intent)
            frameBookHelper!!.setBookChanged(this)
        }

        //根据书架数量确定是否刷新
        if (bookOnLines!!.size > 0) {
            swipeRefreshLayout!!.isRefreshing = true
        }
    }

    private fun initRemoveHelper() {
        if (bookShelfRemoveHelper == null) {
            bookShelfRemoveHelper = BookShelfRemoveHelper(mContext, bookShelfReAdapter)
        }
        if (recyclerView != null) {
            bookShelfRemoveHelper!!.setLayout(swipeRefreshLayout)
        }
        if (fragmentCallback != null) {
            fragmentCallback!!.getRemoveMenuHelper(bookShelfRemoveHelper)
        }
        bookShelfRemoveHelper!!.setOnMenuStateListener(this)
        bookShelfRemoveHelper!!.setOnMenuDeleteListener(this)
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }


    //    private boolean isGetEvent;

    fun onEvent(eventBookshelfAd: EventBookshelfAd) {
        //        if (!isGetEvent) return;
        if (eventBookshelfAd.type_ad == NativeInit.CustomPositionName.SHELF_POSITION.toString()) {
            val activity = activity
            if (activity != null && isAdded) {
                Handler().post {
                    isShowAD = true
                    if (eventBookshelfAd.yqNativeAdInfo != null) {
                        eventBookshelfAd.yqNativeAdInfo.availableTime = System.currentTimeMillis()
                    }
                    adInfoHashMap.put(eventBookshelfAd.position, eventBookshelfAd.yqNativeAdInfo)
                    bookListData
                    bookShelfReAdapter!!.notifyDataSetChanged()
                    AppLog.e(TAG, "notifyDataSetChanged")
                    StatServiceUtils.statBookEventShow(mContext, StatServiceUtils.type_ad_shelf)
                }
            }
        } else if (eventBookshelfAd.type_ad == "bookshelfclick_360") {
            if (eventBookshelfAd.yqNativeAdInfo != null) {
                eventBookshelfAd.yqNativeAdInfo.availableTime = System.currentTimeMillis() + 2000
            }
            adInfoHashMap.put(eventBookshelfAd.position, eventBookshelfAd.yqNativeAdInfo)
        }
    }


    private fun setAdBook(booksOnLine: ArrayList<Book>) {
        AppLog.e("wyhad1-1", adInfoHashMap.toString())

        //长按删除状态下不请求广告
        if (!isShowAD || bookShelfRemoveHelper!!.isRemoveMode) {
            return
        }
        AppLog.e("wyhad1-1", this.isResumed.toString() + "")
        if (!this.isResumed) {
            return
        }
        val adInfo: YQNativeAdInfo?
        if (adInfoHashMap.containsKey(0) && adInfoHashMap[0] != null && adInfoHashMap[0].getAdvertisement() != null
                && (System.currentTimeMillis() - adInfoHashMap[0].getAvailableTime() < 3000 || !adInfoHashMap[0].getAdvertisement().isShowed)) {
            adInfo = adInfoHashMap[0]
        } else {
            adInfo = ownNativeAdManager!!.getSingleADInfoNew(0, NativeInit.CustomPositionName.SHELF_POSITION)
            if (adInfo != null) {
                adInfo.availableTime = System.currentTimeMillis()
                adInfoHashMap.put(0, adInfo)
            }
        }
        if (adInfo != null) {
            val book1 = Book()
            book1.book_type = -2
            book1.info = adInfo
            AppLog.e("wyhad1-1", "adInfo：" + adInfo.advertisement.toString())
            book1.rating = Tools.getIntRandom()
            try {
                iBookList!!.add(0, book1)
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTrace()
            }

        } else {
            AppLog.e("wyhad1-1", "adInfo == null")
        }

        val distance = booksOnLine.size / Constants.dy_shelf_ad_freq

        for (i in 0 until distance) {
            val info: YQNativeAdInfo?
            if (adInfoHashMap.containsKey(i + 1) && adInfoHashMap[i + 1] != null && adInfoHashMap[i + 1].getAdvertisement() != null
                    && (System.currentTimeMillis() - adInfoHashMap[i + 1].getAvailableTime() < 3000 || !adInfoHashMap[i + 1].getAdvertisement().isShowed)) {
                info = adInfoHashMap[i + 1]
            } else {
                info = ownNativeAdManager!!.getSingleADInfoNew(i + 1, NativeInit.CustomPositionName.SHELF_POSITION)
                if (info != null) {
                    info.availableTime = System.currentTimeMillis()
                    adInfoHashMap.put(i + 1, info)
                }
            }

            if (info != null) {
                val book1 = Book()
                book1.book_type = -2
                book1.info = info
                AppLog.e("wyhad1-1", "info：" + info.advertisement.toString())
                book1.rating = Tools.getIntRandom()
                try {
                    iBookList!!.add(Constants.dy_shelf_ad_freq * (i + 1), book1)
                } catch (e: IndexOutOfBoundsException) {
                    e.printStackTrace()
                    break
                }

            } else {
                AppLog.e("wyhad1-1", "info == null")
            }
        }

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDetach() {
        super.onDetach()
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

    override fun onDestroy() {
        super.onDestroy()

        if (BuildConfig.DEBUG) {
            BookApplication.getRefWatcher().watch(this)
        }

        if (frameBookHelper != null) {
            frameBookHelper!!.recycleCallback()
        }

        if (bookOnLines != null) {
            bookOnLines!!.clear()
        }
        adInfoHashMap?.clear()
    }

    /**
     * 根据网络及书架数据设置下拉刷新模式为直接完成或显式下拉
     */
    protected fun isPullAction(rackBookList: ArrayList<Book>?, refreshLayout: SuperSwipeRefreshLayout?): Boolean {
        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            refreshLayout!!.isRefreshing = false
            showToastDelay(R.string.bookshelf_refresh_network_problem)
            return false
        }

        //根据书架数量确定是否刷新
        //        if (rackBookList.size() > 0) {
        //            refreshLayout.setRefreshing(false);
        //        }
        return true
    }

    /**
     * 查Book数据库更新界面
     */
    fun updateUI() {

        bookListData

        if (bookShelfReAdapter == null) {
            bookShelfReAdapter = BookShelfReAdapter(activity, iBookList, this, this, isList)
        }
        if (bookShelfReAdapter != null) {
            for (i in bookOnLines!!.indices) {
                val book = bookOnLines!![i]
                setUpdateState(book)
            }

            bookShelfReAdapter!!.setUpdate_table(update_table)
            bookShelfReAdapter!!.notifyDataSetChanged()
        }
        //判断用户是否是当日首次打开应用,并上传书架的id
        val first_time = sharedPreferences!!.getLong(Constants.TODAY_FIRST_POST_BOOKIDS, 0)

        val currentTime = System.currentTimeMillis()
        val b = AppUtils.isToday(first_time, currentTime)
        if (!b) {
            val bookIdList = StringBuilder()
            for (i in iBookList!!.indices) {
                val book = iBookList!![i]
                bookIdList.append(book.book_id)

                bookIdList.append(if (book.readed == 1) "_1" else "_0")//1已读，0未读
                bookIdList.append(if (i == iBookList!!.size) "" else "$")

            }
            val data = HashMap<String, String>()
            data.put("bookid", bookIdList.toString())
            StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.BOOKLIST, data)
            sharedPreferences!!.edit().putLong(Constants.TODAY_FIRST_POST_BOOKIDS, currentTime).apply()
        }
    }

    private fun initData() {
        val activity = weakReference!!.get() ?: return
        if (bookDaoHelper == null && mContext != null) {
            bookDaoHelper = BookDaoHelper.getInstance()
        }

        esBookOnlineList = bookDaoHelper!!.booksOnLineListYS

        if (update_table == null) {
            update_table = ArrayList()
        }
        update_table!!.clear()

        bookCollect_checked = ArrayList()
    }

    private fun initListener() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout!!.setOnPullRefreshListener(object : SuperSwipeRefreshLayout.OnPullRefreshListener {

                override fun onRefresh() {
                    head_text_view!!.text = "正在刷新"
                    head_image_view!!.visibility = View.GONE
                    head_pb_view!!.visibility = View.VISIBLE
                    checkBookUpdate()
                }

                override fun onPullDistance(distance: Int) {
                    // pull distance
                }

                override fun onPullEnable(enable: Boolean) {
                    head_pb_view!!.visibility = View.GONE
                    head_text_view!!.text = if (enable) "松开刷新" else "下拉刷新"
                    head_image_view!!.visibility = View.VISIBLE
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        head_image_view!!.rotation = (if (enable) 180 else 0).toFloat()
                    }
                }
            })
        }
        if (bookshelf_empty_btn != null) {
            bookshelf_empty_btn!!.setOnClickListener {
                if (fragmentCallback != null) {
                    fragmentCallback!!.setSelectTab(1)

                    StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.TOBOOKCITY)
                }
            }
        }
    }

    private fun initRecyclerView() {

        if (bookOnLines == null) {
            bookOnLines = ArrayList()
        }
        if (bookShelfReAdapter == null) {
            bookShelfReAdapter = BookShelfReAdapter(activity, iBookList, this, this, isList)
        }

        swipeRefreshLayout = bookshelf_content!!.findViewById(R.id.bookshelf_refresh_view) as SuperSwipeRefreshLayout
        swipeRefreshLayout!!.setHeaderViewBackgroundColor(0x00000000)
        swipeRefreshLayout!!.setHeaderView(createHeaderView())
        swipeRefreshLayout!!.isTargetScrollWithLayout = true

        recyclerView = bookshelf_content!!.findViewById(R.id.recycler_view) as RecyclerView
        recyclerView!!.recycledViewPool.setMaxRecycledViews(0, 12)

        layoutManager = ShelfGridLayoutManager(mContext, 1)

        recyclerView!!.layoutManager = layoutManager
        //        recyclerView.getItemAnimator().setSupportsChangeAnimations(false);
        recyclerView!!.itemAnimator.addDuration = 0
        recyclerView!!.itemAnimator.changeDuration = 0
        recyclerView!!.itemAnimator.moveDuration = 0
        recyclerView!!.itemAnimator.removeDuration = 0
        (recyclerView!!.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recyclerView!!.adapter = bookShelfReAdapter

    }

    private fun setBookListHeadData(num: Int) {
        if (num == 0 && swipeRefreshLayout != null) {
            swipeRefreshLayout!!.setPullToRefreshEnabled(false)
            handler.obtainMessage(NO_BOOK_DATA_VIEW_SHOW).sendToTarget()
        } else if (swipeRefreshLayout != null) {
            swipeRefreshLayout!!.setPullToRefreshEnabled(true)
            handler.obtainMessage(NO_BOOK_DATA_VIEW_GONE).sendToTarget()
        }
    }


    private fun setUpdateState(book: Book?) {
        if (book != null) {
            if (book.update_status == 1) {
                if (!update_table!!.contains(book.book_id)) {
                    update_table!!.add(book.book_id)
                }
            } else {
                if (update_table!!.contains(book.book_id)) {
                    update_table!!.remove(book.book_id)
                }
            }
        }
    }

    /**
     * 下拉时检查更新
     */
    private fun checkBookUpdate() {

        if (!isPullAction(bookOnLines, swipeRefreshLayout)) {
            return
        }

        val start_pull_time = System.currentTimeMillis()
        val delay = Math.abs(start_pull_time - load_data_finish_time)

        //下拉刷新时删除标记的360广告信息
        if (adInfoHashMap != null) {
            val iterator = adInfoHashMap.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val key = entry.key
                val value = entry.value
                if (value != null && value.advertisement != null && value.advertisement.platformId == com.dingyueads.sdk.Constants.AD_TYPE_360 && value.advertisement.isClicked) {
                    iterator.remove()
                }
            }
        }

        // 刷新间隔小于30秒无效
        if (delay <= PULL_REFRESH_DELAY) {
            swipeRefreshLayout!!.onRefreshComplete()
            AppLog.d(TAG, "刷新间隔小于30秒不请求数据")
            showToastDelay(R.string.main_update_no_new)
        } else {
            // 刷新间隔大于30秒直接请求更新，
            addUpdateTask()
            AppLog.d(TAG, "刷新间隔大于30秒请求数据")
        }

    }

    private fun addUpdateTask() {
        if (frameBookHelper != null) {
            val updateService = frameBookHelper!!.updateService
            if (bookDaoHelper!!.booksCount > 0 && updateService != null) {
                val list = bookDaoHelper!!.booksList
                AppLog.e("BookUpdateCount", "BookUpdateCount: " + list.size)
                updateService.checkUpdate(BookHelper.getBookUpdateTaskData(list, this))
            }
        }
    }

    private fun showToastDelay(textId: Int) {
        handler.postDelayed({
            val activity = weakReference!!.get()
            if (isAdded && activity != null) {
                if (activity is HomeActivity) {
                    val homeActivity = activity as HomeActivity?
                    homeActivity!!.showToastShort(textId)
                }
            }
        }, 2000)
    }

    private fun showToastDelay(text: String) {
        handler.postDelayed({
            val activity = weakReference!!.get()
            if (isAdded && activity != null) {
                if (activity is HomeActivity) {
                    val homeActivity = activity as HomeActivity?
                    homeActivity!!.showToastLong(text)
                }
            }
        }, 2000)
    }

    private fun showToast(text: Int) {
        val activity = weakReference!!.get()
        if (isAdded && activity != null) {
            if (activity is HomeActivity) {
                val homeActivity = activity as HomeActivity?
                homeActivity!!.showToastShort(text)
            }
        }
    }

    private fun getSelfString(context: Context?, StringId: Int): String {
        if (isAdded && context != null) {
            try {
                return context.resources.getString(StringId)
            } catch (e: Resources.NotFoundException) {
                e.printStackTrace()
                return ""
            }

        }
        return ""
    }

    private fun emptyViewShow() {
        bookshelf_empty!!.visibility = View.VISIBLE
    }

    private fun emptyViewGone() {
        bookshelf_empty!!.visibility = View.GONE
    }

    private fun longPressEdit() {

    }

    private fun refreshDataAfterDelete() {
        //        recyclerView.smoothScrollToPosition(0);
        updateUI()
        if (bookShelfRemoveHelper != null) {
            bookShelfRemoveHelper!!.dismissRemoveMenu()
        }
        if (bookCollect_checked != null && bookCollect_checked!!.size > 0) {
            bookCollect_checked!!.clear()
        }
    }

    //打开长按编辑模式时，过滤掉广告
    private fun filterAd() {
        for (i in iBookList!!.indices) {
            //若当前的书籍是广告，则长按状态删除广告
            if (iBookList!![i].book_type == -2) {
                iBookList!!.removeAt(i)
            }
        }
    }

    /**
     * 点击书架条目情况
     */
    private fun intoNovelContent(index: Int) {
        if (index >= iBookList!!.size || index < 0) {
            return
        }

        val book = iBookList!![index]
        clickAction(book)

        if (book != null) {
            val data = HashMap<String, String>()
            data.put("bookid", book.book_id)
            data.put("rank", (index + 1).toString())
            StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.BOOKCLICK, data)
        }
    }

    /**
     * listitem点击或更新通知点击处理
     */
    private fun clickAction(book: Book?) {
        val activity = weakReference!!.get()
        if (activity == null || book == null) {
            return
        }
        if (!TextUtils.isEmpty(book.book_id) && book.book_type == 0) {
            cancelUpdateStatus(book.book_id)
        }

        if (Constants.isShielding && !noBookSensitive && bookSensitiveWords!!.contains(book.book_id.toString())) {
            ToastUtils.showToastNoRepeat("抱歉，该小说已下架！")
        } else {
            BookHelper.goToCoverOrRead(weakReference!!.get().getApplicationContext(), weakReference!!.get(), book, 0)
        }
    }

    /**
     * 消除数据库中更新状态
     */
    private fun cancelUpdateStatus(book_id: String) {
        val book = Book()
        book.book_id = book_id
        book.update_status = 0
        if (update_table!!.contains(book.book_id)) {
            update_table!!.remove(book_id)
        }
        bookDaoHelper!!.updateBook(book)
    }

    fun setResultRefresh() {
        updateUI()
    }

    override fun onSuccess(result: BookUpdateResult) {
        load_data_finish_time = System.currentTimeMillis()
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout!!.onRefreshComplete()
        }
        onUpdateSuccessToast(result)
        bookrack_update_time = System.currentTimeMillis()
        AppUtils.setLongPreferences(mContext, "bookrack_update_time", bookrack_update_time)
        AppLog.e(TAG, "onSuccess的刷新ui调用")
        isShowAD = true
        updateUI()
        isGetAdEvent = false
        if (!isUpdateFinish)
            isUpdateFinish = true
    }

    override fun onException(e: Exception) {
        load_data_finish_time = System.currentTimeMillis()
        showToastDelay(R.string.bookshelf_refresh_network_problem)
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout!!.onRefreshComplete()
        }
        if (!isUpdateFinish)
            isUpdateFinish = true
    }

    protected fun onUpdateSuccessToast(result: BookUpdateResult?) {
        var newsCount = 0
        val hasUpdateList = ArrayList<BookUpdate>()
        if (result != null && result.items != null && !result.items.isEmpty()) {
            val bookUpdates = result.items
            val size = bookUpdates.size
            for (i in 0 until size) {
                val item = bookUpdates[i]
                if (!TextUtils.isEmpty(item.book_id) && item.update_count != 0) {
                    newsCount++
                    hasUpdateList.add(item)
                }
            }
            if (hasUpdateList.size != 0) {
                showMoreToast(newsCount, hasUpdateList)
            }
        } else {
            showToastDelay(R.string.main_update_no_new)
        }
    }

    private fun showMoreToast(newsCount: Int, hasUpdateList: ArrayList<BookUpdate>) {
        val bookUpdate = hasUpdateList[0]
        val book_name = bookUpdate.book_name
        val activity = weakReference!!.get() ?: return
        if (book_name != null && !TextUtils.isEmpty(book_name)) {

            if (newsCount == 1) {
                showToastDelay("《" + book_name + getSelfString(mContext, R.string.bookshelf_one_book_update) + bookUpdate.last_chapter_name)
            } else {
                val update_size = hasUpdateList.size
                showToastDelay("《" + book_name + getSelfString(mContext, R.string.bookshelf_more_book_update) + update_size + getSelfString(mContext,
                        R.string.bookshelf_update_chapters))
            }
        }
    }

    fun doUpdateBook() {
        addUpdateTask()
    }

    override fun doUpdateBook(updateService: CheckNovelUpdateService?) {
        val activity = weakReference!!.get()
        updateService?.setBookUpdateListener(activity as CheckNovelUpdateService.OnBookUpdateListener)

        addUpdateTask()

    }

    override fun changeDownLoadBtn(isDownLoading: Boolean) {}

    override fun doNotifyDownload() {
        updateUI()
    }

    override fun notification(gid: String) {
        if (!TextUtils.isEmpty(gid)) {
            val book = bookDaoHelper!!.getBook(gid, 0) as Book
            if (book != null) {
                clickAction(book)
            }
        }
    }

    /**
     * 菜单删除按钮触发删除动作
     */
    private fun deleteBooks(deleteBooks: ArrayList<Book>, rankList: ArrayList<Book>?) {

        val size = deleteBooks.size
        Thread(Runnable {
            val books = arrayOfNulls<String>(size)
            val sb = StringBuffer()
            for (i in 0 until size) {
                val book = deleteBooks[i]
                books[i] = book.book_id

                sb.append(book.book_id)
                sb.append(if (book.readed == 1) "_1" else "_0")
                sb.append(if (i == size - 1) "" else "$")

                handler.obtainMessage(REFRESH_DATA_AFTER_DELETE, book.book_id).sendToTarget()
            }
            // 删除书架数据库和章节数据库
            if (bookDaoHelper != null) {
                bookDaoHelper!!.deleteBook(*books)
            }

            val data1 = HashMap<String, String>()
            data1.put("type", "1")
            data1.put("number", size.toString())
            data1.put("bookids", sb.toString())
            StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SHELFEDIT_PAGE, StartLogClickUtil.DELETE1, data1)
        }).start()
    }

    override fun onMenuDelete(checked_state: HashSet<Int>) {
        val checkedBooks = ArrayList<Book>()
        checkedBooks.clear()
        val size = iBookList!!.size
        for (i in 0 until size) {
            if (checked_state.contains(i)) {
                checkedBooks.add(iBookList!![i])
            }
        }
        onMenuDeleteAction(checkedBooks)
    }

    private fun onMenuDeleteAction(deleteBooks: ArrayList<Book>) {
        val activity = weakReference!!.get() ?: return
        if (deleteBooks.size > 0) {
            deleteDialog = MyDialog(activity, R.layout.publish_hint_dialog)
            val base_dialog_title = deleteDialog!!.findViewById(R.id.dialog_title) as TextView
            base_dialog_title.setText(R.string.prompt)
            val base_dialog_content = deleteDialog!!.findViewById(R.id.publish_content) as TextView
            base_dialog_content.gravity = Gravity.CENTER
            base_dialog_content.setText(R.string.determine_delete_book_cache)
            val base_dialog_confirm = deleteDialog!!.findViewById(R.id.publish_stay) as Button
            base_dialog_confirm.setText(R.string.cancel)
            val base_dialog_abrogate = deleteDialog!!.findViewById(R.id.publish_leave) as Button
            base_dialog_abrogate.setText(R.string.confirm)
            base_dialog_abrogate.setOnClickListener {
                deleteDialog!!.dismiss()
                deleteBooks(deleteBooks, bookOnLines)
                StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.bs_click_delete_ok_btn)
            }
            base_dialog_confirm.setOnClickListener {
                deleteDialog!!.dismiss()
                StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.bs_click_delete_cancel_btn)
                val data1 = HashMap<String, String>()
                data1.put("type", "2")
                StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SHELFEDIT_PAGE, StartLogClickUtil.DELETE1, data1)
            }
            deleteDialog!!.show()

        } else {
            //            showToast(R.string.mian_delete_cache_no_choose);
        }
    }

    override fun getMenuShownState(state: Boolean) {
        if (state) {
            swipeRefreshLayout!!.setPullToRefreshEnabled(false)
            if (isShowDownloadBtn) {
                download_bookshelf!!.visibility = View.GONE
            }
        } else {
            if (bookOnLines!!.size != 0) {
                swipeRefreshLayout!!.setPullToRefreshEnabled(true)
            }
            if (isShowDownloadBtn) {
                download_bookshelf!!.visibility = View.VISIBLE
            }
            updateUI()
        }
        if (fragmentCallback != null) {
            fragmentCallback!!.getMenuShownState(state)
        }
    }

    override fun getAllCheckedState(isAll: Boolean) {
        if (fragmentCallback != null) {
            fragmentCallback!!.getAllCheckedState(isAll)
        }
    }

    override fun doHideAd() {
        if (isShowAD) {
            filterAd()
        }
    }

    override fun updateBook() {
        AppLog.e(TAG, "updateBook的刷新UI调用")
        updateUI()
    }

    private fun changeBookSource(source: Source, changeReadFlag: Boolean): Book {
        val bookDaoHelper = BookDaoHelper.getInstance()
        val iBook = bookDaoHelper.getBook(source.book_id, 0)
        iBook.book_source_id = source.book_source_id
        iBook.site = source.host
        iBook.dex = source.dex
        val iterator = source.source.entries.iterator()
        val list = ArrayList<String>()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val value = entry.value
            list.add(value)
        }
        if (list.size > 0) {
            iBook.parameter = list[0]
        }
        if (list.size > 1) {
            iBook.extra_parameter = list[1]
        }
        iBook.last_updatetime_native = source.update_time

        if (changeReadFlag) {
            // 更新阅读标记为未阅读0、章节序为-1
            iBook.readed = 0
            iBook.sequence = -1
        }

        if (bookDaoHelper.isBookSubed(source.book_id)) {
            bookDaoHelper.updateBook(iBook)
        }
        return iBook
    }

    private fun createHeaderView(): View {
        val headerView = LayoutInflater.from(swipeRefreshLayout!!.context)
                .inflate(R.layout.layout_head, null)
        head_pb_view = headerView.findViewById(R.id.head_pb_view) as ProgressBar
        head_text_view = headerView.findViewById(R.id.head_text_view) as TextView
        head_text_view!!.text = "下拉刷新"
        head_image_view = headerView.findViewById(R.id.head_image_view) as ImageView
        head_image_view!!.visibility = View.VISIBLE
        head_image_view!!.setImageResource(R.drawable.pulltorefresh_down_arrow)
        head_pb_view!!.visibility = View.GONE
        return headerView
    }

    class UiHandler internal constructor(vpBook: BookShelfFragment) : Handler() {
        private val reference: WeakReference<BookShelfFragment>

        init {
            reference = WeakReference(vpBook)
        }

        override fun handleMessage(msg: Message) {
            val bookShelfFragment = reference.get() ?: return
            when (msg.what) {

                NO_BOOK_DATA_VIEW_SHOW -> bookShelfFragment.emptyViewShow()
                NO_BOOK_DATA_VIEW_GONE -> bookShelfFragment.emptyViewGone()
                LONG_PRESS_EDIT -> bookShelfFragment.longPressEdit()
                REFRESH_DATA_AFTER_DELETE -> bookShelfFragment.refreshDataAfterDelete()
            }
        }
    }

    companion object {

        val ACTION_CHKHIDE = AppUtils.getPackageName()
        private val NO_BOOK_DATA_VIEW_SHOW = 0x14
        private val NO_BOOK_DATA_VIEW_GONE = NO_BOOK_DATA_VIEW_SHOW + 1
        private val LONG_PRESS_EDIT = NO_BOOK_DATA_VIEW_GONE + 1
        private val REFRESH_DATA_AFTER_DELETE = LONG_PRESS_EDIT + 1
        private val PULL_REFRESH_DELAY = 30 * 1000
        private val TAG = BookShelfFragment::class.java.simpleName
    }
}
