package com.dingyue.searchbook.model


import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.Result
import com.ding.basic.bean.SearchRecommendBook
import com.ding.basic.bean.SearchResult
import com.ding.basic.net.RequestSubscriber
import com.ding.basic.util.sp.SPUtils
import com.dingyue.searchbook.interfaces.OnResultListener
import com.google.gson.Gson
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.constants.Constants


/**
 * Desc 热词和推荐业务逻辑处理
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019 22:39
 */
class HotWordModel {

    val mGson = Gson()

    /**
     * 加载热词
     */
    fun loadHotWordData(listener: OnResultListener<SearchResult>) {

        RequestRepositoryFactory.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).requestSearchOperationV4(

                object : RequestSubscriber<Result<SearchResult>>() {
                    override fun requestResult(result: Result<SearchResult>?) {
                        if (result != null && result.data != null) {
                            val data = result.data
                            SPUtils.editDefaultShared {
                                putString(Constants.SERARCH_HOT_WORD_YOUHUA, mGson.toJson(data, SearchResult::class.java))
                            }

                            listener.onSuccess(data)

                        } else {
                            loadCacheDataFromShare(listener)
                        }

                    }

                    override fun requestError(message: String) {
                        loadCacheDataFromShare(listener)
                    }

                })
    }


    /**
     * 加载推荐书籍
     */
    fun loadRecommendData(listener: OnResultListener<ArrayList<SearchRecommendBook.DataBean>>) {

        RequestRepositoryFactory.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).requestSearchRecommend(
                obtainBookOnLineIds(), object : RequestSubscriber<SearchRecommendBook>() {

            override fun requestResult(result: SearchRecommendBook?) {
                if (result?.data != null) {

                    val data = result.data

                    listener.onSuccess(data as ArrayList<SearchRecommendBook.DataBean>)
                }

            }

            override fun requestError(message: String) {
            }
        })
    }


    /**
     * 获取书架上的书Id
     */
    private fun obtainBookOnLineIds(): String {

        val books = RequestRepositoryFactory.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).loadBooks()

        val sb = StringBuilder()
        books?.let {
            if (it.isNotEmpty()) {
                for (i in it.indices) {
                    val book = it[i]
                    sb.append(book.book_id)
                    sb.append(if (i == it.size - 1) "" else ",")
                }
                return sb.toString()
            }
        }

        return ""
    }

    /**
     * if hasn't net getHotWord from SharePreference cache
     */
    private fun loadCacheDataFromShare(listener: OnResultListener<SearchResult>) {
        val cacheHotWords = SPUtils.getDefaultSharedString(Constants.SERARCH_HOT_WORD_YOUHUA)

        if (cacheHotWords.isNotEmpty()) {
            val searchResult = mGson.fromJson(cacheHotWords, SearchResult::class.java)

            if (searchResult != null) {
                listener.onSuccess(searchResult)
            }
        }
    }

}