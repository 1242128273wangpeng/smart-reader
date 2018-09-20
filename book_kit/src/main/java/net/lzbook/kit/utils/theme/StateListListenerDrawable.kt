package net.lzbook.kit.utils.theme

import android.graphics.drawable.StateListDrawable

/**
 * Created by Xian on 2018/3/3.
 */
class StateListListenerDrawable(val callback: (press: Boolean, enable: Boolean) -> Unit) : StateListDrawable() {
    @Override
    override fun isStateful(): Boolean = true

    @Override
    override fun setState(stateSet: IntArray?): Boolean {
        super.setState(stateSet)

        var press = false
        var enable = false

        stateSet?.forEach {
            press = if (it == android.R.attr.state_pressed) true else press
            enable = if (it == android.R.attr.state_enabled) true else enable
        }

        callback.invoke(press, enable)
        return true
    }
}