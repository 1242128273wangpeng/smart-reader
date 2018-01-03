package net.lzbook.kit.data.bean

import android.graphics.Paint
import net.lzbook.kit.data.bean.NovelLineBean
import java.util.ArrayList
import kotlin.properties.Delegates

/**
 * 阅读器配置
 * Created by wt on 2017/12/26.
 */
object ReadConfig {
    //屏幕宽度
    var screenWidth: Int = 0
    //屏幕高度
    var screenHeight: Int = 0
    //屏幕密度
    var screenDensity: Float = 0.toFloat()
    //屏幕缩放比例
    var screenScaledDensity: Float = 0.toFloat()
    //阅读页行间距
    var READ_INTERLINEAR_SPACE = 0.3f
    //阅读页段间距
    var READ_PARAGRAPH_SPACE = 1.0f
    // 阅读页默认字体大小
    var FONT_SIZE = 18
    //阅读页面章节首页字体
    var FONT_CHAPTER_SIZE = 30
    //阅读页内容页左右边距
    var READ_CONTENT_PAGE_LEFT_SPACE = 20//4-20
    //阅读页内容上下页边距
    val READ_CONTENT_PAGE_TOP_SPACE = 45//20-40
    //阅读位置偏移量
    var offset = 0
    //阅读位置偏移量
    var sequence = 0
    //章节名
    var chapterName:String? = null
    //单页内容
    var chapterNameList: ArrayList<NovelLineBean>? = null
    //WTF
    var bookNameList: ArrayList<NovelLineBean>? = null
    //小说名
    var bookName:String? = null
    //小说作者
    var bookAuthor: String? = null
    //内容宽度
    var mWidth: Float = 0.0f
    //内容起始绘制位置
    var mLineStart: Float = 0.0f
    // 绘制文字的画笔
    var mPaint: Paint? = null
    // 行间距
    var mLineSpace: Float = 0.0f
    // 文字行高度(包括文字高度和行间距)
    var mFontHeight: Float = 0.0f
    // 段间距
    var mDuan: Float = 0.0f
}