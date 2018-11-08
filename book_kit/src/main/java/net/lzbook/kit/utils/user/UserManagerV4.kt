package net.lzbook.kit.utils.user

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import com.alibaba.fastjson.JSON
import com.bumptech.glide.Glide
import com.ding.basic.net.Config
import com.ding.basic.bean.BasicResultV4
import com.ding.basic.bean.LoginRespV4
import com.ding.basic.bean.QQSimpleInfo
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.net.RequestSubscriber
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import com.tencent.connect.share.QQShare
import com.tencent.connect.share.QzoneShare
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.R
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.bean.user.AvatarReq
import net.lzbook.kit.bean.user.ThirdLoginReq
import net.lzbook.kit.bean.user.ThirdLoginReq.Companion.CHANNEL_QQ
import net.lzbook.kit.constants.UserConstants
import net.lzbook.kit.utils.*
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.dingyue.statistics.DyStatService
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.utils.toast.ToastUtil
import net.lzbook.kit.utils.toast.ToastUtil.mainLooperHandler
import net.lzbook.kit.utils.user.bean.UserNameState
import net.lzbook.kit.utils.user.bean.WXAccess
import net.lzbook.kit.utils.user.bean.WXSimpleInfo
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
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
    private val repositoryFactory: RequestRepositoryFactory by lazy {
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

            if (context.packageManager != null && context.packageName != null) {
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
                        mTencent = Tencent.createInstance(qqAppID, context.applicationContext)
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
                    } else {
                        mInitCallback?.invoke(false)
                    }
                }
            }
        } else {
            callback?.invoke(true)
        }
    }

    fun refreshToken(callBack: (() -> Unit)? = null) {
        loge("refreshToken")
        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                .getRefreshToken(object : RequestSubscriber<BasicResultV4<LoginRespV4>>() {
                    override fun requestResult(result: BasicResultV4<LoginRespV4>?) {
                        if (result?.checkResultAvailable()!!) {
                            user?.let {
                                user?.token = result.data?.token
                                onLogin(it)
                                callBack?.invoke()
                            }
                        } else {
                            callBack?.invoke()
                        }
                    }

                    override fun requestError(message: String) {
                        callBack?.invoke()
                    }

                })
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
                                    bindThirdAccount(body, Platform.QQ)

                                } else {
                                    logi("登录第三方")
                                    thirdLogin(body, Platform.QQ)
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
            DyStatService.onEvent(EventPoint.LOGIN_UIDDIFFUSER)
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


    private fun onLogout(onLogout: (() -> Unit)? = null) {
        upUserReadInfo { success ->
            mUserState.set(false)
            this.user = null
            repositoryFactory.deleteLoginUser()//删除用户信息
            repositoryFactory.deleteShelfBook()// 移除书架
            val requestRepositoryFactory = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
            requestRepositoryFactory.deleteAllBookMark() //移除书签
            requestRepositoryFactory.deleteAllHistory() // 移除足迹
            repositoryFactory.requestLogout(object : RequestSubscriber<BasicResultV4<String>>() {
                override fun requestResult(result: BasicResultV4<String>?) {
                    onLogout?.invoke()

                }

                override fun requestError(message: String) {
                    onLogout?.invoke()
                }

            })
        }


    }

    /**
     * 登出上传操作---------------------开始
     */
    public fun upUserReadInfo(onComplete: ((success: Boolean) -> Unit)? = null) {
        user?.let {
            val repositoryFactory = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
            repositoryFactory.getUploadBookShelfFlowable(user!!.account_id)
                    .subscribeOn(Schedulers.io())
                    .flatMap {
                        repositoryFactory.getUploadBookMarkFlowable(user!!.account_id)
                    }
                    .flatMap {
                        repositoryFactory.getUploadBookBrowseFlowable(user!!.account_id)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        onComplete?.invoke(true)
                    }, {
                        onComplete?.invoke(false)
                    }, {
                        Logger.e("UpUserReadInfo: onComplete")
                    })
        }
    }

    /**
     * 登出上传操作---------------------结束
     */


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
    fun bindThirdAccount(accountBody: RequestBody, platform: Platform) {
        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                .bindThirdAccount(accountBody, object : RequestSubscriber<BasicResultV4<LoginRespV4>>() {
                    override fun requestResult(result: BasicResultV4<LoginRespV4>?) {
                        if (result?.checkResultAvailable()!!) {
                            onLogin(result.data!!)
                            successCallback?.invoke(result)
                            if (Platform.QQ == platform) {
                                sharedPreferences?.edit()
                                        ?.putString(LOGIN_METHOD, CHANNEL_QQ)
                                        ?.putString(LOGIN_ID, result.data!!.account_id)
                                        ?.apply()
                            } else if (Platform.WECHAT == platform) {
                                sharedPreferences?.edit()
                                        ?.putString(LOGIN_METHOD, ThirdLoginReq.CHANNEL_WX)
                                        ?.putString(LOGIN_ID, result.data!!.account_id)
                                        ?.apply()
                            }


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
    private fun thirdLogin(loginBody: RequestBody, platform: Platform) {

        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())
                .thirdLogin(loginBody, object : RequestSubscriber<BasicResultV4<LoginRespV4>>() {
                    override fun requestResult(result: BasicResultV4<LoginRespV4>?) {
                        if (result?.checkResultAvailable()!!) {
                            onLogin(result.data!!)
                            successCallback!!.invoke(result)
                            if (Platform.QQ == platform) {
                                sharedPreferences?.edit()
                                        ?.putString(LOGIN_METHOD, CHANNEL_QQ)
                                        ?.putString(LOGIN_ID, result.data!!.account_id)
                                        ?.apply()
                            } else if (Platform.WECHAT == platform) {
                                sharedPreferences?.edit()
                                        ?.putString(LOGIN_METHOD, ThirdLoginReq.CHANNEL_WX)
                                        ?.putString(LOGIN_ID, result.data!!.account_id)
                                        ?.apply()
                            }

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
                            onLogin(result.data!!)
                            callBack.invoke(true, result)
                            sharedPreferences?.edit()
                                    ?.putString(LOGIN_METHOD, "phone")
                                    ?.putString(LOGIN_ID, result.data!!.account_id)
                                    ?.apply()
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
                                bindThirdAccount(body, Platform.WECHAT)

                            } else {
                                logi("登录第三方")
                                thirdLogin(body, Platform.WECHAT)
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
     *  绑定手机号
     */

    fun bindPhoneNumber(phone: String, code: String, callBack: ((Boolean, BasicResultV4<LoginRespV4>?) -> Unit)) {
        val map = HashMap<String, String>()
        map["phoneNumber"] = phone
        map["code"] = code

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

    /**
     * 同步书架、标签、足迹--------
     */

    fun keepReadInfo(onComplete: (() -> Unit)? = null) {
        loge("keepBookShelf")
        val requestFactory: RequestRepositoryFactory = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())

        user?.let {

            requestFactory.keepUserBookShelf(user!!.account_id, {

                requestFactory.keepBookMark(user!!.account_id, {

                    requestFactory.keepBookBrowse(user!!.account_id, onComplete)

                })
            })
        }
    }


    fun Bitmap.toBase64(): String {
        val bStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 50, bStream)
        bStream.flush()
        bStream.close()
        val bytes = bStream.toByteArray()

        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    /**************************** 分享 ****************************/


    fun shareWechat(activity: Activity?, title: String, description: String, url: String, image: String) {
        handleShareWechat(activity, title, description, url, image, SendMessageToWX.Req.WXSceneSession)
    }

    fun shareWechatCircle(activity: Activity?, title: String, description: String, url: String, image: String) {
        handleShareWechat(activity, title, description, url, image, SendMessageToWX.Req.WXSceneTimeline)
    }

    private fun handleShareWechat(activity: Activity?, title: String, description: String, url: String, image: String, type: Int) {
        if (mWXApi == null) {
            mWXApi = WXAPIFactory.createWXAPI(activity?.applicationContext, wxAppID, true)
            mWXApi?.registerApp(wxAppID)
        }

        if (mWXApi?.isWXAppInstalled == false) {
            ToastUtil.showToastMessage("请先安装微信客户端，再进行分享操作！")
            return
        }

        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(image)) {
            ToastUtil.showToastMessage("参数错误，请稍后再试！")
        }

        Observable.create<Bitmap> {
            val bitmap = Glide.with(activity?.applicationContext)
                    .load(image)
                    .asBitmap()
                    .centerCrop()
                    .into(100, 100)
                    .get()
            it.onNext(bitmap)
        }.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ handleShareWechatAction(title, description, url, it, type) }, {
                   ToastUtil.showToastMessage("参数异常，请稍后再试！")
                }, {

                })
    }

    private fun handleShareWechatAction(title: String, description: String, url: String, bitmap: Bitmap, scene: Int) {
        val wxWebpageObject = WXWebpageObject()
        wxWebpageObject.webpageUrl = url

        val wxMediaMessage = WXMediaMessage(wxWebpageObject)

        wxMediaMessage.title = title
        wxMediaMessage.thumbData = bitmap.bitmapTransformByteArray()
        wxMediaMessage.description = description

        val req = SendMessageToWX.Req()
        req.transaction = buildTransaction(AppUtils.getPackageName())
        req.message = wxMediaMessage
        req.scene = scene
        mWXApi?.sendReq(req)
    }

    fun shareQQ(activity: Activity?, title: String, description: String, url: String, image: String) {
        if (mTencent == null) {
            mTencent = Tencent.createInstance(qqAppID, activity?.applicationContext)
        }

        if (mTencent?.isQQInstalled(activity?.applicationContext) == false) {
            ToastUtil.showToastMessage("请先安装QQ客户端，再进行分享操作！")
            return
        }

        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(image)) {
            ToastUtil.showToastMessage("参数错误，请稍后再试！")
        }

        Observable.create<String> {
            it.onNext(image)
        }.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val params = Bundle()
                    params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT)
                    params.putString(QQShare.SHARE_TO_QQ_TITLE, title)

                    if (!TextUtils.isEmpty(description)) {
                        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, description)
                    }

                    params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, url)
                    params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, it)
                    params.putString(QQShare.SHARE_TO_QQ_APP_NAME, activity?.getString(R.string.app_name))
                    params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE)

                    handleShareQQAction(activity, params)
                }
    }

    private fun handleShareQQAction(activity: Activity?, params: Bundle) {
        mainLooperHandler.post {
            if (activity != null) {
                mTencent?.shareToQQ(activity, params, shareListener)
            }
        }
    }

    fun shareQzone(activity: Activity?, title: String, description: String, url: String, image: String) {

        if (mTencent == null) {
            mTencent = Tencent.createInstance(qqAppID, activity?.applicationContext)
        }

        if (mTencent?.isQQInstalled(activity?.applicationContext) == false) {
            ToastUtil.showToastMessage("请先安装QQ客户端，再进行分享操作！")
            return
        }

        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(image)) {
            ToastUtil.showToastMessage("参数错误，请稍后再试！")
        }

        Observable.create<String> {
            it.onNext(image)
        }.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val params = Bundle()

                    params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT)
                    params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title)

                    if (!TextUtils.isEmpty(description)) {
                        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, description)
                    }

                    params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, url)

                    val imgUrlList = ArrayList<String>()
                    imgUrlList.add(it)

                    params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imgUrlList)

                    handleShareQzoneAction(activity, params)
                }
    }

    private fun handleShareQzoneAction(activity: Activity?, params: Bundle) {
        mainLooperHandler.post {
            if (activity != null) {
                mTencent?.shareToQzone(activity, params, shareListener)
            }
        }
    }

    private fun buildTransaction(message: String?): String {
        return if (message == null) System.currentTimeMillis().toString() else message + System.currentTimeMillis()
    }

    fun registerQQShareCallBack(requestCode: Int, resultCode: Int, data: Intent?) {
        Tencent.onActivityResultData(requestCode, resultCode, data, shareListener)
    }

    private val shareListener = object : IUiListener {
        override fun onCancel() {
            Logger.e("QQ分享取消！")
        }

        override fun onComplete(response: Any) {
            Logger.e("QQ分享成功！")
            SPUtils.putDefaultSharedBoolean(SPKey.APPLICATION_SHARE_ACTION, true)
        }

        override fun onError(e: UiError) {
            Logger.e("QQ分享失败: " + e.errorDetail + " : " + e.errorMessage)
        }
    }
}
