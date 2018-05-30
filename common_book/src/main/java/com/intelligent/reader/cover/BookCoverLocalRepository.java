package com.intelligent.reader.cover;

import com.intelligent.reader.repository.BookCoverRepository;

import net.lzbook.kit.data.bean.Bookmark;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.CoverPage;
import net.lzbook.kit.data.db.BookChapterDao;
import net.lzbook.kit.data.db.BookDaoHelper;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * @author lijun Lee
 * @desc 书籍数据 db
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/11/18 10:57
 */

public class BookCoverLocalRepository implements BookCoverRepository {

    private static volatile BookCoverLocalRepository INSTANCE;

    private final Context mContext;

    private BookCoverLocalRepository(Context context) {
        this.mContext = context;
    }

    public static BookCoverLocalRepository getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (BookCoverLocalRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BookCoverLocalRepository(context);
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public Observable<CoverPage> getCoverDetail(String bookId, String sourceId, String host) {
        return null;
    }

    @Override
    public Observable<List<Chapter>> getChapterList(RequestItem requestItem) {
        final BookChapterDao bookChapterDao = new BookChapterDao(mContext, requestItem.book_id);
        return Observable.create(new ObservableOnSubscribe<List<Chapter>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Chapter>> e) throws Exception {
                e.onNext(bookChapterDao.queryBookChapter());
                e.onComplete();
            }
        });
    }

    @Override
    public boolean isBookSubscribe(String bookId) {

        return BookDaoHelper.getInstance().isBookSubed(bookId);
    }

    @Override
    public boolean saveBookChapterList(List<Chapter> chapterList, RequestItem requestItem) {
        if (isBookSubscribe(requestItem.book_id) && chapterList.size() > 0) {
            saveTheChapterAndLastBook(chapterList, requestItem);
            return true;
        }
        return false;
    }

    @Override
    public Observable<ArrayList<Bookmark>> getBookMarkList(final String bookId) {
        return Observable.create(new ObservableOnSubscribe<ArrayList<Bookmark>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<Bookmark>> e) throws Exception {
                e.onNext(BookDaoHelper.getInstance().getBookMarks(bookId));
                e.onComplete();
            }
        });
    }

    @Override
    public void deleteBookMark(ArrayList<Integer> ids) {
        BookDaoHelper.getInstance().deleteBookMark(ids, 0);
    }

    private void saveTheChapterAndLastBook(List<Chapter> chapterList, RequestItem requestItem) {
        BookChapterDao bookChapterDao = new BookChapterDao(mContext, requestItem.book_id);
        int chapterCount = bookChapterDao.getCount();
        if (chapterCount > 0) {
            return;
        }
        bookChapterDao.insertBookChapter(chapterList);
        Chapter lastChapter = chapterList.get(chapterList.size() - 1);
        Book book = new Book();
        book.book_id = requestItem.book_id;
        book.book_source_id = requestItem.book_source_id;
        book.site = requestItem.host;
        book.chapter_count = bookChapterDao.getCount();
        book.last_updatetime_native = lastChapter.time;
        book.last_chapter_name = lastChapter.chapter_name;
        book.last_chapter_md5 = lastChapter.book_chapter_md5;
        book.last_updateSucessTime = System.currentTimeMillis();
//        book.mBookType = requestItem.mBookType;
        BookDaoHelper.getInstance().updateBook(book);
    }
}
