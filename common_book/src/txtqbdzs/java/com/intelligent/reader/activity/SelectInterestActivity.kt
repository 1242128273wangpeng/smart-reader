package com.intelligent.reader.activity

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.KeyEvent
import android.view.View
import com.ding.basic.bean.Interest
import com.intelligent.reader.R
import com.intelligent.reader.adapter.InterestAdapter
import com.intelligent.reader.adapter.RecyclerBaseAdapter
import com.intelligent.reader.presenter.interest.InterestPresenter
import com.intelligent.reader.presenter.interest.InterestView
import iyouqu.theme.FrameActivity
import kotlinx.android.synthetic.txtqbdzs.act_select_interest.*
import net.lzbook.kit.utils.antiShakeClick

/**
 * Desc 选择兴趣
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/10/12 11:14
 */
class SelectInterestActivity : FrameActivity(), View.OnClickListener, InterestView, RecyclerBaseAdapter.OnItemClickListener {

    private val interestPresenter by lazy { InterestPresenter(this, this) }
    private var interestAdapter: InterestAdapter? = null

    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        setContentView(R.layout.act_select_interest)
        initView()
        initListener()
        initInterestData()
    }

    override fun onItemClick(view: View, position: Int) {

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.txt_step_in -> finish()
            R.id.txt_confirm -> selectConfirm()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event)
    }

    override fun supportSlideBack(): Boolean {
        return false
    }

    override fun showInterestList(list: List<Interest>) {
        if (list.isNotEmpty()) {
            if (interestAdapter == null) {
                interestAdapter = InterestAdapter(this)
                interestAdapter!!.setData(list)
                interestAdapter!!.setOnItemClickListener(this)
                rv_interest.adapter = interestAdapter
            } else {
                interestAdapter!!.setData(list)
                interestAdapter!!.notifyDataSetChanged()
            }
        }
    }

    private fun initView() {
        rv_interest.layoutManager = GridLayoutManager(this, 2)
    }

    /**
     * 初始化兴趣数据
     */
    private fun initInterestData() {
        interestPresenter.getInterestList()
    }

    /**
     * 初始化监听
     */
    private fun initListener() {
        txt_step_in.antiShakeClick(this)
        txt_confirm.antiShakeClick(this)
    }

    /**
     * 提交选中项
     */
    private fun selectConfirm() {

    }
}