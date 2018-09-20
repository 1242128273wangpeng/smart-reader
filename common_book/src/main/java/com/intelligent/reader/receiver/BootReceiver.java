package com.intelligent.reader.receiver;

import net.lzbook.kit.service.CheckNovelUpdateService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent2 = new Intent(context, CheckNovelUpdateService.class);
        intent2.setAction(CheckNovelUpdateService.ACTION_CHKUPDATE);
        context.startService(intent2);
    }
}
