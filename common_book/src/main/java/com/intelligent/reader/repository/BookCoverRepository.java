package com.intelligent.reader.repository;


import net.lzbook.kit.data.bean.Bookmark;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.CoverPage;
import net.lzbook.kit.data.bean.RequestItem;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

/**
 * @author lijun Lee
 * @desc 书籍封面数据源接口定义
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/11/16 17:07
 */

public interface BookCoverRepository {

    /**
     * 获取书籍封面
     *
     * @param bookId   书籍Id
     * @param sourceId 来源Id
     * @param host     来源host
     */
    Observable<CoverPage> getCoverDetail(String bookId, String sourceId, String host);


    /**
     * 获取书籍目录
     */
    Observable<List<Chapter>> getChapterList(RequestItem requestItem);

    /**
     * 书籍是否加入书架
     */
    boolean isBookSubscribe(String bookId);

    /**
     * 保存已加入书架书籍目录
     */
    boolean saveBookChapterList(List<Chapter> chapterList, RequestItem requestItem);

    /**
     * 根据书籍Id获取书签集合
     */
    Observable<ArrayList<Bookmark>> getBookMarkList(String bookId);

    /**
     * 删除书签
     */
    void deleteBookMark(ArrayList<Integer> ids);
}
