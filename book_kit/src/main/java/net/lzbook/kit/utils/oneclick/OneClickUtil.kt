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
        private var lastsClickTime: Long = 0

        /***
         * 防止按钮两次点击
         * **/
        fun isDoubleClick(time: Long): Boolean {
            val interval = time -lastsClickTime
            return if (interval > 800) {
                lastsClickTime = time
                false
            } else {
                true
            }
        }

        /***
         * 防止阅读页音量键按钮两次点击
         * **/
        fun isVolumDoubleClick(time: Long): Boolean {
            val interval = time -lastsClickTime
            return if (interval > 300) {
                lastsClickTime = time
                false
            } else {
                true
            }
        }
    }
}
