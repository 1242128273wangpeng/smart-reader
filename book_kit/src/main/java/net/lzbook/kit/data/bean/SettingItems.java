package net.lzbook.kit.data.bean;

import java.io.Serializable;

/**
 * <设置选项数据对象>
 */
public class SettingItems implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -6113647657692733154L;
    //推送开关
    public boolean isPush;
    //推送声音开关
    public boolean isSoundOpen;
    //分时间推送开关
    public boolean isSetPushTime;
    //开始推送时间（小时）
    public int pushTimeStartH;
    //开始推送时间（分钟）
    public int pushTimeStartMin;
    //结束推送时间（小时）
    public int pushTimeStopH;
    //结束推送时间（分钟）
    public int pushTimeStopMin;
    //音量键翻页开关
    public boolean isVolumeTurnover;
    //更随系统开关
    public boolean isFollowSystemBrightness;
    //当前应用亮度
    public int appBrightness;
    //书架排序模式
    public int booklist_sort_type;

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;

        SettingItems that = (SettingItems) object;

        if (isPush != that.isPush) return false;
        if (isSoundOpen != that.isSoundOpen) return false;
        if (isSetPushTime != that.isSetPushTime) return false;
        if (pushTimeStartH != that.pushTimeStartH) return false;
        if (pushTimeStartMin != that.pushTimeStartMin) return false;
        if (pushTimeStopH != that.pushTimeStopH) return false;
        if (pushTimeStopMin != that.pushTimeStopMin) return false;
        if (isVolumeTurnover != that.isVolumeTurnover) return false;
        if (isFollowSystemBrightness != that.isFollowSystemBrightness) return false;
        if (appBrightness != that.appBrightness) return false;
        if (booklist_sort_type != that.booklist_sort_type) return false;

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (isPush ? 1 : 0);
        result = 31 * result + (isSoundOpen ? 1 : 0);
        result = 31 * result + (isSetPushTime ? 1 : 0);
        result = 31 * result + pushTimeStartH;
        result = 31 * result + pushTimeStartMin;
        result = 31 * result + pushTimeStopH;
        result = 31 * result + pushTimeStopMin;
        result = 31 * result + (isVolumeTurnover ? 1 : 0);
        result = 31 * result + (isFollowSystemBrightness ? 1 : 0);
        result = 31 * result + appBrightness;
        result = 31 * result + booklist_sort_type;
        return result;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "SettingItems{" +
                "isPush=" + isPush +
                ", isSoundOpen=" + isSoundOpen +
                ", isSetPushTime=" + isSetPushTime +
                ", pushTimeStartH=" + pushTimeStartH +
                ", pushTimeStartMin=" + pushTimeStartMin +
                ", pushTimeStopH=" + pushTimeStopH +
                ", pushTimeStopMin=" + pushTimeStopMin +
                ", isVolumeTurnover=" + isVolumeTurnover +
                ", isFollowSystemBrightness=" + isFollowSystemBrightness +
                ", appBrightness=" + appBrightness +
                ", booklist_sort_type=" + booklist_sort_type +
                '}';
    }
}
