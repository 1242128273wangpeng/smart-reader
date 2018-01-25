package com.intelligent.reader.presenter.bookEnd

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.intelligent.reader.R
import com.intelligent.reader.activity.CataloguesActivity
import com.intelligent.reader.activity.HomeActivity
import com.intelligent.reader.activity.SearchBookActivity
import com.intelligent.reader.read.help.BookHelper
import com.intelligent.reader.read.mode.ReadState
import com.intelligent.reader.util.EventBookStore
import net.lzbook.kit.book.component.service.DownloadService
import net.lzbook.kit.book.view.MyDialog
import net.lzbook.kit.book.view.RecommendItemView
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.*
import net.lzbook.kit.data.db.BookChapterDao
import net.lzbook.kit.data.db.BookDaoHelper
import net.lzbook.kit.request.own.OtherRequestService
import net.lzbook.kit.utils.*
import java.lang.ref.WeakReference
import java.util.ArrayList

/**
 * Created by zhenXiang on 2017\11\21 0021.
 */

class BookEndPresenter(val act: Activity, val bookEndContract: BookEndContract,
                        var category: String) {
    var activity: WeakReference<Activity>? = null
    var myDialog: MyDialog? = null
    var sourceList = ArrayList<Source>()
    private var mBookDaoHelper: BookDaoHelper? = null

    init {
        activity = WeakReference(act)
        mBookDaoHelper = BookDaoHelper.getInstance()
    }

    //获取书籍来源信息
    fun getBookSource() {
        Thread(Runnable {
            try {
                if (Constants.QG_SOURCE != ReadState.book.site && Constants.SG_SOURCE != ReadState.book.site) {
                    OtherRequestService.requestBookSourceChange(handler, 1, -144, ReadState.book.book_id)
                } else {
                    handler.sendEmptyMessage(0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }

    //书籍来源列表点击
    fun itemClick(source: Source) {

        if (mBookDaoHelper!!.isBookSubed(ReadState.book.book_id)) {
            if (source.book_source_id != ReadState.book.book_source_id) {
                //弹出切源提示
                showChangeSourceNoticeDialog(source)
                return
            }
        }
        intoCatalogActivity(source, false)
    }

    /**
     * 去书城
     */
    fun goToBookStore() {
        val storeIntent = Intent()
        storeIntent.setClass(activity!!.get(), HomeActivity::class.java)
        try {
            val bundle = Bundle()
            bundle.putInt(EventBookStore.BOOKSTORE, EventBookStore.TYPE_TO_BOOKSTORE)
            storeIntent.putExtras(bundle)
            ATManager.exitReading()
            activity!!.get()!!.startActivity(storeIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun goToBookSearchActivity(view: View) {
        val intent = Intent()
        if (view is RecommendItemView) {
            val item = view as RecommendItemView
            intent.putExtra("word", item.title)
            intent.putExtra("search_type", "0")
            intent.putExtra("filter_type", "0")
            intent.putExtra("filter_word", "ALL")
            intent.putExtra("sort_type", "0")
            intent.setClass(activity!!.get(), SearchBookActivity::class.java)
            activity!!.get()!!.startActivity(intent)
            return
        }
    }

    /**
     * 去书架
     */
    fun goToShelf() {
        val shelfIntent = Intent()
        shelfIntent.setClass(activity!!.get(), HomeActivity::class.java)
        try {
            val bundle = Bundle()
            bundle.putInt(EventBookStore.BOOKSTORE, EventBookStore.TYPE_TO_BOOKSHELF)
            shelfIntent.putExtras(bundle)
            ATManager.exitReading()
            activity!!.get()!!.startActivity(shelfIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun intoCatalogActivity(source: Source, b: Boolean) {
        if (ReadState.book != null) {

            val iterator = source.source.entries.iterator()
            val list = ArrayList<String>()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val value = entry.value
                list.add(value)
            }

            //readStatus.requestConfig = BookApplication.getGlobalContext().getSourceConfig(requestItem.host);
            val bookDaoHelper = BookDaoHelper.getInstance()
            if (bookDaoHelper.isBookSubed(source.book_id)) {
                val iBook = bookDaoHelper.getBook(source.book_id, 0)
                iBook.book_source_id = ReadState.book.book_source_id
                iBook.site = ReadState.book.site
                iBook.parameter = ReadState.book.parameter
                iBook.extra_parameter = ReadState.book.extra_parameter
                iBook.last_updatetime_native = source.update_time
                iBook.dex = source.dex
                bookDaoHelper.updateBook(iBook)
                ReadState.book = iBook
                if (b) {
                    val bookChapterDao = BookChapterDao(activity!!.get(), source.book_id)
                    BookHelper.deleteAllChapterCache(source.book_id, 0, bookChapterDao.count)
                    bookChapterDao.deleteBookChapters(0)
                    DownloadService.clearTask(source.book_id)
                    BaseBookHelper.delDownIndex(activity!!.get(), source.book_id)
                }
            } else {
                val iBook = ReadState.book
                iBook.book_source_id = source.book_source_id
                iBook.site = source.host
                iBook.dex = source.dex
            }
            //dataFactory.chapterList.clear();
            openCategoryPage()
        }
    }

    //进入阅读页
    private fun openCategoryPage() {
        //if (readStatus.book.book_type == 0) {
        val intent = Intent(activity!!.get(), CataloguesActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val bundle = Bundle()
        bundle.putSerializable("cover", ReadState.book)
        bundle.putString("book_id", ReadState.book.book_id)
        //AppLog.e(TAG, "OpenCategoryPage: " + readStatus.sequence);
        bundle.putInt("sequence", ReadState.sequence)
        bundle.putBoolean("fromCover", true)
        bundle.putBoolean("fromEnd", true)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        //AppLog.e(TAG, "ReadingActivity: " + readStatus.getRequestItem().toString());
        bundle.putSerializable(Constants.REQUEST_ITEM, RequestItem.fromBook(ReadState.book))
        intent.putExtras(bundle)
        activity!!.get()!!.startActivity(intent)
    }

    //换源弹窗
    fun showChangeSourceNoticeDialog(source: Source) {
        if (!activity!!.get()!!.isFinishing()) {
            dismissDialog()

            myDialog = MyDialog(activity!!.get(), R.layout.publish_hint_dialog)
            myDialog!!.setCanceledOnTouchOutside(true)
            val dialog_cancel = myDialog!!.findViewById(R.id.publish_stay) as Button
            dialog_cancel.setText(R.string.cancel)
            val dialog_confirm = myDialog!!.findViewById(R.id.publish_leave) as Button
            dialog_confirm.setText(R.string.book_cover_confirm_change_source)
            val dialog_information = myDialog!!.findViewById(R.id.publish_content) as TextView
            dialog_information.setText(R.string.book_cover_change_source_prompt)
            dialog_cancel.setOnClickListener { dismissDialog() }
            dialog_confirm.setOnClickListener {
                dismissDialog()
                intoCatalogActivity(source, true)
            }

            myDialog!!.setOnCancelListener(DialogInterface.OnCancelListener { myDialog!!.dismiss() })
            if (!myDialog!!.isShowing()) {
                try {
                    myDialog!!.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    private fun dismissDialog() {
        if (myDialog != null && myDialog!!.isShowing()) {
            myDialog!!.dismiss()
        }
    }

    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                1 -> {

                    val count = (msg.obj as SourceItem).sourceList.size
                    if (count != 0) {
                        for (i in 0..count - 1) {
                            if (i < 3) {
                                sourceList!!.add((msg.obj as SourceItem).sourceList[i])
                            }
                        }
                    }
                    bookEndContract.showSource(true, sourceList)

                }
                0 -> {
                    bookEndContract.showSource(false, sourceList)
                }
                -144 -> {
                    bookEndContract.showSource(false, sourceList)
                }
            }
            super.handleMessage(msg)
        }
    }

}
