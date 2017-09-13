package net.lzbook.kit.book.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import de.greenrobot.event.EventBus
import iyouqu.theme.EventThemeModeChange
import iyouqu.theme.ThemeHelper
import iyouqu.theme.ThemeMode
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.safeRegist
import net.lzbook.kit.utils.safeUnregist

/**
 * Created by xian on 2017/9/11.
 */
class NightShadowView : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        if (ThemeHelper.getInstance(context).isNight) {
            visibility = VISIBLE
            setBackgroundColor(Color.BLACK)
            alpha = Constants.NIGHT_SHADOW_ALPHA
        } else {
            visibility = GONE
        }


    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        EventBus.getDefault().safeRegist(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        EventBus.getDefault().safeUnregist(this)
    }

    fun onEventMainThread(mode: EventThemeModeChange) {
        when (mode.mode) {
            ThemeMode.NIGHT -> {
                visibility = VISIBLE
                setBackgroundColor(Color.BLACK)
                alpha = Constants.NIGHT_SHADOW_ALPHA
            }
            else -> {
                visibility = GONE
            }
        }
    }
}