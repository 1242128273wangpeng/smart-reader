package net.lzbook.kit.data.bean;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CacheTaskConfig {
    @SerializedName("bookAuthor")
    public String bookAuthor;
    @SerializedName("bookName")
    public String bookName;
    @SerializedName("bookSourceId")
    public BookSourceIdCacheTaskConfig bookSourceId;
    @SerializedName("fileUrlList")
    public List<String> fileUrlList;
    @SerializedName("lastChapterId")
    public BookSourceIdCacheTaskConfig lastChapterId;
    @SerializedName("version")
    public int version;

    public static class BookSourceIdCacheTaskConfig {
        @SerializedName("counter")
        public int counter;
        @SerializedName("date")
        public long date;
        @SerializedName("machineIdentifier")
        public int machineIdentifier;
        @SerializedName("processIdentifier")
        public int processIdentifier;
        @SerializedName("time")
        public long time;
        @SerializedName("timeSecond")
        public int timeSecond;
        @SerializedName("timestamp")
        public int timestamp;

        public String toString() {
            return "BookSourceIdCacheTaskConfig{timestamp=" + this.timestamp + ", machineIdentifier=" + this.machineIdentifier + ", processIdentifier=" + this.processIdentifier + ", counter=" + this.counter + ", timeSecond=" + this.timeSecond + ", time=" + this.time + ", date=" + this.date + '}';
        }
    }

    public String toString() {
        return "DataCacheTaskConfig{bookSourceId=" + this.bookSourceId + ", bookName='" + this.bookName + '\'' + ", bookAuthor='" + this.bookAuthor + '\'' + ", lastChapterId=" + this.lastChapterId + ", version=" + this.version + ", fileUrlList=" + this.fileUrlList + '}';
    }
}
