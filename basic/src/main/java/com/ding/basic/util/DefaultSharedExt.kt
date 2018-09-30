package com.ding.basic.util

import java.util.*

/**
 * Desc 系统默认 SharedPreference
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/9/3 16:25
 */


fun isSameDay(lastTime: Long, currentTime: Long):Boolean {
    val pre = Calendar.getInstance()
    val predate = Date(currentTime)
    pre.time = predate

    val cal = Calendar.getInstance()
    val date = Date(lastTime)
    cal.time = date;

    if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
        val sameDay = cal.get(Calendar.DAY_OF_YEAR) - pre.get(Calendar.DAY_OF_YEAR);
        if (sameDay == 0) {
            return true
        }
    }
    return false
}