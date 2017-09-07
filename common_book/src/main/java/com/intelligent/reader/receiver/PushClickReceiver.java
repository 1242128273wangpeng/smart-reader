package com.intelligent.reader.receiver;

import com.intelligent.reader.activity.HomeActivity;

import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.CheckNovelUpdHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PushClickReceiver extends BroadcastReceiver {
    private String TAG = "PushClickReceiver";

    @Override
    public void onReceive(Context ctt, Intent paramIntent) {
        AppLog.d(TAG, "onReceive --- > ");
        CheckNovelUpdHelper.delLocalNotify(ctt);
        ctt.startActivity(new Intent(ctt, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
