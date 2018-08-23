package com.dingyueads.sdk.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Function：解决 Unable to instantiate receiver com.dingyueads.sdk.receiver.AlarmReceiver: java.lang.ClassNotFoundException
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {}

}