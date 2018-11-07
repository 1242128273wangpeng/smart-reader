package com.intelligent.reader.util

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.support.v7.widget.GridLayoutManager
import android.view.View
import com.ding.basic.bean.Interest
import com.dingyue.contract.util.SharedPreUtil
import com.dingyue.contract.util.showToastMessage
import com.intelligent.reader.R
import com.intelligent.reader.adapter.InterestAdapter
import com.intelligent.reader.adapter.RecyclerBaseAdapter
import com.intelligent.reader.presenter.interest.InterestPresenter
import com.intelligent.reader.presenter.interest.InterestView
import kotlinx.android.synthetic.txtqbdzs.select_interest.view.*
import net.lzbook.kit.utils.antiShakeClick

/**
 * Desc 选择兴趣辅助类
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/10/13 11:57
 */
class SelectInterestHelper(val view: View, val context: Context) : InterestView, View.OnClickListener, RecyclerBaseAdapter.OnItemClickListener {

    var overListener: (() -> Unit)? = null

    private val interestPresenter by lazy { InterestPresenter(context, this) }
    private val sharedPreUtil by lazy { SharedPreUtil(SharedPreUtil.SHARE_DEFAULT) }
    private var interestAdapter: InterestAdapter? = null

    init {
        initView()
        initListener()
        initInterestData()
    }

    override fun onItemClick(view: View, position: Int) {
        interestAdapter?.notifySelected(view, position)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.txt_step_in -> stepIn()
            R.id.txt_confirm -> selectConfirm()
        }
    }

    override fun showInterestList(list: List<Interest>) {
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

    override fun showError(message: String) {
        context.showToastMessage(message)
    }

    private fun initView() {
        view.rv_interest.layoutManager = GridLayoutManager(context, 2)
    }

    /**
     * 初始化监听
     */
    private fun initListener() {
        view.txt_step_in.antiShakeClick(this)
        view.txt_confirm.antiShakeClick(this)
    }

    /**
     * 初始化兴趣数据
     */
    private fun initInterestData() {
        interestPresenter.getInterestList()
    }

    /**
     * 跳过
     */
    private fun stepIn() {
        sharedPreUtil.putInt(SharedPreUtil.HAS_SELECT_INTEREST, -1)
        overListener?.invoke()
    }

    /**
     * 提交选中项
     */
    private fun selectConfirm() {
        // 判断选中项
        interestAdapter?.let {
            val list = it.getAllSelectedList()
            if (list.isEmpty()) context.showToastMessage("请选择喜欢的类型")
            else {
                // 加载动画
                loading(1)
                // 保存数据
                sharedPreUtil.putInt(SharedPreUtil.HAS_SELECT_INTEREST, 1)
                sharedPreUtil.putObject(SharedPreUtil.SELECTED_INTEREST_DATA, list)
                // 延时关闭页面
                val loadingAnimation = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.8f)
                loadingAnimation.duration = 1500
                loadingAnimation.start()
                loadingAnimation.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {

                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        handler.removeMessages(1)
                        handler.removeMessages(2)
                        handler.removeMessages(3)
                        overListener?.invoke()
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
                view.txt_confirm.text = "正在进入."
                loading(2)
            }
            2 -> {
                view.txt_confirm.text = "正在进入.."
                loading(3)
            }
            3 -> {
                view.txt_confirm.text = "正在进入..."
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