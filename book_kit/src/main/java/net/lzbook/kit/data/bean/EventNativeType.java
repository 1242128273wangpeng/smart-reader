package net.lzbook.kit.data.bean;

/**
 * 广告type EventBus
 * Created by q on 2015/12/24.
 */
public class EventNativeType {
    //广告位置类型
    public String type_ad;

    public EventNativeType(String typeAd) {
        this.type_ad = typeAd;
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

    @java.lang.Override
    public java.lang.String toString() {
        return "EventNativeType{" +
                "type_ad='" + type_ad + '\'' +
                '}';
    }
}
