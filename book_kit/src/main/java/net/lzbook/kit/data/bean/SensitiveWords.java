package net.lzbook.kit.data.bean;

import java.io.Serializable;
import java.util.List;

public class SensitiveWords implements Serializable {
    //敏感词操作是否成功
    public boolean sucess;
    //敏感词类型
    public String type;
    //与服务端进行校验信息使用
    public String digest;
    //敏感词内容
    public List<String> list;

    //不同的敏感词分类
    private static SensitiveWords readSensitiveWords;
    private static SensitiveWords bookSensitiveWords;
    private static SensitiveWords locationSensitiveWords;
    private static SensitiveWords adcodeSensitiveWords;

    public static SensitiveWords getBookSensitiveWords() {
        return bookSensitiveWords;
    }

    public static void setBookSensitiveWords(SensitiveWords bookSensitiveWords) {
        SensitiveWords.bookSensitiveWords = bookSensitiveWords;
    }

    public static SensitiveWords getReadSensitiveWords() {
        return readSensitiveWords;
    }

    public static void setReadSensitiveWords(SensitiveWords readSensitiveWords) {
        SensitiveWords.readSensitiveWords = readSensitiveWords;
    }

    public static SensitiveWords getLocationSensitiveWords() {
        return locationSensitiveWords;
    }

    public static void setLocationSensitiveWords(SensitiveWords locationSensitiveWords) {
        SensitiveWords.locationSensitiveWords = locationSensitiveWords;
    }

    public static SensitiveWords getAdcodeSensitiveWords() {
        return adcodeSensitiveWords;
    }

    public static void setAdcodeSensitiveWords(SensitiveWords adcodeSensitiveWords) {
        SensitiveWords.adcodeSensitiveWords = adcodeSensitiveWords;
    }

    public boolean isSucess() {
        return sucess;
    }

    public void setSucess(boolean sucess) {
        this.sucess = sucess;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDigest() {
        return digest;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;

        SensitiveWords that = (SensitiveWords) object;

        if (sucess != that.sucess) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (digest != null ? !digest.equals(that.digest) : that.digest != null) return false;
        if (list != null ? !list.equals(that.list) : that.list != null) return false;

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (sucess ? 1 : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (digest != null ? digest.hashCode() : 0);
        result = 31 * result + (list != null ? list.hashCode() : 0);
        return result;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "SensitiveWords{" +
                "sucess=" + sucess +
                ", type='" + type + '\'' +
                ", digest='" + digest + '\'' +
                ", list=" + list +
                '}';
    }
}
