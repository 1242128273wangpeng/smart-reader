package com.intelligent.reader.presenter.search

import android.content.Context
import android.os.Handler
import android.preference.PreferenceManager
import android.text.TextUtils
import android.view.View
import android.widget.*
import com.google.gson.Gson
import com.intelligent.reader.presenter.IPresenter
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.search.SearchAutoCompleteBean
import net.lzbook.kit.data.search.SearchCommonBean
import net.lzbook.kit.data.search.SearchHotBean
import net.lzbook.kit.net.custom.service.NetService
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.*
import java.util.ArrayList
import java.util.HashMap

/**
 * Created by yuchao on 2017/12/1 0001.
 */
class SearchViewPresenter(override var view: SearchView.View?) : IPresenter<SearchView.View> {
    private var mSuggestList: MutableList<SearchCommonBean>? = ArrayList()
    private var hotWords: MutableList<SearchHotBean.DataBean>? = ArrayList()
    private var suggest: String? = null
    private var searchType: String? = null
    private var sharedPreferencesUtils: SharedPreferencesUtils? = null
    private var gson: Gson? = null
    private var authorsBean: MutableList<SearchAutoCompleteBean.DataBean.AuthorsBean> = ArrayList()
    private var labelBean: MutableList<SearchAutoCompleteBean.DataBean.LabelBean> = ArrayList()
    private var bookNameBean: MutableList<SearchAutoCompleteBean.DataBean.NameBean> = ArrayList()
    private var hisDatas: ArrayList<String>? = ArrayList()
    private var hotDatas: ArrayList<String>? = ArrayList()

    init {
        gson = Gson()
        sharedPreferencesUtils = SharedPreferencesUtils(PreferenceManager.getDefaultSharedPreferences(BaseBookApplication.getGlobalContext()))
    }

    fun initHistoryData(context: Context?) {
        if (hisDatas != null && context != null)
            hisDatas!!.clear()
        val historyWord = Tools.getHistoryWord(context)
        if (historyWord != null) {
            hisDatas!!.addAll(historyWord)
        }
    }

    fun setHistoryData(context: Context?) {
        hisDatas = Tools.getHistoryWord(context)
    }

    fun getHistoryData(): ArrayList<String>? {
        return hisDatas
    }

    fun onHistoryItemClick(context: Context?, arg0: AdapterView<*>, arg1: View, arg2: Int, position: Long) {

        StatServiceUtils.statAppBtnClick(context, StatServiceUtils.b_search_click_his_word)
        if (hisDatas != null && !hisDatas!!.isEmpty() && position > -1 &&
                position < hisDatas!!.size) {
            val history = hisDatas!![position.toInt()]
            if (history != null) {
                view?.setEditText(history)
                startSearch(history, "0")

                val data = HashMap<String, String>()
                data.put("keyword", history)
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.HISTORY, data)
            }
        }
    }

    private fun startSearch(searchWord: String?, searchType: String?) {
        if (searchWord != null && searchWord != "") {
            addHistoryWord(searchWord)
            view?.onStartSearch(searchWord, searchType)
        }
    }

    fun addHistoryWord(keyword: String?) {
        if (hisDatas == null) {
            hisDatas = ArrayList<String>()
        }

        if (keyword == null || keyword == "") {
            return
        }
        if (hisDatas!!.contains(keyword)) {
            hisDatas!!.remove(keyword)
        }

        if (!hisDatas!!.contains(keyword)) {
            val size = hisDatas!!.size
            if (size >= 5) {
                hisDatas!!.removeAt(size - 1)
            }
            hisDatas!!.add(0, keyword)
            Tools.saveHistoryWord(BaseBookApplication.getGlobalContext(), hisDatas)
        }

        view?.notifyHisData()
    }

    fun resetHotWordList(context: Context) {

        if (NetWorkUtils.getNetWorkTypeNew(context) == "无") {
            getCacheDataFromShare(false)
        } else {
            AppLog.e("url", UrlUtils.getBookNovelDeployHost() + "===" + NetWorkUtils.getNetWorkTypeNew(context))
            val searchService = NetService.userService
            searchService.hotWord
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<SearchHotBean> {
                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onNext(value: SearchHotBean) {
                            AppLog.e("result", value.toString())
                            parseResult(value, true)
                        }

                        override fun onError(e: Throwable) {
                            getCacheDataFromShare(true)
                            AppLog.e("error", e.toString())
                        }

                        override fun onComplete() {
                            AppLog.e("complete", "complete")
                        }
                    })

            AppLog.e("url", UrlUtils.getBookNovelDeployHost() + "===" + NetWorkUtils.getNetWorkTypeNew(context))
        }
    }

    /**
     * if hasn't net getData from sharepreferenecs cache
     */
    fun getCacheDataFromShare(hasNet: Boolean) {
        if (!TextUtils.isEmpty(sharedPreferencesUtils!!.getString(Constants.SERARCH_HOT_WORD))) {
            view?.showLinearParent(true)
            hotWords!!.clear()
            val cacheHotWords = sharedPreferencesUtils!!.getString(Constants.SERARCH_HOT_WORD)
            val searchHotBean = gson!!.fromJson(cacheHotWords, SearchHotBean::class.java)
            parseResult(searchHotBean, false)
            AppLog.e("urlbean", cacheHotWords)
        } else {
            if (!hasNet) {
                ToastUtils.showToastNoRepeat("网络不给力哦")
            }
            view?.showLinearParent(false)
        }
    }

    /**
     * parse result data
     */
    fun parseResult(value: SearchHotBean?, hasNet: Boolean) {
        hotWords!!.clear()
        if (value != null && value.data != null) {
            hotWords = value.data
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
    }

    fun onSearchResult(suggestList: List<SearchCommonBean>, transmitBean: SearchAutoCompleteBean) {
        if (mSuggestList == null) {
            return
        }
        mSuggestList!!.clear()
        authorsBean.clear()
        labelBean.clear()
        bookNameBean.clear()
        if (transmitBean.data != null) {
            if (transmitBean.data.authors != null) {
                authorsBean = transmitBean.data.authors
            }
            if (transmitBean.data.label != null) {
                labelBean = transmitBean.data.label
            }
            if (transmitBean.data.name != null) {
                bookNameBean = transmitBean.data.name
            }
        }
        for (item in suggestList) {
            mSuggestList!!.add(item)
        }
        Handler().post {
            view?.onSuggestBack()
        }

    }

    fun onDestroy() {

        if (hisDatas != null) {
            hisDatas!!.clear()
            hisDatas = null
        }

        if (mSuggestList != null) {
            mSuggestList!!.clear()
            mSuggestList = null
        }

        if (hotDatas != null) {
            hotDatas!!.clear()
            hotDatas = null
        }
    }

    fun onHotItemClick(context: Context?, parent: AdapterView<*>, view1: View, arg2: Int, position: Long) {
        StatServiceUtils.statAppBtnClick(context, StatServiceUtils.b_search_click_his_word)
        if (hotWords != null && !hotWords!!.isEmpty() && position > -1 && position < hotWords!!.size) {
            val hotWord = hotWords!![arg2 - 1].word
            if (hotWord != null) {
                view?.hotItemClick(hotWord, hotWords!![position.toInt()].wordType.toString() + "")
                startSearch(hotWord, hotWords!![arg2 - 1].wordType.toString() + "")

                val data = HashMap<String, String>()
                data.put("topicword", hotWord)
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.TOPIC, data)
            }
        }

    }


    fun getSearchType(): String? {
        return searchType
    }

    fun getSuggestData(): MutableList<SearchCommonBean>? {
        return mSuggestList
    }

    fun getCurrSuggestData(): String? {
        return suggest
    }

    fun onSuggestItemClick(context: Context?, text: String, arg2: Int) {
        suggest = mSuggestList!![arg2].suggest
        searchType = "0"
        if (mSuggestList!![arg2].wordtype == "label") {
            searchType = "1"
        } else if (mSuggestList!![arg2].wordtype == "author") {
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

    fun removeHis(item: String) {
        if (hisDatas != null && hisDatas!!.contains(item))
            hisDatas!!.remove(item)
    }

    fun saveHis() {
        Tools.saveHistoryWord(BaseBookApplication.getGlobalContext(), hisDatas)
    }

}