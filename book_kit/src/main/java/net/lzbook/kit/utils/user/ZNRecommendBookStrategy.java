package net.lzbook.kit.utils.user;

import com.ding.basic.bean.Book;
import com.ding.basic.bean.RecommendBooksEndResp;
import com.ding.basic.bean.RecommendBooksResp;
import com.ding.basic.bean.ZNBook;

import net.lzbook.kit.utils.logger.AppLog;

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
                return new ZNBook.Builder()
                        .bookId(selectedBook.getBookId() == null ? ""
                                : selectedBook.getBookId())
                        .bookName(selectedBook.getBookName() == null ? ""
                                : selectedBook.getBookName())
                        .bookSourceId(selectedBook.getId() == null ? ""
                                : selectedBook.getId())
                        .author(selectedBook.getAuthorName() == null ? ""
                                : selectedBook.getAuthorName())
                        .category(selectedBook.getLabel() == null ? ""
                                : selectedBook.getLabel())
                        .chapterCount(selectedBook.getChapterCount())
                        .host(selectedBook.getHost() == null ? ""
                                : selectedBook.getHost())
                        .lastChapterName(selectedBook.getLastChapterName() == null ? ""
                                : selectedBook.getLastChapterName())
                        .imgUrl(selectedBook.getSourceImageUrl())
                        .updateTime(selectedBook.getUpdateTime()).build();
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
                return new ZNBook.Builder()
                        .bookId(znBook.getBookId() == null ? ""
                                : znBook.getBookId())
                        .bookName(znBook.getBookName() == null ? ""
                                : znBook.getBookName())
                        .bookSourceId(znBook.getId() == null ? ""
                                : znBook.getId())
                        .author(znBook.getAuthorName() == null ? ""
                                : znBook.getAuthorName())
                        .category(znBook.getLabel() == null ? ""
                                : znBook.getLabel())
                        .chapterCount(znBook.getChapterCount())
                        .host(znBook.getHost() == null ? ""
                                : znBook.getHost())
                        .lastChapterName(znBook.getLastChapterName() == null ? ""
                                : znBook.getLastChapterName())
                        .imgUrl(znBook.getSourceImageUrl())
                        .updateTime(znBook.getUpdateTime())
                        .dex(znBook.getDex()).build();
            }
        }
        return null;
    }

    @Override
    Book getBookendNewBook() {
        return null;
    }
}