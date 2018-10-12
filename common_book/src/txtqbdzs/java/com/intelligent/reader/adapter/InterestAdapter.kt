package com.intelligent.reader.adapter

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import com.ding.basic.bean.Interest
import com.intelligent.reader.R
import kotlinx.android.synthetic.txtqbdzs.item_interest.view.*

/**
 * Desc 兴趣列表适配器
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/10/12 14:38
 */
class InterestAdapter(context: Context) : RecyclerBaseAdapter<Interest>(context, R.layout.item_interest) {

    private var itemLayoutParams: FrameLayout.LayoutParams? = null
    private var containerPadding = 0

    init {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        wm.defaultDisplay.getMetrics(dm)
        val screenW = dm.widthPixels
        itemLayoutParams = FrameLayout.LayoutParams(screenW / 3, screenW / 3 / 2)
        containerPadding = screenW / 3 / 3 / 2
    }

    override fun bindView(itemView: View, data: Interest, position: Int) {
        with(itemView) {
            fl_container.setPadding(containerPadding, containerPadding, containerPadding, 0)
            txt_interest_name.layoutParams = itemLayoutParams
            txt_interest_name.text = data.name
            val imgName: String
            if (data.selected) {
                // 选中
                imgName = "icon_interest_select_${position + 1}"
                txt_interest_name.setTextColor(Color.WHITE)
            } else {
                // 未选中
                imgName = "icon_interest_${position + 1}"
                txt_interest_name.setTextColor(Color.parseColor("#616161"))
            }
            try {
                txt_interest_name.setBackgroundResource(resources.getIdentifier(imgName, "drawable", context.packageName))
            } catch (e: Resources.NotFoundException) {
                e.printStackTrace()
            }
        }
    }
}