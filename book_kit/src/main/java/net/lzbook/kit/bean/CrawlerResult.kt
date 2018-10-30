package net.lzbook.kit.bean

/**
 * 百度数据抓取结果实体
 * Author yangweining
 * Mail weining_yang@dingyuegroup.cn
 * Date 2018/10/30 15:35
 */
class CrawlerResult {
    var title :String? ="" //标题
    var author :String? ="" //作者
    var abstract :String? ="" //简介
    var source :String?="" //来源
    var newChapter :String?="" //最新章节
    var newChapterUrl :String?="" //最新章节点击跳转url
    var url :String?="" //Item点击跳转url
    var updateTime :String?="" //章节更新时间

    override fun toString(): String {
        return "title="+title+"\nauthor="+author+"\nabstract="+abstract+"\nsource="+source+"\nnewChapter"+newChapter+"\nnewChapterUrl="+newChapterUrl+"\nurl="+url+"\nupdateTime="+updateTime
    }
}