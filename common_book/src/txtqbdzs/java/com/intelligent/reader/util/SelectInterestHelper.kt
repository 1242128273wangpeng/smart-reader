package com.intelligent.reader.util

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.support.v7.widget.GridLayoutManager
import android.view.View
import com.ding.basic.bean.Interest
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.intelligent.reader.R
import com.intelligent.reader.adapter.InterestAdapter
import com.reyun.tracking.common.CommonUtil
import kotlinx.android.synthetic.txtqbdzs.select_interest.view.*
import net.lzbook.kit.ui.adapter.base.RecyclerBaseAdapter
import net.lzbook.kit.utils.antiShakeClick
import net.lzbook.kit.utils.toast.ToastUtil

/**
 * Desc 选择兴趣辅助类
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/10/13 11:57
 */
class SelectInterestHelper(val view: View, val context: Context) : View.OnClickListener, RecyclerBaseAdapter.OnItemClickListener {

    var overListener: (() -> Unit)? = null
    var animationOverListener: (() -> Unit)? = null

    private var interestAdapter: InterestAdapter? = null
    private var isLoading = false

    init {
        initView()
        initListener()
    }

    override fun onItemClick(view: View, position: Int) {
        if (isLoading) return
        interestAdapter?.notifySelected(view, position)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_step_in -> stepIn()
            R.id.btn_confirm -> selectConfirm()
        }
    }

    fun showInterestList(list: List<Interest>) {
        if (list.isNotEmpty()) {
            if (interestAdapter == null) {
                interestAdapter = InterestAdapter(context)
                interestAdapter!!.setData(list)
                interestAdapter!!.setOnItemClickListener(this)
                view.rv_interest.adapter = interestAdapter
            } else {
                interestAdapter!!.setData(list)
                interestAdapter!!.notifyDataSetChanged()
            }
            interestAdapter?.turnAll()
        }
    }

    private fun initView() {
        view.rv_interest.layoutManager = GridLayoutManager(context, 2)
    }

    /**
     * 初始化监听
     */
    private fun initListener() {
        view.btn_step_in.antiShakeClick(this)
        view.btn_confirm.antiShakeClick(this)
    }

    /**
     * 跳过
     */
    private fun stepIn() {
        if (isLoading) return
        SPUtils.putDefaultSharedInt(SPKey.HAS_SELECT_INTEREST, -1)
        overListener?.invoke()
        animationOverListener?.invoke()
    }

    /**
     * 提交选中项
     */
    private fun selectConfirm() {
        // 判断选中项
        interestAdapter?.let {
            val list = it.getAllSelectedList()
            if (list.isEmpty()) ToastUtil.showToastMessage("请选择喜欢的类型")
            else {
                view.btn_confirm.isEnabled = false
                view.btn_step_in.isEnabled = false
                isLoading = true
                // 加载动画
                loading(1)
                // 保存数据
                SPUtils.putDefaultSharedInt(SPKey.HAS_SELECT_INTEREST, 1)
                SPUtils.putDefaultSharedObject(SPKey.SELECTED_INTEREST_DATA, list)
                // 延时关闭页面
                val loadingAnimation = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.95f)
                loadingAnimation.duration = 1500
                loadingAnimation.start()
                overListener?.invoke()
                loadingAnimation.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {

                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        handler.removeMessages(1)
                        handler.removeMessages(2)
                        handler.removeMessages(3)
                        animationOverListener?.invoke()
                    }

                    override fun onAnimationCancel(animation: Animator?) {

                    }

                    override fun onAnimationStart(animation: Animator?) {

                    }

                })
            }
        }
    }

    private val handler = Handler(Handler.Callback {
        when (it.what) {
            1 -> {
                view.btn_confirm.text = "正在进入."
                loading(2)
            }
            2 -> {
                view.btn_confirm.text = "正在进入.."
                loading(3)
            }
            3 -> {
                view.btn_confirm.text = "正在进入..."
                loading(1)
            }
        }
        return@Callback false
    })

    /**
     * 正在进入动画
     */
    private fun loading(what: Int) {
        handler.sendEmptyMessageDelayed(what, 300)
    }
}