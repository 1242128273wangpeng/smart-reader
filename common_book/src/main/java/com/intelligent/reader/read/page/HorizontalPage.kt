package com.intelligent.reader.read.page

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.intelligent.reader.read.animation.BitmapManager
import com.intelligent.reader.read.help.DrawTextHelper
import net.lzbook.kit.data.bean.NovelLineBean

/**
 * 水平滑动item
 * 子类逻辑
 * 1、首先viewpager 取出游标 通知子类展示哪章哪页
 * 2、子类去取数据
 * 3、子类要维护页面状态 loading success error
 * 4、状态改变通知父类 （ 页面状态 章节状态 ） ？父类游标
 * 5、父类检查 三个item 的状态 tag 做出整体的判断
 * Created by wt on 2017/12/14.
 */
class HorizontalPage : View {
    private val paint: Paint = Paint()
    var mCurPageBitmap:Bitmap? = null
    var mCurrentCanvas:Canvas? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init()
    }

    private fun init(){

    }

    fun drawPage(mDrawTextHelper: DrawTextHelper) {
        mCurPageBitmap = BitmapManager.getInstance().createBitmap()
        mCurrentCanvas = Canvas(mCurPageBitmap)
//        mDrawTextHelper.drawText(mCurrentCanvas,pageInfo)
        requestLayout()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mCurPageBitmap != null) {
            if (!mCurPageBitmap!!.isRecycled)  canvas.drawBitmap(mCurPageBitmap, 0f, 0f, paint)
        }
    }
}