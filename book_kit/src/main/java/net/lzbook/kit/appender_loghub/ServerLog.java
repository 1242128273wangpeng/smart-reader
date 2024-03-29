package net.lzbook.kit.appender_loghub;


import com.ding.basic.bean.LocalLog;

import net.lzbook.kit.app.base.BaseBookApplication;
import net.lzbook.kit.appender_loghub.common.PLItemKey;
import net.lzbook.kit.utils.user.DeviceID;
import net.lzbook.kit.utils.logger.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.JsonUtils;
import net.lzbook.kit.utils.OpenUDID;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangjwchn on 16/8/2
 */
public class ServerLog {

    private Map<String, Object> mContent = new HashMap<String, Object>();

    private String eventType = LocalLog.getMAJORITY();

    private int id;

    //isClickEvent 标识是否是event点击事件
    public ServerLog(PLItemKey type) {
        if (type.equals(PLItemKey.ZN_APP_APPSTORE)) {
            eventType = LocalLog.getMINORITY();
            if (!mContent.containsKey("project")) {
                mContent.put("project", PLItemKey.ZN_APP_APPSTORE.getProject());
            }
            if (!mContent.containsKey("logstore")) {
                mContent.put("logstore", PLItemKey.ZN_APP_APPSTORE.getLogstore());
            }
            if (!mContent.containsKey("udid")) {
                mContent.put("udid", OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()));//用户ID（唯一,跟设备有关系）
            }
            if (!mContent.containsKey("packagename")) {
                mContent.put("packagename", AppUtils.getPackageName());//app包名
            }
            AppLog.e("liebiao", type + "===");
        } else if (type.equals(PLItemKey.ZN_APP_EVENT)) {
            if (!mContent.containsKey("project")) {
                mContent.put("project", PLItemKey.ZN_APP_EVENT.getProject());
            }
            if (!mContent.containsKey("logstore")) {
                mContent.put("logstore", PLItemKey.ZN_APP_EVENT.getLogstore());
            }

            if (!mContent.containsKey("app_package")) {
                mContent.put("app_package", AppUtils.getPackageName());//app包名
            }
            if (!mContent.containsKey("app_version")) {
                mContent.put("app_version", AppUtils.getVersionName());//app版本
            }
            if (!mContent.containsKey("app_version_code")) {
                mContent.put("app_version_code", AppUtils.getVersionCode() + "");//app内部version code
            }
            if (!mContent.containsKey("app_channel_id")) {
                mContent.put("app_channel_id", AppUtils.getChannelId());//app渠道号
            }
            if (!mContent.containsKey("phone_identity")) {
                mContent.put("phone_identity", DeviceID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()));//手机唯一标识符
            }
            if (!mContent.containsKey("vendor")) {
                mContent.put("vendor", AppUtils.getPhoneBrand() + "," + AppUtils.getPhoneModel() + "," + AppUtils.getRelease());//设备信息
            }
            if (!mContent.containsKey("operator")) {
                mContent.put("operator", AppUtils.getProvidersName(BaseBookApplication.getGlobalContext()));//运营商
            }
            if (!mContent.containsKey("resolution_ratio")) {
                mContent.put("resolution_ratio", AppUtils.getMetrics(BaseBookApplication.getGlobalContext()));//分辨率
            }
            if (!mContent.containsKey("udid")) {
                mContent.put("udid", OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()));//用户ID（唯一,跟设备有关系）
            }

        } else if (type.equals(PLItemKey.ZN_APP_READ_CONTENT)) {
            if (!mContent.containsKey("udid")) {
                mContent.put("udid", OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()));//用户ID（唯一,跟设备有关系）
            }
            if (!mContent.containsKey("project")) {
                mContent.put("project", PLItemKey.ZN_APP_READ_CONTENT.getProject());
            }
            if (!mContent.containsKey("logstore")) {
                mContent.put("logstore", PLItemKey.ZN_APP_READ_CONTENT.getLogstore());
            }

            if (!mContent.containsKey("app_package")) {
                mContent.put("app_package", AppUtils.getPackageName());//app包名
            }
            if (!mContent.containsKey("app_version")) {
                mContent.put("app_version", AppUtils.getVersionName());//app版本
            }
            if (!mContent.containsKey("app_version_code")) {
                mContent.put("app_version_code", AppUtils.getVersionCode() + "");//app内部version code
            }
            if (!mContent.containsKey("app_channel_id")) {
                mContent.put("app_channel_id", AppUtils.getChannelId());//app渠道号
            }
        } else if (type.equals(PLItemKey.ZN_APP_FEEDBACK)) {
            if (!mContent.containsKey("project")) {
                mContent.put("project", PLItemKey.ZN_APP_FEEDBACK.getProject());
            }
            if (!mContent.containsKey("logstore")) {
                mContent.put("logstore", PLItemKey.ZN_APP_FEEDBACK.getLogstore());
            }
            if (!mContent.containsKey("phone_identity")) {
                mContent.put("phone_identity", DeviceID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()));//手机唯一标识符
            }
            if (!mContent.containsKey("vendor")) {
                mContent.put("vendor", AppUtils.getPhoneBrand() + "," + AppUtils.getPhoneModel() + "," + AppUtils.getRelease());//设备信息
            }
            if (!mContent.containsKey("operator")) {
                mContent.put("operator", AppUtils.getProvidersName(BaseBookApplication.getGlobalContext()));//运营商
            }
            if (!mContent.containsKey("resolution_ratio")) {
                mContent.put("resolution_ratio", AppUtils.getMetrics(BaseBookApplication.getGlobalContext()));//分辨率
            }

        }

        mContent.put("__time__", new Long(System.currentTimeMillis() / 1000).intValue());
    }

    public ServerLog(int id, String contentJson) {
        this.id = id;
        mContent = JsonUtils.fromJson(contentJson);
    }

    public void PutTime(int time) {
        mContent.put("__time__", time);
    }

    public void putContent(String key, String value) {
        if (key == null || key.isEmpty()) {
            return;
        }
        if (value == null) {
            mContent.put(key, "");
        } else {
            mContent.put(key, value);
        }
    }

    public Map<String, Object> getContent() {
        return mContent;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventType() {
        return eventType;
    }

    public int getId() {
        return id;
    }
}
