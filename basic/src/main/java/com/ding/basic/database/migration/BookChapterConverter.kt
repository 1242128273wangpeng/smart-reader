package com.ding.basic.database.migration

import com.ding.basic.bean.Chapter

class BookChapterConverter : DBFieldConverter<String, Chapter>{
    override fun convert(old: String): Chapter {
        val chapter = Chapter()
        chapter.name = old
        return chapter
    }
}