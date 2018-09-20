package net.lzbook.kit.utils.oneclick

import net.lzbook.kit.utils.logger.AppLog

/**
 * Created by yuchao on 2017/12/25 0025.
 */
class OneClickUtil(val methodName: String) {
    private var lastClickTime: Long = 0

    fun check(): Boolean {
        val currentTime = System.currentTimeMillis()
        AppLog.e("diffvalue", "doCover diffvalue -----> " + (currentTime - lastClickTime))
        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
            lastClickTime = currentTime
            return false
        } else {
            return true
        }
    }

    companion object {
        val MIN_CLICK_DELAY_TIME = 800
    }
}
