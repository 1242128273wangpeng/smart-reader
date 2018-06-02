package com.intelligent.reader.receiver;

import com.ding.basic.bean.Book;
import com.ding.basic.repository.RequestRepositoryFactory;
import com.dy.reader.activity.ReaderActivity;
import com.intelligent.reader.activity.HomeActivity;

import net.lzbook.kit.app.BaseBookApplication;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

public class DownBookClickReceiver extends BroadcastReceiver {
    public static final String action = "cn.txtzsydsq.reader.receiver.CLICK_DOWN_BOOK";

    @Override
    public void onReceive(Context ctt, Intent paramIntent) {
        boolean isStart = false;
        if (paramIntent != null) {
            ActivityManager am = (ActivityManager) ctt.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningTaskInfo taskInfo : am.getRunningTasks(Integer.MAX_VALUE)) {
                if (HomeActivity.class.getName().equals(taskInfo.baseActivity
                        .getClassName())) {
                    isStart = true;
                    break;
                }
            }
            if (!isStart) {
                String book_id = paramIntent.getStringExtra("book_id");
                if ((RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(book_id) != null)) {
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.setClass(ctt, ReaderActivity.class);
                    if (!TextUtils.isEmpty(book_id)) {
                        Book book = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(book_id);
                        Bundle bundle = new Bundle();
                        bundle.putInt("sequence", book.getSequence());
                        bundle.putInt("offset", book.getOffset());
                        bundle.putSerializable("book", book);
                        intent.putExtras(bundle);
                        ctt.startActivity(intent);
                    }
                }
            } else {
                String book_id = paramIntent.getStringExtra("book_id");
                if ((RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).checkBookSubscribe(book_id) != null)) {
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.setClass(ctt, ReaderActivity.class);
                    if (!TextUtils.isEmpty(book_id)) {
                        Book book = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(book_id);
                        Bundle bundle = new Bundle();
                        bundle.putInt("sequence", book.getSequence());
                        bundle.putInt("offset", book.getOffset());
                        bundle.putSerializable("book", book);
                        intent.putExtras(bundle);
                        ctt.startActivity(intent);
                    }
                } else {
                    Toast.makeText(ctt, "资源已删除", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}