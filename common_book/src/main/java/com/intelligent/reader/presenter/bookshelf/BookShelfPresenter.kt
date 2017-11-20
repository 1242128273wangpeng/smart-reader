package com.intelligent.reader.presenter.bookshelf

import android.content.SharedPreferences
import android.text.TextUtils
import com.dingyueads.sdk.Native.YQNativeAdInfo
import com.dingyueads.sdk.NativeInit
import com.intelligent.reader.presenter.IPresenter
import net.lzbook.kit.ad.OwnNativeAdManager
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.BookUpdate
import net.lzbook.kit.data.bean.BookUpdateResult
import net.lzbook.kit.data.bean.EventBookshelfAd
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.utils.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by qiantao on 2017/11/14 0014
 */
class BookShelfPresenter(override var view: BookShelfView?) : IPresenter<BookShelfView> {

    private val tag = "BookShelfPresenter"

    private var bookDaoHelper: BookDaoHelper = BookDaoHelper.getInstance()

    var iBookList: ArrayList<Book> = ArrayList()

    val adInfoHashMap: HashMap<Int, YQNativeAdInfo> = HashMap()

    private val updateTableList: ArrayList<String> = ArrayList()

    /**
     * 查询书籍列表
     */
    fun queryBookListAndAd(ownNativeAdManager: OwnNativeAdManager?, isNotShowAd: Boolean, isList: Boolean) {
        val bookList = bookDaoHelper.booksOnLineList
        Collections.sort(bookList, FrameBookHelper.MultiComparator())
        iBookList.clear()
        iBookList.addAll(bookList)
        runOnMain {
            view?.onBookListQuery(bookList)
        }
//        if (bookList.isNotEmpty() && Constants.dy_shelf_ad_switch && !Constants.isHideAD
//                && ownNativeAdManager != null) {
        if (true){
            if (isList) {//书架页列表形式
                AppLog.e(tag, "book形式的广告")
                fetchBookAd(true, bookList, ownNativeAdManager, isNotShowAd)
            } else {//书架页九宫格形式
                when (Constants.book_shelf_state) {
                    1 -> {
                        AppLog.e(tag, "banner形式的广告")
                        fetchBannerAd(ownNativeAdManager, isNotShowAd)
                    }
                    2 -> {
                        AppLog.e(tag, "book形式的广告")
                        fetchBookAd(false, bookList, ownNativeAdManager, isNotShowAd)
                        view?.hideBannerAd()
                    }
                    3 -> {
                        AppLog.e(tag, "book形式 + banner形式 的广告")
                        fetchBookAd(false, bookList, ownNativeAdManager, isNotShowAd)
                        fetchBannerAd(ownNativeAdManager, isNotShowAd)
                    }
                }
            }
        }
    }

    private fun fetchBookAd(isList: Boolean, bookList: ArrayList<Book>,
                            ownNativeAdManager: OwnNativeAdManager?, isNotShowAd: Boolean) {
        AppLog.e("wyhad1-1", adInfoHashMap.toString())

        if (isNotShowAd) return
        if (isList || Constants.book_shelf_state != 3) {//列表或九宫格首位广告的显示（当九宫格有banner广告，则不显示）
            val adInfo: YQNativeAdInfo?
            if (adInfoHashMap.containsKey(0) && adInfoHashMap[0] != null && adInfoHashMap[0]?.advertisement != null
                    && (System.currentTimeMillis() - (adInfoHashMap[0]?.availableTime ?: 0) < 3000
                    || adInfoHashMap[0]?.advertisement?.isShowed == false)) {
                adInfo = adInfoHashMap[0]
            } else {
                adInfo = ownNativeAdManager?.getSingleADInfoNew(0, NativeInit.CustomPositionName.SHELF_POSITION)
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
                    iBookList.add(0, book1)
                } catch (e: IndexOutOfBoundsException) {
                    e.printStackTrace()
                }

            } else {
                AppLog.e("wyhad1-1", "adInfo == null")
            }
        }

        val distance: Int = if (isList) {//计算广告展现频率  dy_shelf_ad_freq 为间隔
            bookList.size / Constants.dy_shelf_ad_freq
        } else {
            if (Constants.book_shelf_state != 3) {
                bookList.size / Constants.dy_shelf_ad_freq
            } else {
                (bookList.size - 1) / Constants.dy_shelf_ad_freq
            }
        }

        var currentPosition = 1//九宫格下 当状态为3的时候 从1 开始计算
        for (i in 0 until distance) {
            val info: YQNativeAdInfo?
            if (adInfoHashMap.containsKey(i + 1) && adInfoHashMap[i + 1] != null
                    && adInfoHashMap[i + 1]?.advertisement != null
                    && (System.currentTimeMillis() - (adInfoHashMap[i + 1]?.availableTime ?: 0) < 3000
                    || adInfoHashMap[i + 1]?.advertisement?.isShowed == false)) {
                info = adInfoHashMap[i + 1]
            } else {
                info = ownNativeAdManager?.getSingleADInfoNew(i + 1, NativeInit.CustomPositionName.SHELF_POSITION)
                if (info != null) {
                    info.availableTime = System.currentTimeMillis()
                    AppLog.e("ADSDK", "列表广告放入" + i + 1 + "位置" + "adInfoHashMap 大小" + adInfoHashMap.size)
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
                    if (isList) {
                        iBookList.add(Constants.dy_shelf_ad_freq * (i + 1), book1)
                    } else {
                        iBookList.add(Constants.dy_shelf_ad_freq + currentPosition, book1)
                        currentPosition += Constants.dy_shelf_ad_freq + 1
                    }
                } catch (e: IndexOutOfBoundsException) {
                    e.printStackTrace()
                    break
                }

            } else {
                AppLog.e("wyhad1-1", "info == null")
            }
        }
    }

    private fun fetchBannerAd(ownNativeAdManager: OwnNativeAdManager?, isNotShowAd: Boolean) {
        AppLog.e("wyhad1-1", "fetchBannerAd: ${!isNotShowAd}")
        if (isNotShowAd) return

        val adInfo: YQNativeAdInfo?
        if (adInfoHashMap.containsKey(0) && adInfoHashMap[0] != null
                && adInfoHashMap[0]?.advertisement != null
                && (System.currentTimeMillis() - (adInfoHashMap[0]?.availableTime ?: 0) < 3000
                || adInfoHashMap[0]?.advertisement?.isShowed == false)) {
            adInfo = adInfoHashMap[0]
        } else {
            adInfo = ownNativeAdManager?.getSingleADInfoNew(0, NativeInit.CustomPositionName.SHELF_POSITION)
            if (adInfo != null) {
                adInfo.availableTime = System.currentTimeMillis()
                AppLog.e("ADSDK", "header广告放入0位置" + "adInfoHashMap 大小" + adInfoHashMap.size)
                adInfoHashMap.put(0, adInfo)
            }
        }
        if (adInfo == null) return //获取广告失败直接返回
        runOnMain {
            view?.showBannerAd(adInfo)
        }
    }

    fun handleSuccessUpdate(result: BookUpdateResult) {
        val hasUpdateList = ArrayList<BookUpdate>()
        if (result.items != null && result.items.isNotEmpty()) {
            val bookUpdates = result.items
            val size = bookUpdates.size
            (0 until size).map { bookUpdates[it] }
                    .filterTo(hasUpdateList) { !TextUtils.isEmpty(it.book_id) && it.update_count != 0 }
            if (hasUpdateList.isNotEmpty()) {
                view?.onSuccessUpdateHandle(hasUpdateList.size, hasUpdateList[0])
            }
        } else {
            view?.onSuccessUpdateHandle()
        }
    }

    fun remove360Ads() {
        val iterator = adInfoHashMap.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val value = entry.value
            if (value.advertisement != null
                    && value.advertisement.platformId == com.dingyueads.sdk.Constants.AD_TYPE_360
                    && value.advertisement.isClicked) {
                iterator.remove()
            }
        }
    }

    fun removeAd() {
        iBookList.filter {
            //若当前的书籍是广告
            it.book_type == -2
        }.forEach { book ->
            iBookList.remove(book)
        }
    }

    /**
     * 过滤出更新状态的表
     */
    fun filterUpdateTableList(): ArrayList<String> {
        iBookList.asSequence().forEach { book ->
            if (book.update_status == 1) {
                if (!updateTableList.contains(book.book_id)) {
                    updateTableList.add(book.book_id)
                }
            } else {
                if (updateTableList.contains(book.book_id)) {
                    updateTableList.remove(book.book_id)
                }
            }
        }
        return updateTableList
    }

    /**
     * 取消数据库中更新状态
     */
    fun resetUpdateStatus(book_id: String) {
        val book = Book()
        book.book_id = book_id
        book.update_status = 0
        if (updateTableList.contains(book.book_id)) {
            updateTableList.remove(book_id)
            bookDaoHelper.updateBook(book)
        }
    }

    fun uploadFirstOpenLog(sp: SharedPreferences) {
        //判断用户是否是当日首次打开应用,并上传书架的id
        val lastTime = sp.getLong(Constants.TODAY_FIRST_POST_BOOKIDS, 0)
        val currentTime = System.currentTimeMillis()

        val isSameDay = AppUtils.isToday(lastTime, currentTime)
        if (!isSameDay) {
            val bookIdList = StringBuilder()
            iBookList.forEachIndexed { index, book ->
                bookIdList.append(book.book_id)
                bookIdList.append(if (book.readed == 1) "_1" else "_0")//1已读，0未读
                bookIdList.append(if (index == iBookList.size) "" else "$")
            }
            val data = HashMap<String, String>()
            data.put("bookid", bookIdList.toString())
            StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                    StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.BOOKLIST, data)
            sp.edit().putLong(Constants.TODAY_FIRST_POST_BOOKIDS, currentTime).apply()
        }
    }

    fun uploadItemClickLog(position: Int) {
        val data = HashMap<String, String>()
        data.put("bookid", iBookList[position].book_id)
        data.put("rank", (position + 1).toString())
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.BOOKCLICK, data)
    }

    fun uploadItemLongClickLog() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.LONGTIMEBOOKSHELFEDIT)
    }

    fun handleBookShelfAd(eventBookshelfAd: EventBookshelfAd, isHandle: Boolean,
                          isNotShowAd: Boolean, ownNativeAdManager: OwnNativeAdManager?, isList: Boolean) {
        if (eventBookshelfAd.type_ad == NativeInit.CustomPositionName.SHELF_POSITION.toString()) {
            if (!isHandle) return
            if (eventBookshelfAd.yqNativeAdInfo != null) {
                eventBookshelfAd.yqNativeAdInfo.availableTime = System.currentTimeMillis()
            }
            adInfoHashMap.put(eventBookshelfAd.position, eventBookshelfAd.yqNativeAdInfo)
            queryBookListAndAd(ownNativeAdManager, isNotShowAd, isList)
            runOnMain {
                view?.onBookShelfAdHandle()
            }
        } else if (eventBookshelfAd.type_ad == "bookshelfclick_360") {
            if (eventBookshelfAd.yqNativeAdInfo != null) {
                eventBookshelfAd.yqNativeAdInfo.availableTime = System.currentTimeMillis() + 2000
            }
            adInfoHashMap.put(eventBookshelfAd.position, eventBookshelfAd.yqNativeAdInfo)
        }
    }

    fun deleteBooks(books: ArrayList<Book>) {
        val size = books.size
        doAsync {
            val bookIdArr = arrayOfNulls<String>(size)
            val sb = StringBuffer()
            for (i in 0 until size) {
                val book = books[i]
                bookIdArr[i] = book.book_id

                sb.append(book.book_id)
                sb.append(if (book.readed == 1) "_1" else "_0")
                sb.append(if (i == size - 1) "" else "$")
            }
            // 删除书架数据库和章节数据库
            bookDaoHelper.deleteBook(*bookIdArr)
            runOnMain {
                view?.onBookDelete()
            }

            uploadBookDeleteLog(size, sb)
        }
    }

    private fun uploadBookDeleteLog(size: Int, sb: StringBuffer) {
        val data1 = HashMap<String, String>()
        data1.put("type", "1")
        data1.put("number", size.toString())
        data1.put("bookids", sb.toString())
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.SHELFEDIT_PAGE, StartLogClickUtil.DELETE1, data1)
    }

    fun uploadBookDeleteCancelLog() {
        val data = HashMap<String, String>()
        data.put("type", "2")
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.SHELFEDIT, StartLogClickUtil.DELETE1, data)
    }
}