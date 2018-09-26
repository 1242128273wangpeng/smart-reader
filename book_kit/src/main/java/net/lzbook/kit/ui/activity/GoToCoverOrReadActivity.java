package net.lzbook.kit.ui.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;

import com.ding.basic.bean.Book;
import com.ding.basic.repository.RequestRepositoryFactory;

import net.lzbook.kit.app.base.BaseBookApplication;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.utils.StatServiceUtils;
import net.lzbook.kit.utils.router.BookRouter;


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

        Book book = (Book) intent.getSerializableExtra(Constants.REQUEST_ITEM);
        book = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(book.getBook_id());

        if (book != null) {
            BookRouter.INSTANCE.navigateCoverOrRead( this, book, BookRouter.NAVIGATE_TYPE_BOOKSHELF);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.cancel(getIntent().getIntExtra(Constants.NOTIFY_ID, Constants.DOWNLOAD - 1));
        }

        this.finish();
    }

    @Override
    protected void onDestroy() {
        StatServiceUtils.statAppBtnClick(GoToCoverOrReadActivity.this, StatServiceUtils.download_read);

        Book book = (Book) getIntent().getSerializableExtra(Constants.REQUEST_ITEM);
        book = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBook(book.getBook_id());

        if(book != null){
            BookRouter.INSTANCE.navigateCoverOrRead( this, book, BookRouter.NAVIGATE_TYPE_BOOKSHELF);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.cancel(getIntent().getIntExtra(Constants.NOTIFY_ID, Constants
                        .DOWNLOAD - 1));
            }
        }
        super.onDestroy();
    }
}
