package com.dingyue.searchbook.model

import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.SearchAutoCompleteBeanYouHua
import com.ding.basic.bean.SearchCommonBeanYouHua
import com.ding.basic.net.RequestSubscriber
import com.dingyue.searchbook.interfaces.OnResultListener
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.utils.logger.AppLog


/**
 * Desc：自动补全逻辑处理
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/9/25 0025 16:33
 */
class SuggestModel {

    fun loadSuggestData(finalQuery: String, listener: OnResultListener<ArrayList<Any>>) {

        RequestRepositoryFactory.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).requestAutoCompleteV5(
                finalQuery, object : RequestSubscriber<SearchAutoCompleteBeanYouHua>() {
            override fun requestResult(result: SearchAutoCompleteBeanYouHua?) {
                if (result != null
                        && SearchAutoCompleteBeanYouHua.REQUESR_SUCCESS == result.respCode
                        && result.data != null) {

                    listener.onSuccess(packageData(result))
                }
            }

            override fun requestError(message: String) {
                AppLog.e("请求自动补全失败！")
            }
        })

    }

    /**
     * 根据策略，显示数据的顺序为 两个书名 + 一个间隔 + 两个作者 + 一个间隔 + 两个标签 + 一个间隔 + 剩余书名
     */
    fun packageData(bean: SearchAutoCompleteBeanYouHua?): ArrayList<Any> {
        val resultSuggest = ArrayList<Any>()
//        val view = LayoutInflater.from(mContext).inflate(R.layout.item_suggest_title, null,
//                false)
        val newBean = SearchCommonBeanYouHua()
        newBean.viewType = 1
        //两个书名
        if (bean!!.data!!.name != null && bean.data!!.name!!.size > 0) {
            resultSuggest.add(newBean)

            for (i in 0 until if (bean.data!!.name!!.size >= 2)
                2
            else
                bean.data!!.name!!.size) {
                val nameBean = bean.data!!.name!![i]
                if (nameBean != null) {
                    val searchCommonBean = SearchCommonBeanYouHua()
                    searchCommonBean.suggest = nameBean.suggest
                    searchCommonBean.wordtype = nameBean.wordtype
                    searchCommonBean.image_url = nameBean.imgUrl

                    //-----------------书名特有字段-------------------------------
                    searchCommonBean.host = nameBean.host
                    searchCommonBean.book_id = nameBean.bookid
                    searchCommonBean.book_source_id = nameBean.bookSourceId
                    searchCommonBean.name = nameBean.bookName
                    searchCommonBean.author = nameBean.author
                    searchCommonBean.parameter = nameBean.parameter
                    searchCommonBean.extra_parameter = nameBean.extraParameter
                    searchCommonBean.bookType = nameBean.vip.toString() + ""
                    //------------------------------------------------------------

                    resultSuggest.add(searchCommonBean)
                }
            }
            resultSuggest.add(newBean)
        }

        if (bean.data!!.authors != null && bean.data!!.authors!!.size > 0) {

            //两个作者
            for (i in 0 until if (bean.data!!.authors!!.size >= 2)
                2
            else
                bean.data!!.authors!!.size) {
                val authorsBean = bean.data!!.authors!![i]
                if (authorsBean != null) {
                    val searchCommonBean = SearchCommonBeanYouHua()
                    searchCommonBean.suggest = bean.data!!.authors!![i].suggest
                    searchCommonBean.wordtype = bean.data!!.authors!![i].wordtype
                    searchCommonBean.image_url = ""
                    searchCommonBean.isAuthor = bean.data!!.authors!![i].isAuthor
                    resultSuggest.add(searchCommonBean)

                }
            }
            resultSuggest.add(newBean)
        }

        if (bean.data!!.label != null && bean.data!!.label!!.size > 0) {

            //两个标签
            for (i in 0 until if (bean.data!!.label!!.size >= 2)
                2
            else
                bean.data!!.label!!.size) {
                val labelBean = bean.data!!.label!![i]
                if (labelBean != null) {
                    val searchCommonBean = SearchCommonBeanYouHua()
                    searchCommonBean.suggest = bean.data!!.label!![i].suggest
                    searchCommonBean.wordtype = bean.data!!.label!![i].wordtype
                    searchCommonBean.image_url = ""
                    resultSuggest.add(searchCommonBean)

                }
            }
            resultSuggest.add(newBean)
        } else {
            if (bean.data!!.authors == null || bean.data!!.authors != null && bean.data!!.authors!!.size == 0) {
                resultSuggest.remove(newBean)
            }
        }

        //其余书名
        if (bean.data!!.name != null) {
            for (i in 2 until bean.data!!.name!!.size) {
                val searchCommonBean = SearchCommonBeanYouHua()
                val nameBean = bean.data!!.name!![i]
                if (nameBean != null) {
                    searchCommonBean.suggest = nameBean.suggest
                    searchCommonBean.wordtype = nameBean.wordtype
                    searchCommonBean.image_url = nameBean.imgUrl

                    //-----------------书名特有字段-------------------------------
                    searchCommonBean.host = nameBean.host
                    searchCommonBean.book_id = nameBean.bookid
                    searchCommonBean.book_source_id = nameBean.bookSourceId
                    searchCommonBean.name = nameBean.bookName
                    searchCommonBean.author = nameBean.author
                    searchCommonBean.parameter = nameBean.parameter
                    searchCommonBean.extra_parameter = nameBean.extraParameter
                    searchCommonBean.bookType = nameBean.vip.toString() + ""
                    //------------------------------------------------------------

                    resultSuggest.add(searchCommonBean)
                }
            }
        }

        for (bean1 in resultSuggest) {
            AppLog.e("uuu", bean1.toString())
        }
//        if (searchSuggestCallBack != null) {
//            searchSuggestCallBack.onSearchResult(resultSuggest, bean)
//        }
        return resultSuggest
    }

}