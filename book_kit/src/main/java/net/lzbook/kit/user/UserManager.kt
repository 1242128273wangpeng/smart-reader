package net.lzbook.kit.user

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import com.alibaba.fastjson.JSON
import com.google.gson.Gson
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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.user.bean.LoginReq
import net.lzbook.kit.user.bean.LoginResp
import net.lzbook.kit.utils.log
import net.lzbook.kit.utils.subscribekt
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

            val appInfo = context.packageManager
                    .getApplicationInfo(context.packageName,
                            PackageManager.GET_META_DATA)

            mWXAppID = appInfo.metaData[UserConstants.WECHAT_APPID].toString()
            mQQAppID = appInfo.metaData[UserConstants.QQ_APPID].toString()
            log("initPlatform", mWXAppID, mQQAppID)
            // 通过WXAPIFactory工厂，获取IWXAPI的实例
            mWXApi = WXAPIFactory.createWXAPI(context?.applicationContext, mWXAppID, true);
            val registerApp = mWXApi?.registerApp(mWXAppID)
            log("registerApp", registerApp)

            mTencent = Tencent.createInstance(mQQAppID, context?.getApplicationContext())

            mSharedPreferences = context.getSharedPreferences(UserConstants.USER_INFO_FILE, Context.MODE_PRIVATE)

            val user_info = mSharedPreferences?.getString(UserConstants.KEY_USER_INFO, null)

            if (user_info != null) {
                mUserInfo = mGson.fromJson(user_info, LoginResp::class.java)
                mUserState.set(true)

                refreshToken()
            } else {
                mInitCallback?.invoke(true)
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
            mTencent?.login(activity, "get_simple_userinfo", object : IUiListener {
                override fun onComplete(ret: Any?) {
                    log("login", ret)

                    val jsonObject = JSON.parseObject(ret.toString())
                    val code = jsonObject.getIntValue("ret")
                    if (code == 0) {

                        val openid = jsonObject.getString("openid")
                        val access_token = jsonObject.getString("access_token")

                        val observable = NetService.userService.getSimpleUserInfo(access_token, mQQAppID, openid)
                        observable.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribekt(
                                        onNext = {
                                            ret ->
                                            log("getSimpleUserInfo", ret)

                                            var figureurl: String = ret.figureurl_qq_2 ?: ret.figureurl_qq_1 ?: ""

                                            val qqReq = LoginReq.createQQReq(BaseBookApplication.getGlobalContext(), ret.nickname,
                                                    figureurl,
                                                    ret.gender,
                                                    openid)

                                            val login = NetService.userService.login(qqReq.toMap())
                                            login.subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribeOn(Schedulers.io())
                                                    .subscribekt(
                                                            onNext = {

                                                                resp: LoginResp? ->
                                                                if (resp != null && resp.state.equals("success", true)) {
                                                                    mUserState.set(true)
                                                                    mUserInfo = resp
                                                                    saveUserInfo()
                                                                    mSuccessCallback?.invoke(resp)
                                                                } else {
                                                                    mFailureCallback?.invoke("${resp?.msg}")
                                                                }
                                                            },
                                                            onError = {
                                                                t ->
                                                                t.printStackTrace()
                                                                mFailureCallback?.invoke("${t.message}")
                                                            }
                                                    )
                                        },
                                        onError = {
                                            ret ->
                                            log("getSimpleUserInfo", ret)
                                            ret.printStackTrace()
                                            mFailureCallback?.invoke("${ret.message}")
                                        }
                                )
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
            mLogoutCallback = {
                ret ->
                callback?.invoke(ret)
                mLogoutCallback = null
            }
            mUserState.set(false)
            mSharedPreferences?.edit()?.clear()?.apply()
            val logout = NetService.userService.logout(mutableMapOf(Pair("loginToken", mUserInfo!!.login_token), Pair("uid", mUserInfo!!.uid), Pair("uidThird", mUserInfo!!.uid_third)))
            logout.subscribeOn(Schedulers.io())
                    .subscribekt(
                            onNext = {
                                ret ->
                                log("logout", "${ret.toString()}")
                                if (ret["state"]?.asString?.equals("success", true) ?: false) {
                                    callback?.invoke(true)
                                }
                            },
                            onError = {
                                t ->
                                t.printStackTrace()
                                callback?.invoke(false)
                            }
                    )
        } else {
            callback?.invoke(true)
        }
    }

    fun refreshToken() {
        val refreshToken = NetService.userService.refreshToken(mutableMapOf(Pair("loginToken", mUserInfo!!.login_token), Pair("uid", mUserInfo!!.uid), Pair("uidThird", mUserInfo!!.uid_third)))
        refreshToken.subscribeOn(Schedulers.io())
                .subscribekt(
                        onNext = {
                            ret ->
                            log("refreshToken", "${ret.toString()}")
                            if (ret != null && ret.state.equals("success", true)) {
                                mUserInfo!!.login_token = ret.login_token!!
                                saveUserInfo()
                            } else {
                                log("refreshToken", "${ret.msg}")
                                mUserState.set(false)
                                mSharedPreferences?.edit()?.clear()?.apply()
                            }
                            mInitCallback?.invoke(true)

                        },
                        onError = {
                            t ->
                            t.printStackTrace()
                            log("refreshToken", t)
                            mInitCallback?.invoke(true)
                        }
                )
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

                    val observable = NetService.userService.login(weChatReq.toMap())
                    val disposable = observable.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribekt(
                                    onNext = {
                                        resp: LoginResp? ->
                                        log("onNext", resp)
                                        if (resp != null && resp.state.equals("success", true)) {
                                            mUserState.set(true)
                                            mUserInfo = resp
                                            saveUserInfo()
                                            mSuccessCallback?.invoke(resp)
                                        } else {

                                            mFailureCallback?.invoke("${resp?.msg}")
                                        }
                                    },
                                    onError = {
                                        t: Throwable ->
                                        log("onError", t)
                                        t.printStackTrace()
                                        mFailureCallback?.invoke("${t.message}")
                                    }
                            )
                }else{
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
