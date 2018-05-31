package com.intelligent.reader.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ding.basic.bean.Book;
import com.intelligent.reader.widget.DownloadFinishErrorDialog;
import com.intelligent.reader.widget.DownloadWaitingWifiDialog;

import net.lzbook.kit.app.ActionConstants;
import net.lzbook.kit.book.download.CacheManager;
import net.lzbook.kit.book.download.DownloadState;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.BookTask;
import net.lzbook.kit.utils.AppLog;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * Desc 下载 广播接收器
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/8 0008 14:54
 */

public class CacheUpdateReceiver extends BroadcastReceiver {

    private Activity activity;

    private DownloadFinishErrorDialog finishErrorDialog;
    private DownloadWaitingWifiDialog waitingWifiDialog;

    public CacheUpdateReceiver(Activity activity) {
        this.activity = activity;
        finishErrorDialog = new DownloadFinishErrorDialog(activity);
        waitingWifiDialog = new DownloadWaitingWifiDialog(activity);
        waitingWifiDialog.setOnConfirmListener(new Function1<List<? extends BookTask>, Unit>() {
            @Override
            public Unit invoke(List<? extends BookTask> bookTasks) {
                for (BookTask task : bookTasks) {
                    CacheManager.INSTANCE.start(task.book_id, task.startSequence);
                }
                return null;
            }
        });
    }

    @Override
    public void onReceive(Context context, final Intent intent) {
        AppLog.e("CacheUpdateReceiver", "onReceive : " + intent.getAction());

        if (ActionConstants.ACTION_CACHE_STATUS_CHANGE.equals(intent.getAction())) {
//                onTaskStatusChange();
        } else if (ActionConstants.ACTION_CACHE_COMPLETE_WITH_ERR.equals(intent.getAction())) {
            final Book book = (Book) intent.getSerializableExtra(Constants.REQUEST_ITEM);
            if (!activity.isFinishing() && finishErrorDialog != null && !finishErrorDialog.isShowing()) {
                finishErrorDialog.show(book);
            }

        } else if (ActionConstants.ACTION_CACHE_WAIT_WIFI.equals(intent.getAction())) {
            final List<BookTask> list = CacheManager.INSTANCE.pauseAll(DownloadState.WAITTING_WIFI);
            if (!activity.isFinishing() && list.size() > 0 && waitingWifiDialog != null && !waitingWifiDialog.isShowing()) {
                waitingWifiDialog.show(list);
            }
        }
    }

}