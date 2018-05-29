package com.ding.basic.bean;

import java.io.Serializable;

public class ApplicationUpdate implements Serializable {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ApplicationUpdate that = (ApplicationUpdate) o;

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
}
