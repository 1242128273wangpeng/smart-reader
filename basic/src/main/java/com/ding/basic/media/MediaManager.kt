package com.ding.basic.media

import android.content.Context
import android.util.LruCache
import android.view.View

/**
 * @desc 广告缓存管理
 * @author lijun Lee
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/4/25 12:08
 */
object MediaManager {

    private val adLruCache = LruCache<String, View>(20)

    /**
     * 获取广告视图
     * context : Context
     * sequence : 章节
     * index : 章节某页
     * adType : 广告类型
     * height : 广告视图高度
     * width : 广告视图宽度
     * sequence + index + adType组合成视图Key
     * 通过组合的Key先去缓存获取，缓存有广告则直接返回，反之重新拉取新的广告视图，并加入缓存，再返回广告视图
     */
    fun getAdView(context: Context, sequence: Int, index: Int, adType: String, height: Int = 0, width: Int = 0): View {

        val adKey = StringBuilder()
        adKey.append(sequence.toString())
        adKey.append(index.toString())
        adKey.append(adType)

        var adView = adLruCache.get(adKey.toString())
        if (adView == null) {
            val mediaView = MediaView(context)
            if (height == 0 && width == 0) {
                mediaView.loadData(context, adType)
            } else {
                mediaView.loadData(context, adType, height, width)
            }
            adLruCache.put(adKey.toString(), mediaView)
            adView = mediaView
            return adView
        } else {
            return adView
        }
    }

    /**
     * 计算章节广告标识
     */
//    fun parseNovelMediaId(novelList: List<NovelPageBean>,isWideType: Boolean = false) :List<NovelPageBean>{
//
//    }

    /**
     * 清除缓存
     */
    fun clearCache() {
        adLruCache.evictAll()
    }
}