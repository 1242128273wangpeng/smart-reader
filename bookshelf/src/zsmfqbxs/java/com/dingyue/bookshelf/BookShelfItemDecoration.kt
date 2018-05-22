package com.dingyue.bookshelf

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View

class BookShelfItemDecoration constructor(private var dividerHeight: Int, private var dividerColor: Int, private var dividerType: Int) : RecyclerView.ItemDecoration() {

    private var dividerPaint: Paint? = null

    init {
        dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        dividerPaint?.color = dividerColor
        dividerPaint?.style = Paint.Style.FILL
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        super.getItemOffsets(outRect, view, parent, state)

        val layoutParams = view.layoutParams as RecyclerView.LayoutParams

        val position = layoutParams.viewLayoutPosition

        var childCount = parent.adapter.itemCount

        when (dividerType) {
            CROSS_DIVIDER -> {
                val spans = this.loadLayoutSpanCount(parent)

                when {
                    isLayoutLastRaw(parent, position, spans, childCount) -> {
                        outRect.set(0, 0, dividerHeight, 0)
                    }

                    isLayoutLastColumn(parent, position, spans, childCount) -> {
                        outRect.set(0, 0, 0, dividerHeight)
                    }

                    else -> outRect.set(0, 0, dividerHeight, dividerHeight)
                }
            }

            VERTICAL_DIVIDER -> {
                childCount -= 1
                outRect.set(0, 0, if (position != childCount) dividerHeight else 0, 0)
            }

            HORIZONTAL_DIVIDER -> {
                childCount -= 1
                outRect.set(0, 0, 0, if (position != childCount) dividerHeight else 0)
            }
        }
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        super.onDraw(canvas, parent, state)

        dividerPaint?.color = dividerColor

        when (dividerType) {
            VERTICAL_DIVIDER -> {
                drawVerticalDivider(canvas, parent)
            }
            HORIZONTAL_DIVIDER -> {
                drawHorizontalDivider(canvas, parent)
            }
            else -> {
                drawVerticalDivider(canvas, parent)
                drawHorizontalDivider(canvas, parent)
            }
        }
    }

    /***
     * 绘制横向分割线
     * **/
    private fun drawHorizontalDivider(canvas: Canvas, parent: RecyclerView) {
        val left = parent.paddingLeft
        val right = parent.measuredWidth - parent.paddingRight
        val childSize = parent.childCount

        val spans = this.loadLayoutSpanCount(parent)

        if (spans != -1) {
            if (childSize < (spans + 1)) {
                val winHeight = parent.height

                for (i in 0 until childSize) {
                    val child = parent.getChildAt(i)
                    val layoutParams = child.layoutParams as RecyclerView.LayoutParams

                    var top = child.bottom + layoutParams.bottomMargin
                    var bottom = top + dividerHeight

                    if (dividerPaint != null) {
                        canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), dividerPaint)
                    }

                    if (i == childSize - 1) {
                        while (winHeight > bottom) {
                            top += child.height
                            bottom = top + dividerHeight

                            if (dividerPaint != null) {
                                canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), dividerPaint)
                            }
                        }
                    }
                }
            } else {
                for (i in 0 until childSize) {
                    if (i % spans == 0) {
                        val child = parent.getChildAt(i)
                        val layoutParams = child.layoutParams as RecyclerView.LayoutParams

                        val top = child.bottom + layoutParams.bottomMargin
                        val bottom = top + dividerHeight

                        if (dividerPaint != null) {
                            canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), dividerPaint)
                        }
                    }
                }
            }
        } else {
            for (i in 0 until childSize) {
                val child = parent.getChildAt(i)
                val layoutParams = child.layoutParams as RecyclerView.LayoutParams

                val top = child.bottom + layoutParams.bottomMargin

                val bottom = top + dividerHeight

                if (dividerPaint != null) {
                    canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), dividerPaint)
                }
            }
        }
    }

    /***
     * 绘制纵向分割线
     * **/
    private fun drawVerticalDivider(canvas: Canvas, parent: RecyclerView) {
        val childSize = parent.childCount

        val bottom = parent.measuredHeight - parent.paddingBottom

        val spans = this.loadLayoutSpanCount(parent)

        if (spans != -1) {
            for (i in 0 until Math.min(spans, childSize)) {
                val child = parent.getChildAt(i)
                val layoutParams = child.layoutParams as RecyclerView.LayoutParams

                val top = child.top
                var left = child.right + layoutParams.rightMargin
                var right = left + dividerHeight

                if (dividerPaint != null) {
                    canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), dividerPaint)
                }

                if (childSize == 1) {
                    val winWidth = parent.width
                    while (winWidth > right) {
                        left += child.width
                        right = left + dividerHeight

                        if (dividerPaint != null) {
                            canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), dividerPaint)
                        }
                    }
                }
            }
        }
    }


    /***
     * 获取展示列数
     * **/
    private fun loadLayoutSpanCount(parent: RecyclerView): Int {
        var spans = -1

        val layoutManager = parent.layoutManager

        if (layoutManager is GridLayoutManager) {
            spans = layoutManager.spanCount
        } else if (layoutManager is StaggeredGridLayoutManager) {
            spans = layoutManager.spanCount
        }
        return spans
    }

    /***
     * 判断是否是最后一行
     * **/
    private fun isLayoutLastRaw(parent: RecyclerView, position: Int, spans: Int, childCount: Int): Boolean {
        var count = childCount
        val orientation: Int
        val layoutManager = parent.layoutManager

        if (layoutManager is GridLayoutManager) {
            count -= count % spans
            orientation = layoutManager.orientation
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                count -= count % spans
                if (position >= count) {
                    return true
                }
            } else {
                if ((position + 1) % spans == 0) {
                    return true
                }
            }
        } else if (layoutManager is StaggeredGridLayoutManager) {
            orientation = layoutManager.orientation
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                count -= count % spans
                if (position >= count) {
                    return true
                }
            } else {
                if ((position + 1) % spans == 0) {
                    return true
                }
            }
        }
        return false
    }

    /***
     * 是否是最后一列
     * **/
    private fun isLayoutLastColumn(parent: RecyclerView, position: Int, spans: Int, childCount: Int): Boolean {
        var count = childCount

        val orientation: Int
        val layoutManager = parent.layoutManager

        if (layoutManager is GridLayoutManager) {
             orientation = layoutManager.orientation
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                if ((position + 1) % spans == 0) {
                    return true
                }
            } else {
                count -= count % spans
                if (position >= count) {
                    return true
                }
            }
        } else if (layoutManager is StaggeredGridLayoutManager) {
            orientation = layoutManager.orientation
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                if ((position + 1) % spans == 0) {
                    return true
                }
            } else {
                count -= count % spans
                if (position >= count) {
                    return true
                }
            }
        }
        return false
    }

    companion object {
        //水平
        const val HORIZONTAL_DIVIDER = 0
        //垂直
        const val VERTICAL_DIVIDER = 1
        //水平+垂直
        const val CROSS_DIVIDER = 2
    }
}