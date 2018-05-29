package com.ding.basic.util

import android.text.TextUtils
import com.ding.basic.bean.*
import com.orhanobut.logger.Logger
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

/**
 * Created on 2018/3/15.
 * Created by crazylei.
 */
class ParserUtil {
    companion object {

        @Throws(JSONException::class)
        fun parserApplicationUpdate(json: String): ApplicationUpdate {

            Logger.v("解析应用更新信息！")

            val applicationUpdate = ApplicationUpdate()

            if (!TextUtils.isEmpty(json)) {
                val jsonObject = JSONObject(json)
                val updateObject = jsonObject.optJSONObject("update_vo")

                applicationUpdate.isUpdate = updateObject.optString("is_update")
                applicationUpdate.isForceUpdate = updateObject.optString("is_force_update")
                applicationUpdate.updateVersion = updateObject.optString("update_version")
                applicationUpdate.updateContent = updateObject.optString("update_content")
                applicationUpdate.downloadLink = updateObject.optString("download_link")
                applicationUpdate.md5 = updateObject.optString("md5")
            }

            return applicationUpdate
        }
    }
}