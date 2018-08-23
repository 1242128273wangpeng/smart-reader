package com.ding.basic.bean

import com.ding.basic.util.NotProguard
import java.io.Serializable

@NotProguard
class QQSimpleInfo : Serializable {


    var ret: Int = -1
    var msg: String = ""
    var isLost: Int = -1
    var gender: String = ""
    var isYellowVip: String? = null
    var city: String? = null
    var level: String? = null
    var figureurl_qq_2: String? = null
    var figureurl_qq_1: String? = null
    var isYellowYearVip: String? = null
    var province: String? = null
    var figureurl: String = ""
    var nickname: String = ""
    var yellowVipLevel: String? = null
    var vip: String? = null

    override fun toString(): String {
        return "QQSimpleInfo(ret=$ret, msg='$msg', isLost=$isLost, gender='$gender', isYellowVip=$isYellowVip, city=$city, level=$level, figureurl2=$figureurl_qq_2, figureurl1=$figureurl_qq_1, isYellowYearVip=$isYellowYearVip, province=$province, figureurl='$figureurl', nickname='$nickname', yellowVipLevel=$yellowVipLevel)"
    }
}