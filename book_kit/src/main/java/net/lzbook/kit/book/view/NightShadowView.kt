package net.lzbook.kit.book.view

import android.content.Context
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
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

    private var topLeftRadius: Float = 0F
    private var topRightRadius: Float = 0F
    private var bottomRightRadius: Float = 0F
    private var bottomLeftRadius: Float = 0F

    var isEnable = true

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
        val n = a.indexCount
        for (i in 0 until n) {
            val attr = a.getIndex(i)
            if (attr == R.styleable.NightShadowView_cornerTopLeftRadius) {
                topLeftRadius = a.getDimension(attr, 0F)
            }
            if (attr == R.styleable.NightShadowView_cornerTopRightRadius) {
                topRightRadius = a.getDimension(attr, 0F)
            }
            if (attr == R.styleable.NightShadowView_cornerBottomRightRadius) {
                bottomRightRadius = a.getDimension(attr, 0F)
            }
            if (attr == R.styleable.NightShadowView_cornerBottomLeftRadius) {
                bottomLeftRadius = a.getDimension(attr, 0F)
            }
            if (attr == R.styleable.NightShadowView_cornerRadius) {
                topLeftRadius = a.getDimension(attr, 0F)
                topRightRadius = a.getDimension(attr, 0F)
                bottomRightRadius = a.getDimension(attr, 0F)
                bottomLeftRadius = a.getDimension(attr, 0F)
                break
            }
        }
        a.recycle()
    }

    private fun setMode(flag: Boolean) {
        if (flag) {
            visibility = VISIBLE
            if (topLeftRadius == 0F && topRightRadius == 0F
                    && bottomRightRadius == 0F && bottomLeftRadius == 0F) {
                setBackgroundColor(Color.BLACK)
            } else {
                val shapeDrawable = GradientDrawable()
                shapeDrawable.cornerRadii = floatArrayOf(
                        topLeftRadius, topLeftRadius,
                        topRightRadius, topRightRadius,
                        bottomRightRadius, bottomRightRadius,
                        bottomLeftRadius, bottomLeftRadius
                )
                shapeDrawable.shape = GradientDrawable.RECTANGLE
                shapeDrawable.setColor(Color.BLACK)
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
        if (isEnable) {
            setMode(mode.mode == ThemeMode.NIGHT)
        }
    }
}