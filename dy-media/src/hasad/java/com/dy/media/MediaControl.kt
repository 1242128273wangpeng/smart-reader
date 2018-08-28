package com.dy.media

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import com.dycm_adsdk.PlatformSDK
import com.dycm_adsdk.bean.DyError
import com.dycm_adsdk.callback.AbstractCallback
import com.dycm_adsdk.callback.AdMultiResultCallBack
import com.dycm_adsdk.callback.AdResultCallBack
import com.dycm_adsdk.callback.DYSplashCallBack
import com.dycm_adsdk.view.NativeView


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
            PlatformSDK.adapp()?.dycmSplashAd(context as Activity?, "11-1", layout, object : DYSplashCallBack {
                override fun adExposure() {

                }

                override fun adDismissed() {
                    //开屏页面关闭
                    callback(MediaCode.MEDIA_DISMISS)
                }

                override fun adLoadFail(dyError: DyError?) {
                    // 广告请求失败
                    callback(MediaCode.MEDIA_FAILED)
                }

                override fun adLoadSuccess() {
                    //广告请求成功
                    callback(MediaCode.MEDIA_SUCCESS)
                }

                override fun adClicked() {

                }

                override fun adTick(var1: Long) {

                }

            })
        }
    }

    override fun loadSplashMedia(context: Context, layout: ViewGroup,
                                 callback: (resultCode: Int) -> Unit) {
        PlatformSDK.adapp()?.dycmSplashAd(context as Activity, "10-1", layout, object : DYSplashCallBack {
            override fun adExposure() {
            }

            override fun adDismissed() {
                //开屏页面关闭
                callback(MediaCode.MEDIA_DISMISS)
            }

            override fun adLoadFail(dyError: DyError?) {
                //广告请求失败
                callback(MediaCode.MEDIA_FAILED)
            }

            override fun adLoadSuccess() {
                //广告请求成功
                callback(MediaCode.MEDIA_SUCCESS)
            }

            override fun adClicked() {

            }

            override fun adTick(var1: Long) {

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
                override fun adLoadSuccess(views: MutableList<ViewGroup>?) {
                    if (views != null && views.isNotEmpty()) {
                        activity.uiThread {
                            headerMediaCallback.requestMediaSuccess(views[0])
                        }
                    }
                }

                override fun adLoadFail(dyError: DyError?) {

                }

            })
        }
    }

    override fun loadBookShelfFloatMedia(activity: Activity, viewGroup: ViewGroup?) {
        doAsync {
            PlatformSDK.adapp()?.dycmNativeAd(activity, "1-2", null, object : AbstractCallback() {
                override fun adLoadSuccess(views: MutableList<ViewGroup>?) {
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

                override fun adLoadFail(dyError: DyError?) {
                    activity.uiThread {
                        if (viewGroup != null) {
                            viewGroup.visibility = View.GONE
                        }
                    }
                }

            })
        }
    }

    override fun loadBookShelMedia(activity: Activity, count: Int,
                                   mediaCallback: IMediaControl.MediaCallback) {
        doAsync {
            PlatformSDK.adapp()?.dycmNativeAd(activity, "1-1", RelativeLayout(activity), object : AdMultiResultCallBack {
                override fun adLoadSuccess(views: MutableList<ViewGroup>?) {
                    Log.e("书架广告", "书架广告请求成功：" + views?.size)
                    if (views != null && views.isNotEmpty()) {
                        activity.uiThread {
                            mediaCallback.requestMediaSuccess(views)
                        }
                    }
                }

                override fun adSupplLoadSuccess(views: MutableList<ViewGroup>?) {
                    Log.e("书架广告", "书架广告补余成功：" + views?.size)
                    if (views != null && views.isNotEmpty()) {
                        activity.uiThread {
                            mediaCallback.requestMediaRepairSuccess(views)
                        }
                    }
                }

                override fun adLoadFail(dyError: DyError?) {

                }

            }, count)
        }
    }

    override fun loadBookShelMedia2(activity: Activity, mediaCallback: IMediaControl.MediaCallback) {
        doAsync {
            PlatformSDK.adapp()?.dycmNativeAd(activity, "1-1", null, object : AdResultCallBack {
                override fun adLoadSuccess(views: MutableList<ViewGroup>?) {
                    Log.e("书架广告", "书架广告请求成功：" + views?.size)
                    if (views != null && views.isNotEmpty()) {
                        activity.uiThread {
                            mediaCallback.requestMediaSuccess(views)
                        }
                    }
                }

                override fun adLoadFail(dyError: DyError?) {

                }

            })
        }
    }


    override fun getAdSwitch(ad_mark_id: String) =
            PlatformSDK.config()?.getAdSwitch(ad_mark_id) ?: false

    override fun getChapterFrequency(): Int =
            PlatformSDK.config()?.chapter_limit ?: 0

    override fun dycmNativeAd(context: Activity?, adLocalId: String, view: ViewGroup?,
                              resultSuccessCallback: (List<ViewGroup>?) -> Unit, resultFailCallback: (errorCode: Int) -> Unit) {
        PlatformSDK.adapp()?.dycmNativeAd(context, adLocalId, view, object : MediaAbstractCallback() {
            override fun adLoadSuccess(views: MutableList<ViewGroup>?) {
                resultSuccessCallback.invoke(views)
            }

            override fun adLoadFail(dyError: DyError) {
                resultFailCallback.invoke(dyError.getErrorCode())
            }
        })
    }

    override fun dycmNativeAd(context: Activity?, adLocalId: String, height: Int, width: Int,
                              resultSuccessCallback: (views: List<ViewGroup>?) -> Unit, resultFailCallback: (errorCode: Int) -> Unit) {
        PlatformSDK.adapp()?.dycmNativeAd(context as Activity, adLocalId, height, width, object : MediaAbstractCallback() {
            override fun adLoadSuccess(views: MutableList<ViewGroup>?) {
                resultSuccessCallback.invoke(views)
            }

            override fun adLoadFail(dyError: DyError) {
                resultFailCallback.invoke(dyError.getErrorCode())
            }

        })
    }


    private var restMediaRunnable: Runnable? = null

    private var restMediaHandler: Handler? = null

    private var restDialog: Dialog? = null

    override fun startRestMedia(activity: Activity) {
        if (PlatformSDK.config() == null) return
//        val runtime = 30000L
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
                Log.e("3-1", "3-1广告监控函数 ：check")
                if (restDialog == null) {
                    restDialog = Dialog(activity, R.style.custom_dialog)
                    restDialog?.apply {

                        setContentView(R.layout.dialog_reader_rest)
                        val window = window
                        val params = window.attributes
                        params.width = ViewGroup.LayoutParams.MATCH_PARENT
                        params.height = ViewGroup.LayoutParams.MATCH_PARENT
                        params.gravity = Gravity.CENTER
                        window.attributes = params
                        setCanceledOnTouchOutside(false)
                        setOnDismissListener {
                            restMediaHandler?.removeCallbacksAndMessages(null)
                            restMediaHandler?.postDelayed(restMediaRunnable, runtime)
                            restDialog = null
                        }

                        setOnShowListener {

                            findViewById<ImageView>(R.id.img_close).setOnClickListener {
                                dismiss()
                            }

                            PlatformSDK.adapp()?.dycmSplashAd(activity, "3-1", findViewById<RelativeLayout>(R.id.rl_ad), object : DYSplashCallBack {
                                override fun adExposure() {
                                    Log.e("3-1", "3-1广告监控函数 ：adExposure")
                                }

                                override fun adDismissed() {
                                    Log.e("3-1", "3-1广告监控函数 ：adDismissed")
                                    restMediaHandler?.postDelayed({
                                        if (isShowing)
                                            dismiss()
                                    }, 200)
                                }

                                override fun adLoadFail(dyError: DyError?) {
                                    Log.e("3-1", "3-1广告监控函数 ：拉取失败")
                                    restMediaHandler?.postDelayed({
                                        if (isShowing)
                                            dismiss()
                                    }, 200)
                                }

                                override fun adLoadSuccess() {
                                }

                                override fun adClicked() {

                                }

                                override fun adTick(var1: Long) {
                                    Log.e("3-1", "3-1广告监控函数 ：" + var1)
                                }
                            })
                        }

                        show()
                    }
                }
            }
        }
        restMediaHandler?.postDelayed(restMediaRunnable, runtime)
    }

    override fun stopRestMedia() {
        restDialog = null
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
        PlatformSDK.adapp()?.dycmNativeAd(context as Activity, "9-1", null, object : AbstractCallback() {
            override fun adLoadSuccess(views: MutableList<ViewGroup>?) {
                onCall.invoke(views?.get(0), true)
            }

            override fun adLoadFail(dyError: DyError?) {
//                onCall.invoke(null, false)
            }

        })
    }

    override fun loadBookCoverAd(activity: Activity?, onCall: (view: View?) -> Unit) {

        PlatformSDK.adapp()?.dycmNativeAd(activity, "1-4", null, object : AbstractCallback() {
            override fun adLoadSuccess(views: MutableList<ViewGroup>?) {
                onCall.invoke(views?.get(0))
            }

            override fun adLoadFail(dyError: DyError?) {
//                onCall.invoke(null)
            }

        })
    }
}
