package com.ding.basic.util

import com.ding.basic.bean.Chapter

/**
 * Created on 2018/3/21.
 * Created by crazylei.
 */
class ChapterCacheUtil {

    companion object {

        fun checkChapterCacheExist(chapter: Chapter?): String? {
            return if (chapter == null) {
                null
            } else {
                DataCache.getChapterFromCache(chapter)
            }
        }
    }
}