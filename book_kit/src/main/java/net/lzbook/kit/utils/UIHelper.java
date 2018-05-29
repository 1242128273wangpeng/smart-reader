package net.lzbook.kit.utils;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.cache.DataCleanManager;
import net.lzbook.kit.data.db.BookDaoHelper;

import android.os.Message;

public class UIHelper {

    public static void clearAppCache() {
        Message msg = new Message();
        try {
            BookDaoHelper.getInstance().deleteAllBook();
            DataCleanManager.cleanInternalCache(BaseBookApplication.getGlobalContext());
            msg.what = 1;
        } catch (Exception e) {
            e.printStackTrace();
            msg.what = -1;
        }
    }
}