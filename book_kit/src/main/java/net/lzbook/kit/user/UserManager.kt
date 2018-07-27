package net.lzbook.kit.user

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import com.alibaba.fastjson.JSON
import com.ding.basic.bean.BasicResult
import com.ding.basic.bean.LoginResp
import com.ding.basic.bean.QQSimpleInfo
import com.ding.basic.bean.RefreshResp
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.RequestSubscriber
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.orhanobut.logger.Logger
import com.tencent.mm.opensdk.constants.ConstantsAPI
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
import net.lzbook.kit.user.bean.LoginReq
import net.lzbook.kit.utils.log
import net.lzbook.kit.utils.toMap
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Created by xian on 2017/6/20.
 */

//val WX_APPID = "wx1adbbff037154040"
//val QQ_APPID = "1105963470"


object UserManager : IWXAPIEventHandler {


    /**
     * 是否已经登录
     */
    private var mUserState = AtomicBoolean(false)

    var isUserLogin = false
        private set
        get() {
            return mUserState.get()
        }

    /**
     * 已经登录的平台, NONE, WECHAT, QQ
     */
    var mLoginPlatform = Platform.NONE

    var mSharedPreferences: SharedPreferences? = null

    /**
     * 用户信息
     */
    var mUserInfo: LoginResp? = null
        private set

    var mLogoutCallback: ((Boolean) -> Unit)? = null
    var mSuccessCallback: ((LoginResp) -> Unit)? = null
    var mFailureCallback: ((String) -> Unit)? = null
    var mRequestSmsCodeCallback: ((Boolean, String) -> Unit)? = null


    var mWXAppID = ""
    var mQQAppID = ""

    private var mWXApi: IWXAPI? = null
    private var mTencent: Tencent? = null

    private val mGson = Gson()

    var mInitCallback: ((Boolean) -> Unit)? = null

    private var mInited = false

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

                    mWXAppID = appInfo.metaData[UserConstants.WECHAT_APPID].toString()
                    mQQAppID = appInfo.metaData[UserConstants.QQ_APPID].toString()
                    log("initPlatform", mWXAppID, mQQAppID)
                    if (mWXAppID == null || mQQAppID == null) {
                        log("initPlatform", "cant init with null params")
                        return
                    }
                    // 通过WXAPIFactory工厂，获取IWXAPI的实例
                    mWXApi = WXAPIFactory.createWXAPI(context?.applicationContext, mWXAppID, true);
                    val registerApp = mWXApi?.registerApp(mWXAppID)
                    log("registerApp", registerApp)

                    try {
                        mTencent = Tencent.createInstance(mQQAppID, context?.getApplicationContext())
                    } catch (exception: ExceptionInInitializerError) {
                        exception.printStackTrace()
                    }

                    mSharedPreferences = context.getSharedPreferences(UserConstants.USER_INFO_FILE, Context.MODE_PRIVATE)

                    val user_info = mSharedPreferences?.getString(UserConstants.KEY_USER_INFO, null)

                    if (user_info != null) {
                        mUserInfo = mGson.fromJson(user_info, LoginResp::class.java)
                        mUserState.set(true)

                        refreshToken()
                    } else {
                        mInitCallback?.invoke(true)
                    }
                }
            }

        } else {
            callback?.invoke(true)
        }
    }

    /**
     * 检测相应平台是否可用
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
     * 获取短信验证码
     */
    fun requestSmsCode(mobile: String, callback: ((Boolean, String) -> Unit)) {
        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                .requestSmsCode(mobile, object : RequestSubscriber<BasicResult<String>>() {
                    override fun requestResult(result: BasicResult<String>?) {
                        callback.invoke(true, result!!.data!!)
                    }

                    override fun requestError(message: String) {
                        callback.invoke(false, message)
                    }

                })
    }


    /**
     * @param activity
     * @param platform 登录平台 enum
     * @param onSuccess 登录成功回调 SAM
     * @param onFailure 登录失败回调 SAM
     */
    fun login(activity: Activity?, platform: Platform, onSuccess: ((LoginResp) -> Unit)? = null, onFailure: ((String) -> Unit)? = null) {
        mSuccessCallback = { ret ->
            onSuccess?.invoke(ret)
            mSuccessCallback = null
        }
        mFailureCallback = { ret ->
            onFailure?.invoke(ret)
            mFailureCallback = null
        }
        if (activity == null) {
            onFailure?.invoke("activity must not be null")
            return
        }
        if (Platform.WECHAT == platform) {
            // send oauth request
            val req = SendAuth.Req()
            req.scope = "snsapi_userinfo"
            req.state = "wechat_sdk_demo_test" + Random().nextFloat()
            mWXApi?.sendReq(req)
        } else if (Platform.QQ == platform) {

            if (mTencent == null) {
                try {
                    mTencent = Tencent.createInstance(mQQAppID, activity?.applicationContext)
                } catch (exception: ExceptionInInitializerError) {
                    exception.printStackTrace()
                }
            }

            mTencent?.login(activity, "get_simple_userinfo", object : IUiListener {
                override fun onComplete(ret: Any?) {
                    log("login", ret)

                    val jsonObject = JSON.parseObject(ret.toString())
                    val code = jsonObject.getIntValue("ret")
                    if (code == 0) {

                        val openid = jsonObject.getString("openid")
                        val access_token = jsonObject.getString("access_token")

                        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestUserInformation(access_token, mQQAppID, openid, object : RequestSubscriber<QQSimpleInfo>() {
                            override fun requestResult(result: QQSimpleInfo?) {
                                if (result != null) {
                                    log("getSimpleUserInfo", result)

                                    var figureurl: String = result.figureurl_qq_2
                                            ?: result.figureurl_qq_1 ?: ""

                                    val qqReq = LoginReq.createQQReq(BaseBookApplication.getGlobalContext(), result.nickname, figureurl, result.gender, openid)

                                    RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestLoginAction(qqReq.toMap(), object : RequestSubscriber<LoginResp>() {
                                        override fun requestResult(result: LoginResp?) {
                                            if (result != null && result.state.equals("success", true)) {
                                                mUserState.set(true)
                                                mUserInfo = result
                                                saveUserInfo()
                                                mSuccessCallback?.invoke(result)
                                            } else {
                                                mFailureCallback?.invoke("${result?.msg}")
                                            }
                                        }

                                        override fun requestError(message: String) {
                                            Logger.e("QQ登陆异常！")
                                            mFailureCallback?.invoke(message)
                                        }

                                        override fun requestComplete() {

                                        }
                                    })
                                }
                            }

                            override fun requestError(message: String) {
                                Logger.e("获取用户信息异常！")
                                mFailureCallback?.invoke(message)
                            }

                            override fun requestComplete() {

                            }

                        })

                    } else {
                        mFailureCallback?.invoke("code $code")
                    }

                }

                override fun onCancel() {
                    log("login")
                    mFailureCallback?.invoke("onCancel")
                }

                override fun onError(p0: UiError?) {
                    log("login", p0)
                    mFailureCallback?.invoke("onError ${p0?.errorCode}, ${p0?.errorMessage}")
                }

            })
        }
    }

    private fun saveUserInfo() {
        if (mUserInfo != null && mSharedPreferences != null) {
            mSharedPreferences!!.edit()!!.putString(UserConstants.KEY_USER_INFO, mGson.toJson(mUserInfo))!!.commit()
            log("saveUserInfo", "${mUserInfo.toString()}")
        }
    }

    /**
     * @param callback 结果回调
     */
    fun logout(callback: ((Boolean) -> Unit)? = null) {
        if (mUserState.get()) {
            mLogoutCallback = { ret ->
                callback?.invoke(ret)
                mLogoutCallback = null
            }
            mUserState.set(false)
            mSharedPreferences?.edit()?.clear()?.apply()

            RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestLogoutAction(mutableMapOf(Pair("loginToken", mUserInfo!!.login_token), Pair("uid", mUserInfo!!.uid), Pair("uidThird", mUserInfo!!.uid_third)), object : RequestSubscriber<JsonObject>() {
                override fun requestResult(result: JsonObject?) {
                    log("logout", result.toString())

                    if (result != null && result["state"]?.asString?.equals("success", true) == true) {
                        callback?.invoke(true)
                    }
                }

                override fun requestError(message: String) {
                    Logger.e("登出异常！")
                    callback?.invoke(false)
                }

                override fun requestComplete() {

                }
            })

        } else {
            callback?.invoke(true)
        }
    }

    fun refreshToken() {
        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestRefreshToken(mutableMapOf(Pair("loginToken", mUserInfo!!.login_token), Pair("uid", mUserInfo!!.uid), Pair("uidThird", mUserInfo!!.uid_third)), object : RequestSubscriber<RefreshResp>() {
            override fun requestResult(result: RefreshResp?) {
                log("refreshToken", result.toString())
                if (result != null && result.state.equals("success", true)) {
                    mUserInfo!!.login_token = result.login_token!!
                    saveUserInfo()
                } else {
                    log("refreshToken", "${result?.msg}")
                    mUserState.set(false)
                    mSharedPreferences?.edit()?.clear()?.apply()
                }
                mInitCallback?.invoke(true)
            }

            override fun requestError(message: String) {
                Logger.e("刷新Token异常！")
                mInitCallback?.invoke(true)
            }

            override fun requestComplete() {

            }
        })
    }

    fun handleIntent(intent: Intent?) {
        log("handleIntent")
        if (intent != null)
            mWXApi?.handleIntent(intent, this)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Tencent.onActivityResultData(requestCode, resultCode, data, null)
    }

    override fun onResp(resp: BaseResp?) {
        log("onResp : ", resp?.errCode, resp?.javaClass?.simpleName)
        when (resp?.errCode) {
            BaseResp.ErrCode.ERR_OK -> {
                /* login
                ErrCode	ERR_OK = 0(用户同意)
                ERR_AUTH_DENIED = -4（用户拒绝授权）
                ERR_USER_CANCEL = -2（用户取消）
                code	用户换取access_token的code，仅在ErrCode为0时有效
                state	第三方程序发送时用来标识其请求的唯一性的标志，由第三方程序调用sendReq时传入，由微信终端回传，state字符串长度不能超过1K
                lang	微信客户端当前语言
                country	微信用户当前国家信息
                 */
                if (resp.type == 1) {
                    val auth = resp as SendAuth.Resp
                    log("onResp : " + auth.code)
                    val weChatReq = LoginReq.createWeChatReq(BaseBookApplication.getGlobalContext(), auth.code)

                    RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestLoginAction(weChatReq.toMap(), object : RequestSubscriber<LoginResp>() {
                        override fun requestResult(result: LoginResp?) {
                            if (result != null && result.state.equals("success", true)) {
                                mUserState.set(true)
                                mUserInfo = result
                                saveUserInfo()
                                mSuccessCallback?.invoke(result)
                            } else {
                                mFailureCallback?.invoke("${result?.msg}")
                            }
                        }

                        override fun requestError(message: String) {
                            Logger.e("微信登陆异常！")
                            mFailureCallback?.invoke(message)
                        }

                        override fun requestComplete() {

                        }
                    })


                } else {
                    mFailureCallback?.invoke("unknown")
                }
            }
            BaseResp.ErrCode.ERR_USER_CANCEL -> {
                mFailureCallback?.invoke("USER_CANCEL")
            }
            BaseResp.ErrCode.ERR_AUTH_DENIED -> {
                //用户拒绝授权
                mFailureCallback?.invoke("AUTH_DENIED")
            }
            BaseResp.ErrCode.ERR_UNSUPPORT -> {
                mFailureCallback?.invoke("ERR_UNSUPPORT")
            }
            else -> {
                //unknown
                mFailureCallback?.invoke("unknown")
            }
        }
    }

    override fun onReq(req: BaseReq?) {
        when (req?.type) {
            ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX -> {
            }
            ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX -> {
            }
        }
    }
}


enum class Platform(name: String, id: Int) {
    NONE("NONE", -1), WECHAT("WeChat", 1), QQ("QQ", 0)

}
