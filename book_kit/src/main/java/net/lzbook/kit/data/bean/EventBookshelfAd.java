package net.lzbook.kit.data.bean;


import com.dingyueads.sdk.Native.YQNativeAdInfo;

/**
 * 广告type EventBus
 * Created by q on 2015/12/24.
 */
public class EventBookshelfAd {
    //广告位置类型
    public String type_ad;
    public int position;
    public YQNativeAdInfo yqNativeAdInfo;

    public EventBookshelfAd(String typeAd, int position, YQNativeAdInfo yqNativeAdInfo) {
        this.type_ad = typeAd;
        this.position = position;
        this.yqNativeAdInfo = yqNativeAdInfo;
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;

        EventNativeType that = (EventNativeType) object;

        if (type_ad != null ? !type_ad.equals(that.type_ad) : that.type_ad != null) return false;

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (type_ad != null ? type_ad.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EventBookshelfAd{" +
                "type_ad='" + type_ad + '\'' +
                '}';
    }
}