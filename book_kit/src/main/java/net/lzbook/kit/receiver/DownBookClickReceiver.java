package net.lzbook.kit.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.alibaba.android.arouter.launcher.ARouter;
import com.ding.basic.bean.Book;
import com.ding.basic.RequestRepositoryFactory;;

import net.lzbook.kit.app.base.BaseBookApplication;
import net.lzbook.kit.utils.router.RouterConfig;
import net.lzbook.kit.utils.router.RouterUtil;

public class DownBookClickReceiver extends BroadcastReceiver {
    public static final String action = "cn.txtzsydsq.reader.receiver.CLICK_DOWN_BOOK";

    @Override
    public void onReceive(Context ctt, Intent paramIntent) {
        boolean isStart = false;
        if (paramIntent != null) {
            ActivityManager am = (ActivityManager) ctt.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningTaskInfo taskInfo : am.getRunningTasks(Integer.MAX_VALUE)) {
                if ((ARouter.getInstance().build(RouterConfig.HOME_ACTIVITY).getDestination().getName()).equals(taskInfo.baseActivity
                        .getClassName())) {
                    isStart = true;
                    break;
                }
            }
            if (!isStart) {
                String book_id = paramIntent.getStringExtra("book_id");
                if ((RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(book_id) != null)) {
                    if (!TextUtils.isEmpty(book_id)) {
                        Book book = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(book_id);
                        Bundle bundle = new Bundle();
                        bundle.putInt("sequence", book.getSequence());
                        bundle.putInt("offset", book.getOffset());
                        bundle.putSerializable("book", book);
                        RouterUtil.INSTANCE.navigation(ctt, RouterConfig.READER_ACTIVITY,bundle,(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    }
                }
            } else {
                String book_id = paramIntent.getStringExtra("book_id");
                if ((RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(book_id) != null)) {
                    if (!TextUtils.isEmpty(book_id)) {
                        Book book = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(book_id);
                        Bundle bundle = new Bundle();
                        bundle.putInt("sequence", book.getSequence());
                        bundle.putInt("offset", book.getOffset());
                        bundle.putSerializable("book", book);
                        RouterUtil.INSTANCE.navigation(ctt, RouterConfig.READER_ACTIVITY,bundle,(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    }
                } else {
                    Toast.makeText(ctt, "资源已删除", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}