package com.dy.media

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.dycm_adsdk.callback.AdResultCallBack
import java.lang.ref.WeakReference

/**
 * Desc 广告控制
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/6/7 11:13
 */
interface IMediaControl {

    /**
     * 切屏广告
     */
    fun loadSwitchScreenMedia(context: Context, layout: FrameLayout,
                              callback: (resultCode: Int) -> Unit)

    /**
     * 开屏广告
     */
    fun loadSplashMedia(context: Context, layout: ViewGroup,
                        callback: (resultCode: Int) -> Unit)

    /***
     * 设置书架展示类型
     * **/
    fun insertBookShelfMediaType(isGrid: Boolean)

    /***
     * 获取书架广告间隔
     * **/
    fun loadBookShelfMediaInterval(): Int

    /***
     * 获取书架顶部广告位
     * **/
    fun loadBookShelfHeaderMedia(activity: Activity, headerMediaCallback: HeaderMediaCallback)

    /***
     * 获取书架悬浮广告
     * **/
    fun loadBookShelfFloatMedia(activity: Activity, viewGroup: ViewGroup?)

    fun loadBookShelMedia(activity: Activity, count: Int, mediaCallback: MediaCallback)

    interface MediaCallback {
        fun requestMediaSuccess(views: List<ViewGroup>)

        fun requestMediaRepairSuccess(views: List<ViewGroup>)
    }

    interface HeaderMediaCallback {
        fun requestMediaSuccess(viewGroup: ViewGroup?)
    }

    //阅读页
    fun getAdSwitch(ad_mark_id: String): Boolean

    fun getChapterFrequency(): Int

    fun dycmNativeAd(context: Context?, adLocalId: String, view: ViewGroup?,
                     adResultCallBack: AdResultCallBack)

    fun dycmNativeAd(context: Context?, adLocalId: String, height: Int, width: Int,
                     adResultCallBack: AdResultCallBack)

    fun startRestMedia(onTime: () -> Unit)

    fun loadRestMedia(activity: Activity?, onSuccess: (view: View?) -> Unit)

    fun stopRestMedia()
}