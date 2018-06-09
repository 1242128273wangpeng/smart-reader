package com.dy.media


/**
 * Desc 广告控制 无
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/6/7 11:13
 */

class MediaControl : IMediaControl {

    override fun fetchSwitchScreenAd(context: Context, layout: FrameLayout,
                                     callback: (resultCode: Int) -> Unit) {}

    override fun fetchSplashMedia(context: Context, layout: ViewGroup,
                                  callback: (resultCode: Int) -> Unit) {}

    override fun insertBookShelfMediaType(isGrid: Boolean) {}

    override fun loadBookShelfMediaInterval(): Int {}

    override fun loadBookShelfHeaderMedia(activity: Activity, headerMediaCallback: HeaderMediaCallback) {}

    override fun loadBookShelfFloatMedia(activity: Activity, viewGroup: ViewGroup?) {}

    override fun loadBookShelMedia(activity: Activity, count: Int, mediaCallback: MediaCallback) {}

    //阅读页
    override fun getAdSwitch(ad_mark_id: String): Boolean {}

    override fun getChapterFrequency(): Int {}

    override fun dycmNativeAd(context: Context?, adLocalId: String, view: ViewGroup?,
                     adResultCallBack: AdResultCallBack) {}

    override fun dycmNativeAd(context: Context?, adLocalId: String, height: Int, width: Int,
                     adResultCallBack: AdResultCallBack) {}

    override fun startRestMedia(onTime: () -> Unit) {}

    override fun loadRestMedia(activity: Activity?, onSuccess: (view: View?) -> Unit) {}

    override fun stopRestMedia() {}

    override fun loadBookEndMedia(context: Context, onCall: (view: View?, isSuccess: Boolean) -> Unit)

}
