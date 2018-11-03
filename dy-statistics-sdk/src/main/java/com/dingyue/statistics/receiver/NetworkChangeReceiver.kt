package com.dingyue.statistics.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dingyue.statistics.common.CommonParams
import com.dingyue.statistics.utils.NetworkUtil

/**
 * Desc 网络状态改变监听
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/8/28 14:44
 */
class NetworkChangeReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {
        if ("android.net.conn.CONNECTIVITY_CHANGE" == intent.action) {
            // 检测到网络状态改变，重新获取网络状态
            CommonParams.network = NetworkUtil.getNetworkType(context)
        }
    }
}