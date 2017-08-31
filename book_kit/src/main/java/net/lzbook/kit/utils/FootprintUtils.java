package net.lzbook.kit.utils;

import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.db.table.HistoryInforTable;
import net.lzbook.kit.data.ormlite.bean.HistoryInfo;
import net.lzbook.kit.data.ormlite.dao.DaoUtils;

import java.sql.SQLException;

/**
 * Created by yuchao on 2017/6/20 0020.
 */

public class FootprintUtils {


    public static boolean saveHistoryData(HistoryInfo info){
        try {
            DaoUtils daoUtils = new DaoUtils(HistoryInfo.class);
            if (daoUtils.countOf() >= 200){
                    daoUtils.deleteMiniData(HistoryInforTable.TABLE_NAME, HistoryInforTable.LAST_BROW_TIME);
            }
            daoUtils.createOrUpdate(info);
//            if (daoUtils.isBookExist(info.getBook_id())){
//                daoUtils.update(info);
//            }else {
//                if (daoUtils.countOf() >= 200){
//                    daoUtils.deleteMiniData(HistoryInforTable.TABLE_NAME, HistoryInforTable.LAST_BROW_TIME);
//                }
//                daoUtils.insert(info);
//            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean saveHistoryShelf(Book book){
        HistoryInfo info = new HistoryInfo();
        info.setName(book.name);
        info.setBook_id(book.book_id);
        info.setBook_source_id(book.book_source_id);
        info.setCategory(book.category);
        info.setAuthor(book.author);
        info.setChapter_count(book.chapter_count);
        info.setLast_chapter_name(book.last_chapter_name);
        info.setImg_url(book.img_url);
        info.setSite(book.site);
        info.setStatus(book.status);
        info.setLast_brow_time(System.currentTimeMillis());

        return saveHistoryData(info);
    }
}
