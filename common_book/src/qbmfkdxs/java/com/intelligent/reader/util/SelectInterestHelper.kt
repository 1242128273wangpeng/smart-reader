package com.intelligent.reader.util

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.view.View
import android.widget.CompoundButton
import com.ding.basic.bean.Interest
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.intelligent.reader.R

import kotlinx.android.synthetic.qbmfkdxs.select_interest.view.*
import net.lzbook.kit.presenter.InterestPresenter
import net.lzbook.kit.utils.antiShakeClick
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.utils.toast.ToastUtil.showToastMessage
import net.lzbook.kit.view.InterestView

/**
 * Desc 选择兴趣辅助类
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/10/13 11:57
 */
class SelectInterestHelper(val view: View, val context: Context) : InterestView, View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    var overListener: (() -> Unit)? = null

    private val interestPresenter by lazy { InterestPresenter(context, this) }

    var selectList: MutableList<Interest> = ArrayList()
    var listData: MutableList<Interest> = ArrayList()

    init {
        initInterestData()
        initListener()
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.txt_step_in -> stepIn()
            R.id.txt_confirm -> selectConfirm()
        }
    }

    //[Interest(name=玄幻), Interest(name=古代言情), Interest(name=都市生活), Interest(name=现代言情), Interest(name=异术超能),
    // Interest(name=玄幻言情), Interest(name=末世危机), Interest(name=N次元), Interest(name=都市系统), Interest(name=青春校园)]
    override fun showInterestList(list: List<Interest>) {
        if (list.isNotEmpty() && list.size == 10) {
            view.img_guide.visibility = View.GONE
            view.rl_container.visibility = View.VISIBLE
            listData.addAll(list)
            view.cbx_xh.text = list.get(0).name
            view.cbx_gdyq.text = list.get(1).name
            view.cbx_dssh.text = list.get(2).name
            view.cbx_xdyq.text = list.get(3).name
            view.cbx_yscr.text = list.get(4).name
            view.cbx_xhyq.text = list.get(5).name
            view.cbx_mswj.text = list.get(6).name
            view.cbx_ncy.text = list.get(7).name
            view.cbx_dsxt.text = list.get(8).name
            view.cbx_qcxy.text = list.get(9).name
        } else {

            view.img_guide.visibility = View.VISIBLE
            view.rl_container.visibility = View.GONE
        }
    }

    override fun showError(message: String) {
        ToastUtil.showToastMessage(message)
        view.img_guide.visibility = View.VISIBLE
        view.rl_container.visibility = View.GONE
    }

    /**
     * 初始化监听
     */
    private fun initListener() {
        view.txt_step_in.antiShakeClick(this)
        view.txt_confirm.antiShakeClick(this)
        view.cbx_yscr.setOnCheckedChangeListener(this)
        view.cbx_gdyq.setOnCheckedChangeListener(this)
        view.cbx_qcxy.setOnCheckedChangeListener(this)
        view.cbx_dsxt.setOnCheckedChangeListener(this)
        view.cbx_xdyq.setOnCheckedChangeListener(this)
        view.cbx_xhyq.setOnCheckedChangeListener(this)
        view.cbx_xh.setOnCheckedChangeListener(this)
        view.cbx_dssh.setOnCheckedChangeListener(this)
        view.cbx_ncy.setOnCheckedChangeListener(this)
        view.cbx_mswj.setOnCheckedChangeListener(this)

    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (listData.size == 10) {
            when (buttonView?.id) {
                R.id.cbx_yscr -> {
                    if (isChecked) selectList.add(listData.get(4))
                }
                R.id.cbx_gdyq -> {
                    if (isChecked) selectList.add(listData.get(1))
                }
                R.id.cbx_qcxy -> {
                    if (isChecked) selectList.add(listData.get(9))
                }
                R.id.cbx_dsxt -> {
                    if (isChecked) selectList.add(listData.get(8))
                }
                R.id.cbx_xdyq -> {
                    if (isChecked) selectList.add(listData.get(3))
                }
                R.id.cbx_xhyq -> {
                    if (isChecked) selectList.add(listData.get(5))
                }
                R.id.cbx_xh -> {
                    if (isChecked) selectList.add(listData.get(0))
                }
                R.id.cbx_dssh -> {
                    if (isChecked) selectList.add(listData.get(2))
                }
                R.id.cbx_ncy -> {
                    if (isChecked) selectList.add(listData.get(7))
                }
                R.id.cbx_mswj -> {
                    if (isChecked) selectList.add(listData.get(6))
                }

            }
        }

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
        SPUtils.putDefaultSharedInt(SPKey.HAS_SELECT_INTEREST, -1)
        overListener?.invoke()
    }


    /**
     * 提交选中项
     */
    private fun selectConfirm() {
        if (listData.isNotEmpty() && selectList.isEmpty()) ToastUtil.showToastMessage("请选择喜欢的类型")
        else {
            // 加载动画
            loading(1)
            // 保存数据
            SPUtils.putDefaultSharedInt(SPKey.HAS_SELECT_INTEREST, 1)
            SPUtils.putDefaultSharedObject(SPKey.SELECTED_INTEREST_DATA, selectList)
            // 延时关闭页面
            val loadingAnimation = ObjectAnimator.ofFloat(view, "alpha", 1f, 1f)
            loadingAnimation.duration = 900
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