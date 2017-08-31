package com.intelligent.reader.activity;

import com.intelligent.reader.R;
import com.intelligent.reader.read.help.BookHelper;

import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.utils.AppLog;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import iyouqu.theme.FrameActivity;

import static net.lzbook.kit.utils.ExtensionsKt.loge;
import static net.lzbook.kit.utils.ExtensionsKt.msMainLooperHandler;

/**
 * Created by xian on 2017/4/24.
 */

public class BaseCacheableActivity extends FrameActivity {
    public static final String ACTION_CACHE_START = "ACTION_CACHE_START";
    public static final String ACTION_CACHE_COMPLETE = "ACTION_CACHE_COMPLETE";


    public static final String NEED_SPLASH = "NEED_SPLASH";

    class CacheUpdateReceiver extends BroadcastReceiver {

        private final WeakReference<Activity> mActivityWeakReference;
        MyDialog myDialog = null;

        public CacheUpdateReceiver(Activity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(Context context, final Intent intent) {
            AppLog.e("CacheUpdateReceiver", "onReceive");
            if (mActivityWeakReference.get() != null) {

                final Book book = (Book) intent.getSerializableExtra(Constants.REQUEST_ITEM);

                if (intent.getAction().equalsIgnoreCase(ACTION_CACHE_COMPLETE)) {
                    if (myDialog != null && myDialog.isShowing()) {
                        myDialog.dismiss();
                    }

                    myDialog = new MyDialog(mActivityWeakReference.get(), R.layout.dialog_cache_complete, Gravity.CENTER);
                    TextView txt_cancel = (TextView) myDialog.findViewById(R.id.cache_cancel);
                    TextView txt_go = (TextView) myDialog.findViewById(R.id.cache_gotosee);
                    TextView txt_title = (TextView) myDialog.findViewById(R.id.dialog_top_title);

                    CharSequence text = mActivityWeakReference.get().getResources().getText(R.string.dialog_cache_complete);

                    txt_title.setText(book.name + " " + text);

                    txt_cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            myDialog.dismiss();
                        }
                    });
                    txt_go.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            myDialog.dismiss();
                            System.err.println("requestItem : " + book);
                            BookHelper.goToCatalogOrRead(v.getContext(), mActivityWeakReference.get(), book);

                        }
                    });
                    myDialog.setCanceledOnTouchOutside(true);
                    myDialog.show();

                }
            }
        }

    }


    protected BroadcastReceiver mCacheUpdateReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mCacheUpdateReceiver == null) {
            mCacheUpdateReceiver = new CacheUpdateReceiver(this);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (shouldReceiveCacheEvent()) {
            if (mCacheUpdateReceiver == null) {
                mCacheUpdateReceiver = new CacheUpdateReceiver(this);
            }
            registerCacheReceiver(mCacheUpdateReceiver);
        }
    }

    public boolean shouldReceiveCacheEvent() {
        return true;
    }

    public void registerCacheReceiver(BroadcastReceiver receiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CACHE_COMPLETE);
        intentFilter.addAction(ACTION_CACHE_START);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();

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
            ,500);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(NEED_SPLASH, true);
    }
}
