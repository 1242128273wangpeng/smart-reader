package com.dy.reader.repository;

import com.ding.basic.bean.Book;
import com.ding.basic.bean.Chapter;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Flowable;

/**
 * @author lijun Lee
 * @desc 阅读模块接口定义
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/11/20 15:06
 */

public interface ReaderRepository {

    /**
     * 单章拉取
     */
    Flowable<Chapter> requestSingleChapter(@NotNull Chapter chapter);


    /**
     * 写入缓存
     */
    void writeChapterCache(Chapter chapter, Book book);
}
