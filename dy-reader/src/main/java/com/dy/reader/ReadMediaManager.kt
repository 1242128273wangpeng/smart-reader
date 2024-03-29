package com.dy.reader

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.dy.media.MediaControl
import com.dy.reader.helper.AppHelper
import com.dy.reader.mode.NovelPageBean
import com.dy.reader.page.GLReaderView
import com.dy.reader.setting.ReaderSettings
import com.dy.reader.setting.ReaderStatus
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.uiThread
import org.json.JSONException
import java.io.Closeable
import java.lang.ref.WeakReference
import java.util.*

@SuppressLint("StaticFieldLeak")
/**
 * Desc 阅读广告管理类
 * Author wt
 * 1216174464@qq.com
 * Date 2018/4/28 16:33
 */
object ReadMediaManager {

    val readerSettings = ReaderSettings.instance
    var frameLayout: FrameLayout? = null
        set(value) {
            value?.visibility = View.INVISIBLE
            field = value
        }
    val adCache = DataCache()
    var mActivity: WeakReference<Activity>? = null
    //无缓存加载回调
    var loadAdComplete: ((String) -> Unit)? = null
    var tonken: Long = 1
    /**
     * 必须初始化!
     */
    fun init(act: Activity?) {
        if (act != null) {
            mActivity = WeakReference(act)
        }
    }

    /**
     * 插入所有的广告位
     * @param group 章节
     * @param page 分页内容
     * @param within 5-2 6-2 广告开关
     * @param between 5-1 广告开关
     * @param small 8-1 广告开关
     * @param frequency 5-2 广告频率
     * @return 分页内容
     */
    fun insertChapterAd(group: Int, token: Long,
                        page: ArrayList<NovelPageBean>,
                        within: Boolean = MediaControl.getAdSwitch("5-2") and MediaControl.getAdSwitch("6-2"),
                        between: Boolean = MediaControl.getAdSwitch("5-1") and MediaControl.getAdSwitch("6-1"),
                        vertical_between: Boolean = MediaControl.getAdSwitch("5-3") and MediaControl.getAdSwitch("6-3"),
                        small: Boolean = MediaControl.getAdSwitch("8-1"),
                        frequency: Int = MediaControl.getChapterFrequency()
    ): ArrayList<NovelPageBean> {
        removeOldAd(group)
        if (Constants.isHideAD || NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE || page.size < 3 || AppUtils.isNeedAdControl(Constants.ad_control_reader)) return page


        //check 5-2 or 6-2 adView
        if (readerSettings.animation != GLReaderView.AnimationType.LIST && page.size - 8 > frequency && within) {
            var count = page.size - 8
            var index = frequency
            while (index < count) {
                if (index % frequency == 0) {
                    val novelPageBean = generateNovelPageBean(group, index, page[index - 1].offset)
                    page.add(index, novelPageBean)
                    requestAd(novelPageBean.adType, generateAdMark(1, 10), AppHelper.screenHeight, AppHelper.screenWidth, token)
                    count++
                    index++
                }
                index++
            }
        }
        val last = page.last()
        val contentHeight = if (last.lines.isNotEmpty()) last.height.toInt() else 0
        if (small) {
            if (readerSettings.animation != GLReaderView.AnimationType.LIST) {//check 8-1 adView
                val leftSpace = AppHelper.screenHeight - contentHeight - (AppHelper.screenDensity.times(15)).toInt()
                if (leftSpace >= 200) {
                    last.adType = generateAdType(group, page.size - 1)
                    requestAd(last.adType, generateAdMark(8, 10), (AppHelper.screenHeight - last.height).toInt(), AppHelper.screenWidth, token)
                }
            } else if (!readerSettings.isLandscape) {//6-3 adView
                mActivity?.get()?.apply {
                    last.adType = generateAdType(group, page.size - 1)
//            requestAd(last.adType, generateAdMark(8, 10), (AppHelper.screenHeight - last.height).toInt())
                    val adMark = generateAdMark(8, 10)
                    MediaControl.dycmNativeAd(this, adMark, null, { view ->
                        this@ReadMediaManager.mediaAction(last.adType, adMark,
                                (AppHelper.screenHeight - last.height).toInt(), tonken, view)
                    },{errorCode ->

                    })
                }
            }
        }

        //check 5-1 or 6-1 adView
        if (between && readerSettings.animation != GLReaderView.AnimationType.LIST) {
            val novelPageBean = generateNovelPageBean(group, page.size, last.offset)
            page.add(novelPageBean)
            requestAd(novelPageBean.adType, generateAdMark(9, 10), AppHelper.screenHeight, AppHelper.screenWidth, token)
        } else if (vertical_between && readerSettings.animation == GLReaderView.AnimationType.LIST) {//check 5-3 or 6-3 adView
            val novelPageBean = generateNovelPageBean(group, page.size, last.offset)
            page.add(novelPageBean)
            requestAd(novelPageBean.adType, generateAdMark(9, 10), AppHelper.screenHeight, AppHelper.screenWidth, token)
        }
        return page
    }

    /**
     * 分页请求广告
     * @param adType 广告type
     * @param adMark 广告位
     * @param height 高度 默认值屏幕高度
     * @param width 宽度 默认值屏幕宽度
     */
    fun requestAd(adType: String, adMark: String, height: Int = AppHelper.screenHeight, width: Int = AppHelper.screenWidth, token: Long) {
        //
        mActivity?.get().apply {
            adCache.put(adType, AdBean(height, null, true, adMark))
            if (height == AppHelper.screenHeight) {//5-1 5-2 6-1 6-2
                val space = (AppHelper.screenDensity * readerSettings.readContentPageTopSpace * 2f).toInt()
//                PlatformSDK.adapp().dycmNativeAd(this, adMark, height - space, width, AdCallback(adType, adMark, height,token))
                MediaControl.dycmNativeAd(this, adMark, null, { list ->
                    mediaAction(adType, adMark, height, token, list)
                },{errorCode ->

                })

            } else {//8-1
                val space = (AppHelper.screenDensity * readerSettings.readContentPageTopSpace).toInt()
                MediaControl.dycmNativeAd(this, adMark, height - space, width, { views ->
                    mediaAction(adType, adMark, height, token, views)
                },{errorCode ->

                })
            }
        }
    }

    /**
     * 广告 Type 生成
     * @param group 章节
     * @param index 页码
     * @param admark 广告位
     * @return AdType
     */
    fun generateAdType(group: Int, index: Int): String {
        val adType = StringBuilder().apply {
            append(group.toString())
            append("/")
            append(index.toString())
        }
        return adType.toString()
    }

    /**
     * 广告 Mark 生成
     * @param index 页码
     * @param count 总页码
     * @return AdMark
     */
    fun generateAdMark(index: Int = ReaderStatus.position.index, count: Int = ReaderStatus.position.groupChildCount): String {
        return when (index) {
            count - 2 -> if (readerSettings.animation == GLReaderView.AnimationType.LIST) "6-3" else "8-1"
            count - 1 -> {
                if (readerSettings.animation == GLReaderView.AnimationType.LIST) {
                    if (readerSettings.isLandscape) "6-3" else "5-3"
                } else {
                    if (readerSettings.isLandscape) "6-1" else "5-1"
                }
            }
            else -> if (readerSettings.isLandscape) "6-2" else "5-2"
        }
    }

    /**
     * 广告 Page 生成
     * @param group 章节
     * @param index 页码
     * @param offset 偏移量
     * @return AdMark
     */
    private fun generateNovelPageBean(group: Int, index: Int, offset: Int): NovelPageBean {
        return NovelPageBean(arrayListOf(), offset, arrayListOf()).apply {
            adType = generateAdType(group, index)
        }
    }

    /**
     * 获取广告位Index
     * @param adType 广告type
     * @return 广告位
     */
    fun getAdIndexForString(adType: String): String = adType.split("/").last()

    /**
     * 获取广告位Group
     * @param adType 广告type
     * @return 广告位
     */
    fun getAdGroupForString(adType: String): String = adType.split("/").first()


    /**
     * 清除所有广告
     */
    fun clearAllAd(){
        releaseAdView()
        adCache.clear()
    }

    /**
     * 删除其他无用广告
     */
    private fun removeOldAd(group: Int) {
        val iterator = adCache.map.iterator()
        val cacheNum = if (readerSettings.animation == GLReaderView.AnimationType.LIST) 6 else 2

        val removeList = mutableListOf<AdBean>()

        while (iterator.hasNext()) {
            val item = iterator.next()
            if (getAdGroupForString(item.key).toInt() > group + cacheNum || getAdGroupForString(item.key).toInt() < group - cacheNum) {

                removeList.add(item.value)
                iterator.remove()
            }
        }

        uiThread {
            try {
                removeList.forEach {
                    if(it.view?.parent == null) {
                        if (it.view is Closeable) {
                            (it.view as Closeable).close()
                        }
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }


    private fun releaseAdView() {
        synchronized(adCache.map) {
            val iterator = adCache.map.values.iterator()

            val removeList = mutableListOf<AdBean>()

            while (iterator.hasNext()) {
                val adView = iterator.next()
                removeList.add(adView)
                iterator.remove()
            }


            uiThread {

                try {
                    removeList.forEach{
                        if(it.view?.parent == null) {
                            if (it.view is Closeable) {
                                (it.view as Closeable).close()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 广告容器参数
     */
    private var params: FrameLayout.LayoutParams? = null

    fun getLayoutParams(top: Int = 0): FrameLayout.LayoutParams? {
        if (params == null) {
            params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        params?.setMargins(0, top, 0, 0)
        return params
    }

    /**
     * 广告封装类
     */
    data class AdBean(var height: Int, var view: View?, var loaded: Boolean, var mark: String)

    /**
     * 广告缓存类
     */
    class DataCache {
        val map: TreeMap<String, AdBean> = TreeMap()
        fun put(key: String, ad: AdBean) {
            synchronized(map) {
                if (map.containsKey(key)) {
                    var value = map[key]
                    value?.view?.clearFocus()
                    value?.view = null
                    value = null
                }
                map[key] = ad
            }
        }
        fun get(key: String): AdBean? = map[key]
        fun remove(key: String) {
            synchronized(map) {
                map.remove(key)
            }
        }

        fun clear() {
            synchronized(map) {
                map.clear()
            }
        }
    }

    /**
     * 广告处理
     */
    private fun mediaAction(adType: String, mark: String, height: Int, curTonken: Long, views: List<ViewGroup>?) {

        if (ReadMediaManager.tonken != curTonken) {
            if(views?.isEmpty() == false &&  views[0] is Closeable){
                try {
                    (views[0] as Closeable).close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
//            adCache.put(adType, AdBean(height, null, false, mark))
            adCache.remove(adType)
            Log.e("mediaAction", "token改变了")
            return
        }
        try {
            if (views?.isEmpty() == false
                    && views[0].parent == null) {
                views[0].id = R.id.pac_reader_ad
                adCache.put(adType, AdBean(height, views[0], true, mark))
                loadAdComplete?.invoke(adType)
            } else {
                if (adCache.get(adType) == null || !adCache.get(adType)!!.loaded) {
                    adCache.put(adType, AdBean(height, null, false, mark))
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            if (adCache.get(adType) == null || !adCache.get(adType)!!.loaded) {
                adCache.put(adType, AdBean(height, null, false, mark))
            }
        }
    }

    fun onDestroy() {
        loadAdComplete = null

        ReadMediaManager.clearAllAd()
        ReadMediaManager.frameLayout = null
    }
}