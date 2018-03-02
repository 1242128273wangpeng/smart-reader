package net.lzbook.kit.net;

import com.google.gson.annotations.SerializedName;

public class NetResult<T> {
    @SerializedName(value = "respCode")
    public String respCode = null;
    @SerializedName(value = "message")
    public String message = null;
    @SerializedName(value = "data")
    public T data = null;

    public String toString() {
        return "CacheTaskConfig{respCode='" + this.respCode + '\'' + ", message='" + this.message + '\'' + ", data=" + this.data + '}';
    }
}
