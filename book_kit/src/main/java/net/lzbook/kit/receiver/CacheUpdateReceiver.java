package net.lzbook.kit.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import net.lzbook.kit.R;
import net.lzbook.kit.app.ActionConstants;
import net.lzbook.kit.book.download.CacheManager;
import net.lzbook.kit.book.download.DownloadState;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.BookTask;
import net.lzbook.kit.utils.AppLog;

import java.util.List;

/**
 * Desc 下载广播接收器
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/8 0008 14:54
 */

public class CacheUpdateReceiver extends BroadcastReceiver {

    private Activity activity;
    MyDialog myDialog = null;
    MyDialog netDialog = null;

    public CacheUpdateReceiver(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, final Intent intent) {
        AppLog.e("CacheUpdateReceiver", "onReceive : " + intent.getAction());
        if (!activity.isFinishing()) {

            if (ActionConstants.ACTION_CACHE_STATUS_CHANGE.equals(intent.getAction())) {
//                onTaskStatusChange();
            } else if (ActionConstants.ACTION_CACHE_COMPLETE_WITH_ERR.equals(intent.getAction())) {
                if (myDialog != null && myDialog.isShowing()) {
                    myDialog.dismiss();
                }

                final Book book = (Book) intent.getSerializableExtra(Constants.REQUEST_ITEM);

                myDialog = new MyDialog(activity, R.layout.dialog_cache_complete_with_err,
                        Gravity.CENTER);

                TextView txt_go = (TextView) myDialog.findViewById(R.id.cache_gotosee);
                TextView txt_title = (TextView) myDialog.findViewById(R.id.dialog_book_name);


                txt_title.setText(
                        String.format(activity.getString(R.string.dialog_cache_complete_with_err),
                                book.name));

                txt_go.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myDialog.dismiss();
                    }
                });
                myDialog.setCanceledOnTouchOutside(true);
                if (!activity.isFinishing()) {
                    myDialog.show();
                }
            } else if (ActionConstants.ACTION_CACHE_WAIT_WIFI.equals(intent.getAction())) {
                if (netDialog != null && netDialog.isShowing()) {
                    return;
                }


                final List<BookTask> list = CacheManager.INSTANCE.pauseAll(
                        DownloadState.WAITTING_WIFI);
                if (list.size() > 0) {
                    netDialog = new MyDialog(activity, R.layout.dialog_confirm, Gravity.CENTER);
                    TextView txt_cancel = (TextView) netDialog.findViewById(R.id.txt_cancel);
                    TextView txt_confirm = (TextView) netDialog.findViewById(R.id.txt_confirm);
                    netDialog.setCanceledOnTouchOutside(false);
                    txt_cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            netDialog.dismiss();
                        }
                    });

                    txt_confirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            netDialog.dismiss();

                            for (BookTask task : list) {
                                CacheManager.INSTANCE.start(task.book_id, task.startSequence);
                            }
                        }
                    });

                    netDialog.show();
                }
            }
        }
    }
}