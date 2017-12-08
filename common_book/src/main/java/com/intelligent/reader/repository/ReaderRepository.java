package com.intelligent.reader.repository;

import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.SourceItem;
import net.lzbook.kit.purchase.SingleChapterBean;
import net.lzbook.kit.user.bean.RecommendBooksEndResp;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import io.reactivex.Observable;

/**
 * @author lijun Lee
 * @desc 阅读模块接口定义
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/11/20 15:06
 */

public interface ReaderRepository {

    /**
     * 阅读完结书籍推荐
     */
    Observable<RecommendBooksEndResp> getBookEndRecommendBook(String recommanded, String bookId);

    /**
     * 换源集合
     */
    Observable<SourceItem> getBookSource(String bookId);

    /**
     * 单章拉取
     */
    Observable<Chapter> requestSingleChapter(String host, Chapter chapter);

    /**
     * 单章购买
     */
    Observable<SingleChapterBean> paySingleChapter(String sourceId, String chapterId, String chapterName, String uid);

    /**
     * 是否需要下载
     */
    boolean isNeedDownContent(@NotNull Chapter chapter, boolean downloadFlag);

    /**
     * 批量下载
     */
    void batchChapter(int dex, boolean downloadFlag, Map<String, Chapter> chapterMap) throws Exception;

    /**
     * 更新书当前章节
     */
    void updateBookCurrentChapter(String bookId, Chapter retChapter, int sequence);

    /**
     * @param bookId
     * @param chapter_id
     * @return
     */
    int getChapterIdByChapterId(String bookId, String chapter_id);

    /**
     * @param bookId
     * @param chapterIndex
     * @param i
     */
    void changeChargeBookState(String bookId, int chapterIndex, int i);

    /**
     * 写入缓存
     */
    void writeChapterCache(Chapter chapter, Boolean downloadFlag);
}
