package com.intelligent.reader.presenter.interest

import android.content.Context
import com.ding.basic.bean.BasicResult
import com.ding.basic.bean.Interest
import com.ding.basic.bean.InterestDto
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.RequestSubscriber
import net.lzbook.kit.utils.loge

/**
 * Desc 选择兴趣相关
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/10/12 14:17
 */
class InterestPresenter(val context: Context, private val interestView: InterestView?) {

    /**
     * 获取兴趣列表
     */
    fun getInterestList() {
        // 获取网络数据
        RequestRepositoryFactory.loadRequestRepositoryFactory(context)
                .getInterestList(object : RequestSubscriber<BasicResult<InterestDto>>() {
                    override fun requestResult(result: BasicResult<InterestDto>?) {
                        result?.let {
                            if (it.checkResultAvailable()) {
                                val list = parseData(it.data!!.labelOne, it.data!!.labelTwo)
                                // 刷新UI
                                interestView?.showInterestList(list)
                            } else interestView?.showError("网络不可用")
                        }
                    }

                    override fun requestError(message: String) {
                        loge("requestError============$message")
                        interestView?.showError(message)
                    }
                })
    }

    private fun parseData(one: List<String>?, two: List<String>?): List<Interest> {
        if (one == null || two == null) return emptyList()
        val list = ArrayList<Interest>()
        (0 until (one.size + two.size)).map {
            val pos: Int = it / 2
            if (pos % 2 == 0) {
                // 偶数 取one中的数据
                val item = Interest(one[pos])
                item.type = 1
                list.add(item)
            } else {
                // 奇数 取two
                val item = Interest(two[pos])
                item.type = 2
                list.add(item)
            }
        }
        return list
    }
}