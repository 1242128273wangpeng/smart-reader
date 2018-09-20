package net.lzbook.kit.rvextension

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import java.util.*

/**
 * Date: 2018/7/16 11:29
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 为RecyclerView 添加Header和Footer的adapter
 * 插拔式不影响原来逻辑
 */
class HFRecyclerAdapter :RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val TYPE_HEADER_VIEW = Integer.MIN_VALUE// HeaderView的Type类型
    private val MAX_COUNT = 10 // 添加Header或者Footer的最大值

    private val TYPE_FOOTER_VIEW = TYPE_HEADER_VIEW + MAX_COUNT // FooterViewde Type类型

    private val mHeaderViews = ArrayList<View>()
    private val mFooterViews = ArrayList<View>()
    private var mInnerAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>? = null

    private val mDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            notifyDataSetChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            super.onItemRangeChanged(positionStart, itemCount)
            notifyItemRangeChanged(positionStart + getHeaderViewsCount(), itemCount)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            notifyItemRangeInserted(positionStart + getHeaderViewsCount(), itemCount)
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            notifyItemRangeRemoved(positionStart + getHeaderViewsCount(), itemCount)
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount)
            val headerViewsCountCount = getHeaderViewsCount()
            notifyItemRangeChanged(fromPosition + headerViewsCountCount, toPosition + headerViewsCountCount + itemCount)
        }

    }

    internal fun setAdapter(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>?) {

        if (adapter != null) {
            if (adapter !is RecyclerView.Adapter<*>)
                throw RuntimeException("your adapter must be a RecyclerView.Adapter")
        }

        if (mInnerAdapter != null) {
            notifyItemRangeRemoved(getHeaderViewsCount(), mInnerAdapter!!.itemCount)
            mInnerAdapter!!.unregisterAdapterDataObserver(mDataObserver)
        }

        this.mInnerAdapter = adapter
        mInnerAdapter!!.registerAdapterDataObserver(mDataObserver)
        notifyItemRangeInserted(getHeaderViewsCount(), mInnerAdapter!!.itemCount)


    }

    internal fun addHeaderView(header: View?) {
        if (header == null) {
            throw RuntimeException("header is null")
        }
        if (getHeaderViewsCount() > MAX_COUNT - 1) {
            Log.e("HFRecyclerview", "header is enough")
            return
        }
        if (!mHeaderViews.contains(header)) {
            mHeaderViews.add(header)
            this.notifyDataSetChanged()
        }
    }


    internal fun addHeaderView(index: Int, header: View?) {
        if (header == null) {
            throw RuntimeException("header is null")
        }
        if (getHeaderViewsCount() > MAX_COUNT - 1) {
            Log.e("HFRecyclerview", "header is enough")
            return
        }
        if (!mHeaderViews.contains(header)) {
            mHeaderViews.add(if (index > mHeaderViews.size) mHeaderViews.size else index, header)
            this.notifyDataSetChanged()
        }
    }

    /**
     * @return header个数
     */
    internal  fun getHeaderViewsCount(): Int {
        return mHeaderViews.size
    }


    internal  fun addFooterView(footer: View?) {
        if (footer == null) {
            throw RuntimeException("footer is null")
        }

        if (getFooterViewsCount() > MAX_COUNT - 1) {
            Log.e("HFRecyclerview", "footer is enough")
            return
        }

        if (!mFooterViews.contains(footer)) {
            mFooterViews.add(footer)
            this.notifyDataSetChanged()
        }


    }

    internal fun addFooterView(index: Int, footer: View?) {
        if (footer == null) {
            throw RuntimeException("footer is null")
        }

        if (getFooterViewsCount() > MAX_COUNT - 1) {
            throw RuntimeException("footer is enough")
        }

        if (!mFooterViews.contains(footer)) {
            mFooterViews.add(if (index > mFooterViews.size) mFooterViews.size else index, footer)
            this.notifyDataSetChanged()
        }


    }

    /**
     * footer 个数
     *
     * @return
     */
    internal fun getFooterViewsCount(): Int {
        return mFooterViews.size
    }


    private fun isHeader(position: Int): Boolean {
        return getHeaderViewsCount() > 0 && position < getHeaderViewsCount()
    }

    private fun isFooter(position: Int): Boolean {
        return getFooterViewsCount() > 0 && position >= itemCount - getFooterViewsCount()
    }

    override fun getItemCount(): Int {
        return getHeaderViewsCount() + mInnerAdapter!!.itemCount + getFooterViewsCount()
    }

    override fun getItemViewType(position: Int): Int {
        val innerCount = mInnerAdapter!!.itemCount
        return if (position < getHeaderViewsCount()) {
            TYPE_HEADER_VIEW + position
        } else if (position >= getHeaderViewsCount() && position < getHeaderViewsCount() + innerCount) {
            mInnerAdapter!!.getItemViewType(position - getHeaderViewsCount())
        } else {
            TYPE_FOOTER_VIEW + position - getHeaderViewsCount() - mInnerAdapter!!.itemCount
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType >= TYPE_HEADER_VIEW && viewType < TYPE_HEADER_VIEW + MAX_COUNT) {
            ViewHolder(mHeaderViews[viewType - TYPE_HEADER_VIEW])

        } else if (viewType >= TYPE_FOOTER_VIEW && viewType < TYPE_FOOTER_VIEW + MAX_COUNT) {
            ViewHolder(mFooterViews[viewType - TYPE_FOOTER_VIEW])
        } else {
            mInnerAdapter!!.createViewHolder(parent, viewType)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val headerViewsCountCount = getHeaderViewsCount()
        if (position >= headerViewsCountCount && position < headerViewsCountCount + mInnerAdapter!!.itemCount) {
            mInnerAdapter!!.onBindViewHolder(holder, position - headerViewsCountCount)
        }

    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        mInnerAdapter!!.onViewAttachedToWindow(holder)
        // 处理瀑布流Staggered模式下添加header/footer问题

        val position = holder.layoutPosition
        if (isHeader(position) || isFooter(position)) {
            val layoutParams = holder.itemView.layoutParams
            if (layoutParams != null && layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                layoutParams.isFullSpan = true
            }

        }


    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        //        处理网格布局GridLayoutManager 模式下添加header/footer的问题
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is GridLayoutManager) {
            val lookup = layoutManager.spanSizeLookup
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (isHeader(position) || isFooter(position)) {
                        layoutManager.spanCount
                    } else lookup.getSpanSize(position - getHeaderViewsCount())
                }
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
