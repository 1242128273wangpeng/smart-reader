package com.intelligent.reader.read.help;

import com.intelligent.reader.activity.CataloguesActivity;
import com.intelligent.reader.activity.CoverPageActivity;
import com.intelligent.reader.activity.ReadingActivity;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.data.db.BookChapterDao;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.data.ormlite.bean.HistoryInfo;
import net.lzbook.kit.request.DataCache;
import net.lzbook.kit.utils.BaseBookHelper;
import net.lzbook.kit.utils.FootprintUtils;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.StatServiceUtils;
import net.lzbook.kit.utils.oneclick.AntiShake;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/8 0008.
 */
public class BookHelper extends BaseBookHelper {

    private static AntiShake shake = new AntiShake();

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

                if ((book.sequence > -1 || book.readed == 1 || (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE && isCached( book))) && BookDaoHelper.getInstance().isBookSubed(book.book_id)) {
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
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
     * type 打点需求，0 从书架页进入  1 从缓存管理页进入  2 从书籍详情页推荐页进入 3 从阅读页进入
     * <p/>
     * ctx
     * book
     */
    public static void goToCoverOrRead(Context ctx, Activity activity, Book book, int type) {

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
                } else if ((book.sequence > -1 || book.readed == 1 || (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE && isCached( book))) && BookDaoHelper.getInstance().isBookSubed(book.book_id)) {

                    requestItem.fromType = 0;
                    FootprintUtils.saveHistoryShelf(book);
                    Intent intent = new Intent(ctx, ReadingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("sequence", book.sequence);
                    bundle.putInt("offset", book.offset);
                    bundle.putSerializable("book", book);
                    bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
                    intent.putExtras(bundle);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    if (Constants.QG_SOURCE.equals(book.site)) {
                        requestItem.channel_code = 1;
                    } else {
                        requestItem.channel_code = 2;
                    }
                    activity.startActivity(intent);
                    net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(activity, net.lzbook.kit.utils.StatServiceUtils.bs_click_one_book);
                } else {

                    if (shake.check()){
                        return;
                    }

                    Map<String, String> data = new HashMap<>();
                    data.put("BOOKID", book.book_id);
                    if (type == 0) {
                        data.put("source", "SHELF");
                    } else if (type == 1) {
                        data.put("source", "CACHEMANAGE");
                    } else if (type == 2) {
                        data.put("source", "BOOOKDETAIL");
                    } else if (type == 3) {
                        data.put("source", "READPAGE");
                    }
                    StartLogClickUtil.upLoadEventLog(ctx, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.ENTER, data);



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

    private static boolean isCached(Book book) {

        int index = Math.max(0, book.sequence);
        BookChapterDao bookChapterDao = new BookChapterDao(BaseBookApplication.getGlobalContext(), book.book_id);
        Chapter chapterBySequence = bookChapterDao.getChapterBySequence(index);
        if(chapterBySequence == null){
            return false;
        }
        if(Constants.QG_SOURCE.equals(book.site)) {
            return com.quduquxie.network.DataCache.isChapterExists(chapterBySequence.chapter_id, book.book_id);
        }else{
            return DataCache.isChapterExists(chapterBySequence);
        }
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
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
