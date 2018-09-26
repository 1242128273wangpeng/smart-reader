package net.lzbook.kit.ui.activity

import android.view.KeyEvent.KEYCODE_BACK

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager

import com.alibaba.android.arouter.facade.annotation.Route
import net.lzbook.kit.utils.router.RouterConfig
import com.dy.media.MediaCode
import com.dy.media.MediaControl
import com.dy.media.MediaLifecycle
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_switch_ad.*
import net.lzbook.kit.R
import net.lzbook.kit.utils.toast.ToastUtil.mainLooperHandler

@Route(path = RouterConfig.SWITCH_AD_ACTIVITY)
class SwitchADActivity : Activity() {

    private var canHandleClickAction = false

    private var clickTime = LongArray(2)
    private var loadedAD=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_switch_ad)

        canHandleClickAction = false

        fl_switch_ad_content.removeAllViews()

        updateContentView(fl_switch_ad_content, false)

        ll_switch_ad_close.setOnClickListener { finish() }


        mainLooperHandler.postDelayed({ canHandleClickAction = true }, 2000)
    }

    private fun handleClickAction() {
        System.arraycopy(clickTime, 1, clickTime, 0, clickTime.size - 1)
        clickTime[clickTime.size - 1] = SystemClock.uptimeMillis()

        if (clickTime[0] > SystemClock.uptimeMillis() - 500) {
            finish()
        }
    }

    private fun updateContentView(view: View, isShow: Boolean) {
        if (isShow) {
            view.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
        } else {
            view.setBackgroundColor(Color.parseColor("#00FFFFFF"))
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        var width=window.decorView.width
        var height=window.decorView.height
        if(hasFocus&&!loadedAD&&height>width){
            loadedAD=true

            MediaControl.loadSwitchScreenMedia(this, fl_switch_ad_content) { resultCode ->
                Logger.e("切屏广告: $resultCode")
                when (resultCode) {
                    MediaCode.MEDIA_SUCCESS -> {
                        updateContentView(fl_switch_ad_content, true)
                        ll_switch_ad_close.visibility = View.VISIBLE
                    }

                    MediaCode.MEDIA_FAILED -> {
                        this@SwitchADActivity.finish()
                    }

                    MediaCode.MEDIA_DISMISS -> {
                        this@SwitchADActivity.finish()
                    }
                }
            }



        }

    }

    override fun onResume() {
        super.onResume()
        MediaLifecycle.onResume()
    }

    override fun onPause() {
        super.onPause()
        MediaLifecycle.onPause()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (canHandleClickAction && keyCode == KEYCODE_BACK) {
            handleClickAction()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        MediaLifecycle.onDestroy()
    }
}