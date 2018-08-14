package net.lzbook.kit.utils.topsnackbar

import android.support.annotation.StringRes
import android.view.View
import swipeback.ActivityLifecycleHelper

/**
 * Created by yuchao on 2018/5/30 0030.
 */

object TopSnackBarUtils{

    fun show(str: String?) {
        str?.let {
            val v = ActivityLifecycleHelper.getLatestActivity()?.findViewById<View>(android.R.id.content)
            if (v != null)
                TopSnackBar.make(v, it, BaseTransientBottomBar.LENGTH_LONG).show()
        }
    }

    fun show(@StringRes id: Int) {
        val v = ActivityLifecycleHelper.getLatestActivity()?.findViewById<View>(android.R.id.content)
        if (v != null)
            TopSnackBar.make(v, id, BaseTransientBottomBar.LENGTH_LONG).show()
    }
}
