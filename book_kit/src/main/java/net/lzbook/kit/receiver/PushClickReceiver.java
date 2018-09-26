package net.lzbook.kit.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.lzbook.kit.utils.book.CheckNovelUpdHelper;
import net.lzbook.kit.utils.logger.AppLog;
import net.lzbook.kit.utils.router.RouterConfig;
import net.lzbook.kit.utils.router.RouterUtil;

public class PushClickReceiver extends BroadcastReceiver {
    private String TAG = "PushClickReceiver";

    @Override
    public void onReceive(Context ctt, Intent paramIntent) {
        AppLog.d(TAG, "onReceive --- > ");
        CheckNovelUpdHelper.delLocalNotify(ctt);
        RouterUtil.INSTANCE.navigation(ctt, RouterConfig.HOME_ACTIVITY,Intent.FLAG_ACTIVITY_NEW_TASK);
    }
}
