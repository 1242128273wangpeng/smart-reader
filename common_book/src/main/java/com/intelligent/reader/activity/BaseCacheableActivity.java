package com.intelligent.reader.activity;

import com.intelligent.reader.R;

import net.lzbook.kit.app.ActionConstants;
import net.lzbook.kit.book.download.CacheManager;
import net.lzbook.kit.book.download.DownloadState;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.BookTask;
import net.lzbook.kit.utils.AppLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import iyouqu.theme.FrameActivity;

import static net.lzbook.kit.utils.ExtensionsKt.loge;
import static net.lzbook.kit.utils.ExtensionsKt.msMainLooperHandler;

/**
 * Created by xian on 2017/4/24.
 */

public class BaseCacheableActivity extends FrameActivity {


    public static final String NEED_SPLASH = "NEED_SPLASH";
    protected BroadcastReceiver mCacheUpdateReceiver;
    private MyDialog netDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mCacheUpdateReceiver == null) {
            mCacheUpdateReceiver = new CacheUpdateReceiver();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (shouldReceiveCacheEvent()) {
            if (mCacheUpdateReceiver == null) {
                mCacheUpdateReceiver = new CacheUpdateReceiver();
            }
            registerCacheReceiver(mCacheUpdateReceiver);
        }
    }

    public boolean shouldReceiveCacheEvent() {
        return true;
    }

    public void onTaskStatusChange(){

    }

    public void registerCacheReceiver(BroadcastReceiver receiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActionConstants.ACTION_CACHE_COMPLETE_WITH_ERR);
        intentFilter.addAction(ActionConstants.ACTION_CACHE_WAIT_WIFI);
        intentFilter.addAction(ActionConstants.ACTION_CACHE_STATUS_CHANGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (shouldReceiveCacheEvent()) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mCacheUpdateReceiver);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.getBoolean(NEED_SPLASH)) {

            loge("NEED_SPLASH : " + this.getClass().getSimpleName());
            msMainLooperHandler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {

                            finish();

                            Intent intent = new Intent(BaseCacheableActivity.this, SplashActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }
                    , 500);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(NEED_SPLASH, true);
    }

    class CacheUpdateReceiver extends BroadcastReceiver {

        MyDialog myDialog = null;
        
        @Override
        public void onReceive(Context context, final Intent intent) {
            AppLog.e("CacheUpdateReceiver", "onReceive : " + intent.getAction());
            if (!BaseCacheableActivity.this.isFinishing()) {

                if(ActionConstants.ACTION_CACHE_STATUS_CHANGE.equals(intent.getAction())) {
                    onTaskStatusChange();
                }else if (ActionConstants.ACTION_CACHE_COMPLETE_WITH_ERR.equals(intent.getAction())) {
                    if (myDialog != null && myDialog.isShowing()) {
                        myDialog.dismiss();
                    }

                    final Book book = (Book) intent.getSerializableExtra(Constants.REQUEST_ITEM);

                    myDialog = new MyDialog(BaseCacheableActivity.this, R.layout.dialog_cache_complete_with_err, Gravity.CENTER);

                    TextView txt_go = (TextView) myDialog.findViewById(R.id.cache_gotosee);
                    TextView txt_title = (TextView) myDialog.findViewById(R.id.dialog_book_name);


                    txt_title.setText(String.format(BaseCacheableActivity.this.getString(R.string.dialog_cache_complete_with_err), book.name));

                    txt_go.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            myDialog.dismiss();
                        }
                    });
                    myDialog.setCanceledOnTouchOutside(true);
                    if (!isFinishing()) {
                        myDialog.show();
                    }
                }else if(ActionConstants.ACTION_CACHE_WAIT_WIFI.equals(intent.getAction())){
                    if(netDialog != null && netDialog.isShowing()){
                        return;
                    }


                    final List<BookTask> list = CacheManager.INSTANCE.pauseAll(DownloadState.WAITTING_WIFI);
                    if(list.size() > 0) {
                        netDialog = new MyDialog(BaseCacheableActivity.this, R.layout.dialog_confirm, Gravity.CENTER);
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
}
