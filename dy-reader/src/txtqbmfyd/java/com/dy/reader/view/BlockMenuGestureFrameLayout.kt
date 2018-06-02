package com.dy.reader.view

import android.content.Context
import android.content.res.Configuration
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.dy.reader.event.EventSetting
import com.dy.reader.helper.AppHelper
import com.dy.reader.page.GLReaderView
import com.dy.reader.setting.ReaderSettings
import com.dy.reader.setting.ReaderStatus
import org.greenrobot.eventbus.EventBus

class BlockMenuGestureFrameLayout : FrameLayout {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    val menuRect by lazy {
        Rect(width / 3, height / 5, width / 3 * 2, height / 5 * 4)
    }

    var shouldShowMenu = false

    val downPointF = PointF()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        menuRect.set(Rect(width / 3, height / 5, width / 3 * 2, height / 5 * 4))
    }

    fun canBlock():Boolean{
        return ReaderSettings.instance.animation != GLReaderView.AnimationType.LIST
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if(!canBlock()){
            return super.dispatchTouchEvent(ev)
        }

        var flag = true
        when (ev?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downPointF.set(ev.x, ev.y)
                shouldShowMenu = menuRect.contains(ev.x.toInt(), ev.y.toInt())
            }
            MotionEvent.ACTION_MOVE -> {
                if (ReaderStatus.isMenuShow) {
                    flag = false
                }
                if (Math.abs(ev.x - downPointF.x) >= AppHelper.touchSlop
                        || Math.abs(ev.y - downPointF.y) >= AppHelper.touchSlop) {
                    shouldShowMenu = false
                }
            }
            MotionEvent.ACTION_UP -> {
                if (shouldShowMenu || ReaderStatus.isMenuShow) {
                    EventBus.getDefault().post(EventSetting(EventSetting.Type.MENU_STATE_CHANGE))
                    flag = false
                }
            }
        }
        if (flag) {
            return super.dispatchTouchEvent(ev)
        } else {
            return false
        }
    }

}