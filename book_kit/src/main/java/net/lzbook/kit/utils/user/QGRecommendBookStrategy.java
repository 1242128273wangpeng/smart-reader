package net.lzbook.kit.utils.user;

import com.ding.basic.bean.Book;
import com.ding.basic.bean.QGBook;
import com.ding.basic.bean.RecommendBooksEndResp;
import com.ding.basic.bean.RecommendBooksResp;

import net.lzbook.kit.utils.logger.AppLog;

import java.util.List;

/**
 * 项目名称：11m
 * 类描述：
 * 创建人：Zach
 * 创建时间：2017/11/3 0003
 */
@Deprecated
public class QGRecommendBookStrategy extends RecommendBookStrategy {

    //书架页青果推荐书籍集合
    private List<RecommendBooksResp.DataBean.MapBean.QgListBean> mQgListBooksList;
    //书末推荐青果推荐书籍集合1
    private List<RecommendBooksEndResp.DataBean.MapBean.QgList1Bean> mQgList1Bean;
    //书末推荐青果推荐书籍集合2
    private List<RecommendBooksEndResp.DataBean.MapBean.QgList2Bean> mQgList2Bean;


    QGRecommendBookStrategy(RecommendBooksResp bean) {
        bookBeans = bean;
        if (bookBeans != null) {
            getBookFromList();
        }
    }

    QGRecommendBookStrategy(RecommendBooksEndResp bean) {
        mBooksEndBean = bean;
        if (mBooksEndBean != null) {
            getQGBook1FromList();
            getQGBook2FromList();
        }
    }

    /**
     * 获取书架推荐位的青果书籍
     */
    @Override
    public Object getBookFromList() {
        if (bookBeans.getData().getMap() != null) {
            mQgListBooksList = bookBeans.getData().getMap().getQgList();
        }
        return mQgListBooksList;
    }

    /**
     * 获取书末推荐位的青果书籍1
     */
    private void getQGBook1FromList() {
        RecommendBooksEndResp.DataBean data = mBooksEndBean.getData();
        if (data != null) {
            RecommendBooksEndResp.DataBean.MapBean map = data.getMap();
            if (map != null) {
                mQgList1Bean = map.getQgList1();
                AppLog.e("青果书籍策略", "后台获取到的青果书籍1：" + mQgList1Bean.toString());
            }
        }
    }

    /**
     * 获取书末推荐位的青果书籍1
     */
    private void getQGBook2FromList() {
        RecommendBooksEndResp.DataBean data = mBooksEndBean.getData();
        if (data != null) {
            RecommendBooksEndResp.DataBean.MapBean map = data.getMap();
            if (map != null) {
                mQgList2Bean = map.getQgList2();
                AppLog.e("青果书籍策略2", "后台获取到的青果书籍2：" + mQgList2Bean.toString());
            }
        }
    }


    /**
     * 从青果书籍中选出书架推荐书籍
     */
    @Override
    Book getRecommendBook() {
        RecommendBooksResp.DataBean.MapBean.QgListBean selectedBook;
        while (mQgListBooksList.size() != 0) {
            selectedBook = mQgListBooksList.remove(0);
            if (!mDislikeBooksList.contains(selectedBook.getId())) {
                return new QGBook.Builder()
                        .bookId(selectedBook.getId() == null ? ""
                                : selectedBook.getId())
                        .bookName(selectedBook.getBookName() == null ? ""
                                : selectedBook.getBookName())
                        .bookSourceId(selectedBook.getId())
                        .author(selectedBook.getAuthor_name() == null ? ""
                                : selectedBook.getAuthor_name())
                        .category(selectedBook.getLabels() == null ? ""
                                : selectedBook.getLabels())
                        .chapterCount(selectedBook.getChapter_sn())
                        .host(selectedBook.getHost() == null ? ""
                                : selectedBook.getHost())
                        .lastChapterName(selectedBook.getChapter_name() == null ? ""
                                : selectedBook.getChapter_name())
                        .imgUrl(selectedBook.getImage())
                        .updateTime(selectedBook.getUpdate_time()).build();
            }
        }
        return null;
    }

    /**
     * 从青果书籍中选出书书末推荐书籍
     */
    @Override
    Book getBookendRecommendBook() {
        RecommendBooksEndResp.DataBean.MapBean.QgList1Bean qgBook;
        if (mQgList1Bean != null && mQgList1Bean.size() != 0) {
            qgBook = mQgList1Bean.remove(0);
            if (qgBook != null) {
                return new QGBook.Builder()
                        .bookId(qgBook.getId() == null ? ""
                                : qgBook.getId())
                        .bookName(qgBook.getBookName() == null ? ""
                                : qgBook.getBookName())
                        .bookSourceId(qgBook.getId())
                        .author(qgBook.getAuthor_name() == null ? ""
                                : qgBook.getAuthor_name())
                        .category(qgBook.getLabels() == null ? ""
                                : qgBook.getLabels())
                        .chapterCount(qgBook.getChapter_sn())
                        .host(qgBook.getHost() == null ? ""
                                : qgBook.getHost())
                        .lastChapterName(qgBook.getChapter_name() == null ? ""
                                : qgBook.getChapter_name())
                        .imgUrl(qgBook.getImage())
                        .updateTime(qgBook.getUpdate_time()).build();
            }
        }
        return null;
    }

    /**
     * 从青果书籍中选出推荐新书
     */
    @Override
    Book getBookendNewBook() {
        RecommendBooksEndResp.DataBean.MapBean.QgList2Bean qgBook;
        if (mQgList2Bean != null && mQgList2Bean.size() != 0) {
            qgBook = mQgList2Bean.remove(0);
            if (qgBook != null) {
                return new QGBook.Builder().bookId(qgBook.getId()).bookName(
                        qgBook.getBookName()).bookSourceId(qgBook.getId()).author(
                        qgBook.getAuthor_name())
                        .category(qgBook.getLabels()).chapterCount(qgBook.getChapter_sn()).host(
                                qgBook.getHost()).lastChapterName(qgBook.getChapter_name())
                        .imgUrl(qgBook.getImage()).updateTime(qgBook.getUpdate_time()).build();
            }
        }
        return null;
    }


}