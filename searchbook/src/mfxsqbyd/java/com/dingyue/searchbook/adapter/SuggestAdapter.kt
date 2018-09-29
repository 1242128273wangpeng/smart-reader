package com.dingyue.searchbook.adapter

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.ding.basic.bean.SearchCommonBeanYouHua
import com.dingyue.searchbook.R
import com.dycm_adsdk.utils.LogUtils
import net.lzbook.kit.utils.logger.AppLog
import net.lzbook.kit.utils.AppUtils


/**
 * Desc：自动补全实体类
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/9/25 0025 17:18
 */
class SuggestAdapter(private val list: MutableList<Any>?, editInput: String) : BaseAdapter() {

    private var editInput: String? = ""

    init {
        this.editInput = editInput

    }

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
//        list[position]
//        return if (list!![position] is SearchCommonBeanYouHua){
//            ITEM_VIEW_TYPE_DATA
//        }
//
//        else
//            ITEM_VIEW_TYPE_GAP
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
                    holder.rl_book = suggestView.findViewById(R.id.rl_book)
                    holder.tv_book_name = suggestView.findViewById(R.id.tv_book_name)
                    holder.tv_author = suggestView.findViewById(R.id.tv_author)
                    holder.tv_search_item = suggestView.findViewById(R.id.tv_search_item)

                    suggestView.tag = holder
                } else {
                    holder = suggestView.tag as ViewHolder
                }


                val bean = list!![position] as SearchCommonBeanYouHua

                var content = bean.suggest
                var finalInput = ""

                if (editInput != null) {
                    finalInput = AppUtils.deleteAllIllegalChar(editInput)
                }
                content = content.replace(finalInput.toRegex(), "<font color='#FFBA01'>$finalInput</font>")


                when (bean.wordtype) {
                    "author" -> {
                        holder.rl_book.visibility = View.GONE
                        holder.tv_search_item.visibility = View.VISIBLE
                        holder.tv_search_item.text = Html.fromHtml(content)
                    }
                    "label" -> {
                        holder.rl_book.visibility = View.GONE
                        holder.tv_search_item.visibility = View.VISIBLE
                        holder.tv_search_item.text = Html.fromHtml(content)
                    }
                    "name" -> {
                        holder.rl_book.visibility = View.VISIBLE
                        holder.tv_search_item.visibility = View.GONE
                        holder.tv_book_name.text = Html.fromHtml(content)
                        holder.tv_author.text = bean.author
                        //如果不是以上三种的话，说明返回的数据为书籍名，则通过url加载后台返回的图片URL地址（加上非空判断）
                        if (bean.image_url != null) {
                            Glide.with(context).load(bean.image_url).placeholder(
                                    R.drawable.bg_book_cover_default).error(
                                    R.drawable.bg_book_cover_default).into(
                                    holder.iv_icon)
                        }
                    }


                }
            }


            // item中间的gap显示
            ITEM_VIEW_TYPE_GAP ->

                if (suggestView == null) {
                    suggestView = LayoutInflater.from(context).inflate(
                            R.layout.item_suggest_title, parent, false)

                    val descText = suggestView.findViewById<TextView>(R.id.tv_desc)

                    if (position + 1 < list!!.size) {
                        val type = (list[position + 1] as SearchCommonBeanYouHua).wordtype

                        descText.text = when (type) {
                            "name" -> "图书"
                            "label" -> "标签"
                            "author" -> "作者"
                            else -> {
                                "图书"
                            }
                        }
                    }

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
        lateinit var rl_book: RelativeLayout
        lateinit var iv_icon: ImageView
        lateinit var tv_search_item: TextView
        lateinit var iv_type: ImageView
        lateinit var tv_book_name: TextView
        lateinit var tv_author: TextView
    }

    fun setEditInput(editInput: String) {
        this.editInput = editInput
    }

    companion object {
        //item的类型
        private val ITEM_VIEW_TYPE_DATA = 0
        private val ITEM_VIEW_TYPE_GAP = 1
        private val ITEM_VIEW_TYPE_COUNT = 2
    }


}