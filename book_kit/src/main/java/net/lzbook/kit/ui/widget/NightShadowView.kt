package net.lzbook.kit.ui.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import net.lzbook.kit.R
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.theme.EventThemeModeChange
import net.lzbook.kit.utils.theme.ThemeHelper
import net.lzbook.kit.utils.theme.ThemeMode
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * Created by xian on 2017/9/11
 */
class NightShadowView : View {
    constructor(context: Context?) : super(context)

    private var topLeftRadius: Float = 0F
    private var topRightRadius: Float = 0F
    private var bottomRightRadius: Float = 0F
    private var bottomLeftRadius: Float = 0F
    private var resDrawable: Drawable? = null

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
            if (attr == R.styleable.NightShadowView_drawable) {
                resDrawable = a.getDrawable(attr)
            }
        }
        a.recycle()
    }

    private fun setMode(flag: Boolean) {
        if (flag) {
            visibility = VISIBLE
            if (resDrawable != null) {
                setBackgroundDrawable(resDrawable)
            } else {
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
            }
            alpha = Constants.NIGHT_SHADOW_ALPHA
        } else {
            visibility = GONE
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setMode(ThemeHelper.getInstance(context).isNight)
        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(mode: EventThemeModeChange) {
        if (isEnable) {
            setMode(mode.mode == ThemeMode.NIGHT)
        }
    }
}