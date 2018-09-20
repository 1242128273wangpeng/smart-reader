package net.lzbook.kit.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.GridView


/**
 * Desc：重写GridView的onMeasure方法，使其不会出现滚动条，ScrollView嵌套ListView也是同样的道理
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/9/20 0020 10:17
 */
class ScrollForGridView : GridView {

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)


    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val expandSpec = View.MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE shr 2, View.MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, expandSpec)
    }

}