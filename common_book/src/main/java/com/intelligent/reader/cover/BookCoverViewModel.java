package com.intelligent.reader.cover;

import com.intelligent.reader.DisposableAndroidViewModel;
import com.intelligent.reader.repository.BookCoverRepository;

import net.lzbook.kit.data.bean.Bookmark;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.CoverPage;
import net.lzbook.kit.data.bean.RequestItem;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author lijun Lee
 * @desc 书籍封面ViewModel
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/11/17 12:07
 */

public class BookCoverViewModel extends DisposableAndroidViewModel {

    private final BookCoverRepository mBookCoverRepository;

    private BookCoverViewCallback mBookCoverViewCallback;

    private BookChapterViewCallback mBookChapterViewCallback;

    public BookCoverViewModel(BookCoverRepository bookCoverRepository) {
        super();
        this.mBookCoverRepository = bookCoverRepository;
    }

    /**
     * 获取书籍封面
     * 请把每个带有Observable的事件请求放入事件流容器中  “BookCoverViewModel.addDisposable(disposable)”
     * 最后在UI界面处于onStop状态 “unSubscribe” 解除订阅，防止未处理事件流回到已销毁的UI界面，造成内存溢出风险
     */
    public void getCoverDetail(String bookId, String sourceId, String host) {
        Disposable disposable = mBookCoverRepository.getCoverDetail(bookId, sourceId, host)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<CoverPage>() {
                    @Override
                    public void accept(CoverPage coverPage) throws Exception {
                        if (mBookCoverViewCallback != null) {
                            mBookCoverViewCallback.onCoverDetail(coverPage);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if (mBookCoverViewCallback != null) {
                            mBookCoverViewCallback.onFail(throwable.getMessage());
                        }
                    }
                });
        addDisposable(disposable);
    }

    /**
     * 获取书籍目录
     */
    public void getChapterList(final RequestItem requestItem) {
        Disposable disposable = mBookCoverRepository.getChapterList(requestItem)
                .doOnNext(new Consumer<List<Chapter>>() {
                    @Override
                    public void accept(List<Chapter> chapters) throws Exception {
                        // 已被订阅则加入数据库
                        mBookCoverRepository.saveBookChapterList(chapters, requestItem);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Chapter>>() {
                    @Override
                    public void accept(List<Chapter> chapters) throws Exception {
                        if (mBookChapterViewCallback != null) {
                            mBookChapterViewCallback.onChapterList(chapters);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if (mBookChapterViewCallback != null) {
                            mBookChapterViewCallback.onFail(throwable.getMessage());
                        }
                    }
                });
        addDisposable(disposable);
    }

    /**
     * 根据书籍id获取书签
     */
    public void getBookMarkList(String bookId) {
        Disposable disposable = mBookCoverRepository.getBookMarkList(bookId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ArrayList<Bookmark>>() {
                    @Override
                    public void accept(ArrayList<Bookmark> bookmarks) throws Exception {
                        if (mBookChapterViewCallback != null) {
                            mBookChapterViewCallback.onBookMarkList(bookmarks);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
        addDisposable(disposable);
    }

    /**
     * 删除书签
     */
    public void deleteBookMark(ArrayList<Integer> ids) {
        mBookCoverRepository.deleteBookMark(ids);
    }

    public void setBookCoverViewCallback(BookCoverViewCallback bookCoverViewCallback) {
        this.mBookCoverViewCallback = bookCoverViewCallback;
    }

    public void setBookChapterViewCallback(BookChapterViewCallback bookChapterViewCallback) {
        this.mBookChapterViewCallback = bookChapterViewCallback;
    }

    /**
     * 暂不使用生命周期组件，可暂使用引用回调，或data binding通知UI改变状态
     */
    public interface BookCoverViewCallback {

        void onCoverDetail(CoverPage coverPage);

        void onFail(String msg);
    }

    public interface BookChapterViewCallback {

        void onChapterList(List<Chapter> chapters);

        void onBookMarkList(List<Bookmark> bookmarks);

        void onFail(String msg);
    }
}
