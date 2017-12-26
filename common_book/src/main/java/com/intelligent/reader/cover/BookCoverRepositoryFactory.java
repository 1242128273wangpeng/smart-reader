package com.intelligent.reader.cover;


import com.intelligent.reader.repository.BookCoverRepository;

import net.lzbook.kit.data.bean.Bookmark;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.CoverPage;
import net.lzbook.kit.data.bean.RequestItem;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

/**
 * @author lijun Lee
 * @desc 书籍数据源
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/11/16 17:11
 */

public class BookCoverRepositoryFactory implements BookCoverRepository {

    public static final String QG_SOURCE = "api.qingoo.cn";
    private volatile static BookCoverRepositoryFactory INSTANCE = null;
    /**
     * 青果数据源
     */
    private final BookCoverRepository mCoverQGBookRepository;

    /**
     * 自有数据源
     */
    private final BookCoverRepository mCoverBookRepository;

    /**
     * 本地数据源
     */
    private final BookCoverRepository mCoverBookLocalRepository;

    private boolean mChapterComeFromLocal = false;

    private BookCoverRepositoryFactory(BookCoverRepository coverBookCoverRepository, BookCoverRepository coverQGBookCoverRepository,
                                       BookCoverRepository coverBookCoverLocalRepository) {
        this.mCoverBookRepository = coverBookCoverRepository;
        this.mCoverQGBookRepository = coverQGBookCoverRepository;
        this.mCoverBookLocalRepository = coverBookCoverLocalRepository;
    }

    public static BookCoverRepositoryFactory getInstance(BookCoverRepository coverBookRepository, BookCoverRepository coverQGBookRepository,
                                                         BookCoverRepository coverBookLocalRepository) {
        if (INSTANCE == null) {
            synchronized (BookCoverRepositoryFactory.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BookCoverRepositoryFactory(coverBookRepository, coverQGBookRepository, coverBookLocalRepository);
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public Observable<CoverPage> getCoverDetail(String bookId, String sourceId, String host) {
        if (host.equals(QG_SOURCE)) {
            return mCoverQGBookRepository.getCoverDetail(bookId, sourceId, host);
        } else {
            return mCoverBookRepository.getCoverDetail(bookId, sourceId, host);
        }
    }

    @Override
    public Observable<ArrayList<Chapter>> getChapterList(RequestItem requestItem) {
        // 已加入书架，且书籍目录存在本地数据库
        if (mCoverBookLocalRepository.isBookSubscribe(requestItem.book_id)) {
            mChapterComeFromLocal = true;
            return mCoverBookLocalRepository.getChapterList(requestItem);
        }

        mChapterComeFromLocal = false;
        if (requestItem.host.equals(QG_SOURCE)) {
            return mCoverQGBookRepository.getChapterList(requestItem);
        } else {
            return mCoverBookRepository.getChapterList(requestItem);
        }
    }

    @Override
    public boolean isBookSubscribe(String bookId) {
        return mCoverBookLocalRepository.isBookSubscribe(bookId);
    }

    @Override
    public boolean saveBookChapterList(List<Chapter> chapterList, RequestItem requestItem) {
        if (!mChapterComeFromLocal) {
            return mCoverBookLocalRepository.saveBookChapterList(chapterList, requestItem);
        }
        return false;
    }

    @Override
    public Observable<ArrayList<Bookmark>> getBookMarkList(String bookId) {
        return mCoverBookLocalRepository.getBookMarkList(bookId);
    }

    @Override
    public void deleteBookMark(ArrayList<Integer> ids) {
        mCoverBookLocalRepository.deleteBookMark(ids);
    }
}
