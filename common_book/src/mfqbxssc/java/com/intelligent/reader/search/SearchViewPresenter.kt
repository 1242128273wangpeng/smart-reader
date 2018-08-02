package com.intelligent.reader.presenter.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.text.TextUtils
import android.view.View
import android.widget.*
import com.google.gson.Gson
import com.intelligent.reader.activity.CoverPageActivity
import com.intelligent.reader.presenter.IPresenter
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.RequestItem
import net.lzbook.kit.data.search.*
import net.lzbook.kit.net.Result
import net.lzbook.kit.net.custom.service.NetService
import net.lzbook.kit.utils.*
import java.util.ArrayList
import java.util.HashMap

/**
 * Created by yuchao on 2017/12/1 0001.
 */
class SearchViewPresenter(override var view: SearchSCView.View?) : IPresenter<SearchSCView.View> {
    private var mSuggestList: MutableList<SearchSuggest>? = ArrayList()
    private var hotWords: MutableList<HotWordBean>? = ArrayList()
    private var suggest: String? = null
    private var searchType: String? = null
    private var sharedPreferencesUtils: SharedPreferencesUtils? = null
    private var gson: Gson? = null
    private var authorsBean: MutableList<SearchAutoCompleteBeanYouHua.DataBean.AuthorsBean> = ArrayList()
    private var labelBean: MutableList<SearchAutoCompleteBeanYouHua.DataBean.LabelBean> = ArrayList()
    private var bookNameBean: MutableList<SearchAutoCompleteBeanYouHua.DataBean.NameBean> = ArrayList()
    private var hisDatas: ArrayList<String>? = ArrayList()
    private var hotDatas: ArrayList<String>? = ArrayList()
    private var isAuthor = 0
    private var searchCommonBean: SearchCommonBeanYouHua? = null

    //从标签和作者的webView页面返回是否保留焦点
    var isFocus = true
    //运营模块返回标识
    var isBackSearch = false

    init {
        gson = Gson()
        sharedPreferencesUtils = SharedPreferencesUtils(PreferenceManager.getDefaultSharedPreferences(BaseBookApplication.getGlobalContext()))
    }

    fun initHistoryData(context: Context?) {
        if (hisDatas != null ){
            hisDatas!!.clear()
        }
        if( context != null ){
            val historyWord = Tools.getHistoryWord(context)
            if (historyWord != null && hisDatas != null) {
                hisDatas!!.addAll(historyWord)
            }
        }

    }

    fun setHistoryData(context: Context?) {
        hisDatas = Tools.getHistoryWord(context)
    }

    fun getHistoryData(): ArrayList<String>? {
        return hisDatas
    }

    fun onHistoryItemClick(context: Context?, arg0: AdapterView<*>?, arg1: View?, arg2: Int, position: Long) {

        if(context != null) {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.b_search_click_his_word)
            if (hisDatas != null && !hisDatas!!.isEmpty() && position > -1 &&
                    position < hisDatas!!.size) {
                val history = hisDatas!![position.toInt()]
                if (history != null) {
                    view?.setEditText(history)
                    startSearch(history, "0", 0)

                    val data = HashMap<String, String>()
                    data.put("keyword", history)
                    data.put("rank", (position + 1).toString())
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.BARLIST, data)
                }
            }
        }
    }

    fun startSearch(searchWord: String?, searchType: String?,isAuthor: Int) {
        if (searchWord != null && !TextUtils.isEmpty(searchWord)) {
            addHistoryWord(searchWord)
            view?.onStartSearch(searchWord, searchType,isAuthor)
        }
    }

    fun addHistoryWord(keyword: String?) {
        if (hisDatas == null) {
            hisDatas = ArrayList<String>()
        }

        if (keyword == null || TextUtils.isEmpty(keyword)) {
            return
        }
        if (hisDatas!!.contains(keyword)) {
            hisDatas!!.remove(keyword)
        }

        if (!hisDatas!!.contains(keyword)) {
            val size = hisDatas!!.size
            if (size >= 30) {
                hisDatas!!.removeAt(size - 1)
            }
            hisDatas!!.add(0, keyword)
            Tools.saveHistoryWord(BaseBookApplication.getGlobalContext(), hisDatas)
        }

        view?.notifyHisData()
    }

    fun resetHotWordList(context: Context?,relative_parent:RelativeLayout) {

        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            relative_parent.visibility =  View.GONE
        } else {
            view?.showLoading()
            val searchService = NetService.userService
            searchService.getHotWord_V4()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<Result<SearchResult>> {
                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onNext(value: Result<SearchResult>) {
                            AppLog.e("result", value.toString())
                            if (value != null && value.data != null && value.data.hotWords != null) {

                                val result = value.data
                                AppLog.d("SearchViewHelper", gson!!.toJson(result, SearchResult::class.java))

                                sharedPreferencesUtils!!.putString(Constants.SERARCH_HOT_WORD, gson!!.toJson(result, SearchResult::class.java))
                                parseResult(result)
                            } else {
                                getCacheDataFromShare(true)
                            }
                        }

                        override fun onError(e: Throwable) {
                            getCacheDataFromShare(true)
                            AppLog.e("error", e.toString())
                        }

                        override fun onComplete() {
                            AppLog.e("complete", "complete")
                        }
                    })

        }
    }

    /**
     * if hasn't net getData from sharepreferenecs cache
     */

    fun getCacheDataFromShare(hasNet: Boolean) {
        if (sharedPreferencesUtils != null && !TextUtils.isEmpty(sharedPreferencesUtils!!.getString(Constants.SERARCH_HOT_WORD))) {
            val cacheHotWords = sharedPreferencesUtils!!.getString(Constants.SERARCH_HOT_WORD)
            val searchResult = gson!!.fromJson(cacheHotWords, SearchResult::class.java)
            if (searchResult != null) {
                parseResult(searchResult)
            } else {
                view?.showLinearParent(false)
            }
            AppLog.e("urlbean", cacheHotWords)
        } else {
            if (!hasNet) {
                ToastUtils.showToastNoRepeat("网络不给力哦")
            }
            view?.showLinearParent(false)
        }
        view?.dimissLoading()
    }


    /**
     * 返回isFocus 和 isBackSearch 的值，以此来确定searchBookActivity页面显示的模块
     * @return
     */
    fun getShowStatus(): Boolean {
        if (!isBackSearch && isFocus) {
            return true
        } else {
            return false
        }
    }

    /**
     * parse result data
     */
    fun parseResult(value: SearchResult?) {
        hotWords!!.clear()
        view?.dimissLoading()
        if (value != null && value.hotWords != null) {
            hotWords = value.hotWords
            if (hotWords != null && hotWords!!.size >= 0) {
                view?.showLinearParent(true)

                view?.setHotWordAdapter(hotWords)
            } else {
                view?.showLinearParent(false)
            }
        } else {
            view?.showLinearParent(false)
        }
    }



    fun onSearchResult(suggestList: List<SearchSuggest>, transmitBean: SearchAutoCompleteBeanYouHua) {
        if (mSuggestList == null) {
            return
        }
        mSuggestList!!.clear()
        authorsBean.clear()
        labelBean.clear()
        bookNameBean.clear()
        if (transmitBean != null && transmitBean.data != null) {
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
            val hotWord = hotWords!!.get(arg2 - 1)
            if (hotWord != null && hotWord.keyword != null ) {
                view?.hotItemClick(hotWord.keyword, hotWords!!.get(arg2 - 1).keywordType.toString() + "")

                isFocus = false
                startSearch(hotWord.keyword, hotWords!!.get(arg2 - 1).keywordType.toString() + "", 0)
                val data = HashMap<String, String>()
                data.put("topicword", hotWord.keyword)
                data.put("rank", hotWord.sort.toString())
                data.put("type", hotWord.superscript)
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.TOPIC, data)
            }
        }

    }


    fun getSearchType(): String? {
        return searchType
    }

    fun getSuggestData(): MutableList<SearchSuggest>? {
        return mSuggestList
    }

    fun getCurrSuggestData(): String? {
        return suggest
    }

    fun onSuggestItemClick(context: Context?, text: String, arg2: Int) {

        if(mSuggestList != null && arg2 < mSuggestList!!.size){
            searchCommonBean = mSuggestList!!.get(arg2).commonBean

            suggest = searchCommonBean?.suggest
            searchType = "0"
            isAuthor = 0
            val data = HashMap<String, String>()
            if (searchCommonBean?.wordtype == "label") {
                searchType = "1"
                isAuthor = 0
                isFocus = false
            } else if (searchCommonBean?.wordtype == "author") {
                searchType = "2"
                if(searchCommonBean != null){
                    isAuthor = searchCommonBean!!.getIsAuthor()
                }
                isBackSearch = false
                isFocus = true
            } else if (searchCommonBean?.wordtype == "name") {
                searchType = "3"
                isFocus = true
                isBackSearch = false
                isAuthor = 0
                data.put("bookid", searchCommonBean?.book_id+"")

                //统计进入到书籍封面页
                val data1 = HashMap<String, String>()
                data1.put("BOOKID", searchCommonBean?.book_id+"")
                data1.put("source", "SEARCH")
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.ENTER, data1)


                val requestItem = RequestItem()
                requestItem.book_id = searchCommonBean?.getBook_id()
                requestItem.book_source_id = searchCommonBean?.getBook_source_id()
                requestItem.host = searchCommonBean?.getHost()
                requestItem.name = searchCommonBean?.getName()
                requestItem.author = searchCommonBean?.getAuthor()
                requestItem.parameter = searchCommonBean?.getParameter()
                requestItem.extra_parameter = searchCommonBean?.getExtra_parameter()

                val intent = Intent()
                intent.setClass(context, CoverPageActivity::class.java)
                val bundle = Bundle()
                bundle.putSerializable(Constants.REQUEST_ITEM, requestItem)
                intent.putExtras(bundle)
                context?.startActivity(intent)
                addHistoryWord(suggest)
            } else {
                searchType = "0"
                isAuthor = 0
            }
            var dividerCount = 0;

            if (!TextUtils.isEmpty(suggest)) {
                data.put("keyword", suggest+"")
                data.put("enterword", text)
                data.put("type", searchType ?: "")
                val packageName = AppUtils.getPackageName()
                if(packageName.equals("cc.kdqbxs.reader") || packageName.equals("cc.mianfeinovel") ||
                        packageName.equals("cc.lianzainovel") || packageName.equals("cc.quanben.novel")){ //上新版搜索二期时用
                    if(arg2 == 0){
                        data.put("rank", (arg2 + 1).toString())
                    }else if(arg2 > 0 ){
                        for(i in 1 .. arg2){
                            if(mSuggestList!!.get(i).type == 2 || mSuggestList!!.get(i).type == 1){
                                dividerCount++;
                            }
                        }
                        data.put("rank", (arg2 - dividerCount +1).toString())
                    }

                }else{
                    if (arg2 + 1 > 0 && arg2 + 1 <= 2) {
                        data.put("rank", (arg2 + 1).toString() + "")
                    } else if (arg2 + 1 > 3 && arg2 + 1 <= 5) {
                        data.put("rank", arg2.toString() + "")
                    } else if (arg2 + 1 > 6 && arg2 + 1 <= 8) {
                        data.put("rank", (arg2 - 1).toString() + "")
                    } else if (arg2 + 1 > 9) {
                        data.put("rank", (arg2 - 2).toString() + "")
                    }
                }

                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.TIPLISTCLICK, data)

                if (!searchType .equals("3")) {
                    startSearch(suggest, searchType, isAuthor)
                }
            }
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