package com.intelligent.reader.util

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Message
import android.preference.PreferenceManager
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.ding.basic.bean.SearchAutoCompleteBean
import com.ding.basic.bean.SearchAutoCompleteBeanYouHua
import com.ding.basic.bean.SearchCommonBeanYouHua
import com.ding.basic.bean.SearchHotBean
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.RequestSubscriber
import com.google.gson.Gson
import com.intelligent.reader.R
import com.orhanobut.logger.Logger
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.base.BaseBookApplication
import net.lzbook.kit.base.IPresenter
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.*
import net.lzbook.kit.utils.logger.AppLog
import net.lzbook.kit.utils.sp.SharedPreferencesUtils
import net.lzbook.kit.widget.MyDialog
import java.lang.ref.WeakReference
import java.util.ArrayList
import java.util.HashMap

/**
 * Created by yuchao on 2017/12/1 0001.
 */
class SearchHelpPresenter(override var view: SearchView.HelpView?) : IPresenter<SearchView.HelpView> {

    private var mSuggestList: MutableList<SearchCommonBeanYouHua>? = ArrayList()
    private var hotWords: MutableList<SearchHotBean.DataBean>? = ArrayList()
    private var suggest: String? = null
    private var searchType: String? = null
    private var sharedPreferencesUtils: SharedPreferencesUtils? = null
    private var gson: Gson? = null
    private var authorsBean: MutableList<SearchAutoCompleteBeanYouHua.DataBean.AuthorsBean> = ArrayList()
    private var labelBean: MutableList<SearchAutoCompleteBeanYouHua.DataBean.LabelBean> = ArrayList()
    private var bookNameBean: MutableList<SearchAutoCompleteBeanYouHua.DataBean.NameBean> = ArrayList()
    private var mHistoryDatas: ArrayList<String>? = ArrayList()

    init {
        gson = Gson()
        sharedPreferencesUtils = SharedPreferencesUtils(PreferenceManager.getDefaultSharedPreferences(BaseBookApplication.getGlobalContext()))
    }

    fun initHistoryData(context: Context?) {
        mHistoryDatas = Tools.getHistoryWord(context)
    }

    fun getHistoryData(): ArrayList<String>? {
        return mHistoryDatas
    }

    fun onHistoryItemClick(context: Context?, arg0: AdapterView<*>?, arg1: View?, arg2: Int, position: Long) {
        if(context != null) {
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
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.HISTORY, data)
                }
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
            if (size >= 5) {
                mHistoryDatas!!.removeAt(size - 1)
            }
            mHistoryDatas!!.add(0, keyword)
            Tools.saveHistoryWord(BaseBookApplication.getGlobalContext(), mHistoryDatas)
        }

        view?.notifyHisData()
        view?.setHistoryHeadersTitleView()
    }

    fun resetHotWordList(context: Context) {

        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            getCacheDataFromShare(false)
        } else {

            view?.showDialogState(true)
            RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestHotWords(object : RequestSubscriber<SearchHotBean>() {
                override fun requestResult(result: SearchHotBean?) {
                    parseResult(result, true)

                }

                override fun requestError(message: String) {
                    Logger.e("获取搜索热词异常！")
                    getCacheDataFromShare(true)
                    view?.showDialogState(false)
                }

                override fun requestComplete() {

                }
            })

        }
    }

    /**
     * if hasn't net getData from sharepreferenecs cache
     */
    fun getCacheDataFromShare(hasNet: Boolean) {
        if (sharedPreferencesUtils != null && !TextUtils.isEmpty(sharedPreferencesUtils!!.getString(Constants.SERARCH_HOT_WORD))) {
            view?.showLinearParent(true)
            hotWords!!.clear()
            val cacheHotWords = sharedPreferencesUtils!!.getString(Constants.SERARCH_HOT_WORD)
            val searchHotBean = gson!!.fromJson(cacheHotWords, SearchHotBean::class.java)
            parseResult(searchHotBean, false)
            AppLog.e("urlbean", cacheHotWords)
        } else {
//            if (!hasNet) {
//                ToastUtils.showToastNoRepeat("网络不给力哦")
//            }
            view?.showLinearParent(false)
        }
    }

    /**
     * parse result data
     */
    fun parseResult(value: SearchHotBean?, hasNet: Boolean) {
        hotWords!!.clear()
        if (value != null && value.data != null) {
            hotWords = value.data as MutableList<SearchHotBean.DataBean>?
            if (hotWords != null && hotWords!!.size >= 0) {
                view?.showLinearParent(true)
                if (hasNet) {
                    sharedPreferencesUtils!!.putString(Constants.SERARCH_HOT_WORD, gson!!.toJson(value, SearchHotBean::class.java))
                }
                view?.setHotWordAdapter(hotWords)
            } else {
                sharedPreferencesUtils!!.putString(Constants.SERARCH_HOT_WORD, "")
                view?.showLinearParent(false)
            }
        }
        view?.showDialogState(false)
    }


    fun showDialog(activity: Activity?) {
        if (activity != null && !activity!!.isFinishing) {
            val myDialog = MyDialog(activity, R.layout.publish_hint_dialog)
            myDialog.setCanceledOnTouchOutside(true)
            val dialog_title = myDialog.findViewById<TextView>(R.id.dialog_title)
            dialog_title.setText(R.string.prompt)
            val dialog_content = myDialog.findViewById<TextView>(R.id.publish_content)
            dialog_content.setText(R.string.determine_clear_serach_history)
            val dialog_comfire = myDialog.findViewById<TextView>(R.id.publish_leave)

            dialog_comfire.setOnClickListener {
                val data = HashMap<String, String>()
                data.put("type", "1")
                StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH, StartLogClickUtil.HISTORYCLEAR, data)
                mSearchHandler?.sendEmptyMessage(10)
                myDialog.dismiss()
            }
            val dialog_cancle = myDialog.findViewById<TextView>(R.id.publish_stay)
            dialog_cancle.setOnClickListener {
                val data = HashMap<String, String>()
                data.put("type", "0")
                StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.HISTORYCLEAR, data)
                myDialog.dismiss()
            }
            myDialog.setOnCancelListener { myDialog.dismiss() }
            if (!myDialog.isShowing) {
                try {
                    myDialog.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    fun clearHistory() {
        if (mHistoryDatas != null)
            mHistoryDatas!!.clear()
        view?.setHistoryHeadersTitleView()
        view?.notifyHisData()
        Tools.saveHistoryWord(BaseBookApplication.getGlobalContext(), mHistoryDatas)
    }

    fun result(result: List<SearchCommonBeanYouHua>) {
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

    fun onSearchResult(suggestList: List<SearchCommonBeanYouHua>, transmitBean: SearchAutoCompleteBeanYouHua) {
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

    internal class SearchHandler(helper: SearchHelpPresenter) : Handler() {
        private val reference: WeakReference<SearchHelpPresenter>

        init {
            reference = WeakReference(helper)
        }

        override fun handleMessage(msg: Message) {
            val helper = reference.get() ?: return
            when (msg.what) {
                10 -> helper.clearHistory()

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

    fun onHotItemClick(context: Context?, parent: AdapterView<*>, view1: View, position: Int, id: Long) {
        StatServiceUtils.statAppBtnClick(context, StatServiceUtils.b_search_click_allhotword)
        val hotWord = hotWords!![position].word
        val data = HashMap<String, String>()
        data.put("topicword", hotWord!!)
        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.TOPIC, data)

        AppLog.e("wordType", hotWord + hotWords!![position].wordType + "")

        view?.hotItemClick(hotWord, hotWords!![position].wordType.toString() + "")
    }

    fun setSearchType(type: String) {
        searchType = type
    }

    fun getSearchType(): String? {
        return searchType
    }

    fun getSuggestData(): MutableList<SearchCommonBeanYouHua>? {
        return mSuggestList
    }

    fun getCurrSuggestData(): String? {
        return suggest
    }

    fun onSuggestItemClick(context: Context?, text: String, arg2: Int) {
        if(mSuggestList == null || arg2 >= mSuggestList!!.size){
            return
        }
        suggest = mSuggestList!![arg2].suggest
        searchType = "0"
        if (mSuggestList!![arg2].wordtype == "label") {
            searchType = "1"
        } else if (mSuggestList!![arg2].wordtype == "authorType") {
            searchType = "2"
        } else if (mSuggestList!![arg2].wordtype == "name") {
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

            //                    mSearchEditText.setSelection(suggest.length());
            startSearch(suggest, searchType ?: "")
        }

    }


}