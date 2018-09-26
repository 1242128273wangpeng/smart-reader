package net.lzbook.kit.utils.book;

import com.ding.basic.bean.Book;
import com.ding.basic.bean.HistoryInfo;
import com.ding.basic.database.helper.BookDataProviderHelper;

import net.lzbook.kit.app.base.BaseBookApplication;


/**
 * Created by yuchao on 2017/6/20 0020.
 */

public class FootprintUtils {


    public static boolean saveHistoryData(HistoryInfo info) {
        BookDataProviderHelper bookDataHelper = BookDataProviderHelper.Companion.loadBookDataProviderHelper(BaseBookApplication.getGlobalContext());
        if (bookDataHelper.getHistoryCount() >= 200) {
            bookDataHelper.deleteSmallTimeHistory();
        }
        bookDataHelper.insertOrUpdateHistory(info);
        return true;
    }

    public static boolean saveHistoryShelf(Book book) {
        HistoryInfo info = new HistoryInfo();
        info.setName(book.getName());
        info.setBook_id(book.getBook_id());
        info.setBook_source_id(book.getBook_source_id());
        info.setLabel(book.getLabel());
        info.setAuthor(book.getAuthor());
        info.setChapter_count(book.getChapter_count());


        info.setImg_url(book.getImg_url());
        info.setHost(book.getHost());
        info.setStatus(book.getStatus());
        info.setDesc(book.getDesc());
        info.setBrowse_time(System.currentTimeMillis());

        return saveHistoryData(info);
    }
}
