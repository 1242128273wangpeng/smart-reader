package com.intelligent.reader.adapter

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import com.ding.basic.bean.Interest
import com.ding.basic.util.AnimationUtil
import com.intelligent.reader.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.txtqbdzs.item_interest.view.*
import net.lzbook.kit.ui.adapter.base.RecyclerBaseAdapter
import net.lzbook.kit.utils.AppUtils
import java.util.concurrent.TimeUnit

/**
 * Desc 兴趣列表适配器
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/10/12 14:38
 */
class InterestAdapter(context: Context) : RecyclerBaseAdapter<Interest>(context, R.layout.item_interest) {

    private var itemLayoutParams: FrameLayout.LayoutParams? = null
    private var containerPadding = 0
    private val animationTime = 150L
    private var turn = false
    private val paddingLeft_dp by lazy { AppUtils.dip2px(context, 14f) }

    companion object {
        val animatingPosition = ArrayList<String>()
    }

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

            txt_interest_name_selected.layoutParams = itemLayoutParams
            txt_interest_name_selected.text = data.name

            val imgName = "icon_interest_${position + 1}"
            val imgNameSelected = "icon_interest_select_${position + 1}"

            try {
                txt_interest_name.setBackgroundResource(resources.getIdentifier(imgName, "drawable", context.packageName))
                txt_interest_name_selected.setBackgroundResource(resources.getIdentifier(imgNameSelected, "drawable", context.packageName))
                txt_interest_name.setPadding(paddingLeft_dp,0,0,0)
                txt_interest_name_selected.setPadding(paddingLeft_dp,0,0,0)
            } catch (e: Resources.NotFoundException) {
                e.printStackTrace()
            }

            // 默认将选中项翻转
            if (turn) AnimationUtil.flipAnimatorYViewShow(txt_interest_name_selected, txt_interest_name, 130L, position)
        }
    }

    /**
     * 翻转全部
     */
    fun turnAll() {
        Observable.timer(450, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy {
                    turn = true
                    notifyDataSetChanged()
                }
    }

    /**
     * 通知选中项（改变动画和属性）
     */
    fun notifySelected(view: View, position: Int) {
        if (animatingPosition.contains(position.toString())) return
        animatingPosition.add(position.toString())
        if (list[position].selected) {
            // 变为未选中
            list[position].selected = false
            AnimationUtil.flipAnimatorYViewShow(view.txt_interest_name_selected, view.txt_interest_name, animationTime, position) {
                animatingPosition.remove(it.toString())
            }
        } else {
            // 变为选中
            list[position].selected = true
            AnimationUtil.flipAnimatorYViewShow(view.txt_interest_name, view.txt_interest_name_selected, animationTime, position) {
                animatingPosition.remove(it.toString())
            }
        }
    }

    /**
     * 获取所有选中项
     */
    fun getAllSelectedList(): List<Interest> {
        return list.filter { it.selected }
    }

}