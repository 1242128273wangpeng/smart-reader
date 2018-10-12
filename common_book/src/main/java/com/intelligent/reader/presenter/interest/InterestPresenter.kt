package com.intelligent.reader.presenter.interest

import android.app.Activity
import com.ding.basic.bean.Interest

/**
 * Desc 选择兴趣相关
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/10/12 14:17
 */
class InterestPresenter(val activity: Activity, private val interestView: InterestView?) {

    /**
     * 获取兴趣列表
     */
    fun getInterestList() {
        // 获取网络数据
        val list = ArrayList<Interest>()
        (0..9).map {
            list.add(Interest("兴趣：$it"))
        }
        // 刷新UI
        interestView?.showInterestList(list)
    }
}