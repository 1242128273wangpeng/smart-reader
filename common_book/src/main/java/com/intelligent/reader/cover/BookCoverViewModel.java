package com.intelligent.reader.cover;

import android.text.TextUtils;

import com.ding.basic.bean.Book;
import com.ding.basic.bean.Bookmark;
import com.ding.basic.bean.Chapter;
import com.ding.basic.database.helper.BookDataProviderHelper;
import com.ding.basic.repository.RequestRepositoryFactory;
import com.ding.basic.request.RequestSubscriber;
import com.intelligent.reader.DisposableAndroidViewModel;
import com.orhanobut.logger.Logger;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.book.download.CacheManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author lijun Lee
 * @desc 书籍封面ViewModel
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/11/17 12:07
 */

public class BookCoverViewModel extends DisposableAndroidViewModel {

    private BookCoverViewCallback bookCoverViewCallback;

    private BookChapterViewCallback bookChapterViewCallback;

    private BookDataProviderHelper bookDataProviderHelper = BookDataProviderHelper.Companion.loadBookDataProviderHelper(BaseBookApplication.getGlobalContext());

    public BookCoverViewModel() {
        super();
    }

    /***
     * 获取书籍封面
     * **/
    public void requestBookDetail(String book_id, String book_source_id, String book_chapter_id) {
        RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestBookDetail(book_id, book_source_id, book_chapter_id, new RequestSubscriber<Book>() {
            @Override
            public void requestResult(@Nullable Book result) {
                if (bookCoverViewCallback != null) {
                    bookCoverViewCallback.requestCoverDetailSuccess(result);
                }
            }

            @Override
            public void requestError(@NotNull String message) {
                Logger.e("请求封面异常！");
                if (bookCoverViewCallback != null) {
                    bookCoverViewCallback.requestCoverDetailFail(message);
                }
            }

            @Override
            public void requestComplete() {
                Logger.i("请求封面完成！");
            }
        });
    }

    /***
     * 获取书籍目录
     * **/
    public void requestBookCatalog(final Book book) {
        if (!TextUtils.isEmpty(book.getBook_id()) && !TextUtils.isEmpty(book.getBook_source_id())) {
            RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                    .requestCatalog(book.getBook_id(), book.getBook_source_id(), book.getBook_chapter_id())
                    .subscribeOn(Schedulers.io())
                    .doOnNext(new Consumer<List<com.ding.basic.bean.Chapter>>() {
                        @Override
                        public void accept(List<com.ding.basic.bean.Chapter> chapters) throws Exception {
                            CacheManager.INSTANCE.freshBook(book.getBook_id(), false);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(
                    new Consumer<List<com.ding.basic.bean.Chapter>>() {
                        @Override
                        public void accept(List<com.ding.basic.bean.Chapter> result) throws Exception {
                            if (bookChapterViewCallback != null) {
                                bookChapterViewCallback.requestCatalogSuccess(result);
                            }
                        }
                    }
                    , new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable message) throws Exception {
                            Logger.e("获取章节目录异常！");
                            if (bookChapterViewCallback != null) {
                                bookChapterViewCallback.requestCatalogFail(message.getMessage());
                            }
                        }
                    });
        } else {
            if (bookChapterViewCallback != null) {
                bookChapterViewCallback.requestCatalogFail("参数异常！");
            }
        }
    }

    /**
     * 根据书籍id获取书签
     */
    public void getBookMarkList(String bookId) {
        if (bookChapterViewCallback != null) {
            bookChapterViewCallback.requestBookmarkList(bookDataProviderHelper.getBookMarks(bookId));
        }
    }

    /**
     * 删除书签
     */
    public void deleteBookMark(ArrayList<Integer> ids) {
        bookDataProviderHelper.deleteBookMark(ids);
    }

    public void setBookCoverViewCallback(BookCoverViewCallback bookCoverViewCallback) {
        this.bookCoverViewCallback = bookCoverViewCallback;
    }

    public void setBookChapterViewCallback(BookChapterViewCallback bookChapterViewCallback) {
        this.bookChapterViewCallback = bookChapterViewCallback;
    }

    /***
     * 请求书籍详情回调
     * **/
    public interface BookCoverViewCallback {

        void requestCoverDetailFail(String message);

        void requestCoverDetailSuccess(Book book);
    }

    /***
     * 请求目录和书签回调
     * **/
    public interface BookChapterViewCallback {

        void requestCatalogFail(String message);

        void requestCatalogSuccess(List<Chapter> chapters);

        void requestBookmarkList(ArrayList<Bookmark> bookmarks);
    }
}