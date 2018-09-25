package com.intelligent.reader.util

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.TextView
import com.ding.basic.bean.SearchAutoCompleteBeanYouHua
import com.ding.basic.bean.SearchCommonBeanYouHua
import com.ding.basic.bean.SearchHotBean
import com.google.gson.Gson
import com.intelligent.reader.R
import com.intelligent.reader.presenter.search.SearchView
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.base.BaseBookApplication
import net.lzbook.kit.base.IPresenter
import net.lzbook.kit.utils.StatServiceUtils
import net.lzbook.kit.utils.Tools
import net.lzbook.kit.widget.MyDialog
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by yuchao on 2017/12/1 0001.
 */
class SearchHelpYouHuaPresenter(override var view: SearchView.HelpView?) : IPresenter<SearchView.HelpView> {
    private var mSuggestList: MutableList<Any>? = ArrayList()
    private var hotWords: MutableList<SearchHotBean.DataBean>? = ArrayList()
    private var suggest: String? = null
    private var searchType: String? = null
    private var gson: Gson? = null
    private var authorsBean: MutableList<SearchAutoCompleteBeanYouHua.DataBean.AuthorsBean> = ArrayList()
    private var labelBean: MutableList<SearchAutoCompleteBeanYouHua.DataBean.LabelBean> = ArrayList()
    private var bookNameBean: MutableList<SearchAutoCompleteBeanYouHua.DataBean.NameBean> = ArrayList()
    private var mHistoryDatas: ArrayList<String>? = ArrayList()

    init {
        gson = Gson()
    }

    fun initHistoryData(context: Context?) {
        mHistoryDatas = Tools.getHistoryWord(context)
    }

    fun getHistoryData(): ArrayList<String>? {
        return mHistoryDatas
    }

    fun onHistoryItemClick(context: Context?, arg0: AdapterView<*>, arg1: View, arg2: Int, position: Long) {
        StatServiceUtils.statAppBtnClick(context, StatServiceUtils.b_search_click_his_word)
        if (mHistoryDatas != null && !mHistoryDatas!!.isEmpty() && position > -1 &&
                position < mHistoryDatas!!.size) {
            val history = mHistoryDatas!![position.toInt()]
            if (history != null) {
                view?.setEditText(history)
                //                            mSearchEditText.setSelection(history.length());
                startSearch(history, "0")

                val data = HashMap<String, String>()
                data.put("keyword", history)
                data.put("rank", position.toString() + "")
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.BARLIST, data)
            }
        }
    }

    private fun startSearch(searchWord: String?, searchType: String?) {
        if (searchWord != null && !TextUtils.isEmpty(searchWord)) {
            addHistoryWord(searchWord)
            view?.onStartSearch(searchWord, searchType)
        }
    }

    fun addHistoryWord(keyword: String?) {
        if (mHistoryDatas == null) {
            mHistoryDatas = ArrayList<String>()
        }

        if (keyword == null || TextUtils.isEmpty(keyword)) {
            return
        }
        if (mHistoryDatas!!.contains(keyword)) {
            mHistoryDatas!!.remove(keyword)
        }

        if (!mHistoryDatas!!.contains(keyword)) {
            val size = mHistoryDatas!!.size
            if (size >= 10) {
                mHistoryDatas!!.removeAt(size - 1)
            }
            mHistoryDatas!!.add(0, keyword)
            Tools.saveHistoryWord(BaseBookApplication.getGlobalContext(), mHistoryDatas)
        }

        view?.notifyHisData()
        view?.setHistoryHeadersTitleView()
    }


    fun showDialog(activity: Activity?) {
        if (activity != null && !activity!!.isFinishing) {
            val dialog = MyDialog(activity, R.layout.dialog_delete_history, Gravity.CENTER)
            dialog.findViewById<TextView>(R.id.txt_cancel).setOnClickListener(View.OnClickListener {
                val data = HashMap<String, String>()
                data.put("type", "0")
                StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.HISTORYCLEAR, data)
                dialog.dismiss()
            })

            dialog.findViewById<TextView>(R.id.txt_continue).setOnClickListener(View.OnClickListener {
                val data = HashMap<String, String>()
                data.put("type", "1")
                StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH, StartLogClickUtil.HISTORYCLEAR, data)
                mSearchHandler?.sendEmptyMessage(10)
                view?.onHistoryClear()
                dialog.dismiss()
            })

            if (!dialog.isShowing) {
                dialog.show()
            }
        }
    }

    private fun clearHistory() {
        if (mHistoryDatas != null)
            mHistoryDatas!!.clear()
        view?.setHistoryHeadersTitleView()
        view?.notifyHisData()
        Tools.saveHistoryWord(BaseBookApplication.getGlobalContext(), mHistoryDatas)
    }

    private fun result(result: List<SearchCommonBeanYouHua>) {
        if (mSuggestList == null)
            return
        mSuggestList!!.clear()
        var index = 0
        for (item in result) {
            if (index > 4)
            // 只显示5个
                break

            mSuggestList!!.add(item)
            index++
        }
        view?.onSuggestBack()

    }

    fun onSearchResult(suggestList: List<Any>, transmitBean: SearchAutoCompleteBeanYouHua) {
        if (mSuggestList == null) {
            return
        }
        mSuggestList!!.clear()
        authorsBean.clear()
        labelBean.clear()
        bookNameBean.clear()
        if (transmitBean != null && transmitBean.data != null) {
            if (transmitBean.data!!.authors != null) {
                authorsBean = transmitBean.data!!.authors as MutableList<SearchAutoCompleteBeanYouHua.DataBean.AuthorsBean>
            }
            if (transmitBean.data!!.label != null) {
                labelBean = transmitBean.data!!.label as MutableList<SearchAutoCompleteBeanYouHua.DataBean.LabelBean>
            }
            if (transmitBean.data!!.name != null) {
                bookNameBean = transmitBean.data!!.name as MutableList<SearchAutoCompleteBeanYouHua.DataBean.NameBean>
            }
        }
        for (item in suggestList) {
            mSuggestList!!.add(item)
        }
        if (mSearchHandler == null)
            return
        mSearchHandler.post {
            view?.onSuggestBack()
        }
    }

    internal class SearchHandler(helper: SearchHelpYouHuaPresenter) : Handler() {
        private val reference: WeakReference<SearchHelpYouHuaPresenter>

        init {
            reference = WeakReference(helper)
        }

        override fun handleMessage(msg: Message) {
            val helper = reference.get() ?: return
            when (msg.what) {
                10 -> {
                    helper.clearHistory()
                }

                20 -> helper.result(msg.obj as ArrayList<SearchCommonBeanYouHua>)
                else -> {
                }
            }
        }
    }

    private val mSearchHandler = SearchHandler(this)

    fun clear() {
        if (mSuggestList != null) {
            mSuggestList!!.clear()
            mSuggestList = null
        }

    }

    fun onDestroy() {

        if (mHistoryDatas != null) {
            mHistoryDatas!!.clear()
            mHistoryDatas = null
        }

        if (mSuggestList != null) {
            mSuggestList!!.clear()
            mSuggestList = null
        }

    }

    fun hideInputMethod(paramView: View?) {
        if (paramView == null || paramView.context == null)
            return
        val imm = paramView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive) {
            imm.hideSoftInputFromWindow(paramView.applicationWindowToken, 0)
        }
    }


    fun setSearchType(type: String) {
        searchType = type
    }

    fun getSearchType(): String? {
        return searchType
    }

    fun getSuggestData(): MutableList<Any>? {
        return mSuggestList
    }

    fun getCurrSuggestData(): String? {
        return suggest
    }

    fun onSuggestItemClick(context: Context?, text: String, arg2: Int) {
        val obj = mSuggestList!!.get(arg2)
        if (obj is SearchCommonBeanYouHua) {
            suggest = obj.suggest
            searchType = "0"
            if (obj == "label") {
                searchType = "1"
            } else if (obj == "author") {
                searchType = "2"
            } else if (obj == "name") {
                searchType = "3"
            } else {
                searchType = "0"
            }

            if (!TextUtils.isEmpty(suggest)) {
                val data = HashMap<String, String>()
                data.put("keyword", suggest!!)
                data.put("enterword", text)
                data.put("rank", (arg2 + 1).toString())
                data.put("type", searchType ?: "")
                if (arg2 + 1 <= authorsBean.size) {
                    data.put("typerank", (arg2 + 1).toString() + "")
                } else if (arg2 + 1 <= authorsBean.size + labelBean.size) {
                    data.put("typerank", (arg2 + 1 - authorsBean.size).toString() + "")
                } else if (arg2 + 1 <= authorsBean.size + labelBean.size + bookNameBean.size) {
                    data.put("typerank", (arg2 + 1 - authorsBean.size - labelBean.size).toString() + "")
                } else {
                    data.put("typerank", (arg2 + 1).toString() + "")
                }
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.TIPLISTCLICK, data)
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.SEARCHRESULT_PAGE, StartLogClickUtil.SEARCHRESULT_BOOK, data)

                //                    mSearchEditText.setSelection(suggest.length());
                startSearch(suggest, searchType ?: "")
            }
        } else {
            return
        }


    }


}