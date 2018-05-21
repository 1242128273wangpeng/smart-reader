package com.dingyue.bookshelf.contract

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.dycm_adsdk.PlatformSDK
import com.dycm_adsdk.callback.AbstractCallback
import com.dycm_adsdk.callback.ResultCode
import net.lzbook.kit.utils.AppLog
import net.lzbook.kit.utils.doAsync
import net.lzbook.kit.utils.uiThread
import org.json.JSONException
import org.json.JSONObject

/**
 * Desc 书架广告帮助类
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/14 15:30
 */
object BookShelfADContract {

    /***
     * 设置书架展示类型
     * **/
    fun insertBookShelfType(isGrid: Boolean) {
        if (PlatformSDK.config() != null) {
            PlatformSDK.config().setBookShelfGrid(isGrid)
        }
    }

    /***
     * 获取书架广告间隔
     * **/
    fun loadBookShelfADInterval(): Int {
        return if (PlatformSDK.config() != null) {
            PlatformSDK.config().adCount
        } else {
            10
        }
    }

    /***
     * 获取书架顶部广告位
     * **/
    fun loadBookShelfHeaderAD(activity: Activity, headerADCallback: HeaderADCallback) {
        doAsync {
            if (PlatformSDK.adapp() != null) {
                PlatformSDK.adapp().dycmNativeAd(activity, "1-1", null, object : AbstractCallback() {
                    override fun onResult(adswitch: Boolean, views: List<ViewGroup>?, jsonResult: String?) {
                        super.onResult(adswitch, views, jsonResult)
                        if (!adswitch) {
                            return
                        }

                        try {
                            val jsonObject = JSONObject(jsonResult)
                            if (jsonObject.has("state_code")) {
                                when (ResultCode.parser(jsonObject.getInt("state_code"))) {
                                    ResultCode.AD_REQ_SUCCESS -> {
                                        if (views != null && views.isNotEmpty()) {
                                            activity.uiThread {
                                                headerADCallback.requestADSuccess(views[0])
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
    }

    /***
     * 获取书架悬浮广告
     * **/
    fun loadBookShelfFloatAD(activity: Activity, viewGroup: ViewGroup?) {
        doAsync {
            if (PlatformSDK.adapp() != null) {
                PlatformSDK.adapp().dycmNativeAd(activity, "1-2", null, object : AbstractCallback() {
                    override fun onResult(adSwitch: Boolean, views: List<ViewGroup>?, jsonResult: String?) {
                        super.onResult(adSwitch, views, jsonResult)
                        AppLog.e("书架广告", "书架顶部广告请求：" + views?.size)
                        if (!adSwitch) {
                            return
                        }
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
    }

    fun loadBookShelAD(activity: Activity, count: Int, adCallback: ADCallback) {
        doAsync {
            if (PlatformSDK.adapp() != null) {
                PlatformSDK.adapp().dycmNativeAd(activity.applicationContext, "1-1", RelativeLayout(activity), object : AbstractCallback() {
                    override fun onResult(adswitch: Boolean, views: List<ViewGroup>?, jsonResult: String?) {
                        if (!adswitch) {
                            return
                        }

                        try {
                            val jsonObject = JSONObject(jsonResult)
                            if (jsonObject.has("state_code")) {
                                when (ResultCode.parser(jsonObject.getInt("state_code"))) {
                                    ResultCode.AD_REQ_SUCCESS -> {
                                        AppLog.e("书架广告", "书架广告请求成功：" + views?.size)
                                        if (views != null && views.isNotEmpty()) {
                                            activity.uiThread {
                                                adCallback.requestADSuccess(views)
                                            }
                                        }
                                    }
                                    ResultCode.AD_REPAIR_SUCCESS -> {
                                        AppLog.e("书架广告", "书架广告补余成功：" + views?.size)
                                        if (views != null && views.isNotEmpty()) {
                                            activity.uiThread {
                                                adCallback.requestADRepairSuccess(views)
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
    }

    interface ADCallback {
        fun requestADSuccess(views: List<ViewGroup>)

        fun requestADRepairSuccess(views: List<ViewGroup>)
    }

    interface HeaderADCallback {
        fun requestADSuccess(viewGroup: ViewGroup?)
    }
}