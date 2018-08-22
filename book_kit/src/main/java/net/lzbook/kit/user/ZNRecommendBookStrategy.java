package net.lzbook.kit.user;

import com.ding.basic.bean.Book;
import com.ding.basic.bean.RecommendBooksEndResp;
import com.ding.basic.bean.RecommendBooksResp;
import com.ding.basic.bean.ZNBook;

import net.lzbook.kit.utils.AppLog;

import java.util.List;

/**
 * 项目名称：11m
 * 创建人：Zach
 * 创建时间：2017/11/2 0002
 */
@Deprecated
public class ZNRecommendBookStrategy extends RecommendBookStrategy {

    //智能推荐书籍集合
    private List<RecommendBooksResp.DataBean.MapBean.ZnListBean> mZnListBooksList;

    //智能书末推荐书籍集合
    private List<RecommendBooksEndResp.DataBean.MapBean.ZnListBean> mBookendBooksList;


    ZNRecommendBookStrategy(RecommendBooksResp bean) {
        bookBeans = bean;
        if (bookBeans != null) {
            getBookFromList();
        }
    }

    ZNRecommendBookStrategy(RecommendBooksEndResp bean) {
        mBooksEndBean = bean;
        if (mBooksEndBean != null) {
            getBookFromBookendList();
        }
    }

    /**
     * 获取智能书籍
     */
    @Override
    public Object getBookFromList() {
        if (bookBeans.getData().getMap() != null) {
            mZnListBooksList = bookBeans.getData().getMap().getZnList();
        }
        return mZnListBooksList;
    }

    private void getBookFromBookendList() {
        RecommendBooksEndResp.DataBean data = mBooksEndBean.getData();
        if (data != null) {
            RecommendBooksEndResp.DataBean.MapBean map = data.getMap();
            if (map != null) {
                mBookendBooksList = map.getZnList();
                AppLog.e("智能书籍策略", "后台获取到的智能书籍：" + mBookendBooksList.toString());
            }
        }
    }

    /**
     * 从智能书籍中选出书架推荐书籍
     */
    @Override
    Book getRecommendBook() {
        RecommendBooksResp.DataBean.MapBean.ZnListBean selectedBook;
        while (mZnListBooksList.size() != 0) {
            selectedBook = mZnListBooksList.remove(0);
            if (!mDislikeBooksList.contains(selectedBook.getBookId())) {
                return new ZNBook.Builder().bookId(selectedBook.getBookId()).bookName(
                        selectedBook.getBookName()).bookSourceId(selectedBook.getId()).author(
                        selectedBook.getAuthorName())
                        .category(selectedBook.getLabel()).chapterCount(
                                selectedBook.getChapterCount()).host(
                                selectedBook.getHost()).lastChapterName(
                                selectedBook.getLastChapterName())
                        .imgUrl(selectedBook.getSourceImageUrl()).updateTime(
                                selectedBook.getUpdateTime()).build();
            }
        }
        return null;
    }

    /**
     * 从智能书籍中选出书末推荐书籍
     */
    @Override
    Book getBookendRecommendBook() {
        RecommendBooksEndResp.DataBean.MapBean.ZnListBean znBook;
        if (mBookendBooksList != null && mBookendBooksList.size() != 0) {
            znBook = mBookendBooksList.remove(0);
            if (znBook != null) {
                return new ZNBook.Builder().bookId(znBook.getBookId()).bookName(
                        znBook.getBookName()).bookSourceId(znBook.getId()).author(
                        znBook.getAuthorName())
                        .category(znBook.getLabel()).chapterCount(znBook.getChapterCount()).host(
                                znBook.getHost()).lastChapterName(znBook.getLastChapterName())
                        .imgUrl(znBook.getSourceImageUrl()).updateTime(znBook.getUpdateTime()).dex(
                                znBook.getDex()).build();
            }
        }
        return null;
    }

    @Override
    Book getBookendNewBook() {
        return null;
    }
}