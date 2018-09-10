package com.dy.media

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout


/**
 * Desc 广告控制 无
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/6/7 11:13
 */

object MediaControl : IMediaControl {

    override fun loadSwitchScreenMedia(context: Context, layout: FrameLayout,
                                       callback: (resultCode: Int) -> Unit) {
    }

    override fun loadSplashMedia(context: Context, layout: ViewGroup,
                                 callback: (resultCode: Int) -> Unit) {
    }

    override fun insertBookShelfMediaType(isGrid: Boolean) {}

    override fun loadBookShelfMediaInterval(): Int {
        return 0
    }

    override fun loadBookShelfHeaderMedia(activity: Activity, headerMediaCallback: IMediaControl.HeaderMediaCallback) {}

    override fun loadBookShelfFloatMedia(activity: Activity, viewGroup: ViewGroup?) {}

    override fun loadBookShelMedia(activity: Activity, count: Int, mediaCallback: IMediaControl.MediaCallback) {}
    override fun loadBookShelMedia2(activity: Activity, mediaCallback: IMediaControl.MediaCallback){}
    //阅读页
    override fun getAdSwitch(ad_mark_id: String): Boolean {
        return false
    }

    override fun getChapterFrequency(): Int {
        return 0
    }

    override fun dycmNativeAd(context: Context?, adLocalId: String, view: ViewGroup?,
                              resultCalback: (switch: Boolean, views: List<ViewGroup>?, jsonResult: String?) -> Unit) {

    }

    override fun dycmNativeAd(context: Context?, adLocalId: String, height: Int, width: Int,
                              resultCalback: (switch: Boolean, views: List<ViewGroup>?, jsonResult: String?) -> Unit) {

    }

    override fun startRestMedia(activity: Activity) {}

    override fun stopRestMedia() {}

    override fun addPageAd(child: View) {}

    override fun loadBookEndMedia(context: Context, onCall: (view: View?, isSuccess: Boolean) -> Unit) {

    }

    override fun loadBookCoverAd(activity: Activity?, onCall: (view: View?) -> Unit) {}

}
