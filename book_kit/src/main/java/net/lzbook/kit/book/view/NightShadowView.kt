package net.lzbook.kit.book.view

import android.content.Context
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.util.AttributeSet
import android.view.View
import de.greenrobot.event.EventBus
import iyouqu.theme.EventThemeModeChange
import iyouqu.theme.ThemeHelper
import iyouqu.theme.ThemeMode
import net.lzbook.kit.R
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.safeRegist
import net.lzbook.kit.utils.safeUnregist


/**
 * Created by xian on 2017/9/11.
 */
class NightShadowView : View {
    constructor(context: Context?) : super(context)

    private var radius: Float = 0F

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        obtainStyle(context, attrs)
    }


    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        obtainStyle(context, attrs)
    }

    init {
        setMode(ThemeHelper.getInstance(context).isNight)
    }

    private fun obtainStyle(context: Context?, attrs: AttributeSet?) {
        val a = context!!.obtainStyledAttributes(
                attrs, R.styleable.NightShadowView)
        val N = a.getIndexCount()
        for (i in 0..N - 1) {
            val attr = a.getIndex(i)
            if (attr == R.styleable.NightShadowView_cornerRadius) {
                radius = a.getDimension(attr, 0F)
                break
            }
        }
        a.recycle()
    }

    private fun setMode(flag: Boolean) {
        if (flag) {
            visibility = VISIBLE
            if (radius == 0F) {
                setBackgroundColor(Color.BLACK)
            } else {
                val shapeDrawable = ShapeDrawable(RectShape())
                shapeDrawable.paint.color = Color.BLACK
                shapeDrawable.paint.style = Paint.Style.FILL
                shapeDrawable.paint.pathEffect = CornerPathEffect(radius)
                setBackgroundDrawable(shapeDrawable)
            }
            alpha = Constants.NIGHT_SHADOW_ALPHA
        } else {
            visibility = GONE
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setMode(ThemeHelper.getInstance(context).isNight)
        EventBus.getDefault().safeRegist(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        EventBus.getDefault().safeUnregist(this)
    }

    fun onEventMainThread(mode: EventThemeModeChange) {
        setMode(mode.mode == ThemeMode.NIGHT)
    }
}