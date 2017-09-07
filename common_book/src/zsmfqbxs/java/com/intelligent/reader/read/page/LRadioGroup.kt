package com.intelligent.reader.read.page

import android.content.Context
import android.support.annotation.IdRes
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import android.widget.LinearLayout


/**
 * Created by xian on 2017/8/9.
 */
class LRadioGroup : LinearLayout {

    private val mCheckables = ArrayList<View>()

    constructor(context: Context) : super(context) {}

    @JvmOverloads constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun addView(child: View, index: Int,
                         params: android.view.ViewGroup.LayoutParams) {
        super.addView(child, index, params)
        parseChild(child)
    }

    fun parseChild(child: View) {
        if (child is Checkable) {
            mCheckables.add(child)
            child.setOnClickListener {
                check(child.id)
                mOnCheckedChangeListener?.invoke(child.id)
            }
        } else if (child is ViewGroup) {
            parseChildren(child as ViewGroup)
        }
    }

    fun parseChildren(child: ViewGroup) {
        for (i in 0..child.childCount - 1) {
            parseChild(child.getChildAt(i))
        }
    }

    fun clearCheck() {
        for (i in 0..mCheckables.size - 1) {
            val view = mCheckables[i] as Checkable
            view.isChecked = false
        }
    }

    fun check(@IdRes id: Int) {
        for (i in 0..mCheckables.size - 1) {
            val view = mCheckables[i]
            if (view.id == id) {
                (view as Checkable).isChecked = true
            } else {
                (view as Checkable).isChecked = false
            }
        }
    }

    private var mOnCheckedChangeListener: ((Int) -> Unit)? = null

    fun setOnCheckedChangeListener(listener: ((Int) -> Unit)?) {
        mOnCheckedChangeListener = listener
    }

}