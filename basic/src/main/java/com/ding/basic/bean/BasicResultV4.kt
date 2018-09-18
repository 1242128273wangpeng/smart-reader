package com.ding.basic.bean

import com.ding.basic.net.ResultCode
import org.greenrobot.eventbus.EventBus
import java.io.Serializable

class BasicResultV4<T> : Serializable {

    var respCode: Int = 0

    var data: T? = null

    var message: String? = null

    fun checkResultAvailable(): Boolean {
        return if (respCode == ResultCode.RESULT_SUCCESS && data != null) {
            true
        } else {
            if (respCode.toString()=="40001"){
                EventBus.getDefault().postSticky(UserEvent(UserEvent.LOGIN_INVALID))
            }else if (respCode.toString()=="40003"){
                EventBus.getDefault().postSticky(UserEvent(UserEvent.LOGIN_OUT_DATE))
            }

            message = parseErrorMsg(respCode.toString())
            false
        }
    }

    override fun toString(): String {
        return "BasicResult(code=$respCode, data=$data, msg=$message)"
    }

    fun parseErrorMsg(msgCode: String): String {
        when (msgCode) {

            "50000" -> {
                return "未知错误"
            }
            "401" -> {
                return "登录错误"
            }
            "40001" -> {
                return "登录失效"
            }
            "40002" -> {
                return "Token错误"
            }
            "40003" -> {
                return "登录过期，请重新登录"
            }
            "40101" -> {
                return "用户名已存在"
            }
            "40102" -> {
                return "用户名不符合要求"
            }
            "40103" -> {
                return "手机号已存在"
            }
            "40104" -> {
                return "手机号不符合要求"
            }
            "40105" -> {
                return "头像类型错误"
            }
            "40106" -> {
                return "头像过大"
            }
            "40107" -> {
                return "上传头像失败"
            }
            "40108" -> {
                return "用户不存在"
            }
            "40109" -> {
                return "用户名修改次数超限"
            }
            "40110" -> {
                return "性别参数错误"
            }
            "40111" -> {
                return "第三方头像路径为空"
            }
            "40112" -> {
                return "第三方昵称为空"
            }
            "40113" -> {
                return "昵称涉嫌违规，请重新编辑"
            }
            "40114" -> {
                return "图片涉嫌违规，请重新上传"
            }
            "40201" -> {
                return "此账号已注册"
            }
            "40202" -> {
                return "此手机号已注册"
            }
            "40203" -> {
                return "此账号已绑定"
            }
            "40301" -> {
                return "验证码已发送"
            }
            "40302" -> {
                return "验证码已过期，请重新获取"
            }
            "40303" -> {
                return "验证码错误，请重新输入"
            }
            "40304" -> {
                return "短信发送失败"
            }
            "40305" -> {
                return "手机短信验证码生成错误"
            }
            "40306" -> {
                return "手机短信发送超限"
            }
            "49997" -> {
                return "服务器繁忙，请稍后再试"
            }
            "49998" -> {
                return "参数错误"
            }
            "49999" -> {
                return "未知异常"
            }
            "80001" -> {
                return "服务不可用哦，请稍后再试"
            }
            else -> {
                return "网络不给力哦，请稍后再试"
            }

        }
    }
}