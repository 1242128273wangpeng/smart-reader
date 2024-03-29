package com.intelligent.reader.activity


import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.view.View.OnClickListener
import android.widget.AbsListView
import android.widget.AbsListView.OnScrollListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.baidu.mobstat.StatService
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.Book
import com.ding.basic.bean.Bookmark
import com.ding.basic.bean.Chapter
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.dingyue.statistics.DyStatService
import com.intelligent.reader.R
import com.intelligent.reader.adapter.BookmarkAdapter
import com.intelligent.reader.adapter.CatalogAdapter
import com.intelligent.reader.view.TransformReadDialog
import kotlinx.android.synthetic.main.layout_empty_catalog.*
import kotlinx.android.synthetic.txtqbmfxs.act_catalog.*
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.bean.EventBookmark
import net.lzbook.kit.bean.OfflineDownloadEvent
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.presenter.CataloguesPresenter
import net.lzbook.kit.receiver.OffLineDownLoadReceiver
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.ui.widget.MyDialog
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.antiShakeClick
import net.lzbook.kit.utils.book.RepairHelp
import net.lzbook.kit.utils.logger.AppLog
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.view.CataloguesContract
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import java.util.concurrent.Callable


/**
 * Function：小说目录页
 *
 * Created by JoannChen on 2018/6/13 0013 21:08
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
@Route(path = RouterConfig.CATALOGUES_ACTIVITY)
class CataloguesActivity : BaseCacheableActivity(), OnClickListener, OnScrollListener, OnItemClickListener, CataloguesContract {

    override fun insertBookShelfResult(result: Boolean) {
    }

    override fun changeShelfButtonClickable(clickable: Boolean) {
    }

    override fun bookSubscribeState(subscribe: Boolean) {
    }


    var type = 2
    internal var colorSelected: Int = 0
    internal var colorNormal: Int = 0
    internal var sortIcon = 0//背景色

    //当前页标识
    private var currentView: View? = null
    //是否是最后一页
    private var is_last_chapter: Boolean = false
    //是否来源于封面页
    private var fromCover: Boolean = false
    //是否来源于完结页
    private var fromEnd: Boolean = false
    //加载页
    private var loadingPage: LoadingPage? = null
    private var sequence: Int = 0
    //小说ID
    private val nid: Int = 0
    //小说
    private var book: Book? = null
    //小说帮助类
    private var mCatalogAdapter: CatalogAdapter? = null
    private var mBookmarkAdapter: BookmarkAdapter? = null
    private var chapterList: ArrayList<Chapter>? = ArrayList()
    private var bookmarkList: ArrayList<Bookmark>? = ArrayList()
    private var isPositive = true
    /**
     * 标识List的滚动状态。
     */
    private var scrollState: Int = 0
    private var downLoadReceiver: OffLineDownLoadReceiver? = null
    private var mCataloguesPresenter: CataloguesPresenter? = null
    private var transformReadDialog: TransformReadDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_catalog)


        colorSelected = ContextCompat.getColor(this, R.color.theme_primary_ffffff)
        colorNormal = ContextCompat.getColor(this, R.color.theme_primary)

        initUI()
        initListener()

        val bundle = intent.extras ?: return

        initData(bundle)
        initCatalogAndBookmark()

        if (fromEnd) {
            isPositive = false
            changeSortState(isPositive)
        }

        EventBus.getDefault().register(this)
    }

    private fun initUI() {

        // 目录、书签
        tab_catalog.antiShakeClick(this)
        tab_bookmark.antiShakeClick(this)

        //返回、倒序
        img_back.antiShakeClick(this)
        tv_catalog_novel_sort.antiShakeClick(this)

        // 返回顶层按钮
        iv_back_reading.antiShakeClick(this)


        rl_layout_empty_online.visibility = View.GONE
        char_hint.visibility = View.INVISIBLE

        currentView = tab_catalog

        changeSortState(isPositive)

    }

    private fun initListener() {
        catalog_main.onItemClickListener = this
        catalog_main.setOnScrollListener(this)

        bookmark_main.onItemClickListener = this

        catalog_empty_refresh.setOnClickListener(this)

        iv_fixbook.setOnClickListener(this)
    }

    private fun initData(bundle: Bundle) {

//        if (bundle.containsKey("cover")) {
        book = bundle.getSerializable("cover") as Book
//        }

        if (book == null) {
            exitAndUpdate()
            return
        }

        sequence = Math.max(bundle.getInt("sequence"), 0)
        is_last_chapter = bundle.getBoolean("is_last_chapter", false)
        fromCover = bundle.getBoolean("fromCover", true)
        fromEnd = bundle.getBoolean("fromEnd", false)

        catalog_novel_name!!.text = book!!.name
        if (RepairHelp.isShowFixBtn(this, book!!.book_id)) {
            iv_fixbook!!.visibility = View.VISIBLE
        } else {
            iv_fixbook!!.visibility = View.GONE
        }


        mCataloguesPresenter = CataloguesPresenter(this, book!!, this, this, fromCover)
        transformReadDialog = TransformReadDialog(this)

        transformReadDialog?.insertContinueListener {
            val data = HashMap<String, String>()
            data["type"] = "1"
            DyStatService.onEvent(EventPoint.BOOOKDETAIL_TRANSCODEPOPUP, data)

            intoReadingActivity()

            if (!this.isFinishing) {
                transformReadDialog?.dismiss()
            }
        }

        transformReadDialog?.insertCancelListener {
            val data = HashMap<String, String>()
            data["type"] = "2"
            DyStatService.onEvent(EventPoint.BOOOKDETAIL_TRANSCODEPOPUP, data)

            if (!this.isFinishing) {
                transformReadDialog?.dismiss()
            }
        }

        getChapterData()

        mCataloguesPresenter?.loadBookMark()

    }

    /***
     * 进入阅读页
     * **/
    private fun intoReadingActivity() {
        if (TextUtils.isEmpty(book!!.book_id)) {
            return
        }

        val bundle = Bundle()

        val flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val localBook = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(book!!.book_id)

        if (localBook != null) {
            if (book!!.sequence != -2) {
                bundle.putInt("sequence", localBook.sequence)
                bundle.putInt("offset", localBook.offset)
            } else {
                bundle.putInt("sequence", -1)
                bundle.putInt("offset", 0)
            }

            bundle.putSerializable("book", localBook)
        } else {
            bundle.putSerializable("book", book)
        }

        RouterUtil.navigation(this, RouterConfig.READER_ACTIVITY, bundle, flags)
    }

    override fun showReadDialog() {
        if (!this.isFinishing) {
            val isChecked = SPUtils.getDefaultSharedBoolean(SPKey.NOT_SHOW_NEXT_TIME, false)
            if (isChecked) {
                intoReadingActivity()
            } else {
                transformReadDialog?.show()
            }
        }
    }

    private fun getChapterData() {
        if (book != null) {
            if (loadingPage != null) {
                loadingPage?.onSuccess()
            }

            loadingPage = LoadingPage(this, LoadingPage.setting_result)
            loadingPage!!.setCustomBackgroud()

            mCataloguesPresenter?.requestCatalogList(false)

            loadingPage?.isCategory = true
            loadingPage?.setReloadAction(Callable<Void> {
                mCataloguesPresenter?.requestCatalogList(false)
                null
            })
        }
    }


    private fun dataError() {
        if (loadingPage != null) {
            loadingPage!!.onError()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(eventBookmark: EventBookmark) {
        if (eventBookmark.type == EventBookmark.type_delete) {
            AppLog.e(TAG, "eventBookmark:" + eventBookmark.bookmark.id + " name:" + eventBookmark.bookmark.chapter_name)
            val bookmark = eventBookmark.bookmark
            if (mCataloguesPresenter != null) {
                mCataloguesPresenter!!.onEventReceive(bookmark)
            }
        }
    }

    private fun showNullBookMarkNoteLayout() {
        if (currentView === tab_bookmark) {
            if (bookmarkList != null && bookmarkList!!.size == 0) {
                if (rl_layout_empty_online != null)
                    rl_layout_empty_online!!.visibility = View.VISIBLE
            } else {
                if (rl_layout_empty_online != null)
                    rl_layout_empty_online!!.visibility = View.GONE
            }
        } else {
            if (rl_layout_empty_online != null)
                rl_layout_empty_online!!.visibility = View.GONE
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        if (isModeChange) {
            setMode()
        }
        StatService.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        StatService.onPause(this)
    }

    private fun initCatalogAndBookmark() {
        mCatalogAdapter = CatalogAdapter(this, chapterList, "")
        catalog_main!!.adapter = mCatalogAdapter
        if (is_last_chapter) {
            mCatalogAdapter!!.setSelectedItem(chapterList!!.size)
            catalog_main!!.setSelection(chapterList!!.size)
        } else {
            mCatalogAdapter!!.setSelectedItem(sequence + 1)
            catalog_main!!.setSelection(sequence + 1)
        }

    }

    override fun onStart() {
        super.onStart()
        if (downLoadReceiver == null) {
            downLoadReceiver = OffLineDownLoadReceiver(this)
        }
        downLoadReceiver!!.registerAction()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        if (downLoadReceiver != null) {
            try {
                unregisterReceiver(downLoadReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }

        }


        if (mCataloguesPresenter != null) {
            mCataloguesPresenter!!.removeHandler()
            mCataloguesPresenter!!.unRegisterRec()
        }
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun notifyChangeDownLoad(event: OfflineDownloadEvent) {
        if (mCatalogAdapter != null) {
            mCatalogAdapter!!.notifyDataSetChanged()
        }
    }

    // 目录
    override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
        if (chapterList != null && !chapterList!!.isEmpty()) {
            char_hint!!.text = String.format(getString(R.string.chapter_sort), chapterList!![firstVisibleItem].sequence + 1)
        }
    }

    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
        this.scrollState = scrollState
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE || scrollState == OnScrollListener.SCROLL_STATE_FLING) {
            if (mCataloguesPresenter != null) {
                mCataloguesPresenter!!.delayOverLayHandler()
            }
        } else {
            if (char_hint != null) {
                char_hint!!.visibility = View.VISIBLE
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.img_back -> {
                if (!fromCover) {
                    if (mCataloguesPresenter != null) {
                        sequence = Math.min(sequence, (chapterList?.size ?: 1) - 1)
                        mCataloguesPresenter!!.activityResult(sequence)
                    }
                }
                DyStatService.onEvent(EventPoint.BOOKCATALOG_BACK, mapOf("type" to "1"))
                finish()
            }
            R.id.iv_back_reading -> finish()
            R.id.catalog_empty_refresh -> getChapterData()
            R.id.tab_catalog -> {
                if (catalog_main != null) {
                    catalog_main!!.visibility = View.VISIBLE
                }
                if (rl_catalog_novel != null) {
                    rl_catalog_novel!!.visibility = View.VISIBLE
                }
                if (bookmark_main != null) {
                    bookmark_main!!.visibility = View.GONE
                }
                if (rl_layout_empty_online != null) {
                    rl_layout_empty_online!!.visibility = View.GONE
                }
                currentView = tab_catalog
            }
            R.id.tab_bookmark -> {
                if (catalog_main != null) {
                    catalog_main!!.visibility = View.GONE
                }
                if (rl_catalog_novel != null) {
                    rl_catalog_novel!!.visibility = View.GONE
                }
                if (bookmark_main != null) {
                    bookmark_main!!.visibility = View.VISIBLE
                }
                currentView = tab_bookmark
                showNullBookMarkNoteLayout()
            }
            R.id.iv_catalog_novel_sort//正序、逆序
                , R.id.tv_catalog_novel_sort -> if (chapterList != null && !chapterList!!.isEmpty()) {
                //书签点击的统计
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.rb_catalog_click_book_mark)
                isPositive = !isPositive
                Collections.reverse(chapterList!!)
                mCatalogAdapter!!.list = chapterList
                mCatalogAdapter!!.notifyDataSetChanged()
                changeSortState(isPositive)
            }
            R.id.iv_fixbook -> if (mCataloguesPresenter != null) {
                mCataloguesPresenter!!.fixBook()
            }
            else -> {
            }
        }
    }

    private fun changeSortState(b: Boolean) {
        if (tv_catalog_novel_sort != null && iv_catalog_novel_sort != null) {
            if (b) {
                tv_catalog_novel_sort!!.setText(R.string.catalog_negative)
                sortIcon = R.mipmap.dir_sort_negative
                //正序的统计
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.rb_catalog_click_zx_btn)

                iv_catalog_novel_sort!!.setImageResource(sortIcon)

            } else {
                tv_catalog_novel_sort!!.setText(R.string.catalog_positive)
                sortIcon = R.mipmap.dir_sort_positive
                iv_catalog_novel_sort!!.setImageResource(sortIcon)
                //倒序的统计
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.rb_catalog_click_dx_btn)
            }
        }
    }

    private fun startDeleteBookmarks(currentView: View, list: ArrayList<Int>) {
        if (list.size > 0) {
            val mDialog = MyDialog(this@CataloguesActivity, R.layout.publish_hint_dialog, Gravity.CENTER, true)
            val dialogPrompt = mDialog.findViewById<TextView>(R.id.dialog_title)
            dialogPrompt.setText(R.string.prompt)
            val dialogInformation = mDialog.findViewById<TextView>(R.id.publish_content)
            dialogInformation.setText(R.string.determine_remove_bookmark)
            dialogInformation.gravity = Gravity.CENTER

            val dialogCancel = mDialog.findViewById<Button>(R.id.publish_stay)
            dialogCancel.setText(R.string.cancel)
            val dialogConfirm = mDialog.findViewById<Button>(R.id.publish_leave)
            dialogConfirm.setText(R.string.delete)

            dialogConfirm.setOnClickListener {
                if (currentView === tab_bookmark) {
                    if (mCataloguesPresenter != null) {
                        mCataloguesPresenter!!.doDeleteBookmarks(list)
                    }
                }
                mDialog.dismiss()
            }

            dialogCancel.setOnClickListener { mDialog.dismiss() }
            try {
                mDialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun onBackPressed() {
        if (!fromCover && mCataloguesPresenter != null) {
            if (!chapterList!!.isEmpty()) {
                sequence = Math.min(sequence, (chapterList?.size ?: 1) - 1)
            }
            mCataloguesPresenter!!.activityResult(sequence)
        }
        exitAndUpdate()
    }

    private fun exitAndUpdate() {
        //如果是从通知栏过来, 且已经退出到home了, 要回到应用中
        if (isTaskRoot) {
            val intent = Intent(this, SplashActivity::class.java)
            startActivity(intent)
        }
        super.onBackPressed()
    }

    private fun exit() {
        finish()
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        var isCatalog = true
        if (parent === catalog_main) {
            isCatalog = true

        } else {
            isCatalog = false
        }

        if (mCataloguesPresenter != null) {
            mCataloguesPresenter!!.catalogToReading(position, isCatalog)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add("menu")
        return super.onCreateOptionsMenu(menu)
    }

    override fun requestCatalogSuccess(chapterList: ArrayList<Chapter>) {
        this.chapterList = chapterList
        if (loadingPage != null) {
            loadingPage!!.onSuccess()
        }
        catalog_chapter_count!!.text = "共" + chapterList.size + "章"
        if (mCatalogAdapter != null) {
            if (fromEnd) {
                isPositive = false
                Collections.reverse(chapterList)
            }
            mCatalogAdapter!!.list = chapterList
            mCatalogAdapter!!.notifyDataSetChanged()
        }

        //设置选中的条目
        var position = 0
        if (is_last_chapter) {
            position = chapterList.size
        } else {
            position = sequence
        }

        if (catalog_main != null) {
            catalog_main!!.setSelection(position)
        }

        if (fromEnd) {
            catalog_main!!.setSelection(0)
        }

        if (mCatalogAdapter != null)
            mCatalogAdapter!!.setSelectedItem(position)
    }

    override fun requestCatalogError() {
        dataError()
    }

    override fun handOverLay() {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE || scrollState == OnScrollListener.SCROLL_STATE_FLING && char_hint != null) {
            char_hint!!.visibility = View.INVISIBLE
        }
    }

    override fun deleteBookmarks(deleteList: ArrayList<Int>) {
        startDeleteBookmarks(tab_bookmark!!, deleteList)
    }

    override fun notifyDataChange(isCatalog: Boolean, bookmarkList: ArrayList<Bookmark>) {

        this.bookmarkList = bookmarkList

        if (mBookmarkAdapter == null)
            mBookmarkAdapter = BookmarkAdapter(this, bookmarkList)
        if (bookmark_main != null)
            bookmark_main!!.adapter = mBookmarkAdapter

        if (mBookmarkAdapter != null) {
            mBookmarkAdapter!!.notifyDataSetChanged()
        }
        if (isCatalog) {
            showNullBookMarkNoteLayout()
        }
    }


    override fun successAddIntoShelf(isAddIntoShelf: Boolean) {

    }

    override fun changeDownloadButtonStatus() {

    }
}