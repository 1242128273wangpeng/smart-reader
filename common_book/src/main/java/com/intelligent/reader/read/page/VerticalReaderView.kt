package com.intelligent.reader.read.page

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.intelligent.reader.read.help.IReadPageChange
import com.intelligent.reader.read.help.IReadView
import com.intelligent.reader.read.mode.ReadInfo
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.NovelLineBean
import java.util.ArrayList

class VerticalReaderView : RecyclerView, IReadView {

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun entrance(mReadInfo: ReadInfo) {

    }

    override fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(width, height, oldw, oldh)

    }

    override fun freshTime(time: CharSequence?) {

    }

    override fun freshBattery(percent: Float) {

    }

    override fun setBackground(background: Int) {

    }

    override fun setLoadChapter(msg:Int, chapter: Chapter, chapterList: ArrayList<ArrayList<NovelLineBean>>?) {
    }

    override fun setLoadAd(view: View) {

    }


    override fun setReadInfo(mReadInfo: ReadInfo?) {

    }

    private var mReadPageChange: IReadPageChange? = null

    override fun setIReadPageChange(mReadPageChange: IReadPageChange?) {
        this.mReadPageChange = mReadPageChange
    }
}