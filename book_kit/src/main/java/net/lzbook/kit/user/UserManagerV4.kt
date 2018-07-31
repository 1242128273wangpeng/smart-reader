package net.lzbook.kit.user

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import com.alibaba.fastjson.JSON
import com.ding.basic.Config
import com.ding.basic.bean.BasicResultV4
import com.ding.basic.bean.LoginRespV4
import com.ding.basic.bean.QQSimpleInfo
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.RequestSubscriber
import com.google.gson.Gson
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.data.user.ThirdLoginReq
import net.lzbook.kit.data.user.ThirdLoginReq.Companion.CHANNEL_QQ
import net.lzbook.kit.user.bean.AvatarReq
import net.lzbook.kit.user.bean.UserNameState
import net.lzbook.kit.user.bean.WXAccess
import net.lzbook.kit.user.bean.WXSimpleInfo
import net.lzbook.kit.utils.log
import net.lzbook.kit.utils.loge
import net.lzbook.kit.utils.logi
import okhttp3.RequestBody
import org.json.JSONObject
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Date: 2018/7/30 15:29
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 用户中心管理类v4接口
 */
object UserManagerV4 : IWXAPIEventHandler {

    private var mWXApi: IWXAPI? = null
    private var mTencent: Tencent? = null
    var wxAppID = ""
    var wxAppSecret = ""
    var qqAppID = ""
    private const val LAST_LOGIN = "last_login"
    const val LOGIN_METHOD = "login_method"
    const val LOGIN_ID = "login_id"
    private var lastLoginId: String? = null
    /**
     * 是否已经登录
     */
    private var mUserState = AtomicBoolean(false)

    var isUserLogin = false
        private set
        get() {
            return mUserState.get()
        }
    var sharedPreferences: SharedPreferences? = null
    /**
     * new 用户信息
     */
    var user: LoginRespV4? = null
        private set

    private var isBind = false
    var successCallback: ((BasicResultV4<LoginRespV4>) -> Unit)? = null
    var failedCallback: ((String) -> Unit)? = null
    var mInitCallback: ((Boolean) -> Unit)? = null
    private var mInited = false
    val repositoryFactory: RequestRepositoryFactory by lazy {
        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
    }

    /**
     * 初始化登录平台
     */
    fun initPlatform(context: Context?, callback: ((Boolean) -> Unit)? = null) {

        if (context == null) {
            log("registerApp", "cant init with null context")
            return
        }

        if (!mInited) {
            mInited = true

            mInitCallback = callback

            if (context.packageManager != null) {
                val appInfo = context.packageManager
                        .getApplicationInfo(context.packageName,
                                PackageManager.GET_META_DATA)

                if (appInfo != null) {

                    wxAppID = appInfo.metaData[UserConstants.WECHAT_APPID].toString()
                    wxAppSecret = appInfo.metaData[UserConstants.WECHAT_SECRET].toString()
                    loge("wxAppSecret", wxAppSecret)
                    qqAppID = appInfo.metaData[UserConstants.QQ_APPID].toString()
                    qqLoginListener = QQLoginListener(qqAppID)
                    log("initPlatform", wxAppID, qqAppID)


                    if (wxAppID == null || qqAppID == null) {
                        log("initPlatform", "cant init with null params")
                        return
                    }
                    // 通过WXAPIFactory工厂，获取IWXAPI的实例
                    mWXApi = WXAPIFactory.createWXAPI(context.applicationContext, wxAppID, true)
                    val registerApp = mWXApi?.registerApp(wxAppID)
                    log("registerApp", registerApp)

                    try {
                        mTencent = Tencent.createInstance(qqAppID, context.getApplicationContext())
                    } catch (exception: ExceptionInInitializerError) {
                        exception.printStackTrace()
                    }

                    sharedPreferences = context.getSharedPreferences(LAST_LOGIN, Context.MODE_PRIVATE)
                    lastLoginId = sharedPreferences?.getString(LOGIN_ID, null)

                    user = repositoryFactory.queryLoginUser()
                    if (user != null) {
                        logi(user.toString())
                        mUserState.set(true)
                        Config.insertRequestParameter("loginToken", user!!.token!!)
                        mInitCallback?.invoke(true)
//                mOriginBookShelfData = queryAllBook()
//                mOriginBookMarksData = getBookMarkBody(user?.accountId ?: "", queryAllBook())
                    } else {
                        mInitCallback?.invoke(false)
                    }
                }
            }
        } else {
            callback?.invoke(true)
        }
    }

    /**
     * 检测第三方登录平台是否可用
     */
    fun isPlatformEnable(platform: Platform): Boolean {
        var enable = false
        when (platform) {
            Platform.WECHAT -> {
                enable = mWXApi?.isWXAppInstalled ?: false
            }
            Platform.QQ -> {
                enable = true
            }
        }
        println("${platform.name} $enable")
        return enable
    }

    /**
     * 第三方登录
     */
    fun thirdLogin(activity: Activity, platform: Platform, isBind: Boolean = false,
                   onSuccess: ((BasicResultV4<LoginRespV4>) -> Unit)? = null,
                   onFailure: ((String) -> Unit)? = null) {

        this.isBind = isBind

        successCallback = { ret ->
            onSuccess?.invoke(ret)
            successCallback = null
        }
        failedCallback = { ret ->
            onFailure?.invoke(ret)
            failedCallback = null
        }
        if (Platform.WECHAT == platform) {
            val req = SendAuth.Req()
            req.scope = "snsapi_userinfo"
            req.state = "wechat_sdk_demo_test" + Random().nextFloat()
            mWXApi?.sendReq(req)
        } else if (Platform.QQ == platform) {
            mTencent?.login(activity, "get_simple_userinfo", qqLoginListener)
        }
    }


    fun handleIntent(intent: Intent?) {
        log("handleIntent")
        if (intent != null)
            mWXApi?.handleIntent(intent, this)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Tencent.onActivityResultData(requestCode, resultCode, data, null)
    }

    private var qqLoginListener: QQLoginListener? = null

    /**
     * QQ登录
     */
    private class QQLoginListener(private val qqAppID: String)
        : IUiListener {

        override fun onComplete(ret: Any?) {
            logi("onComplete", ret.toString())

            val jsonObject = JSON.parseObject(ret.toString())
            val code = jsonObject.getIntValue("ret")
            if (code != 0) {
                failedCallback?.invoke("$code")
                return
            }

            val openid = jsonObject.getString("openid")
            val accessToken = jsonObject.getString("access_token")
            val expiresIn = jsonObject.getString("expires_in")
            RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                    .requestUserInformation(accessToken, qqAppID, openid, object : RequestSubscriber<QQSimpleInfo>() {

                        override fun requestResult(result: QQSimpleInfo?) {
                            if (result != null) {

                                val avatarUrl: String = result.figureurl_qq_2
                                        ?: result.figureurl_qq_1 ?: ""
                                val qqReq = ThirdLoginReq(openid, accessToken,
                                        expiresIn, "", "", CHANNEL_QQ,
                                        result.nickname, result.gender, avatarUrl)
                                val body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),
                                        Gson().toJson(qqReq))
                                logi("isBind: $isBind")

                                if (isBind) {
                                    logi("绑定第三方")
                                    bindThirdAccount(body)

                                } else {
                                    logi("登录第三方")
                                    thirdLogin(body)
                                }
                            }


                        }

                        override fun requestError(message: String) {
                            failedCallback?.invoke(message)
                        }

                    })


        }


        override fun onCancel() {
            loge("onCancel")
            failedCallback?.invoke("已取消")
        }

        override fun onError(p0: UiError?) {
            loge("onError", "${p0?.errorCode}, ${p0?.errorMessage}")
            failedCallback?.invoke("QQ错误")
        }

    }

    /**
     * 保存登录用户信息
     */
    private fun onLogin(user: LoginRespV4) {
        mUserState.set(true)
        this.user = user
        Config.insertRequestParameter("loginToken", user!!.token!!)
        logi(user.toString())
        repositoryFactory.insertOrUpdate(user)
        if (lastLoginId != null && lastLoginId != user.account_id) {
            StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                    StartLogClickUtil.LOGIN, StartLogClickUtil.UIDDIFFUSER)
        }
        lastLoginId = user.account_id
    }


    /**
     * 退出登录
     */
    fun logout(onLogout: (() -> Unit)? = null) {
        if (mUserState.get()) {
            onLogout(onLogout)
        }
    }

    private fun onLogout(onLogout: (() -> Unit)?) {
        mUserState.set(false)
        this.user = null
        repositoryFactory.deleteLoginUser()
//        if (onLogout == null) return@uploadReadInfo

        repositoryFactory.requestLogout(object : RequestSubscriber<BasicResultV4<String>>() {
            override fun requestResult(result: BasicResultV4<String>?) {
                onLogout?.invoke()

            }

            override fun requestError(message: String) {
                onLogout?.invoke()
            }

        })


    }

    /**
     * 更新用户数据
     */
    fun updateUser(user: LoginRespV4) {
        logi(user.toString())
        this.user = user
        repositoryFactory.insertOrUpdate(user)
    }

    override fun onResp(resp: BaseResp?) {
        logi("onResp : ", resp.toString())
        when (resp?.errCode) {
            BaseResp.ErrCode.ERR_OK -> {
                if (resp.type != 1) {
                    failedCallback?.invoke("微信未知错误")
                    return
                }
                val auth = resp as SendAuth.Resp
                val wxReq = ThirdLoginReq(ThirdLoginReq.CHANNEL_WX)

                RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                        .requestWXAccessToken(wxAppID, wxAppSecret, auth.code, "authorization_code", object : RequestSubscriber<WXAccess>() {
                            override fun requestResult(result: WXAccess?) {
                                if (result != null) {
                                    wxReq.oauthId = result.openid
                                    wxReq.accessToken = result.access_token
                                    wxReq.accessTokenSeconds = result.expires_in
                                    wxReq.refreshToken = result.refresh_token
                                    wxReq.refreshTokenSeconds = "2592000"
                                    requestWXUserInfo(wxReq, result.access_token, result.openid)

                                }


                            }

                            override fun requestError(message: String) {

                            }

                        })


            }
            BaseResp.ErrCode.ERR_USER_CANCEL -> {
                failedCallback?.invoke("已取消")
            }
            BaseResp.ErrCode.ERR_AUTH_DENIED -> {
                //用户拒绝授权
                failedCallback?.invoke("已拒绝")
            }
            BaseResp.ErrCode.ERR_UNSUPPORT -> {
                failedCallback?.invoke("微信不支持")
            }
            else -> {
                //unknown
                failedCallback?.invoke("微信未知错误")
            }
        }

    }

    override fun onReq(p0: BaseReq?) {

    }

    /**
     * 绑定第三方账号
     */
    fun bindThirdAccount(accountBody: RequestBody) {
        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                .bindThirdAccount(accountBody, object : RequestSubscriber<BasicResultV4<LoginRespV4>>() {
                    override fun requestResult(result: BasicResultV4<LoginRespV4>?) {
                        if (result?.checkResultAvailable()!!) {
                            successCallback!!.invoke(result)
                            onLogin(result.data!!)
                        } else {
                            failedCallback?.invoke(result.message.toString())
                        }
                    }

                    override fun requestError(message: String) {
                        failedCallback?.invoke(message)
                    }

                })
    }

    /**
     * 第三方登录
     */
    private fun thirdLogin(loginBody: RequestBody) {

        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                .thirdLogin(loginBody, object : RequestSubscriber<BasicResultV4<LoginRespV4>>() {
                    override fun requestResult(result: BasicResultV4<LoginRespV4>?) {
                        if (result?.checkResultAvailable()!!) {
                            successCallback!!.invoke(result)
                            onLogin(result.data!!)

                        } else {
                            failedCallback?.invoke(result.message.toString())
                        }
                    }

                    override fun requestError(message: String) {
                        failedCallback?.invoke(message)
                    }

                })

    }


    /**
     * 获取短信验证码
     */
    fun requestSmsCode(mobile: String, callback: ((Boolean, String) -> Unit)) {
        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                .requestSmsCode(mobile, object : RequestSubscriber<BasicResultV4<String>>() {
                    override fun requestResult(result: BasicResultV4<String>?) {
                        callback.invoke(true, result!!.data!!)
                    }

                    override fun requestError(message: String) {
                        callback.invoke(false, message)
                    }

                })
    }

    /**
     * 短信验证码登录
     */
    fun requestSmsLogin(smsBody: RequestBody, callBack: ((Boolean, BasicResultV4<LoginRespV4>?) -> Unit)) {
        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                .requestSmsLogin(smsBody, object : RequestSubscriber<BasicResultV4<LoginRespV4>>() {
                    override fun requestResult(result: BasicResultV4<LoginRespV4>?) {
                        if (result?.checkResultAvailable()!!) {
                            callBack.invoke(true, result)
                            onLogin(result.data!!)
                        } else {
                            callBack.invoke(false, result)
                        }

                    }

                    override fun requestError(message: String) {
                        callBack.invoke(false, null)
                    }

                })

    }

    /**
     * 上传用户头像
     */

    fun uploadUserAvatar(bitmap: Bitmap, callBack: ((Boolean, BasicResultV4<LoginRespV4>?) -> Unit)) {
        val avatar = bitmap.toBase64()
        val avatarReq = AvatarReq("jpg", avatar)
        val body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),
                Gson().toJson(avatarReq))

        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                .uploadUserAvatar(body, object : RequestSubscriber<BasicResultV4<LoginRespV4>>() {
                    override fun requestResult(result: BasicResultV4<LoginRespV4>?) {
                        if (result?.checkResultAvailable()!!) {
                            callBack.invoke(true, result)
                        } else {
                            callBack.invoke(false, result)
                        }

                    }

                    override fun requestError(message: String) {
                        callBack.invoke(false, null)
                    }

                })
    }

    /**
     * 获取用户修改昵称剩余天数
     */

    fun requestUserNameState(callBack: ((Boolean, BasicResultV4<UserNameState>?) -> Unit)) {
        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                .requestUserNameState(object : RequestSubscriber<BasicResultV4<UserNameState>>() {
                    override fun requestResult(result: BasicResultV4<UserNameState>?) {
                        if (result?.checkResultAvailable()!!) {
                            callBack.invoke(true, result)
                        } else {
                            callBack.invoke(false, result)
                        }

                    }

                    override fun requestError(message: String) {
                        callBack.invoke(false, null)
                    }

                })
    }

    fun requestWXUserInfo(wxReq: ThirdLoginReq, token: String, openid: String) {

        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                .requestWXUserInfo(token, openid, object : RequestSubscriber<WXSimpleInfo>() {
                    override fun requestResult(result: WXSimpleInfo?) {
                        if (result != null) {
                            wxReq.name = result.nickname
                            wxReq.sex = if (result.sex == 1) "男" else "女"
                            wxReq.avatarUrl = result.headimgurl
                            val body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),
                                    Gson().toJson(wxReq))
                            logi("isBind: $isBind")
                            if (isBind) {
                                logi("绑定第三方")
                                bindThirdAccount(body)

                            } else {
                                logi("登录第三方")
                                thirdLogin(body)
                            }

                        }

                    }

                    override fun requestError(message: String) {
                        failedCallback?.invoke(message)

                    }

                })

    }

    /**
     * 修改性别
     */
    fun uploadUserGender(gender: String, callBack: ((Boolean, BasicResultV4<LoginRespV4>?) -> Unit)) {
        val map = HashMap<String, String>()
        map["gender"] = gender
        val body = RequestBody.create(okhttp3.MediaType.parse("Content-Type, application/json"),
                JSONObject(map).toString())

        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                .uploadUserGender(body, object : RequestSubscriber<BasicResultV4<LoginRespV4>>() {
                    override fun requestResult(result: BasicResultV4<LoginRespV4>?) {
                        if (result?.checkResultAvailable()!!) {
                            callBack.invoke(true, result)
                        } else {
                            callBack.invoke(false, result)
                        }

                    }

                    override fun requestError(message: String) {
                        callBack.invoke(false, null)
                    }

                })
    }
    /**
     *  修改昵称
     */

    fun uploadUserName(name: String, callBack: ((Boolean, BasicResultV4<LoginRespV4>?) -> Unit)) {
        val map = HashMap<String, String>()
        map["name"] = name
        val body = RequestBody.create(okhttp3.MediaType.parse("Content-Type, application/json"),
                JSONObject(map).toString())

        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                .uploadUserName(body, object : RequestSubscriber<BasicResultV4<LoginRespV4>>() {
                    override fun requestResult(result: BasicResultV4<LoginRespV4>?) {
                        if (result?.checkResultAvailable()!!) {
                            callBack.invoke(true, result)
                        } else {
                            callBack.invoke(false, result)
                        }

                    }

                    override fun requestError(message: String) {
                        callBack.invoke(false, null)
                    }

                })
    }

    /**
     *  修改昵称
     */

    fun bindPhoneNumber(phone: String,code:String, callBack: ((Boolean, BasicResultV4<LoginRespV4>?) -> Unit)) {
        val map = HashMap<String, String>()
        map["phoneNumber"] = phone
        map["code"]=code

        val body = RequestBody.create(okhttp3.MediaType.parse("Content-Type, application/json"),
                JSONObject(map).toString())

        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                .bindPhoneNumber(body, object : RequestSubscriber<BasicResultV4<LoginRespV4>>() {
                    override fun requestResult(result: BasicResultV4<LoginRespV4>?) {
                        if (result?.checkResultAvailable()!!) {
                            callBack.invoke(true, result)
                        } else {
                            callBack.invoke(false, result)
                        }

                    }

                    override fun requestError(message: String) {
                        callBack.invoke(false, null)
                    }

                })
    }



}