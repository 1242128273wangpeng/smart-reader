package com.intelligent.reader.cover;

import com.intelligent.reader.repository.BookCoverRepository;

import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.Bookmark;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.CoverPage;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.net.custom.service.UserService;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

/**
 * @author lijun Lee
 * @desc 书籍数据 自有
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/11/16 17:42
 */

public class BookCoverOtherRepository implements BookCoverRepository {

    private static volatile BookCoverOtherRepository INSTANCE;

    private final UserService mApi;

    private BookCoverOtherRepository(UserService api) {
        this.mApi = api;
    }

    public static BookCoverOtherRepository getInstance(@NonNull UserService api) {
        if (INSTANCE == null) {
            synchronized (BookCoverOtherRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BookCoverOtherRepository(api);
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public Observable<CoverPage> getCoverDetail(String bookId, String sourceId, String host) {
        return mApi.getCoverDetail(bookId, sourceId).map(new Function<CoverPage, CoverPage>() {
            @Override
            public CoverPage apply(CoverPage coverPage) throws Exception {
//                if (Book.STATE_SERIAL.equals(coverPage.bookVo.bookStatus)) {
//                    coverPage.bookVo.book_status = Book.SERIAL;
//                } else {
//                    coverPage.bookVo.book_status = Book.FINISH;
//                }
//                coverPage.bookVo.flag = coverPage.vip_flag;
//                coverPage.bookVo.last_chapter_name = coverPage.bookVo.lastChapter.getName();
                return coverPage;
            }
        });
    }

    @Override
    public Observable<List<Chapter>> getChapterList(final RequestItem requestItem) {
        return mApi.getChapterList(requestItem.book_id, requestItem.book_source_id, requestItem);
    }

    @Override
    public boolean isBookSubscribe(String bookId) {
        return false;
    }

    @Override
    public boolean saveBookChapterList(List<Chapter> chapterList, RequestItem requestItem) {
        return false;
    }

    @Override
    public Observable<ArrayList<Bookmark>> getBookMarkList(String bookId) {
        return null;
    }

    @Override
    public void deleteBookMark(ArrayList<Integer> ids) {

    }
}
