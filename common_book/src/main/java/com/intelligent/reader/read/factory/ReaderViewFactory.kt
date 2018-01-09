package com.intelligent.reader.read.factory

import android.content.Context
import com.intelligent.reader.read.animation.ShiftTransformer
import com.intelligent.reader.read.help.IReadView
import net.lzbook.kit.data.bean.ReadViewEnums
import com.intelligent.reader.read.page.HorizontalReaderView
import com.intelligent.reader.read.page.VerticalReaderView

/**
 * IReaderView 工厂类
 * Created by wt on 2017/12/14.
 */
class ReaderViewFactory(val context: Context)  {

    /**
     * @param anim 动画模式
     * @return 获取IReadView对象
     */
    fun getView(anim: ReadViewEnums.Animation): IReadView {
        return when (anim) {
            ReadViewEnums.Animation.list -> {
                VerticalReaderView(context)
            }
            ReadViewEnums.Animation.shift ->{
                HorizontalReaderView(context, ShiftTransformer())
            }
            ReadViewEnums.Animation.curl ->{
                HorizontalReaderView(context)
            }
            else -> {
                HorizontalReaderView(context)
            }
        }
    }
}