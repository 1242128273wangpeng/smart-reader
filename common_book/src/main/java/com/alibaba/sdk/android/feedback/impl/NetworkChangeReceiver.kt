package com.alibaba.sdk.android.feedback.impl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Function：解决 Unable to instantiate receiver com.alibaba.sdk.android.feedback.impl.NetworkChangeReceiver: java.lang.ClassNotFoundException
 *
 * Created by JoannChen on 2018/5/15 0015 15:38
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
class NetworkChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {}

}