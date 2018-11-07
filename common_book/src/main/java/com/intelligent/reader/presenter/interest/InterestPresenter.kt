package com.intelligent.reader.presenter.interest

import android.content.Context
import com.ding.basic.bean.BasicResult
import com.ding.basic.bean.Interest
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
                .getInterestList(object : RequestSubscriber<BasicResult<List<Interest>>>() {
                    override fun requestResult(result: BasicResult<List<Interest>>?) {
                        result?.let {
                            if (it.checkResultAvailable()) {
                                // 刷新UI
                                interestView?.showInterestList(it.data.orEmpty())
                            } else interestView?.showError("网络不可用")
                        }
                    }

                    override fun requestError(message: String) {
                        loge("requestError============$message")
                    }
                })
    }
}