package net.lzbook.kit.data.bean

import android.graphics.Paint
import net.lzbook.kit.constants.Constants
import java.util.*

/**
 * 阅读器配置
 * Created by wt on 2017/12/26.
 */
object ReadConfig :Observable(){
    //动画模式
    var animation = when (Constants.PAGE_MODE) {
        1 -> ReadViewEnums.Animation.curl
        2 -> ReadViewEnums.Animation.shift
        3 -> ReadViewEnums.Animation.list
        else -> ReadViewEnums.Animation.slide
    }
    //屏幕宽度
    var screenWidth: Int = 0
    //屏幕高度
    var screenHeight: Int = 0
    //屏幕密度
    var screenDensity: Float = 0.toFloat()
    //屏幕缩放比例
    var screenScaledDensity: Float = 0.toFloat()
    //阅读页段间距
    var READ_PARAGRAPH_SPACE = 1.0f
    //阅读页行间距
    var READ_INTERLINEAR_SPACE = 0.3f
        set(value){
            if (field != value) {
                field = value
                setChanged()
                notifyObservers("READ_INTERLINEAR_SPACE")
            }
        }
    // 阅读字体大小
    var FONT_SIZE = 18
        set(value) {
            if (field != value) {
                field = value
                setChanged()
                notifyObservers("FONT_SIZE")
            }
        }
    //阅读模式 背景
    var MODE:Int = 51
        set(value) {
            if (field != value) {
                field = value
                setChanged()
                notifyObservers("MODE")
            }
        }
    //横竖屏切换
    var IS_LANDSCAPE = false
        set(value) {
            if (field != value) {
                field = value
                setChanged()
                notifyObservers("SCREEN")
            }
        }
    //跳章 改为true 则为跳章
    var jump:Boolean = false
        set(value) {
            if (value) {
                setChanged()
                notifyObservers("JUMP")
            }
        }

    // 阅读页默认字体大小
    val FONT_CHAPTER_DEFAULT = 18
    //阅读页面章节首页字体
    var FONT_CHAPTER_SIZE = 30
    //阅读页内容页左右边距
    var READ_CONTENT_PAGE_LEFT_SPACE = 20//4-20
    //阅读页内容上下页边距
    var READ_CONTENT_PAGE_TOP_SPACE = 45//20-40
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

    fun registObserver(observer: Observer){
        addObserver(observer)
    }
    fun unregistObserver(observer: Observer){
        deleteObserver(observer)
    }
    fun unregistObserverAll(){
        deleteObservers()
    }
}