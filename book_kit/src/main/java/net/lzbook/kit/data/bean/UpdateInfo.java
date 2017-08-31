package net.lzbook.kit.data.bean;

import java.io.Serializable;

public class UpdateInfo implements Serializable {

    private static final long serialVersionUID = -2282441705584324766L;
    //是否升级
    public String forceUpdate = "0";
    //升级信息
    public String updateInfo;
    //升级URL
    public String appUrl;
    //新版本名称
    public String newVersionName = "";
    //操作是否成功
    public boolean success;

    @java.lang.Override
    public java.lang.String toString() {
        return "UpdateInfo{" +
                "forceUpdate='" + forceUpdate + '\'' +
                ", updateInfo='" + updateInfo + '\'' +
                ", appUrl='" + appUrl + '\'' +
                ", newVersionName='" + newVersionName + '\'' +
                ", success=" + success +
                '}';
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;

        UpdateInfo that = (UpdateInfo) object;

        if (success != that.success) return false;
        if (forceUpdate != null ? !forceUpdate.equals(that.forceUpdate) : that.forceUpdate != null)
            return false;
        if (updateInfo != null ? !updateInfo.equals(that.updateInfo) : that.updateInfo != null)
            return false;
        if (appUrl != null ? !appUrl.equals(that.appUrl) : that.appUrl != null) return false;
        if (newVersionName != null ? !newVersionName.equals(that.newVersionName) : that.newVersionName != null)
            return false;

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (forceUpdate != null ? forceUpdate.hashCode() : 0);
        result = 31 * result + (updateInfo != null ? updateInfo.hashCode() : 0);
        result = 31 * result + (appUrl != null ? appUrl.hashCode() : 0);
        result = 31 * result + (newVersionName != null ? newVersionName.hashCode() : 0);
        result = 31 * result + (success ? 1 : 0);
        return result;
    }
}
