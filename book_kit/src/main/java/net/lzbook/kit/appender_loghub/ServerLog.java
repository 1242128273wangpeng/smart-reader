package net.lzbook.kit.appender_loghub;


import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.common.PLItemKey;
import net.lzbook.kit.user.DeviceID;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.JsonUtils;
import net.lzbook.kit.utils.OpenUDID;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Transient;

import java.util.HashMap;
import java.util.Map;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by wangjwchn on 16/8/2.
 */
@Entity
public class ServerLog {
    @Transient
    public static final String MAJORITY = "majority";

    @Transient
    public static final String MINORITY = "minority";

    @Transient
    private Map<String, Object> mContent = new HashMap<String, Object>();

    @Id
    private Long id;

    private String eventType = MAJORITY;

    private String timeStamp;

    private String contentJson;

    public ServerLog() {
        this.mContent.put("__time__", Integer.valueOf((new Long(System.currentTimeMillis() / 1000L)).intValue()));
    }

    //isClickEvent 标识是否是event点击事件
    public ServerLog(PLItemKey type) {
        if (type.equals(PLItemKey.ZN_APP_APPSTORE)) {
            eventType = MINORITY;
            if (!mContent.containsKey("project")) {
                mContent.put("project", PLItemKey.ZN_APP_APPSTORE.getProject());
            }
            if (!mContent.containsKey("logstore")) {
                mContent.put("logstore", PLItemKey.ZN_APP_APPSTORE.getLogstore());
            }
            if (!mContent.containsKey("udid")) {
                mContent.put("udid", OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()));//用户ID（唯一,跟设备有关系）
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

    @Keep
    public ServerLog(Long id, String eventType, String timeStamp, String contentJson) {
        this.id = id;
        this.eventType = eventType;
        this.timeStamp = timeStamp;
        this.contentJson = contentJson;
        mContent = JsonUtils.fromJson(contentJson);
    }

    public void PutTime(int time) {
        mContent.put("__time__", time);
    }

    public void PutContent(String key, String value) {
        if (key == null || key.isEmpty()) {
            return;
        }
        if (value == null) {
            mContent.put(key, "");
        } else {
            mContent.put(key, value);
        }
    }

    public Map<String, Object> GetContent() {
        return mContent;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventType() {
        return this.eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getContentJson() {
        return this.contentJson;
    }

    public void setContentJson(String contentJson) {
        this.contentJson = contentJson;
    }
}
