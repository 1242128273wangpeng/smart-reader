package net.lzbook.kit.utils

import android.text.TextUtils
import net.lzbook.kit.bean.CrawlerResult
import net.lzbook.kit.utils.logger.AppLog
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.net.URLEncoder
import java.util.concurrent.Executors


/**
 * 百度数据抓取工具
 * Author yangweining
 * Mail weining_yang@dingyuegroup.cn
 * Date 2018/10/30 15:04
 */
object BDCrawler {
    val TAG = "BDCrawler:"
    val USERAGENT = "Mozilla/5.0 (Linux; U; Android 2.3.7; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1"
    val TIMEOUT = 10 * 1000
    //           var path="https://m.baidu.com/s?word={0}&ref=www_iphone&ssid=0&from=0&bd_page_type=1&uid=0&pu=usm%403%2Csz%40320_1001%2Cta%40iphone_2_5.0_3_537";
    var path = "https://m.baidu.com/s?word={0}"
    var cachedThreadPool = Executors.newCachedThreadPool()
    var callbackList = mutableListOf<CrawlerTask?>()

    /**
     * 开始抓取
     */
    fun startCrawler(keyWord: String, crawlerCallback: CrawlerCallback) {
        var crawlerTask = CrawlerTask(keyWord, crawlerCallback)
        callbackList.add(crawlerTask)
        cachedThreadPool.submit(crawlerTask)
    }

    /**
     * 取消抓取
     */
    fun cancelCrawler() {
        callbackList.forEach({
            it?.crawlerCallback = null
        })
        callbackList.clear()
    }

    interface CrawlerCallback {
        fun onSuccess(resultList: MutableList<CrawlerResult>)
        fun onFail()
    }

    /**
     * 加载网页任务
     */
    class CrawlerTask(var keyWord: String, var crawlerCallback: CrawlerCallback?) : Runnable {

        override fun run() {
            keyWord += "免费阅读"
            val sword = URLEncoder.encode(keyWord, "utf-8")
            var path = path.replace("{0}", sword)
            AppLog.i(TAG + "keyWord：" + keyWord + ",path：$path")
            var resultList = mutableListOf<CrawlerResult>()
            try {
                val doc = Jsoup.connect(path)
                        .userAgent(USERAGENT)
                        .timeout(TIMEOUT)
                        .get()
                if (crawlerCallback == null) {
                    return
                }
                parsePage(doc, resultList)
                AppLog.i(TAG + "pageSize:" + resultList.size)
                if (resultList.size > 0) {
                    runOnMain {
                        crawlerCallback?.onSuccess(resultList)
                    }
                } else {
                    runOnMain {
                        crawlerCallback?.onFail()
                    }
                    return
                }
                if (crawlerCallback == null) {
                    return
                }
                try {
                    val elements1 = doc.getElementsByClass("new-nextpage-only")
                    if (elements1 != null && elements1.size > 0) {
                        val href = elements1[0].attr("href")
                        AppLog.i(TAG + "page1 path：$href")
                        if (!TextUtils.isEmpty(href)) {
                            resultList = mutableListOf()
                            val doc1 = Jsoup.connect(href)
                                    .userAgent(USERAGENT)
                                    .timeout(TIMEOUT)
                                    .get()
                            if (crawlerCallback == null) {
                                return
                            }
                            parsePage(doc1, resultList)
                            AppLog.i(TAG + "pageSize:" + resultList.size)
                            if (resultList.size > 0) {
                                runOnMain {
                                    crawlerCallback?.onSuccess(resultList)
                                }
                            }
                            if (crawlerCallback == null) {
                                return
                            }
                            try {
                                val elements2 = doc1.getElementsByClass("new-nextpage")
                                if (elements2 != null && elements2.size > 0) {
                                    val href2 = elements2[0].attr("href")
                                    AppLog.i(TAG + "page2 path：$href2")
                                    if (!TextUtils.isEmpty(href2)) {
                                        resultList = mutableListOf()
                                        val doc2 = Jsoup.connect(href2)
                                                .userAgent(USERAGENT)
                                                .timeout(TIMEOUT)
                                                .get()
                                        if (crawlerCallback == null) {
                                            return
                                        }
                                        parsePage(doc2, resultList)
                                        AppLog.i(TAG + "pageSize:" + resultList.size)
                                        if (resultList.size > 0) {
                                            runOnMain {
                                                crawlerCallback?.onSuccess(resultList)
                                            }
                                        }
                                    }
                                }
                            } catch (e: Throwable) {
                                e.printStackTrace()
                            }
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                runOnMain {
                    crawlerCallback?.onFail()
                }
            }
        }
    }

    /**
     * 解析网页
     */
    private fun parsePage(doc: Document, resultList: MutableList<CrawlerResult>) {
//        val elements = doc.getElementsByClass("c-result result")
        val elements = doc.select(".c-result").select(".result")
        for (i in elements.indices) {
            var result: CrawlerResult
            val srcid = elements[i].attr("srcid")
            if ("nvl_normal".equals(srcid) || "nvl_site".equals(srcid) || "nvl_trans".equals(srcid)) {
                continue
            } else if ("nvl_flow".equals(srcid)) {
                result = CrawlerResult()
                parseFlowItem(elements, i, result)
                saveResult(result, resultList)
            } else if ("nvl_zbook".equals(srcid)) {
                result = CrawlerResult()
                parseZbookItem(elements, i, result)
                saveResult(result, resultList)
            } else {
                result = CrawlerResult()
                parseNormalItem(elements, i, result)
                saveResult(result, resultList)
            }
            AppLog.i(TAG + result)
        }
    }

    /**
     * 存储解析结果
     */
    fun saveResult(result: CrawlerResult, resultList: MutableList<CrawlerResult>) {
        if (TextUtils.isEmpty(result.title) || TextUtils.isEmpty(result.abstract) || TextUtils.isEmpty(result.source)) {
            AppLog.i(TAG + "抓取的数据缺失，丢掉...")
        } else {
            resultList.add(result)
        }
    }

    /**
     * 解析自然结果类型Item
     */
    fun parseNormalItem(elements: Elements, i: Int, result: CrawlerResult) {
        val json = elements[i].attr("data-log")
        try {
            if (!TextUtils.isEmpty(json)) {
                val jsonObject = JSONObject(json)
                result.url = jsonObject.optString("mu")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val titleE = elements[i].getElementsByClass("c-title-text")
        try {
            if (TextUtils.isEmpty(result.url)) {
                var els = titleE.parents()
                if (els.size > 1) {
                    result.url = els.get(1).attr("href")
                }
            }
        } catch (e: Throwable) {

        }
        if (titleE != null && titleE.size > 0) {
            result.title = titleE[0].html()
        }
        //                val authorE = elements[i].getElementsByClass("c-color-gray c-row")
        val authorE = elements[i].select(".c-color-gray").select(".c-row")
        try {
            if (authorE != null && authorE.size > 0) {
                val child = authorE[0].child(0)
                if (child != null) {
                    result.author = child.child(0).text()
                    try {
                        result.updateTime = child.child(1).text()
                    } catch (e: Exception) {

                    }

                }
            }
        } catch (e: Exception) {
        }

        val tipE = elements[i].getElementsByClass("c-line-clamp2")
        try {
            if (tipE != null && tipE.size > 0) {
                var span = tipE[0].getElementsByTag("span")
                result.abstract = span[0].html()
            }
        } catch (e: Throwable) {

        }
        try {
            if (TextUtils.isEmpty(result.abstract)) {
                val tipE1 = elements[i].getElementsByClass("c-line-clamp3")
                if (tipE1 != null && tipE1.size > 0) {
                    var span = tipE1[0].getElementsByTag("span")
                    result.abstract = span[0].html()
                }
            }
        } catch (e: Throwable) {

        }
        try {
            if (TextUtils.isEmpty(result.abstract)) {
                val tipE1 = elements[i].getElementsByClass("c-line-clamp4")
                if (tipE1 != null && tipE1.size > 0) {
                    var span = tipE1[0].getElementsByTag("span")
                    result.abstract = span[0].html()
                }
            }
        } catch (e: Exception) {

        }

        //                val newChartE = elements[i].getElementsByClass("c-slink c-slink-auto c-gap-top-small c-gap-bottom-small c-blocka")
        val newChartE = elements[i].select(".c-slink").select(".c-slink-auto").select(".c-gap-top-small").select(".c-gap-bottom-small").select(".c-blocka")
        if (newChartE != null && newChartE.size > 0) {
            result.newChapterUrl = newChartE[0].attr("href")
            val element = newChartE[0].child(0)
            if (element != null) {
                result.newChapter = element.text()
            }
        }
        //                val sourceE = elements[i].getElementsByClass("link c-blocka")
        val sourceE = elements[i].select(".link").select(".c-blocka")
        if (sourceE != null) {
            result.source = sourceE.text()
        }
        if (TextUtils.isEmpty(result.source)) {
            //                    val sourceE1 = elements[i].getElementsByClass("c-showurl c-footer-showurl")
            val sourceE1 = elements[i].select(".c-showurl").select(".c-footer-showurl")
            if (sourceE1 != null && sourceE1.size > 0) {
                result.source = sourceE1[0].text()
            }
        }
        if (TextUtils.isEmpty(result.source)) {
            val sourceE1 = elements[i].getElementsByClass("link")
            if (sourceE1 != null && sourceE1.size > 0) {
                result.source = sourceE1[0].text()
            }
        }
    }

    /**
     * 解析Zbook类型Item
     */
    fun parseZbookItem(elements: Elements, i: Int, result: CrawlerResult) {
        val json = elements[i].attr("data-log")
        try {
            if (!TextUtils.isEmpty(json)) {
                val jsonObject = JSONObject(json)
                result.url = jsonObject.optString("mu")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val titleE = elements[i].getElementsByClass("c-title-text")
        try {
            if (TextUtils.isEmpty(result.url)) {
                var els = titleE.parents()
                if (els.size > 1) {
                    result.url = els.get(1).attr("href")
                }
            }
        } catch (e: Throwable) {

        }
        if (titleE != null && titleE.size > 0) {
            result.title = titleE[0].html()
        }
        val authorE = elements[i].getElementsByClass("wa-nvl-zbook-info")
        try {
            if (authorE != null && authorE.size > 0) {
                val child = authorE[0].child(0).child(0)
                result.author = child.text()
            }
        } catch (e: Exception) {

        }

        try {
            if (authorE != null && authorE.size > 0) {
                result.abstract = authorE[0].child(1).html()
            }
        } catch (e: Exception) {

        }

        try {
            val sourceE = elements[i].getElementsByClass("c-tabs-nav-li-span")
            if (sourceE != null) {
                result.source = sourceE.text()
            }
        } catch (e: Exception) {

        }
        try {
            if (TextUtils.isEmpty(result.source)) {
                val sourceE = elements[i].select(".c-footer-no-showurl").select(".c-row")
                if (sourceE != null && sourceE.size > 0) {
                    result.source = sourceE[0].child(0).child(0).text()
                }
            }
        } catch (e: Exception) {

        }

        val newCharaterE = elements[i].select(".wa-nvl-zbook-chapter-link").select(".c-blocka")
        try {
            if (newCharaterE != null && newCharaterE.size > 0) {
                result.newChapterUrl = newCharaterE[0].attr("href")
                result.newChapter = newCharaterE[0].child(0).child(0).text()
            }
        } catch (e: Exception) {

        }
    }

    /**
     * 解析flow类型Item
     */
    fun parseFlowItem(elements: Elements, i: Int, result: CrawlerResult) {
        val json = elements[i].attr("data-log")
        try {
            if (!TextUtils.isEmpty(json)) {
                val jsonObject = JSONObject(json)
                result.url = jsonObject.optString("mu")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val titleE = elements[i].getElementsByClass("c-title-text")
        try {
            if (TextUtils.isEmpty(result.url)) {
                var els = titleE.parents()
                if (els.size > 1) {
                    result.url = els.get(1).attr("href")
                }
            }
        } catch (e: Throwable) {

        }
        if (titleE != null && titleE.size > 0) {
            result.title = titleE[0].html()
        }
        val authorE = elements[i].getElementsByClass("wa-nvl-common-excerpt-info")
        try {
            if (authorE != null && authorE.size > 0) {
                val child = authorE[0].child(0)
                result.author = child.childNode(0).toString()
            }
        } catch (e: Exception) {

        }

        try {
            if (authorE != null && authorE.size > 0) {
                result.abstract = authorE[0].child(1).child(0).child(0).html()
            }
        } catch (e: Exception) {

        }

        //                val sourceE = elements[i].getElementsByClass("link c-blocka")
        val sourceE = elements[i].select(".link").select(".c-blocka")
        if (sourceE != null) {
            result.source = sourceE.text()
        }
        if (TextUtils.isEmpty(result.source)) {
            //                    val sourceE1 = elements[i].getElementsByClass("c-showurl c-footer-showurl")
            val sourceE1 = elements[i].select(".c-showurl").select(".c-footer-showurl")
            if (sourceE1 != null && sourceE1.size > 0) {
                result.source = sourceE1[0].text()
            }
        }
        if (TextUtils.isEmpty(result.source)) {
            val sourceE1 = elements[i].getElementsByClass("link")
            if (sourceE1 != null && sourceE1.size > 0) {
                result.source = sourceE1[0].text()
            }
        }
        //                val newCharaterE = elements[i].getElementsByClass("wa-nvl-flow-chapter-link c-blocka")
        val newCharaterE = elements[i].select(".wa-nvl-flow-chapter-link").select(".c-blocka")
        try {
            if (newCharaterE != null && newCharaterE.size > 0) {
                result.newChapterUrl = newCharaterE[0].attr("href")
                result.newChapter = newCharaterE[0].child(0).child(0).text()
            }
        } catch (e: Exception) {

        }
    }
}