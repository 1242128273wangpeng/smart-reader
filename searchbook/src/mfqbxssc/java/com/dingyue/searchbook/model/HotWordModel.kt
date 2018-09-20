package com.dingyue.searchbook.model

import com.ding.basic.bean.Result
import com.ding.basic.bean.SearchRecommendBook
import com.ding.basic.bean.SearchResult
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.RequestSubscriber
import com.ding.basic.util.editShared
import com.ding.basic.util.getSharedString
import com.dingyue.searchbook.IResultListener
import com.google.gson.Gson
import net.lzbook.kit.app.BaseBookApplication
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
    fun loadHotWordData(listener: IResultListener<SearchResult>) {

        RequestRepositoryFactory.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).requestSearchOperationV4(

                object : RequestSubscriber<Result<SearchResult>>() {
                    override fun requestResult(result: Result<SearchResult>?) {
                        if (result != null && result.data != null) {
                            val data = result.data
                            BaseBookApplication.getGlobalContext().editShared {
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
    fun loadRecommendData(listener: IResultListener<ArrayList<SearchRecommendBook.DataBean>>) {

        RequestRepositoryFactory.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).requestSearchRecommend(
                obtainBookOnLineIds(), object : RequestSubscriber<SearchRecommendBook>() {

            override fun requestResult(result: SearchRecommendBook?) {
                if (result?.data != null) {

                    val data = result.data
                    /*searchHotTitleLayout.relative_hot.setVisibility(View.VISIBLE)
                   if (data.size > 8) {
                       searchHotTitleLayout.relative_hot1.setVisibility(View.VISIBLE)
                   }*/
                    listener.onSuccess(data!!)
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
    private fun loadCacheDataFromShare(listener: IResultListener<SearchResult>) {
        val cacheHotWords = BaseBookApplication.getGlobalContext().getSharedString(Constants.SERARCH_HOT_WORD_YOUHUA)

        if (cacheHotWords.isNotEmpty()) {
            val searchResult = mGson.fromJson(cacheHotWords, SearchResult::class.java)

            if (searchResult != null) {
                listener.onSuccess(searchResult)
            }
        }
    }

}