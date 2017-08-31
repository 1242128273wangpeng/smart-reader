package net.lzbook.kit.data.bean;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class Source implements Serializable {
    public String book_id;
    public String book_source_id;
    //小说来源
    public String host;
    //小说来源网址
    public String url;
    //来源是否为端转换，端转换信息为null，数据来源不是端转换
    public String terminal;
    //最新章节名称
    public String last_chapter_name;
    //当前来源小说更新时间
    public long update_time;
    //用于保存当前小说来源
    public LinkedHashMap<String, String> source;

    public int dex;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Source source1 = (Source) o;

        if (update_time != source1.update_time) return false;
        if (dex != source1.dex) return false;
        if (book_id != null ? !book_id.equals(source1.book_id) : source1.book_id != null) return false;
        if (book_source_id != null ? !book_source_id.equals(source1.book_source_id) : source1.book_source_id != null) return false;
        if (host != null ? !host.equals(source1.host) : source1.host != null) return false;
        if (url != null ? !url.equals(source1.url) : source1.url != null) return false;
        if (terminal != null ? !terminal.equals(source1.terminal) : source1.terminal != null) return false;
        if (last_chapter_name != null ? !last_chapter_name.equals(source1.last_chapter_name) : source1.last_chapter_name != null) return false;
        return source != null ? source.equals(source1.source) : source1.source == null;

    }

    @Override
    public int hashCode() {
        int result = book_id != null ? book_id.hashCode() : 0;
        result = 31 * result + (book_source_id != null ? book_source_id.hashCode() : 0);
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (terminal != null ? terminal.hashCode() : 0);
        result = 31 * result + (last_chapter_name != null ? last_chapter_name.hashCode() : 0);
        result = 31 * result + (int) (update_time ^ (update_time >>> 32));
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + dex;
        return result;
    }

    @Override
    public String toString() {
        return "Source{" +
                "book_id='" + book_id + '\'' +
                ", book_source_id='" + book_source_id + '\'' +
                ", host='" + host + '\'' +
                ", url='" + url + '\'' +
                ", terminal='" + terminal + '\'' +
                ", last_chapter_name='" + last_chapter_name + '\'' +
                ", update_time=" + update_time +
                ", source=" + source +
                ", dex=" + dex +
                '}';
    }
}
