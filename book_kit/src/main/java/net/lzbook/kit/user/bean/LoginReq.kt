package net.lzbook.kit.user.bean

import net.lzbook.kit.user.DeviceID
import net.lzbook.kit.utils.NotProguard
import net.lzbook.kit.utils.OpenUDID

/**
 * Created by xian on 2017/6/20.
 */
@NotProguard
data class LoginReq(var facilityId: String,
                    var udid: String,
                    var pageName: String,
                    var nickname: String,
                    var headPortrait: String,
                    var sex: String,
                    var uidThird: String,
                    var code: String,
                    var loginPlatform: Int
) {
    companion object {
        @JvmStatic fun createWeChatReq(context: android.content.Context, code: String): LoginReq {
            return LoginReq(DeviceID.getOpenUDIDInContext(context), OpenUDID.getOpenUDIDInContext(context), context.packageName, "", "", "", "", code, 1)
        }

        @JvmStatic fun createQQReq(context: android.content.Context, nickname: String,
                                   headPortrait: String,
                                   sex: String,
                                   uidThird: String): LoginReq {
            return LoginReq(DeviceID.getOpenUDIDInContext(context), OpenUDID.getOpenUDIDInContext(context), context.packageName, nickname, headPortrait, sex, uidThird, "", 0)
        }
    }

    override fun toString(): String {
        return "LoginReq(facilityId='$facilityId', udid='$udid', pageName='$pageName', nickname='$nickname', headPortrait='$headPortrait', sex='$sex', uidThird='$uidThird', code='$code', mLoginPlatform=$loginPlatform)"
    }


}