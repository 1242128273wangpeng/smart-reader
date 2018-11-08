package net.lzbook.kit.view

import com.ding.basic.bean.Interest

/**
 * Desc view回调接口
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/10/12 14:28
 */
interface InterestView {

    fun showInterestList(list: List<Interest>)

    fun showError(message: String)
}