package com.intelligent.reader.read.mode

import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.NovelLineBean
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 阅读信息
 * Created by wt on 2017/12/13.
 */
data class ReadInfo(var curBook:Book,var curChapterIndex:Int,var animaEnums:ReadViewEnums.Animation){//当前Book//当前章Index//动画模式
    var curChapter: Chapter?=null//当前Chapter
    var curChapterList:ArrayList<Chapter>?=null//目录
    var curOriginList:CopyOnWriteArrayList<ArrayList<NovelLineBean>> = CopyOnWriteArrayList()//当前章分页后数据 重新分页操作
    var curPageIndex:Int?=null//当前页Index
    var curPageSumIndex:Int?=null//当前页总Index
    var width_nativead:Int?=null//广告宽度
    var height_nativead:Int?=null//广告高度
    var height_middle_nativead:Int?=null//中间广告
    var textFonitSize:Int?=null//字体大小
    var textColor:Int?=null//字体颜色
    var characterSpacee:Int?=null//间距
    var backgroudId:Int?=null//背景颜色
    var offset:Int?=null//偏移量
}