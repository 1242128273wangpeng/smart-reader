package net.lzbook.kit.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import net.lzbook.kit.bean.OfflineDownloadEvent;

import org.greenrobot.eventbus.EventBus;

public class OffLineDownLoadReceiver extends BroadcastReceiver {

    public static final String action = "cn.txtzsydsq.reader.offlinedownload";
    private Context mContext;
    private OffLineDownLoadReceiver receiver;

    public OffLineDownLoadReceiver(Context context) {
        this.mContext = context;
        this.receiver = this;
    }

    public void registerAction() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(action);
        mContext.registerReceiver(receiver, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(action)) {
//            if (context instanceof CataloguesActivity) {
//                ((CataloguesActivity) mContext).notifyChangeDownLoad();
//            }
            EventBus.getDefault().post(new OfflineDownloadEvent());
        }
    }
}
