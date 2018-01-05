package com.intelligent.reader.read.mode

import net.lzbook.kit.data.bean.Chapter

/**
 * 阅读状态
 * Created by wt on 2018/1/4.
 */
object ReadState {
    //阅读当前章节顺序
    var sequence = 0
    //阅读当前页偏移量
    var offset = 0
    //目录
    var chapterList: ArrayList<Chapter> = ArrayList()
    //章节名
    var chapterName:String? = null
    //小说名
    var bookName:String? = null
    //小说作者
    var bookAuthor: String? = null
}