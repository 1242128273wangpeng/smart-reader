package com.dingyue.downloadmanager.recl

import android.graphics.*
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup

/**
 * Created on 2018/4/25.
 * Created by crazylei.
 **/
open class DownloadItemDecoration internal constructor(private val downloadHeaderInterface: DownloadHeaderInterface, private val firstHeaderHeight: Int, private val headerHeight: Int) : RecyclerView.ItemDecoration() {

    private val headerViewCache = HashMap<Boolean, Bitmap>()

    var paint = Paint()

    init {
        paint.color = Color.parseColor("#00000000")
    }

    override fun getItemOffsets(outRect: Rect, view: View, recyclerView: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, recyclerView, state)

        val position = recyclerView.getChildAdapterPosition(view)

        downloadHeaderInterface.requestItemCacheState(position) ?: return

        if (position == 0 || checkItemShowHeader(position)) {
            if (position == 0) {
                outRect.top = firstHeaderHeight
            } else {
                outRect.top = headerHeight
            }
        } else {
            outRect.top = 0
        }
    }

    override fun onDraw(canvas: Canvas, recyclerView: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, recyclerView, state)

        val left = recyclerView.paddingLeft
        val right = recyclerView.width - recyclerView.paddingRight

        val childCount = recyclerView.childCount

        for (index in 0 until childCount) {
            val view = recyclerView.getChildAt(index)
            val position = recyclerView.getChildAdapterPosition(view)

            if (position == 0 || checkItemShowHeader(position)) {
                val height = if (position == 0) {
                    firstHeaderHeight
                } else {
                    headerHeight
                }

                val top = view.top - height
                val bottom = view.top


                canvas.drawRect(left.toFloat(), (top - height).toFloat(), right.toFloat(), bottom.toFloat(), paint)

                val bitmap = requestHeaderBitmap(position, left, right)

                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, left.toFloat(), (bottom - height).toFloat(), null)
                }
            }
        }
    }

    private fun requestHeaderBitmap(position: Int, left: Int, right: Int): Bitmap? {
        val cacheState: Boolean? = downloadHeaderInterface.requestItemCacheState(position)

        var headerBitmap: Bitmap? = null

        if (cacheState != null) {
            headerBitmap = headerViewCache[cacheState]
        }

        if (headerBitmap == null) {
            val header: View = downloadHeaderInterface.requestItemHeaderView(position)
                    ?: return null

            measureHeaderView(header, left, right)

            headerBitmap = Bitmap.createBitmap(header.drawingCache)

            if (cacheState != null) {
                headerViewCache[cacheState] = headerBitmap
            }
        }
        return headerBitmap
    }

    private fun measureHeaderView(header: View, left: Int, right: Int) {
        header.isDrawingCacheEnabled = true

        val layoutParams = ViewGroup.LayoutParams(right, headerHeight)
        header.layoutParams = layoutParams

        header.measure(View.MeasureSpec.makeMeasureSpec(right, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(headerHeight, View.MeasureSpec.EXACTLY))
        header.layout(left, 0 - headerHeight, right, 0)
    }


    private fun checkItemShowHeader(position: Int): Boolean {
        return if (position == 0) {
            true
        } else {
            val currentState = downloadHeaderInterface.requestItemCacheState(position)
            val previousState = downloadHeaderInterface.requestItemCacheState(position - 1)
            previousState != currentState
        }
    }

    internal interface DownloadHeaderInterface {
        fun requestItemCacheState(position: Int): Boolean?

        fun requestItemHeaderView(position: Int): View?
    }
}