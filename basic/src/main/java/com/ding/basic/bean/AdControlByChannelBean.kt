package com.ding.basic.bean

import java.io.Serializable


/**
 * Desc 广告分渠道号，版本 动态参数
 * Author zhenxiang
 * Mail zhenxiang_lin@dingyuegroup.cn
 * Date 2018\9\7 0007 10:52
 */
class AdControlByChannelBean : Serializable{
    var respCode: String? = null
    var message: String? = null
    var data: List<DataBean>? = null

    class DataBean {

        var id: IdBean? = null
        var packageName: String? = null
        var channelId: String? = null
        var channelName: String? = null
        var version: String? = null
        var adSpaceType: String? = null
        var status: String? = null

        class IdBean {
            /**
             * timestamp : 1536227312
             * machineIdentifier : 7855012
             * processIdentifier : 7392
             * counter : 13443942
             * date : 1536227312000
             * time : 1536227312000
             * timeSecond : 1536227312
             */

            var timestamp: Int = 0
            var machineIdentifier: Int = 0
            var processIdentifier: Int = 0
            var counter: Int = 0
            var date: Long = 0
            var time: Long = 0
            var timeSecond: Int = 0
        }
    }
}