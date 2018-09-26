package net.lzbook.kit.model;

import com.ding.basic.bean.Book;
import com.ding.basic.bean.Bookmark;
import com.ding.basic.database.helper.BookDataProviderHelper;
import com.ding.basic.repository.RequestRepositoryFactory;
import com.ding.basic.request.RequestSubscriber;
import com.orhanobut.logger.Logger;

import net.lzbook.kit.app.base.BaseBookApplication;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * @author lijun Lee
 * @desc 书籍封面ViewModel
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/11/17 12:07
 */

public class BookCoverViewModel extends DisposableAndroidViewModel {

    private BookCoverViewCallback bookCoverViewCallback;

    private BookChapterViewCallback bookChapterViewCallback;

    private BookDataProviderHelper bookDataProviderHelper =
            BookDataProviderHelper.Companion.loadBookDataProviderHelper(
                    BaseBookApplication.getGlobalContext());

    public BookCoverViewModel() {
        super();
    }

    /***
     * 获取书籍封面
     * **/
    public void requestBookDetail(final String book_id, final String book_source_id,
            final String book_chapter_id) {
        RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).requestBookDetail(book_id, book_source_id,
                book_chapter_id, new RequestSubscriber<Book>() {
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

    /**
     * 根据书籍id获取书签
     */
    public void getBookMarkList(String bookId) {
        if (bookChapterViewCallback != null) {
            bookChapterViewCallback.requestBookmarkList(
                    bookDataProviderHelper.getBookMarks(bookId));
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
        void requestBookmarkList(ArrayList<Bookmark> bookmarks);
    }
}