package net.lzbook.kit.utils;

import com.quduquxie.Constants;
import com.quduquxie.bean.Book;
import com.quduquxie.bean.Chapter;

import net.lzbook.kit.data.bean.BookUpdate;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/11/4 0004.
 */
public class BeanParser {

    /**
     * 自有数据类型转化为青果数据类型
     */
    public static Chapter parseToQGBean(net.lzbook.kit.data.bean.Chapter chapter) {
        if (chapter == null) {
            return null;
        }
        Chapter qgChapter = new Chapter();
        qgChapter.content = chapter.content;
        qgChapter.isSuccess = chapter.isSuccess;
        qgChapter.create_time = chapter.time;
        qgChapter.id_book = chapter.book_id;
        qgChapter.id_chapter = chapter.chapter_id;
        qgChapter.name = chapter.chapter_name;
        qgChapter.serial_number = chapter.sequence + 1;
        AppLog.e("parseToQGBean", "serial_number=" + qgChapter.serial_number);
        qgChapter.sequence = chapter.sequence;
        if (chapter.status.tips.equals(net.lzbook.kit.data.bean.Chapter.Status.CONTENT_NORMAL.tips)) {
            qgChapter.status_chapter = Constants.ENABLE;
        } else {
            qgChapter.status_chapter = Constants.DISABLE_DES;
        }

        return qgChapter;
    }

    /**
     * 青果数据转化为自由数据类型
     */
    public static net.lzbook.kit.data.bean.Chapter parseToOWNBean(Chapter chapter) {
        if (chapter == null) {
            return null;
        }
        net.lzbook.kit.data.bean.Chapter ownChapter = new net.lzbook.kit.data.bean.Chapter();
        ownChapter.content = chapter.content;
        ownChapter.isSuccess = chapter.isSuccess;
        ownChapter.word_count = (int) chapter.word_count;
        ownChapter.chapter_id = chapter.id_chapter;
        ownChapter.time = chapter.create_time;
        ownChapter.book_id = chapter.id_book;
        ownChapter.sequence = chapter.serial_number - 1;
        ownChapter.sort = chapter.serial_number;
        ownChapter.chapter_name = chapter.name;
        return ownChapter;
    }

    public static Book parseToQGBook(net.lzbook.kit.data.bean.Book ownBook) {
        Book book = new Book();
        book.id_book = ownBook.book_id;
        book.name = ownBook.name;
        book.description = ownBook.desc;
        book.category = ownBook.category;
        book.id_last_chapter_serial_number = ownBook.chapter_count;
        book.chapters_update_index = ownBook.chapters_update_index;
        return book;
    }

    public static BookUpdate parseToOwnBookUpdate(com.quduquxie.bean.BookUpdate qgBookUpdate, String bookName) {
        BookUpdate bookUpdate = new BookUpdate();
        bookUpdate.book_id = qgBookUpdate.id_book;
        bookUpdate.book_name = bookName;
        bookUpdate.update_count = qgBookUpdate.update_count;
        bookUpdate.last_time = qgBookUpdate.create_time;
        bookUpdate.last_chapter_name = qgBookUpdate.name;
        bookUpdate.last_sort = qgBookUpdate.serial_number;
        return bookUpdate;
    }

    public static ArrayList<BookUpdate> buildOwnBookUpdateList(ArrayList<com.quduquxie.bean.BookUpdate> list, ArrayList<com.quduquxie.bean.Book> booksToUpdateOfQG) {
        ArrayList<BookUpdate> bookUpdates = new ArrayList<>();
        for (int i = 0; i < booksToUpdateOfQG.size(); i++) {
            for (int j = 0; j < list.size(); j++) {
                if (booksToUpdateOfQG.get(i).id_book.equals(list.get(j).id_book)) {
                    bookUpdates.add(parseToOwnBookUpdate(list.get(j), booksToUpdateOfQG.get(i).name));
                }
            }
        }
        return bookUpdates;
    }

    public static ArrayList<Chapter> buildQGChapterList(ArrayList<net.lzbook.kit.data.bean.Chapter> chapterList, int start, int size) {
        ArrayList<com.quduquxie.bean.Chapter> list = new ArrayList<>();
        for (int i = start; i < start + size; i++) {
            if (i > -1 && i < chapterList.size()) {
                list.add(BeanParser.parseToQGBean(chapterList.get(i)));
            } else {
                break;
            }
        }
        return list;
    }

    public static ArrayList<net.lzbook.kit.data.bean.Chapter> buildOWNChapterList(ArrayList<com.quduquxie.bean.Chapter> chapterList, int start, int size) {
        ArrayList<net.lzbook.kit.data.bean.Chapter> list = new ArrayList<>();
        for (int i = start; i < start + size; i++) {
            if (i > -1 && i < chapterList.size()) {
                list.add(BeanParser.parseToOWNBean(chapterList.get(i)));
            } else {
                break;
            }
        }
        return list;
    }

}
