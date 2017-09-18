package com.intelligent.reader.read.help;

import com.intelligent.reader.activity.CataloguesActivity;
import com.intelligent.reader.activity.CoverPageActivity;
import com.intelligent.reader.activity.ReadingActivity;

import net.lzbook.kit.book.download.DownloadState;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.data.ormlite.bean.HistoryInfo;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.BaseBookHelper;
import net.lzbook.kit.utils.FootprintUtils;
import net.lzbook.kit.utils.StatServiceUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Administrator on 2016/8/8 0008.
 */
public class BookHelper extends BaseBookHelper {

    public static void goToCatalogOrRead(Context ctx, Activity activity, Book book) {

        switch (book.book_type) {
            case Book.TYPE_ONLINE:
                // 逻辑已经改 这里修改更新状态为false
                Book updateBook = new Book();
                updateBook.book_id = book.book_id;
                updateBook.book_source_id = book.book_source_id;
                updateBook.update_status = 0;
                updateBook.book_type = 0;
                updateBook.dex = book.dex;
                updateBook.initialization_status = book.initialization_status;
                BookDaoHelper.getInstance().updateBook(updateBook);
                RequestItem requestItem = new RequestItem();
                requestItem.book_id = book.book_id;
                requestItem.book_source_id = book.book_source_id;
                requestItem.host = book.site;
                requestItem.name = book.name;
                requestItem.author = book.author;
                requestItem.dex = book.dex;
                requestItem.parameter = book.parameter;
                requestItem.extra_parameter = book.extra_parameter;

                if ((book.sequence > -1 || book.readed == 1 || isDownFnish(ctx, book)) && BookDaoHelper.getInstance().isBookSubed(book.book_id)) {
                    AppLog.i("DownloadState---", "goToCoverOrRead " + isDownFnish(ctx, book));
                    requestItem.fromType = 0;
                    if (Constants.QG_SOURCE.equals(book.site)) {
                        requestItem.channel_code = 1;
                    } else {
                        requestItem.channel_code = 2;
                    }
                    Intent intent = new Intent(ctx, ReadingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("sequence", book.sequence);
                    bundle.putInt("offset", book.offset);
                    bundle.putSerializable("book", book);
                    bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
                    intent.putExtras(bundle);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    activity.startActivity(intent);
                    net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(activity, net.lzbook.kit.utils.StatServiceUtils.bs_click_one_book);

                } else {
                    BookDaoHelper bookDaoHelper = BookDaoHelper.getInstance();
                    book.initialization_status = 0;
                    bookDaoHelper.updateBookNew(book);
                    //跳转到目录页
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("cover", book);
                    bundle.putInt("sequence", book.sequence);
                    bundle.putBoolean("fromCover", true);
                    bundle.putBoolean("is_last_chapter", false);
                    bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
                    intent.setClass(ctx, CataloguesActivity.class);
                    intent.putExtras(bundle);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    activity.startActivity(intent);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 跳转小说封面或者小说阅读页
     * <p/>
     * ctx
     * book
     */
    public static void goToCoverOrRead(Context ctx, Activity activity, Book book) {

        switch (book.book_type) {
            case Book.TYPE_ONLINE:
                // 逻辑已经改 这里修改更新状态为false
                Book updateBook = new Book();
                updateBook.book_id = book.book_id;
                updateBook.book_source_id = book.book_source_id;
                updateBook.update_status = 0;
                updateBook.book_type = 0;
                updateBook.dex = book.dex;
                updateBook.initialization_status = book.initialization_status;
                BookDaoHelper.getInstance().updateBook(updateBook);
                RequestItem requestItem = new RequestItem();
                requestItem.book_id = book.book_id;
                requestItem.book_source_id = book.book_source_id;
                requestItem.host = book.site;
                requestItem.name = book.name;
                requestItem.author = book.author;
                requestItem.dex = book.dex;
                requestItem.parameter = book.parameter;
                requestItem.extra_parameter = book.extra_parameter;
                if (book.readed == -2 || book.initialization_status == 5) {
                    BookDaoHelper bookDaoHelper = BookDaoHelper.getInstance();
                    book.initialization_status = 0;
                    bookDaoHelper.updateBookNew(book);
                    //跳转到目录页
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("cover", book);
                    bundle.putInt("sequence", book.sequence);
                    bundle.putBoolean("fromCover", true);
                    bundle.putBoolean("is_last_chapter", false);
                    bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
                    intent.setClass(ctx, CataloguesActivity.class);
                    intent.putExtras(bundle);
                    activity.startActivity(intent);
                } else if ((book.sequence > -1 || book.readed == 1 || isDownFnish(ctx, book)) && BookDaoHelper.getInstance().isBookSubed(book.book_id)) {
                    AppLog.i("DownloadState---", "goToCoverOrRead " + isDownFnish(ctx, book));

                    requestItem.fromType = 0;
                    FootprintUtils.saveHistoryShelf(book);
                    Intent intent = new Intent(ctx, ReadingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("sequence", book.sequence);
                    bundle.putInt("offset", book.offset);
                    bundle.putSerializable("book", book);
                    bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
                    intent.putExtras(bundle);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    if (Constants.QG_SOURCE.equals(book.site)) {
                        requestItem.channel_code = 1;
                    } else {
                        requestItem.channel_code = 2;
                    }
                    activity.startActivity(intent);
                    net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(activity, net.lzbook.kit.utils.StatServiceUtils.bs_click_one_book);
                } else {
                    Intent intent = new Intent();
                    intent.setClass(ctx, CoverPageActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
                    intent.putExtras(bundle);
                    activity.startActivity(intent);
                }
                break;
            default:
                break;
        }
    }

    public static boolean isDownFnish(Context ctx, Book book) {

        DownloadState state = BookHelper.getInitDownstate(ctx, book, BookHelper.getStartDownIndex(ctx, book));
        return state == DownloadState.FINISH;
    }

    /**
     * 跳转小说封面或者小说阅读页
     * <p/>
     * ctx
     * book
     */
    public static void goToRead(Context context, Book book) {
        Book currentBook = new Book();
        currentBook.book_id = book.book_id;
        currentBook.update_status = 0;
        currentBook.book_type = 0;
        currentBook.dex = book.dex;
        BookDaoHelper.getInstance().updateBook(currentBook);
        Intent intent = new Intent(context, ReadingActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("sequence", book.sequence);
        bundle.putInt("offset", book.offset);
        bundle.putSerializable("book", book);
        bundle.putSerializable(Constants.REQUEST_ITEM, getRequestItem(book));
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void goToCover(Activity activity, HistoryInfo info) {

        RequestItem requestItem = new RequestItem();
        requestItem.book_id = info.getBook_id();
        requestItem.book_source_id = info.getBook_source_id();
        requestItem.host = info.getSite();
        requestItem.name = info.getName();
        requestItem.author = info.getAuthor();

        Intent intent = new Intent();
        intent.setClass(activity, CoverPageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
        intent.putExtras(bundle);
        activity.startActivity(intent);
        StatServiceUtils.statAppBtnClick(activity, StatServiceUtils.cover_into_his);
    }
}
