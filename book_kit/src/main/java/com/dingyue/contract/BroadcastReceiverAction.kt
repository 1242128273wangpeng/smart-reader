package com.dingyue.contract

import net.lzbook.kit.utils.AppUtils

/**
 * Desc 定义BroadcastReceiver中的Action
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/9 10:45
 */
object BroadcastReceiverAction {

    val PACKAGE_NAME = AppUtils.getPackageName()

    val ACTION_UPDATE_NOTIFY = "$PACKAGE_NAME.update_notify"

}