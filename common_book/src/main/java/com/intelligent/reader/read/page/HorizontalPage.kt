package com.intelligent.reader.read.page

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.intelligent.reader.read.DataProvider
import com.intelligent.reader.read.animation.BitmapManager
import com.intelligent.reader.read.help.DrawTextHelper
import com.intelligent.reader.read.help.ReadSeparateHelper
import com.intelligent.reader.read.mode.ReadCursor
import com.intelligent.reader.read.mode.ReadViewEnums
import net.lzbook.kit.data.bean.Chapter
import java.util.*

/**
 * 水平滑动item
 * 子类逻辑
 * 1、首先viewpager 取出游标 通知子类展示哪章哪页
 * 2、子类去取数据
 * 3、子类要维护页面状态 loading success error
 * 4、状态改变通知父类 （ 页面状态 章节状态 ） ？父类游标
 * 5、父类检查 三个item 的状态 tag 做出整体的判断
 * 页面状态逻辑
 * 默认loding
 * 如果当前页为loading 禁止vp滑动
 * error
 * 重新加载数据
 *
 * Created by wt on 2017/12/14.
 */
class HorizontalPage : View {
    private val paint: Paint = Paint()
    var mCurPageBitmap: Bitmap? = null
    var mCurrentCanvas: Canvas? = null
    private var mDrawTextHelper: DrawTextHelper? = null
    var cursor: ReadCursor? = null

    constructor(context: Context, noticePageListener: NoticePageListener) : this(context, null, noticePageListener)

    constructor(context: Context, attrs: AttributeSet?, noticePageListener: NoticePageListener) : this(context, attrs, 0, noticePageListener)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, noticePageListener: NoticePageListener) : super(context, attrs, defStyleAttr) {
        this.noticePageListener = noticePageListener
        init()
    }

    private fun init() {
        mDrawTextHelper = DrawTextHelper(context.resources)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mCurPageBitmap != null) {
            if (!mCurPageBitmap!!.isRecycled) canvas.drawBitmap(mCurPageBitmap, 0f, 0f, paint)
        }
    }

    /**
     * 入口模式
     * 加载3章至内存
     */
    fun entrance(cursor: ReadCursor) {
        DataProvider.getInstance().loadChapter(cursor.curBook, cursor.sequence, ReadViewEnums.PageIndex.current, object : DataProvider.ReadDataListener {
            override fun loadDataSuccess(c: Chapter, type: ReadViewEnums.PageIndex) {
                //当前章拉去成功 执行一般方法
                //true 通知其他页面加载
                setCursor(cursor, true)
            }
            override fun loadDataError(message: String) = Unit
        })
        DataProvider.getInstance().loadChapter(cursor.curBook, cursor.sequence, ReadViewEnums.PageIndex.previous, object : DataProvider.ReadDataListener {
            override fun loadDataSuccess(c: Chapter, type: ReadViewEnums.PageIndex) = Unit
            override fun loadDataError(message: String) = Unit
        })
        DataProvider.getInstance().loadChapter(cursor.curBook, cursor.sequence, ReadViewEnums.PageIndex.next, object : DataProvider.ReadDataListener {
            override fun loadDataSuccess(c: Chapter, type: ReadViewEnums.PageIndex) = Unit
            override fun loadDataError(message: String) = Unit
        })
    }

    /**
     * 一般模式
     * 1、判断缓存
     * 2、预加载数据
     * 3、展示数据
     * @param cursor
     * @param entrance true：入口模式进入
     */
    fun setCursor(cursor: ReadCursor, entrance: Boolean) {
        this.cursor = cursor
        val provider = DataProvider.getInstance()
        //判断预加载
        val chapterKeyArray = provider.chapterMap.keys.toIntArray()
        Arrays.sort(chapterKeyArray)
        if (chapterKeyArray.isNotEmpty() and (cursor.sequence >= chapterKeyArray.last())) {//预加载
            //如果请求6章，最后内存是6 循环请求1次、如果请求7章，最后内存是6 循环请求2次
            for (i in chapterKeyArray.last()..cursor.sequence + 1) {
                DataProvider.getInstance().loadChapter(cursor.curBook, i, ReadViewEnums.PageIndex.previous, object : DataProvider.ReadDataListener {
                    override fun loadDataSuccess(c: Chapter, type: ReadViewEnums.PageIndex)  = Unit
                    override fun loadDataError(message: String) = Unit
                })
            }
            if(chapterKeyArray.size>4){//清除内存缓存
                provider.chapterMap.remove(chapterKeyArray.first())
            }
        }

        //判断item 需要的章节是否在缓存
        val chapter = provider.chapterMap[cursor.sequence]
        if (chapter != null) {//加载数据
            drawPage(cursor, chapter, entrance)
        } else {//无缓存数据
            entrance(cursor)
        }
    }

    /**
     * 画页面
     * @param entrance true：入口模式进入
     */
    private fun drawPage(cursor: ReadCursor, chapter: Chapter, entrance: Boolean) {
        cursor.readStatus.chapterName = chapter.chapter_name
        val chapterList = ReadSeparateHelper.getInstance(cursor.readStatus).initTextSeparateContent(chapter.content)//分页
        if (!chapterList.isEmpty() and (cursor.pageIdex <= chapterList.size)) {//集合不为空，角标小于集合长度
            if (entrance) {//通知其他页面加载，矫正坐标
                noticePageListener?.curPageChangSuccess(cursor.pageIdex, chapterList.size)//当前页数和当前总页数
            }
            this.cursor!!.pageIdexSum = chapterList.size//确定总长度
            val pageList = if (cursor.pageIdex == -1) {//分页前不知道上一章长度
                this.cursor!!.pageIdex = chapterList.size//确定长度
                chapterList[chapterList.size - 1]
            } else {
                chapterList[cursor.pageIdex - 1]
            }
            mCurPageBitmap = BitmapManager.getInstance().createBitmap()
            mCurrentCanvas = Canvas(mCurPageBitmap)
            mDrawTextHelper?.drawText(mCurrentCanvas, pageList)
            postInvalidate()
        }
    }

    var noticePageListener: NoticePageListener? = null

    interface NoticePageListener {
        fun curPageChangSuccess(pageIndex: Int, pageSum: Int)
    }
}