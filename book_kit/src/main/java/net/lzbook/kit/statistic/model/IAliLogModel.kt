package net.lzbook.kit.statistic.model

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.statistic.alilog.Log
import net.lzbook.kit.user.DeviceID
import net.lzbook.kit.user.UserManager
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.OpenUDID

/**
 * Created by xian on 2017/7/3.
 */
abstract class IAliLogModel {
    open abstract val key: String
    open abstract val project: String
    open abstract val logStore: String

    /*设备id*/
    val device_id: String? = DeviceID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext())
    /*针对设备为用户生成id*/
    val udid: String? = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext())

    /*用户id*/
    var uid: String? = null
        get () {
            return if (UserManager.isUserLogin) UserManager.mUserInfo?.uid else ""
        }

    /*app包名*/
    val app_package: String = BaseBookApplication.getGlobalContext().packageName
    /*app版本*/
    val app_version: String? = AppUtils.getVersionName()
    /*app内部version code*/
    val app_version_code: String? = "${AppUtils.getVersionCode()}"
    /*app渠道号*/
    val app_channel_id: String? = AppUtils.getChannelId()

    /*搜索时间*/
    val timestamp = System.currentTimeMillis()

    fun toLog(): Log {
        val log = Log()
        val json = JSON.toJSON(this) as JSONObject
        json.entries.forEach {
            log.PutContent(it.key, (it.value as Any?).toString())
        }
        return log
    }


}