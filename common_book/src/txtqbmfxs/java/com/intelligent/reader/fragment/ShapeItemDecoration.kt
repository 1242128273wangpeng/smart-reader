package com.intelligent.reader.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout

/**
 * Created by xian on 2017/8/19.
 */

class ShapeItemDecoration
/**
 * Creates a divider [RecyclerView.ItemDecoration] that can be used with a
 * [LinearLayoutManager].
 *
 * @param context Current context, it will be used to access resources.
 * @param orientation Divider orientation. Should be [.HORIZONTAL] or [.VERTICAL].
 */
(context: Context, orientation: Int) : RecyclerView.ItemDecoration() {

    private var mDivider: Drawable? = null

    /**
     * Current orientation. Either [.HORIZONTAL] or [.VERTICAL].
     */
    private var mOrientation: Int = 0

    private val mBounds = Rect()

    init {
        val a = context.obtainStyledAttributes(ATTRS)
        mDivider = a.getDrawable(0)
        a.recycle()
        setOrientation(orientation)
    }

    /**
     * Sets the orientation for this divider. This should be called if
     * [RecyclerView.LayoutManager] changes orientation.
     *
     * @param orientation [.HORIZONTAL] or [.VERTICAL]
     */
    fun setOrientation(orientation: Int) {
        if (orientation != HORIZONTAL && orientation != VERTICAL) {
            throw IllegalArgumentException(
                    "Invalid orientation. It should be either HORIZONTAL or VERTICAL")
        }
        mOrientation = orientation
    }

    /**
     * Sets the [Drawable] for this divider.
     *
     * @param drawable Drawable that should be used as a divider.
     */
    fun setDrawable(drawable: Drawable?) {
        if (drawable == null) {
            return
        }
        mDivider = drawable
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        if (parent.layoutManager == null) {
            return
        }
        if (mOrientation == VERTICAL) {
            drawVertical(c, parent)
        } else {
            drawHorizontal(c, parent)
        }
    }

    @SuppressLint("NewApi")
    private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val left: Int
        val right: Int
        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            canvas.clipRect(left, parent.paddingTop, right,
                    parent.height - parent.paddingBottom)
        } else {
            left = 0
            right = parent.width
        }

        val childCount = parent.childCount
        for (i in 0..childCount - 1) {
            if (i == parent.childCount - 1)
                break
            val child = parent.getChildAt(i)
            parent.getDecoratedBoundsWithMargins(child, mBounds)
            val bottom = mBounds.bottom + Math.round(ViewCompat.getTranslationY(child))
            val top = bottom - mDivider!!.intrinsicHeight
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(canvas)
        }
        canvas.restore()
    }

    @SuppressLint("NewApi")
    private fun drawHorizontal(canvas: Canvas, parent: RecyclerView) {
        canvas.save()
        val top: Int
        val bottom: Int
        if (parent.clipToPadding) {
            top = parent.paddingTop
            bottom = parent.height - parent.paddingBottom
            canvas.clipRect(parent.paddingLeft, top,
                    parent.width - parent.paddingRight, bottom)
        } else {
            top = 0
            bottom = parent.height
        }

        val childCount = parent.childCount
        for (i in 0..childCount - 1) {
            if (i == parent.childCount - 1)
                break
            val child = parent.getChildAt(i)
            parent.layoutManager.getDecoratedBoundsWithMargins(child, mBounds)
            val right = mBounds.right + Math.round(ViewCompat.getTranslationX(child))
            val left = right - mDivider!!.intrinsicWidth
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(canvas)
        }
        canvas.restore()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                state: RecyclerView.State?) {
        if (mOrientation == VERTICAL) {
            outRect.set(0, 0, 0, mDivider!!.intrinsicHeight)
        } else {
            outRect.set(0, 0, mDivider!!.intrinsicWidth, 0)
        }
    }

    companion object {
        val HORIZONTAL = LinearLayout.HORIZONTAL
        val VERTICAL = LinearLayout.VERTICAL

        private val ATTRS = intArrayOf(android.R.attr.listDivider)
    }
}
