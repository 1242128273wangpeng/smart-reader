package net.lzbook.kit.bean.user

import net.lzbook.kit.utils.NotProguard

@NotProguard
class QQSimpleInfo {

/*
{
    "ret": 0,
    "msg": "",
    "is_lost": 0,
    "nickname": "Danny",
    "gender": "男",
    "province": "北京",
    "city": "海淀",
    "figureurl": "http://qzapp.qlogo.cn/qzapp/1105963470/F65F48D936A4649A60558E0BC3432E29/30",
    "figureurl_1": "http://qzapp.qlogo.cn/qzapp/1105963470/F65F48D936A4649A60558E0BC3432E29/50",
    "figureurl_2": "http://qzapp.qlogo.cn/qzapp/1105963470/F65F48D936A4649A60558E0BC3432E29/100",
    "figureurl_qq_1": "http://q.qlogo.cn/qqapp/1105963470/F65F48D936A4649A60558E0BC3432E29/40",
    "figureurl_qq_2": "http://q.qlogo.cn/qqapp/1105963470/F65F48D936A4649A60558E0BC3432E29/100",
    "is_yellow_vip": "0",
    "vip": "0",
    "yellow_vip_level": "0",
    "level": "0",
    "is_yellow_year_vip": "0"
}
*/

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
