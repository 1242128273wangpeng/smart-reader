package com.dy.reader.presenter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.annotation.StringRes
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.*
import com.ding.basic.bean.*
import com.ding.basic.database.helper.BookDataProviderHelper
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.RequestSubscriber
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.util.showToastMessage
import com.dy.reader.R
import com.dy.reader.activity.ReaderActivity
import com.dy.reader.data.DataProvider
import com.dy.reader.event.EventLoading
import com.dy.reader.event.EventSetting
import com.dy.reader.help.NovelHelper
import com.dy.reader.setting.ReaderSettings
import com.dy.reader.setting.ReaderStatus
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import iyouqu.theme.ThemeMode
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.ChapterErrorBean
import net.lzbook.kit.data.bean.ReadConfig
import net.lzbook.kit.data.db.help.ChapterDaoHelper
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.*
import org.greenrobot.eventbus.EventBus
import java.io.UnsupportedEncodingException
import java.lang.Exception
import java.lang.ref.WeakReference
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Created by xian on 2017/8/8.
 */
class ReadSettingPresenter : NovelHelper.OnSourceCallBack {

    private val font_count = 50

    val mBookDataHelper = BookDataProviderHelper.loadBookDataProviderHelper(BaseBookApplication.getGlobalContext())

    private var activity: WeakReference<ReaderActivity>

    var myNovelHelper: NovelHelper? = null
    private var isSourceListShow: Boolean = false

    constructor(act: ReaderActivity) {
        activity = WeakReference(act)
        myNovelHelper = NovelHelper(act)
        myNovelHelper?.setOnSourceCallBack(this)
    }

    override fun showCatalogActivity(source: Source?) {
        if (source != null && !TextUtils.isEmpty(source.book_source_id) && ReaderStatus.book != null) {
            if ((RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(ReaderStatus.book.book_id) != null)) {
                val iBook = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(ReaderStatus.book.book_id)
                if (iBook != null && source.book_source_id != iBook.book_source_id) {
                    //弹出切源提示
                    var map2 = java.util.HashMap<String, String>()
                    map2.put("type", "1")
                    StartLogClickUtil.upLoadEventLog(activity.get(), StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.READ_SOURCECHANGECONFIRM, map2)
                    intoCatalogActivity(source, true)
                    return
                }
            }
        }
        intoCatalogActivity(source!!, false)
    }

    private fun intoCatalogActivity(source: Source, b: Boolean) {
        if ((RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(ReaderStatus.book.book_id) != null)) {
            val iBook = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(ReaderStatus.book.book_id)
            if (iBook != null) {
                iBook.book_source_id = source.book_source_id
                iBook.book_chapter_id = source.book_chapter_id
                iBook.host = source.host
                iBook.last_chapter?.update_time = source.update_time
                RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).updateBook(iBook)
                ReaderStatus.book = iBook
            }
            if (b) {
                //停止预缓存逻辑
                DataProvider.clearPreLoad()

                val bookChapterDao = ChapterDaoHelper.loadChapterDataProviderHelper(BaseBookApplication.getGlobalContext(), ReaderStatus.book.book_id)
                bookChapterDao.deleteAllChapters()
                BookDataProviderHelper.loadBookDataProviderHelper(BaseBookApplication.getGlobalContext()).deleteBookMark(ReaderStatus.book.book_id)
            }
        } else {
            val iBook = ReaderStatus.book
            iBook.book_source_id = source.book_source_id
            iBook.book_chapter_id = source.book_chapter_id
            iBook.host = source.host
            ReaderStatus.book = iBook
        }
        if (ReaderStatus.book != null) {
            val bundle = Bundle()
            bundle.putSerializable("cover", ReaderStatus.book)
            bundle.putString("book_id", ReaderStatus.book.book_id)
            bundle.putInt("sequence", ReaderStatus.book.sequence)
            bundle.putBoolean("fromCover", false)
            val activity = this.activity.get()
            if (activity is Activity) {
                RouterUtil.navigationWithCode(activity, RouterConfig.CATALOGUES_ACTIVITY, bundle, 1)
            }
        }
    }

    fun cache() {
        if (RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(ReaderStatus.book.book_id) == null && RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).insertBook(ReaderStatus.book) <= 0) {
            return
        }
        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            showToastShort("网络不给力，请稍后再试")
            return
        }
        clickDownload(activity.get()!!, ReaderStatus.book, Math.max(ReaderStatus.position.group, 0))
    }

    fun showMore() {
        val data = java.util.HashMap<String, String>()
        data.put("bookid", ReaderStatus.book.book_id)
        ReaderStatus.chapterId?.let {
            data.put("chapterid", it)
        }
        StartLogClickUtil.upLoadEventLog(activity.get()!!, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.MORE1, data)
    }

    /**
     * 点击下载按钮
     *
     *
     * context
     * gid
     * mBook
     * sequence
     */
    fun clickDownload(context: Context, mBook: Book, sequence: Int) {

        val dialog = MyDialog(activity.get(), R.layout.reading_cache, Gravity.BOTTOM, true)
        val reading_all_down = dialog.findViewById(R.id.reading_all_down) as TextView
        reading_all_down.setOnClickListener(View.OnClickListener {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_download_all)
            if (NetWorkUtils.getNetWorkType(context) == NetWorkUtils.NETWORK_NONE) {
                Toast.makeText(context, context.getText(R.string.game_network_none), Toast.LENGTH_LONG).show()
                return@OnClickListener
            }
            BaseBookHelper.startDownBookTask(activity.get(), mBook, 0)
            dialog.dismiss()

            val data = java.util.HashMap<String, String>()
            data.put("bookid", ReaderStatus.book.book_id)
            if (ReaderStatus.currentChapter != null) {
                data.put("chapterid", ReaderStatus.currentChapter!!.chapter_id!!)
            }
            data.put("type", "1")
            StartLogClickUtil.upLoadEventLog(activity.get()!!, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.CACHE, data)
        })
        val reading_current_down = dialog.findViewById(R.id.reading_current_down) as TextView
        reading_current_down.setOnClickListener(View.OnClickListener {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_download_from_now)
            if (NetWorkUtils.getNetWorkType(context) == NetWorkUtils.NETWORK_NONE) {
                Toast.makeText(context, context.getText(R.string.game_network_none), Toast.LENGTH_LONG).show()
                return@OnClickListener
            }
            BaseBookHelper.startDownBookTask(activity.get(), mBook, sequence)

            dialog.dismiss()

            val data = java.util.HashMap<String, String>()
            data.put("bookid", ReaderStatus.book.book_id)
            if (ReaderStatus.currentChapter != null) {
                data.put("chapterid", ReaderStatus.currentChapter!!.chapter_id!!)
            }
            data.put("type", "2")
            StartLogClickUtil.upLoadEventLog(activity.get()!!, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.CACHE, data)
        })
        val cancel = dialog.findViewById(R.id.reading_cache_cancel) as TextView

        cancel.setOnClickListener {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_download_cancel)
            if (dialog != null && dialog.isShowing) {
                dialog.dismiss()
                val data = java.util.HashMap<String, String>()
                data.put("bookid", ReaderStatus.book.book_id)
                if (ReaderStatus.currentChapter != null) {
                    data.put("chapterid", ReaderStatus.currentChapter!!.chapter_id!!)
                }
                data.put("type", "0")
                StartLogClickUtil.upLoadEventLog(activity.get()!!, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.CACHE, data)
            }
        }
        dialog.show()

    }

    private fun showToastShort(s: String) {
        if (activity.get() != null) {
            Toast.makeText(activity.get(), s, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showToastShort(@StringRes s: Int) {
        if (activity.get() != null) {
            Toast.makeText(activity.get(), s, Toast.LENGTH_SHORT).show()
        }
    }


    fun changeSource() {
        if (ReaderStatus.position.group == -1) {
            showToastShort(R.string.read_changesource_tip)
            return
        }
        if (Constants.QG_SOURCE == ReaderStatus.book.host) {
            showToastShort("该小说暂无其他来源！")
            return
        }


        if (isSourceListShow) {
            isSourceListShow = false
        } else {

            EventBus.getDefault().post(EventSetting(EventSetting.Type.MENU_STATE_CHANGE, false))

            if (ReaderStatus.book != null && !TextUtils.isEmpty(ReaderStatus.book.book_id)) {
                RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestBookSources(ReaderStatus.book.book_id, ReaderStatus.book.book_source_id, ReaderStatus.book.book_chapter_id, object : RequestSubscriber<BookSource>() {
                    override fun requestResult(result: BookSource?) {
                        if (result != null) {
                            this@ReadSettingPresenter.onGetSourceList(result.items as ArrayList<Source>?)
                        }
                    }

                    override fun requestError(message: String) {}

                    override fun requestComplete() {}
                })
            }
        }
    }


    fun onGetSourceList(sourcesList: ArrayList<Source>?) {
        if (sourcesList?.isNotEmpty() == true) {
            myNovelHelper?.showSourceDialog(sourcesList)
        } else {
            activity.get()?.applicationContext?.showToastMessage("暂无其它来源")
        }
    }

    fun bookMark(): Int {
        StatServiceUtils.statAppBtnClick(activity.get(), StatServiceUtils.rb_click_add_book_mark_btn)
        return addOptionMark(font_count, ReaderStatus.book.item_type)
    }

    /**
     * 添加手动书签
     */
    fun addOptionMark(font_count: Int, type: Int): Int {
        if (activity.get() == null) {
            return 0
        }

        if (RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(ReaderStatus.book.book_id) == null && RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).insertBook(ReaderStatus.book) <= 0) {
            return 0
        }

        if (!mBookDataHelper.isBookMarkExist(ReaderStatus.book.book_id, ReaderStatus.position.group, ReaderStatus.position.offset)) {
            var logMap = HashMap<String, String>()
            logMap.put("type", "1")
            StartLogClickUtil.upLoadEventLog(activity.get(), StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.BOOKMARKEDIT, logMap)

            val chapter = ReaderStatus.currentChapter ?: return 0

            val bookMark = Bookmark()
//            val requestItem = ReaderStatus.getRequestItem()

            bookMark.book_id = ReaderStatus.book!!.book_id
            bookMark.book_source_id = ReaderStatus.book!!.book_source_id
            bookMark.sequence = if (ReaderStatus.position.group + 1 > ReaderStatus.chapterList.size) ReaderStatus.chapterList.size else ReaderStatus.position.group
            bookMark.offset = ReaderStatus.position.offset
            bookMark.insert_time = System.currentTimeMillis()
            //if (ReaderStatus.book.dex == 1) {
            /*} else if (ReaderStatus.book.dex == 0) {
                bookMark.book_url = dataFactory.currentChapter.curl1;
            }*/
            bookMark.chapter_name = chapter.name
            //获取本页内容
            val content = DataProvider.findCurrentPageNovelLineBean() ?: return 0

            val sb = StringBuilder()
            if (ReaderStatus.position.group == -1) {
                bookMark.chapter_name = "《" + ReaderStatus.book!!.name + "》书籍封面页"
            } else if (ReaderStatus.position.index == 0 && content.size - 3 >= 0) {
                for (i in 3 until content.size) {
                    sb.append(content[i].lineContent)
                }
            } else {
                for (i in content.indices) {
                    sb.append(content[i].lineContent)
                }
            }

            // 去除第一个字符为标点符号的情况
            var content_text = sb.toString().trim { it <= ' ' }
            content_text = content_text.trim { it <= ' ' }

            content_text = AppUtils.deleteTextPoint(content_text)
            // 控制字数
            if (content_text.length > font_count) {
                content_text = content_text.substring(0, font_count)
            }
            bookMark.chapter_content = content_text
            mBookDataHelper.insertBookMark(bookMark)
            return 1
        } else {
            var logMap = HashMap<String, String>()
            logMap.put("type", "2")
            logMap.put("bookid", ReaderStatus.book!!.book_id)
            logMap.put("chapterid", ReaderStatus.chapterId.toString())
            StartLogClickUtil.upLoadEventLog(activity.get(), StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.BOOKMARKEDIT, logMap)
            mBookDataHelper.deleteBookMark(ReaderStatus.book!!.book_id!!, ReaderStatus.position.group, ReaderStatus.position.offset)
            return 2
        }
    }

    fun bookInfo() {
        StartLogClickUtil.upLoadEventLog(activity.get(), StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.BOOKDETAIL)
        //先这样实现吧...
        if (activity.get() is ReaderActivity) {
//            (activity.get() as ReaderActivity).showMenu(false)
        }
        val bundle = Bundle()
        bundle.putString("book_id", ReaderStatus.book.book_id)
        bundle.putString("book_source_id", ReaderStatus.book.book_source_id)
        bundle.putString("book_chapter_id", ReaderStatus.book.book_chapter_id)
        if (activity.get() != null) {
            RouterUtil.navigation(activity.get()!!, RouterConfig.COVER_PAGE_ACTIVITY, bundle)
        }

        val data = java.util.HashMap<String, String>()
        data.put("ENTER", "READPAGE")
        StartLogClickUtil.upLoadEventLog(activity.get(), StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.ENTER, data)

    }

    fun openWeb() {
        var url: String? = null
        if (ReaderStatus.currentChapter != null) {
            //if (ReaderStatus.book.dex == 1) {
            url = UrlUtils.buildContentUrl(ReaderStatus.currentChapter!!.url)
            /*} else if (ReaderStatus.book.dex == 0) {
                    url = dataFactory.currentChapter.curl1;*/
            //}
        }
        if (!TextUtils.isEmpty(url)) {
            try {
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                activity.get()?.startActivity(intent)
            }catch (e:Exception){
                e.printStackTrace()
            }
            val data = java.util.HashMap<String, String>()
            if (ReaderStatus != null) {
                data.put("bookid", ReaderStatus.book.book_id)
            }
            StartLogClickUtil.upLoadEventLog(activity.get(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.ORIGINALLINK, data)
        } else {
            activity.get()?.applicationContext?.showToastMessage("无法查看原文链接")
        }
    }


    fun chageNightMode(mode: Int = 0, useLightMode: Boolean = true) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity?.get()?.applicationContext)
        val edit = sharedPreferences.edit()
        val data = java.util.HashMap<String, String>()

        if (activity?.get()?.mThemeHelper!!.isNight) {
            //夜间模式只有一种背景， 不能存储
            //            edit.putInt("current_night_mode", ReadConfig.readThemeMode);
            if (useLightMode) {
                ReaderSettings.instance.readThemeMode = sharedPreferences.getInt("current_light_mode", 51)
            } else {
                ReaderSettings.instance.readThemeMode = mode
            }
            activity?.get()?.mThemeHelper?.setMode(ThemeMode.THEME1)
            data.put("type", "2")
            StartLogClickUtil.upLoadEventLog(activity?.get()?.getApplicationContext(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.NIGHTMODE1, data)
        } else {
            edit.putInt("current_light_mode", ReaderSettings.instance.readThemeMode)
            //            ReadConfig.readThemeMode = sharedPreferences.getInt("current_night_mode", 61);
            //夜间模式只有一种背景
            ReaderSettings.instance.readThemeMode = 61
            activity?.get()?.mThemeHelper?.setMode(ThemeMode.NIGHT)
            data.put("type", "1")
            StartLogClickUtil.upLoadEventLog(activity?.get()?.getApplicationContext(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.NIGHTMODE1, data)
        }
        edit.putInt("content_mode", ReaderSettings.instance.readThemeMode)
        edit.apply()

        changeNight()
    }

    fun changeNight() {
        val editor = activity?.get()?.getSharedPreferences("config", Context.MODE_PRIVATE)?.edit()
        if (ReaderSettings.instance.readThemeMode == 61) {
            if ("light" == ResourceUtil.mode) {
                editor?.putString("mode", "night")
                ResourceUtil.mode = "night"
                editor?.apply()
                EventBus.getDefault().post(EventSetting(EventSetting.Type.REFRESH_MODE))
            }
        } else {
            if ("night" == ResourceUtil.mode) {
                editor?.putString("mode", "light")
                ResourceUtil.mode = "light"
                editor?.apply()
                EventBus.getDefault().post(EventSetting(EventSetting.Type.REFRESH_MODE))
            }
        }
    }

    private var myDialog: MyDialog? = null

    private var type = -1

    fun readFeedBack() {
        if (activity != null && activity!!.get() != null && !activity!!.get()!!.isFinishing()) {
            if (ReaderStatus.position.group == -1) {
                activity.get()?.applicationContext?.showToastMessage("请到错误章节反馈")
                return
            }
            myDialog = MyDialog(activity?.get(), R.layout.dialog_feedback)
            myDialog!!.setCanceledOnTouchOutside(true)
            val dialog_title = myDialog!!.findViewById(R.id.dialog_title) as TextView
            dialog_title.setText(R.string.read_bottom_feedback)
            val checkboxsParent = myDialog!!.findViewById(R.id.feedback_checkboxs_parent) as LinearLayout
            val checkboxs = arrayOfNulls<CheckBox>(7)
            val relativeLayouts = arrayOfNulls<RelativeLayout>(7)
            var index = 0
            for (i in 0..checkboxsParent.childCount - 1) {
                val relativeLayout = checkboxsParent.getChildAt(i) as RelativeLayout
                relativeLayouts[i] = relativeLayout
                relativeLayouts[i]!!.setTag(i)
                for (j in 0..relativeLayout.childCount - 1) {
                    val v = relativeLayout.getChildAt(j)
                    if (v is CheckBox) {
                        checkboxs[index] = v
                        index++
                    }
                }
            }

            if (ReaderSettings.instance.isLandscape) {
                myDialog!!.findViewById(R.id.sv_feedback).layoutParams.height = activity.get()?.resources!!.getDimensionPixelOffset(R.dimen.dimen_view_height_160)
            } else {
                myDialog!!.findViewById(R.id.sv_feedback).layoutParams.height = FrameLayout.LayoutParams.WRAP_CONTENT
            }

            for (relativeLayout in relativeLayouts) {
                relativeLayout!!.setOnClickListener({ v ->
                    for (checkBox in checkboxs) {
                        checkBox!!.setChecked(false)
                    }
                    checkboxs[v.tag as Int]!!.setChecked(true)
                })
            }
            val submitButton = myDialog!!.findViewById(R.id.feedback_submit) as Button
            submitButton.setOnClickListener {
                StatServiceUtils.statAppBtnClick(activity?.get()?.getApplicationContext(), StatServiceUtils.rb_click_feedback_submit)
                for (n in checkboxs.indices) {
                    if (checkboxs[n]!!.isChecked()) {
                        type = n + 1
                    }
                }
                if (type == -1) {
                    activity.get()?.applicationContext?.showToastMessage("请选择错误类型")
                } else {
                    //                        data.put("type", "1");
                    //						StartLogClickUtil.upLoadEventLog(ReadingActivity.this, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.REPAIRDEDIALOGUE, data);
                    submitFeedback(type)
                    dismissDialog()
                    type = -1
                }
            }

            val cancelImage = myDialog!!.findViewById(R.id.feedback_cancel) as Button
            cancelImage.setOnClickListener {
                //                    data.put("type", "2");
                //                    StartLogClickUtil.upLoadEventLog(ReadingActivity.this, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.REPAIRDEDIALOGUE, data);
                dismissDialog()
            }

            if (!myDialog!!.isShowing) {
                try {
                    myDialog!!.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    private fun dismissDialog() {
        if (myDialog?.isShowing!!) {
            myDialog!!.dismiss()
        }
    }

    private fun submitFeedback(type: Int) {
        if (NetWorkUtils.getNetWorkType(activity?.get()) == NetWorkUtils.NETWORK_NONE) {
            activity.get()?.applicationContext?.showToastMessage("网络异常")
            return
        }
        val chapterErrorBean = ChapterErrorBean()
        val book = ReaderStatus.book
        chapterErrorBean.bookName = getEncode(book.name!!)
        chapterErrorBean.author = getEncode(book.author!!)
        chapterErrorBean.channelCode = if (Constants.QG_SOURCE == book.host) "1" else "2"
        var currChapter: Chapter? = null
        if (RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(ReaderStatus.book.book_id) != null) {
            val bookChapterDao = ChapterDaoHelper.loadChapterDataProviderHelper(BaseBookApplication.getGlobalContext(), book.book_id!!)
            currChapter = bookChapterDao.queryChapterBySequence(ReaderStatus.position.group)
        }
        if (currChapter == null) {
            val time = Observable.timer(1000, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        activity.get()?.applicationContext?.showToastMessage("已发送")
                    }, { e -> e.printStackTrace() })
            disposable.add(time)
//            handler.postDelayed({ readReference?.get()?.showToastShort("已发送") }, 1000)
            return
        }
        chapterErrorBean.bookSourceId = if (TextUtils.isEmpty(currChapter.book_source_id)) book.book_source_id else currChapter.book_source_id
        chapterErrorBean.chapterId = if (TextUtils.isEmpty(currChapter.chapter_id)) "" else currChapter.chapter_id
        chapterErrorBean.chapterName = getEncode(currChapter.name ?: "")
        chapterErrorBean.host = currChapter.host
        chapterErrorBean.serial = currChapter.sequence
        chapterErrorBean.type = type
        val curl = currChapter.url
        if (!TextUtils.isEmpty(curl)) {
            if (curl!!.contains("/V1/book/")) {
                val s = book.book_id + "/"
                val start = curl.indexOf(s) + s.length
                val end = curl.indexOf("/", start)
                chapterErrorBean.bookChapterId = curl.substring(start, end)
            }
        }
        if (TextUtils.isEmpty(chapterErrorBean.bookChapterId)) {
            chapterErrorBean.bookChapterId = ""
        }
        if (TextUtils.isEmpty(chapterErrorBean.host)) {
            chapterErrorBean.host = ""
        }
        val loadDataManager = LoadDataManager(activity?.get())
        loadDataManager.submitBookError(chapterErrorBean)
        StartLogClickUtil.upLoadChapterError(chapterErrorBean)
        val time = Observable.timer(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    activity.get()?.applicationContext?.showToastMessage("已发送")
                }, { e -> e.printStackTrace() })
        disposable.add(time)
    }

    var disposable: ArrayList<Disposable> = ArrayList()

    private fun getEncode(content: String): String {
        if (!TextUtils.isEmpty(content)) {
            try {
                return URLEncoder.encode(content, "utf-8")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }
        return ""
    }


    fun jumpNextChapterLog(type: Int) {
        val data = java.util.HashMap<String, String>()
        ReaderStatus.book?.let {
            data.put("bookid", it.book_id!!)
        }
        ReaderStatus.chapterId?.let {
            data.put("chapterid", it)
        }
        data.put("type", type.toString())
        StartLogClickUtil.upLoadEventLog(activity?.get(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.CHAPTERTURN, data)
    }

    fun readCatalogLog() {
        val data = java.util.HashMap<String, String>()
        ReaderStatus.book?.let {
            data.put("bookid", it.book_id!!)
        }
        ReaderStatus.chapterId?.let {
            data.put("chapterid", it)
        }
        StartLogClickUtil.upLoadEventLog(activity?.get(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.CATALOG, data)
    }


    fun back() {
        //先这样实现吧...
        val activity = this.activity.get()
        if (activity is ReaderActivity) {
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.BACK)
            activity.onBackPressed()
        }
    }

    fun clear() {
        for (d in disposable) {
            d.dispose()
        }
        disposable.clear()
    }

}