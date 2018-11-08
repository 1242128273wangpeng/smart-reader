package com.dy.reader.presenter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.StringRes
import android.text.TextUtils
import android.widget.Toast
import com.ding.basic.bean.*
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.net.RequestSubscriber
import com.dy.reader.R
import com.dy.reader.activity.ReaderActivity
import com.dy.reader.data.DataProvider
import com.dy.reader.dialog.ReaderCacheDialog
import com.dy.reader.dialog.ReaderFeedbackDialog
import com.dy.reader.event.EventSetting
import com.dy.reader.help.NovelHelper
import com.dy.reader.setting.ReaderSettings
import com.dy.reader.setting.ReaderStatus
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.bean.ChapterErrorBean
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.ResourceUtil
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.book.BaseBookHelper
import net.lzbook.kit.utils.book.LoadDataManager
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.theme.ThemeMode
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.utils.webview.UrlUtils
import net.lzbook.kit.ui.widget.ApplicationShareDialog
import com.dingyue.statistics.DyStatService
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.pointpage.EventPoint

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

    private val requestRepositoryFactory = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())

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
            if ((requestRepositoryFactory.checkBookSubscribe(ReaderStatus.book.book_id) != null)) {
                val iBook = requestRepositoryFactory.loadBook(ReaderStatus.book.book_id)
                if (iBook != null && source.book_source_id != iBook.book_source_id) {
                    //弹出切源提示
                    DyStatService.onEvent(EventPoint.READPAGEMORE_SOURCECHANGECONFIRM, mapOf("type" to "1"))
                    intoCatalogActivity(source, true)
                    return
                }
            }
        }
        intoCatalogActivity(source!!, false)
    }

    private fun intoCatalogActivity(source: Source, changeSource: Boolean) {
        var book = requestRepositoryFactory.loadBook(ReaderStatus.book.book_id)

        if (book != null) {
            book.host = source.host
            book.book_source_id = source.book_source_id
            book.book_chapter_id = source.book_chapter_id

            if (changeSource) {
                //停止预缓存逻辑
                DataProvider.clearPreLoad()
            }
        } else {
            book = ReaderStatus.book
            book.host = source.host
            book.book_source_id = source.book_source_id
            book.book_chapter_id = source.book_chapter_id
        }

        val bundle = Bundle()
        bundle.putSerializable("cover", book)
        bundle.putString("book_id", book.book_id)
        bundle.putInt("sequence", ReaderStatus.book.sequence)
        bundle.putBoolean("fromCover", true)
        bundle.putBoolean("changeSource", changeSource)

        val activity = this.activity.get()

        if (activity != null) {
            RouterUtil.navigationWithCode(activity, RouterConfig.CATALOGUES_ACTIVITY, bundle, 1)
        }
    }

    fun cache() {
        if (requestRepositoryFactory.checkBookSubscribe(ReaderStatus.book.book_id) == null && requestRepositoryFactory.insertBook(ReaderStatus.book) <= 0) {
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
        data["bookid"] = ReaderStatus.book.book_id
        data["chapterid"] = ReaderStatus.chapterId
        DyStatService.onEvent(EventPoint.READPAGE_MORE,data)
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
        if (activity.get() != null && !activity.get()!!.isFinishing) {
            val readerCacheDialog = ReaderCacheDialog(activity.get()!!)

            readerCacheDialog.cacheAllListener = {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_download_all)

                if (NetWorkUtils.getNetWorkType(context) == NetWorkUtils.NETWORK_NONE) {
                    ToastUtil.showToastMessage(R.string.game_network_none)
                } else {

                    BaseBookHelper.startDownBookTask(activity.get(), mBook, 0)
                    readerCacheDialog.dismiss()

                    val data = java.util.HashMap<String, String>()
                    data["bookid"] = ReaderStatus.book.book_id
                    if (ReaderStatus.currentChapter != null) {
                        data["chapterid"] = ReaderStatus.currentChapter!!.chapter_id
                    }
                    data["type"] = "1"
                    DyStatService.onEvent(EventPoint.READPAGE_CACHE, data)

                }
            }
            readerCacheDialog.cacheCurrentStartListener = {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_download_from_now)
                if (NetWorkUtils.getNetWorkType(context) == NetWorkUtils.NETWORK_NONE) {
                    ToastUtil.showToastMessage(R.string.game_network_none)
                } else {
                    BaseBookHelper.startDownBookTask(activity.get(), mBook, sequence)

                    readerCacheDialog.dismiss()

                    val data = java.util.HashMap<String, String>()
                    data["bookid"] = ReaderStatus.book.book_id
                    if (ReaderStatus.currentChapter != null) {
                        data["chapterid"] = ReaderStatus.currentChapter!!.chapter_id
                    }
                    data["type"] = "2"
                    DyStatService.onEvent(EventPoint.READPAGE_CACHE, data)
                }
            }

            readerCacheDialog.cacheCancelListener = {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_download_cancel)
                readerCacheDialog.dismiss()
                val data = java.util.HashMap<String, String>()
                data["bookid"] = ReaderStatus.book.book_id
                if (ReaderStatus.currentChapter != null) {
                    data["chapterid"] = ReaderStatus.currentChapter!!.chapter_id
                }
                data["type"] = "0"
                DyStatService.onEvent(EventPoint.READPAGE_CACHE, data)
            }

            readerCacheDialog.show()
        }
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
        if (ReaderStatus.book.fromQingoo()) {
            showToastShort("该小说暂无其他来源！")
            return
        }


        if (isSourceListShow) {
            isSourceListShow = false
        } else {

            EventBus.getDefault().post(EventSetting(EventSetting.Type.MENU_STATE_CHANGE, false))

            if (ReaderStatus.book != null && !TextUtils.isEmpty(ReaderStatus.book.book_id)) {
                requestRepositoryFactory.requestBookSources(ReaderStatus.book.book_id, ReaderStatus.book.book_source_id, ReaderStatus.book.book_chapter_id, object : RequestSubscriber<BookSource>() {
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
            ToastUtil.showToastMessage("暂无其它来源")
        }
    }

    fun bookMark(): Int {
        StatServiceUtils.statAppBtnClick(activity.get(), StatServiceUtils.rb_click_add_book_mark_btn)
        return addOptionMark(font_count, ReaderStatus.book.item_type)
    }

    /**
     * 添加手动书签
     */
    private fun addOptionMark(font_count: Int, type: Int): Int {
        if (activity.get() == null) {
            return 0
        }

        // 如果是扉页，直接return
        val sequence = if (ReaderStatus.position.group + 1 > ReaderStatus.chapterList.size) ReaderStatus.chapterList.size else ReaderStatus.position.group
        if (sequence == -1) {
            return 0
        }

        var localBook = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(ReaderStatus.book.book_id)

        if (localBook == null) {
            localBook = ReaderStatus.book
            localBook.readed = 1
            localBook.chapter_count = ReaderStatus.chapterList.size

            if (RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).insertBook(localBook) <= 0) {
                return 0
            }
        }

        if (!requestRepositoryFactory.isBookMarkExist(ReaderStatus.book.book_id, ReaderStatus.position.group, ReaderStatus.position.offset)) {
            var logMap = HashMap<String, String>()
            logMap.put("type", "1")
            logMap.put("bookid", ReaderStatus.book?.book_id)
            logMap.put("chapterid", ReaderStatus?.chapterId)
            StartLogClickUtil.upLoadEventLog(activity.get(), StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.LABELEDIT, logMap)

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
            requestRepositoryFactory.insertBookMark(bookMark)
            return 1
        } else {
            val logMap = HashMap<String, String>()
            logMap["type"] = "2"
            logMap["bookid"] = ReaderStatus.book.book_id
            logMap["chapterid"] = ReaderStatus.chapterId
            DyStatService.onEvent(EventPoint.READPAGE_LABELEDIT, logMap)
            requestRepositoryFactory.deleteBookMark(ReaderStatus.book.book_id, ReaderStatus.position.group, ReaderStatus.position.offset)
            return 2
        }
    }

    fun bookInfo() {
        DyStatService.onEvent(EventPoint.READPAGEMORE_BOOKDETAIL)
        //先这样实现吧...
        if (activity.get() is ReaderActivity) {
//            (activity.get() as ReaderActivity).showMenu(false)
        }
        val bundle = Bundle()
        bundle.putString("author", ReaderStatus.book.author)
        bundle.putString("book_id", ReaderStatus.book.book_id)
        bundle.putString("book_source_id", ReaderStatus.book.book_source_id)
        bundle.putString("book_chapter_id", ReaderStatus.book.book_chapter_id)
        if (activity.get() != null) {
            RouterUtil.navigation(activity.get()!!, RouterConfig.COVER_PAGE_ACTIVITY, bundle)
        }

        DyStatService.onEvent(EventPoint.BOOOKDETAIL_ENTER, mapOf("bookid" to ReaderStatus.book.book_id, "source" to "READPAGE"))
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
            DyStatService.onEvent(EventPoint.READPAGE_ORIGINALLINK, mapOf("bookid" to ReaderStatus.book.book_id))
        } else {
            ToastUtil.showToastMessage("无法查看原文链接")
        }
    }

    fun showShareDialog() {
        DyStatService.onEvent(EventPoint.READPAGE_SHARE)
        if (activity.get() != null && !activity.get()!!.isFinishing) {
            val applicationShareDialog = ApplicationShareDialog(activity.get())
            applicationShareDialog.show()

            val activity = this.activity.get()

            if (activity is ReaderActivity) {
                activity.registerShareCallback(true)
            }
        }
    }

    fun chageNightMode(mode: Int = 0, useLightMode: Boolean = true) {
        val data = java.util.HashMap<String, String>()

        if (activity.get()?.mThemeHelper!!.isNight) {
            if (useLightMode) {
                ReaderSettings.instance.readThemeMode = ReaderSettings.instance.readLightThemeMode
            } else {
                ReaderSettings.instance.readThemeMode = mode
            }

            activity.get()?.mThemeHelper?.setMode(ThemeMode.THEME1)
            DyStatService.onEvent(EventPoint.READPAGE_NIGHTMODE, mapOf("type" to "2")) // 2代表日间
        } else {

            //夜间模式只有一种背景
            ReaderSettings.instance.readLightThemeMode = ReaderSettings.instance.readThemeMode
            ReaderSettings.instance.readThemeMode = 61
            activity.get()?.mThemeHelper?.setMode(ThemeMode.NIGHT)
            DyStatService.onEvent(EventPoint.READPAGE_NIGHTMODE, mapOf("type" to "1")) // 1代表夜间
        }
//        ReaderSettings.instance.save()

        changeNight()
    }

    fun changeNight() {
        val editor = activity.get()?.getSharedPreferences("config", Context.MODE_PRIVATE)?.edit()
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

    fun readFeedBack() {
        if (activity.get() != null && !activity.get()!!.isFinishing) {
            if (ReaderStatus.position.group == -1) {
                ToastUtil.showToastMessage("请到错误章节反馈")
                return
            }
            DyStatService.onEvent(EventPoint.READPAGEMORE_FEEDBACK)
            val readerFeedbackDialog = ReaderFeedbackDialog(activity.get()!!)

            readerFeedbackDialog.insertSubmitListener {

                StatServiceUtils.statAppBtnClick(activity.get()?.applicationContext, StatServiceUtils.rb_click_feedback_submit)

                submitFeedback(it)
            }

            readerFeedbackDialog.insertCancelListener {

            }

            readerFeedbackDialog.show()
        }
    }

    private fun submitFeedback(type: Int) {
        if (NetWorkUtils.getNetWorkType(activity?.get()) == NetWorkUtils.NETWORK_NONE) {
            ToastUtil.showToastMessage("网络异常")
            return
        }
        val chapterErrorBean = ChapterErrorBean()
        val book = ReaderStatus.book
        chapterErrorBean.bookName = getEncode(book.name!!)
        chapterErrorBean.author = getEncode(book.author!!)
        chapterErrorBean.channelCode = if (book.fromQingoo()) "1" else "2"
        var currChapter: Chapter? = null
        if (requestRepositoryFactory.checkBookSubscribe(ReaderStatus.book.book_id) != null) {
            currChapter = requestRepositoryFactory.queryChapterBySequence(ReaderStatus.book.book_id , ReaderStatus.position.group)
        }
        if (currChapter == null) {
            val time = Observable.timer(1000, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        ToastUtil.showToastMessage("已发送")
                    }, { e -> e.printStackTrace() })
            disposable.add(time)
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
        DyStatService.onChapterError(chapterErrorBean.toMap())
        val time = Observable.timer(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    ToastUtil.showToastMessage("已发送")
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
        data["bookid"] = ReaderStatus.book.book_id
        data["chapterid"] = ReaderStatus.chapterId
        data["type"] = type.toString()
        DyStatService.onEvent(EventPoint.READPAGE_CHAPTERTURN, data)
    }

    fun readCatalogLog() {
        DyStatService.onEvent(EventPoint.READPAGE_CATALOG, mapOf("bookid" to ReaderStatus.book.book_id, "chapterid" to ReaderStatus.chapterId))
    }


    fun back() {
        //先这样实现吧...
        val activity = this.activity.get()
        if (activity is ReaderActivity) {
            DyStatService.onEvent(EventPoint.READPAGE_BACK, mapOf("type" to "1"))
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