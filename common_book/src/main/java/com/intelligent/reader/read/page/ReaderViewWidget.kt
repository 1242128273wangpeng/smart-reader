package com.intelligent.reader.read.page

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import com.intelligent.reader.read.factory.ReaderViewFactory
import com.intelligent.reader.read.help.IReadPageChange
import com.intelligent.reader.read.help.IReadView
import com.intelligent.reader.read.help.IReadWidget
import com.intelligent.reader.read.mode.ReadInfo
import com.intelligent.reader.read.mode.ReadViewEnums
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.NovelLineBean
import java.util.ArrayList

/**
 * 阅读容器
 * Created by wt on 2017/12/13.
 */
class ReaderViewWidget : FrameLayout, IReadWidget {

    private var mReaderViewFactory: ReaderViewFactory? = null

    private var mReaderView: IReadView? = null

    private var mTextureView: TextureView? = null

    private var animaEnums: ReadViewEnums.Animation? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    /**
     * 初始化GLSufaceView
     */
    fun initGLSufaceView() {
        if (mTextureView == null) {
//          mTextureView =
            addView(mTextureView)
        }
    }

    /**
     * 初始化ReaderViewFactory
     */
    fun initReaderViewFactory() {
        if (mReaderViewFactory == null) {
            mReaderViewFactory = ReaderViewFactory(context)
        }
    }

    /**
     * 入口
     * @param mReadInfo 阅读信息
     */
    override fun entrance(mReadInfo: ReadInfo) {
        if (animaEnums != mReadInfo.animaEnums) { //如果阅读模式发生变化
            if (mReaderView != null) removeView(mReaderView as View)//移除
            mReaderView = mReaderViewFactory?.getView(mReadInfo.animaEnums)//创建
            if (mReaderView != null) addView(mReaderView as View)//添加
            animaEnums = mReadInfo.animaEnums//记录动画模式
        }
        mReaderView?.entrance(mReadInfo)
    }

    /**
     * 设置时间
     * @param time 时间
     */
    override fun freshTime(time: CharSequence?) {
        mReaderView?.freshTime(time)
    }

    /**
     * 设置电池
     * @param percent 电量
     */
    override fun freshBattery(percent: Float) {
        mReaderView?.freshBattery(percent)
    }

    /**
     * 设置背景颜色
     * @param background 颜色值
     */
    override fun setBackground(background: Int) {
        mReaderView?.setBackground(background)
    }

    /**
     * 设置阅读信息
     * @param mReadInfo 新阅读信息
     */
    override fun setReadInfo(mReadInfo: ReadInfo) {
        mReaderView?.setReadInfo(mReadInfo)
    }

    /**
     * 设置 IReadView 实现 View 的变化监听
     * @param mReadPageChange 监听对象
     */
    override fun setIReadPageChange(mReadPageChange: IReadPageChange?) {
        mReaderView?.setIReadPageChange(mReadPageChange)
    }

    /**
     * 返回章节
     * @param chapter 监听对象
     */
    override fun setLoadChapter(msg:Int, chapter: Chapter, chapterList: ArrayList<ArrayList<NovelLineBean>>?) {
        mReaderView?.setLoadChapter(msg,chapter,chapterList)
    }

    /**
     * 返回广告
     * @param view 广告view
     */
    override fun setLoadAd(view: View?) {
        mReaderView?.setLoadAd(view)
    }
}