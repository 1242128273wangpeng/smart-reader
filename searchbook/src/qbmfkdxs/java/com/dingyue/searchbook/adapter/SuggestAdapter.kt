package com.dingyue.searchbook.adapter

import android.support.v4.content.ContextCompat
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.ding.basic.bean.SearchCommonBeanYouHua
import com.dingyue.searchbook.R
import net.lzbook.kit.utils.AppUtils


/**
 * Desc：自动补全实体类
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/9/25 0025 17:18
 */
class SuggestAdapter(private val list: MutableList<Any>?, val editInput: String) : BaseAdapter() {

    override fun getViewTypeCount(): Int {
        return ITEM_VIEW_TYPE_COUNT
    }

    override fun getItemViewType(position: Int): Int {
        list?.let {
            if (it[position] is SearchCommonBeanYouHua) {
                return if ((it[position] as SearchCommonBeanYouHua).viewType == 1) {
                    ITEM_VIEW_TYPE_GAP
                } else {
                    ITEM_VIEW_TYPE_DATA
                }

            }
        }

        return ITEM_VIEW_TYPE_GAP
    }

    override fun getCount(): Int {
        return list?.size ?: 0
    }

    override fun getItem(position: Int): Any? {
        return if (list == null) null else list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val context = parent.context
        val holder: ViewHolder
        var suggestView = convertView

        //判断suggestView的type，通过type判断item是显示数据还是Title
        when (getItemViewType(position)) {
            //数据填充item
            ITEM_VIEW_TYPE_DATA -> {

                if (suggestView == null) {

                    suggestView = LayoutInflater.from(context).inflate(R.layout.item_suggest, parent, false)

                    holder = ViewHolder()
                    holder.iv_icon = suggestView.findViewById(R.id.iv_icon)
                    holder.iv_type = suggestView.findViewById(R.id.iv_type)
                    holder.iv_shadow = suggestView.findViewById(R.id.iv_shadow)
                    holder.tv_search_item = suggestView.findViewById(R.id.tv_search_item)

                    suggestView.tag = holder
                } else {
                    holder = suggestView.tag as ViewHolder
                }


                val bean = list!![position] as SearchCommonBeanYouHua


                //如果不是以上三种的话，说明返回的数据为书籍名，则通过url加载后台返回的图片URL地址（加上非空判断）

                // 动态修改关键字颜色
                when (bean.wordtype) {
                    "author" -> {
                        holder.iv_shadow.visibility = View.GONE
                        holder.iv_icon.setImageResource(R.drawable.search_personal)
                        holder.iv_type.setImageResource(R.drawable.search_writer)

                    }
                    "label" -> {
                        holder.iv_shadow.visibility = View.GONE
                        holder.iv_icon.setImageResource(R.drawable.search_label_icon)
                        holder.iv_type.setImageResource(R.drawable.search_label)

                    }
                    "name" -> {
                        holder.iv_shadow.visibility = View.VISIBLE
                        //如果不是以上三种的话，说明返回的数据为书籍名，则通过url加载后台返回的图片URL地址（加上非空判断）
                        if (bean.image_url != null) {
                            Glide.with(context).load(bean.image_url).placeholder(
                                    R.drawable.bg_book_cover_default).error(
                                    R.drawable.bg_book_cover_default).into(holder.iv_icon)
                        } else {
                            holder.iv_icon.setImageResource(R.drawable.bg_book_cover_default)
                        }
                        holder.iv_type.setImageResource(R.drawable.search_book)
                    }
                }

                // 动态修改关键字颜色
                var content = bean.suggest
                val finalInput = AppUtils.deleteAllIllegalChar(editInput)
                val color = ContextCompat.getColor(context, R.color.primary)
                val colorTag = String.format("<font color='%s'>", AppUtils.colorHoHex(color))
                content = content.replace(finalInput.toRegex(), "$colorTag$finalInput</font>")
                holder.tv_search_item.text = Html.fromHtml(content)

            }

            // item中间的gap显示
            ITEM_VIEW_TYPE_GAP ->

                if (suggestView == null) {
                    suggestView = LayoutInflater.from(context).inflate(
                            R.layout.item_suggest_title, parent, false)

                }
            else -> {
            }
        }


        return suggestView
    }


    fun clear() {
        list?.clear()
    }

    private class ViewHolder {
        lateinit var iv_icon: ImageView
        lateinit var tv_search_item: TextView
        lateinit var iv_type: ImageView
        lateinit var iv_shadow: ImageView
    }

    companion object {
        //item的类型
        private val ITEM_VIEW_TYPE_DATA = 0
        private val ITEM_VIEW_TYPE_GAP = 1
        private val ITEM_VIEW_TYPE_COUNT = 2
    }


}