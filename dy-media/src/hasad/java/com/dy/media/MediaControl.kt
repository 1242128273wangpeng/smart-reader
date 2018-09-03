package com.dy.media

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.dycm_adsdk.PlatformSDK
import com.dycm_adsdk.callback.AbstractCallback
import com.dycm_adsdk.callback.ResultCode
import com.dycm_adsdk.utils.DyLogUtils
import com.dycm_adsdk.view.NativeView
import org.json.JSONException
import org.json.JSONObject


/**
 * Desc 广告控制 有
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/6/7 11:13
 */

object MediaControl : IMediaControl {

    override fun loadSwitchScreenMedia(context: Context, layout: FrameLayout,
                                       callback: (resultCode: Int) -> Unit) {
        if (PlatformSDK.config()?.getAdSwitch("11-1") == true) {
            PlatformSDK.adapp()?.dycmSplashAd(context, "11-1", layout, object : AbstractCallback() {
                override fun onResult(adswitch: Boolean, jsonResult: String?) {
                    super.onResult(adswitch, jsonResult)
                    if (!adswitch) return
                    try {
                        val jsonObject = JSONObject(jsonResult)
                        if (jsonObject.has("state_code")) {
                            val resultCode = ResultCode.parser(jsonObject.getInt("state_code"))
                            when (resultCode) {
                                ResultCode.AD_REQ_SUCCESS -> {
                                    //广告请求成功
                                    DyLogUtils.dd("AD_REQ_SUCCESS" + jsonResult)
                                    callback(MediaCode.MEDIA_SUCCESS)
                                }
                                ResultCode.AD_REQ_FAILED -> {
                                    //广告请求失败
                                    DyLogUtils.dd("AD_REQ_FAILED" + jsonResult)
                                    callback(MediaCode.MEDIA_FAILED)
                                }
                                ResultCode.AD_ONCLICKED_CODE -> {
                                    //开屏页面点击
                                    DyLogUtils.dd("AD_ONCLICKED_CODE" + jsonResult)
                                }
                                ResultCode.AD_DISMISSED_CODE -> {
                                    //开屏页面关闭
                                    callback(MediaCode.MEDIA_DISMISS)
                                }
                                ResultCode.AD_ONTICK_CODE -> {
                                    //剩余显示时间
                                    DyLogUtils.dd("AD_ONTICK_CODE" + jsonResult)
                                }
                                else -> {
                                }
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            })
        }
    }

    override fun loadSplashMedia(context: Context, layout: ViewGroup,
                                 callback: (resultCode: Int) -> Unit) {
        PlatformSDK.adapp()?.dycmSplashAd(context, "10-1", layout, object : AbstractCallback() {
            override fun onResult(adswitch: Boolean, jsonResult: String?) {
                if (adswitch) {
                    try {
                        val jsonObject = JSONObject(jsonResult)
                        if (jsonObject.has("state_code")) {
                            when (ResultCode.parser(jsonObject.getInt("state_code"))) {
                                ResultCode.AD_REQ_SUCCESS -> {
                                    //广告请求成功
                                    callback(MediaCode.MEDIA_SUCCESS)
                                    DyLogUtils.dd("AD_REQ_SUCCESS $jsonResult")
                                }
                                ResultCode.AD_REQ_FAILED -> {
                                    //广告请求失败
                                    DyLogUtils.dd("AD_REQ_FAILED $jsonResult")
                                    callback(MediaCode.MEDIA_FAILED)
                                }
                                ResultCode.AD_DISMISSED_CODE -> {
                                    //开屏页面关闭
                                    callback(MediaCode.MEDIA_DISMISS)
                                }
                                ResultCode.AD_ONCLICKED_CODE -> {
                                    //开屏页面点击
                                    DyLogUtils.dd("AD_ONCLICKED_CODE $jsonResult")
                                }
                                ResultCode.AD_ONTICK_CODE -> {
                                    //剩余显示时间
                                    DyLogUtils.dd("AD_ONTICK_CODE $jsonResult")
                                }
                                else -> {
                                }
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                } else {
                    callback(MediaCode.MEDIA_DISABLE)
                }
            }
        })
    }

    override fun insertBookShelfMediaType(isGrid: Boolean) {
        PlatformSDK.config()?.setBookShelfGrid(isGrid)
    }

    override fun loadBookShelfMediaInterval(): Int = PlatformSDK.config()?.adCount ?: 10

    override fun loadBookShelfHeaderMedia(activity: Activity,
                                          headerMediaCallback: IMediaControl.HeaderMediaCallback) {
        doAsync {
            PlatformSDK.adapp()?.dycmNativeAd(activity, "1-1", null, object : AbstractCallback() {
                override fun onResult(adswitch: Boolean, views: List<ViewGroup>?, jsonResult: String?) {
                    super.onResult(adswitch, views, jsonResult)
                    if (!adswitch) return
                    try {
                        val jsonObject = JSONObject(jsonResult)
                        if (jsonObject.has("state_code")) {
                            when (ResultCode.parser(jsonObject.getInt("state_code"))) {
                                ResultCode.AD_REQ_SUCCESS -> {
                                    if (views != null && views.isNotEmpty()) {
                                        activity.uiThread {
                                            headerMediaCallback.requestMediaSuccess(views[0])
                                        }
                                    }
                                }
                                else -> {

                                }
                            }
                        }
                    } catch (exception: JSONException) {
                        exception.printStackTrace()
                    }
                }
            })
        }
    }

    override fun loadBookShelfFloatMedia(activity: Activity, viewGroup: ViewGroup?) {
        doAsync {
            PlatformSDK.adapp()?.dycmNativeAd(activity, "1-2", null, object : AbstractCallback() {
                override fun onResult(adSwitch: Boolean, views: List<ViewGroup>?, jsonResult: String?) {
                    super.onResult(adSwitch, views, jsonResult)
                    Log.e("书架广告", "书架悬浮广告请求：" + views?.size)
                    if (!adSwitch) return

                    try {
                        val jsonObject = JSONObject(jsonResult)
                        if (jsonObject.has("state_code")) {
                            when (ResultCode.parser(jsonObject.getInt("state_code"))) {
                                ResultCode.AD_REQ_SUCCESS -> {
                                    if (views != null && views.isNotEmpty()) {
                                        activity.uiThread {
                                            if (viewGroup != null) {
                                                viewGroup.visibility = View.VISIBLE
                                                viewGroup.removeAllViews()
                                                viewGroup.addView(views[0])
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    activity.uiThread {
                                        if (viewGroup != null) {
                                            viewGroup.visibility = View.GONE
                                        }
                                    }
                                }
                            }
                        }
                    } catch (exception: JSONException) {
                        exception.printStackTrace()
                    }
                }
            })
        }
    }

    override fun loadBookShelMedia(activity: Activity, count: Int,
                                   mediaCallback: IMediaControl.MediaCallback) {
        doAsync {
            PlatformSDK.adapp()?.dycmNativeAd(activity, "1-1", RelativeLayout(activity), object : AbstractCallback() {
                override fun onResult(adswitch: Boolean, views: List<ViewGroup>?, jsonResult: String?) {
                    if (!adswitch) {
                        return
                    }

                    try {
                        val jsonObject = JSONObject(jsonResult)
                        if (jsonObject.has("state_code")) {
                            when (ResultCode.parser(jsonObject.getInt("state_code"))) {
                                ResultCode.AD_REQ_SUCCESS -> {
                                    Log.e("书架广告", "书架广告请求成功：" + views?.size)
                                    if (views != null && views.isNotEmpty()) {
                                        activity.uiThread {
                                            mediaCallback.requestMediaSuccess(views)
                                        }
                                    }
                                }
                                ResultCode.AD_REPAIR_SUCCESS -> {
                                    Log.e("书架广告", "书架广告补余成功：" + views?.size)
                                    if (views != null && views.isNotEmpty()) {
                                        activity.uiThread {
                                            mediaCallback.requestMediaRepairSuccess(views)
                                        }
                                    }
                                }
                                else -> {

                                }
                            }
                        }
                    } catch (exception: JSONException) {
                        exception.printStackTrace()
                    }
                }
            }, count)
        }
    }

    override fun getAdSwitch(ad_mark_id: String) =
            PlatformSDK.config()?.getAdSwitch(ad_mark_id) ?: false

    override fun getChapterFrequency(): Int =
            PlatformSDK.config()?.chapter_limit ?: 0

    override fun dycmNativeAd(context: Context?, adLocalId: String, view: ViewGroup?,
                              resultCallback: (switch: Boolean, List<ViewGroup>?, jsonResult: String?) -> Unit) {
        PlatformSDK.adapp()?.dycmNativeAd(context, adLocalId, view, object : MediaAbstractCallback() {
            override fun onResult(adswitch: Boolean, views: List<ViewGroup>?, jsonResult: String?) {
                super.onResult(adswitch, view, jsonResult)
                resultCallback.invoke(adswitch, views, jsonResult)
            }
        })
    }

    override fun dycmNativeAd(context: Context?, adLocalId: String, height: Int, width: Int,
                              resultCallback: (switch: Boolean, views: List<ViewGroup>?, jsonResult: String?) -> Unit) {
        PlatformSDK.adapp()?.dycmNativeAd(context, adLocalId, height, width, object : MediaAbstractCallback() {
            override fun onResult(adswitch: Boolean, views: List<ViewGroup>?, jsonResult: String?) {
                super.onResult(adswitch, views, jsonResult)
                resultCallback.invoke(adswitch, views, jsonResult)
            }
        })
    }



    private var restMediaRunnable: Runnable? = null

    private var restMediaHandler: Handler? = null

    override fun startRestMedia(onTime: () -> Unit) {
        if (PlatformSDK.config() == null) return
        val runtime = if (PlatformSDK.config().restAd_sec == 0) {
            30.times(60000).toLong()
        } else {
            PlatformSDK.config().restAd_sec.times(60000).toLong()
        }
        if (restMediaHandler == null) {
            restMediaHandler = Handler(Looper.getMainLooper())
        }
        if (restMediaRunnable == null) {
            restMediaRunnable = Runnable {
                onTime.invoke()
                restMediaHandler?.postDelayed(restMediaRunnable, runtime)
            }
            restMediaHandler?.postDelayed(restMediaRunnable, runtime)
        }
    }

    override fun loadRestMedia(activity: Activity?, onSuccess: (view: View?) -> Unit) {
        PlatformSDK.adapp()?.dycmNativeAd(activity, "3-1", null, object : AbstractCallback() {
            override fun onResult(adswitch: Boolean, views: List<ViewGroup>?, jsonResult: String?) {
                super.onResult(adswitch, views, jsonResult)
                if (!adswitch) return
                try {
                    val jsonObject = JSONObject(jsonResult)
                    if (jsonObject.has("state_code")) {
                        when (ResultCode.parser(jsonObject.getInt("state_code"))) {
                            ResultCode.AD_REQ_SUCCESS -> {
                                onSuccess.invoke(views?.get(0))
                            }
                            ResultCode.AD_REQ_FAILED -> {
                            }
                            else -> {
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }

    override fun stopRestMedia() {
        restMediaRunnable = null
        restMediaHandler?.removeCallbacksAndMessages(null)
        restMediaHandler = null
    }

    override fun addPageAd(child: View) {
        try {
            if (child is NativeView) {
                PlatformSDK.config()?.ExposureToPlugin(child)
            } else {
                if (child is ViewGroup) {
                    val cChild = child.getChildAt(0)
                    if (cChild != null && cChild is NativeView) {
                        PlatformSDK.config()?.ExposureToPlugin(cChild)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun loadBookEndMedia(context: Context, onCall: (view: View?, isSuccess: Boolean) -> Unit) {
        PlatformSDK.adapp()?.dycmNativeAd(context, "9-1", null, object : AbstractCallback() {
            override fun onResult(adswitch: Boolean, views: List<ViewGroup>, jsonResult: String?) {
                super.onResult(adswitch, views, jsonResult)
                if (!adswitch) {
                    return
                }
                try {
                    val jsonObject = JSONObject(jsonResult)
                    if (jsonObject.has("state_code")) {
                        when (ResultCode.parser(jsonObject.getInt("state_code"))) {
                            ResultCode.AD_REQ_SUCCESS
                            -> {
                                onCall.invoke(views[0], true)
                            }
                            else -> {
                                onCall.invoke(null, false)
                            }
                        }
                    }
                } catch (exception: JSONException) {
                    exception.printStackTrace()
                }
            }
        })
    }

    override fun loadBookCoverAd(activity: Activity?, onCall: (view: View?) -> Unit) {
        PlatformSDK.adapp()?.dycmNativeAd(activity, "1-4", null, object : AbstractCallback() {
            override fun onResult(adswitch: Boolean, views: List<ViewGroup>, jsonResult: String?) {
                super.onResult(adswitch, views, jsonResult)
                if (!adswitch) {
                    return
                }
                try {
                    val jsonObject = JSONObject(jsonResult)
                    if (jsonObject.has("state_code")) {
                        when (ResultCode.parser(jsonObject.getInt("state_code"))) {
                            ResultCode.AD_REQ_SUCCESS
                            -> {
                                onCall.invoke(views[0])
                            }
                            else -> {
                                onCall.invoke(null)
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        })
    }
}
