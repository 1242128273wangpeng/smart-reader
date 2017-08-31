package net.lzbook.kit.utils;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.cache.DataCleanManager;
import net.lzbook.kit.data.db.BookDaoHelper;

import android.os.Message;

public class UIHelper {

    public static void clearAppCache() {
//        final Handler handler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                switch (msg.what) {
//                    case 1:
//                        ToastUtils.showToastNoRepeat("缓存清除成功");
//                        break;
//                    case -1:
//                        ToastUtils.showToastNoRepeat("缓存清除失败");
//                        break;
//                }
//            }
//        };
//        new Thread() {
//            @Override
//            public void run() {
                Message msg = new Message();
                try {
                    BookDaoHelper.getInstance(BaseBookApplication.getGlobalContext()).deleteAllBook();
                    DataCleanManager.cleanInternalCache(BaseBookApplication.getGlobalContext());



                    msg.what = 1;
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.what = -1;
                }
//                handler.sendMessage(msg);
//            }
//        }.start();
    }
}