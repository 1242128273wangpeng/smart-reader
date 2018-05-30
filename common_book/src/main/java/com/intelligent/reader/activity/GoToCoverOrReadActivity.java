package com.intelligent.reader.activity;

import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.utils.StatServiceUtils;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;


public class GoToCoverOrReadActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null) {
            this.finish();
            return;
        }

        System.err.println("GoToCoverOrReadActivity onCreate");
        this.finish();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        System.err.println("GoToCoverOrReadActivity onRestart");

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        System.err.println("GoToCoverOrReadActivity onNewIntent");

        StatServiceUtils.statAppBtnClick(GoToCoverOrReadActivity.this, StatServiceUtils.download_read);
        BookDaoHelper helper = BookDaoHelper.getInstance();

        Book book = (Book) intent.getSerializableExtra(Constants.REQUEST_ITEM);
        book = helper.getBook(book.book_id, 0);
        System.err.println("GoToCoverOrReadActivity helper book : " + book);
        BookHelper.goToCatalogOrRead(this, this, book);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(getIntent().getIntExtra(Constants.NOTIFY_ID, Constants
                .DOWNLOAD - 1));
        this.finish();
    }

    @Override
    protected void onDestroy() {
        System.err.println("GoToCoverOrReadActivity onDestroy");
        StatServiceUtils.statAppBtnClick(GoToCoverOrReadActivity.this, StatServiceUtils.download_read);
        BookDaoHelper helper = BookDaoHelper.getInstance();

        Book book = (Book) getIntent().getSerializableExtra(Constants.REQUEST_ITEM);
        book = helper.getBook(book.book_id, 0);
        System.err.println("GoToCoverOrReadActivity helper book : " + book);
        BookHelper.goToCatalogOrRead(this, this, book);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(getIntent().getIntExtra(Constants.NOTIFY_ID, Constants
                .DOWNLOAD - 1));
        super.onDestroy();
    }
}
