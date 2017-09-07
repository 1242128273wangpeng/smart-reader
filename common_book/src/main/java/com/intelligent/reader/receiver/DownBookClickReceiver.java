package com.intelligent.reader.receiver;

import com.intelligent.reader.activity.HomeActivity;
import com.intelligent.reader.activity.ReadingActivity;

import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.db.BookDaoHelper;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
                int gid = paramIntent.getIntExtra("gid", 0);
                BookDaoHelper mBookDaoHelper = BookDaoHelper.getInstance(ctt);
                if (mBookDaoHelper != null && mBookDaoHelper.isBookSubed(gid)) {
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(ctt, ReadingActivity.class);
                    if (gid != 0) {
                        Book book = BookDaoHelper.getInstance(ctt).getBook(gid, 0);
                        Bundle bundle = new Bundle();
                        bundle.putInt("sequence", book.sequence);
                        bundle.putInt("offset", book.offset);
                        bundle.putSerializable("book", book);
                        bundle.putSerializable("nid", book.nid);
                        intent.putExtras(bundle);
                        ctt.startActivity(intent);
                    }
                }
            } else {
                int gid = paramIntent.getIntExtra("gid", 0);
                BookDaoHelper mBookDaoHelper = BookDaoHelper.getInstance(ctt);
                if (mBookDaoHelper != null && mBookDaoHelper.isBookSubed(gid)) {
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(ctt, ReadingActivity.class);
                    if (gid != 0) {
                        Book book = (Book) BookDaoHelper.getInstance(ctt).getBook(gid, 0);
                        Bundle bundle = new Bundle();
                        bundle.putInt("sequence", book.sequence);
                        bundle.putInt("offset", book.offset);
                        bundle.putSerializable("book", book);
                        bundle.putSerializable("nid", book.nid);
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
