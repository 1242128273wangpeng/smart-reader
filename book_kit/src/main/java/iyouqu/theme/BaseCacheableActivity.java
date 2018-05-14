package iyouqu.theme;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import net.lzbook.kit.app.ActionConstants;
import net.lzbook.kit.receiver.CacheUpdateReceiver;
import net.lzbook.kit.router.RouterConfig;
import net.lzbook.kit.router.RouterUtil;

import static net.lzbook.kit.utils.ExtensionsKt.loge;
import static net.lzbook.kit.utils.ExtensionsKt.msMainLooperHandler;

/**
 * Created by xian on 2017/4/24.
 */

public class BaseCacheableActivity extends FrameActivity {


    public static final String NEED_SPLASH = "NEED_SPLASH";
    protected BroadcastReceiver mCacheUpdateReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mCacheUpdateReceiver == null) {
            mCacheUpdateReceiver = new CacheUpdateReceiver(this);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

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

                            RouterUtil.INSTANCE.navigation(BaseCacheableActivity.this, RouterConfig.SPLASH_ACTIVITY, Intent.FLAG_ACTIVITY_CLEAR_TASK);
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

}
