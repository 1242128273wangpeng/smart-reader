package net.lzbook.kit.book.component.service;

import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.DeleteBookHelper;

import android.app.IntentService;
import android.content.Intent;

public class DeleteBookIntentService extends IntentService {

    public static final String ACTION_DO_DELETE = AppUtils.getPackageName() + ".book.action_delete_book";
    String TAG = "DeleteBookIntentService";
    private DeleteBookHelper helper;

    public DeleteBookIntentService() {
        super("DeleteBookIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        helper = new DeleteBookHelper(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    public void onDestroy() {
        super.onDestroy();
        helper.doRemoveCallbackHelper();
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            AppLog.d(TAG, "intent.getAction() = " + intent.getAction());
            if (ACTION_DO_DELETE.equals(intent.getAction())) {
                helper.doDeleteHelper();
            }
        }
    }
}