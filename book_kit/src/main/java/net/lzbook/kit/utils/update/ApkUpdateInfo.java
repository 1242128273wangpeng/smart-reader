package net.lzbook.kit.utils.update;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 后台检测更新后传回的数据
 * Created by Administrator on 2016/9/22.
 */
public class ApkUpdateInfo {
    //判断是否更新 1是  0否
    public String isUpdate;
    //判断是否强制更新 1是  0否
    public String isForceUpdate;
    //更新版本
    public String updateVersion;
    //更新内容
    public String updateContent;
    //新版Apk下载的链接
    public String downloadLink;
    //md5对比
    public String md5;

    public ApkUpdateInfo(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONObject updateVo = jsonObject.optJSONObject("update_vo");
        this.isUpdate = updateVo.optString("is_update");
        this.isForceUpdate = updateVo.optString("is_force_update");
        this.updateVersion = updateVo.optString("update_version");
        this.updateContent = updateVo.optString("update_content");
        this.downloadLink = updateVo.optString("download_link");
        this.md5 = updateVo.optString("md5");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApkUpdateInfo that = (ApkUpdateInfo) o;

        if (isUpdate != null ? !isUpdate.equals(that.isUpdate) : that.isUpdate != null)
            return false;
        if (isForceUpdate != null ? !isForceUpdate.equals(that.isForceUpdate) : that.isForceUpdate != null)
            return false;
        if (updateVersion != null ? !updateVersion.equals(that.updateVersion) : that.updateVersion != null)
            return false;
        if (updateContent != null ? !updateContent.equals(that.updateContent) : that.updateContent != null)
            return false;
        if (downloadLink != null ? !downloadLink.equals(that.downloadLink) : that.downloadLink != null)
            return false;
        return md5 != null ? md5.equals(that.md5) : that.md5 == null;

    }

    @Override
    public int hashCode() {
        int result = isUpdate != null ? isUpdate.hashCode() : 0;
        result = 31 * result + (isForceUpdate != null ? isForceUpdate.hashCode() : 0);
        result = 31 * result + (updateVersion != null ? updateVersion.hashCode() : 0);
        result = 31 * result + (updateContent != null ? updateContent.hashCode() : 0);
        result = 31 * result + (downloadLink != null ? downloadLink.hashCode() : 0);
        result = 31 * result + (md5 != null ? md5.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ApkUpdateInfo{" +
                "isUpdate='" + isUpdate + '\'' +
                ", isForceUpdate='" + isForceUpdate + '\'' +
                ", updateVersion='" + updateVersion + '\'' +
                ", updateContent='" + updateContent + '\'' +
                ", downloadLink='" + downloadLink + '\'' +
                ", md5='" + md5 + '\'' +
                '}';
    }
}
