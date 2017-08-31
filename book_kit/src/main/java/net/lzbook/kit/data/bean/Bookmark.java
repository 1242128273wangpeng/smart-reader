package net.lzbook.kit.data.bean;

import java.io.Serializable;

/**
 * 书签
 */
public class Bookmark implements Serializable {
    public int id;
    //记录当前阅读位置
    public int sequence = -1;
    //记录书签偏移量
    public int offset = -1;
    //定义的当前章节序号
    public int sort;
    //添加书签的时间
    public long last_time;
    //当前章节内容的URL
    public String book_url;
    //当前章节名
    public String chapter_name;
    //当前章节内容
    public String chapter_content;
    // 小说id
    public String book_id;
    //小说来源id
    public String book_source_id;
    //中间变量(用于存储搜狗md，百度bookID等字段)
    public String parameter;
    //预留字段
    public String extra_parameter;

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;

        Bookmark bookmark = (Bookmark) object;

        if (sequence != bookmark.sequence) return false;
        if (offset != bookmark.offset) return false;
        if (sort != bookmark.sort) return false;
        if (last_time != bookmark.last_time) return false;
        if (chapter_name != null ? !chapter_name.equals(bookmark.chapter_name) : bookmark.chapter_name != null)
            return false;
        if (chapter_content != null ? !chapter_content.equals(bookmark.chapter_content) : bookmark.chapter_content != null)
            return false;
        if (book_id != null ? !book_id.equals(bookmark.book_id) : bookmark.book_id != null)
            return false;
        if (book_source_id != null ? !book_source_id.equals(bookmark.book_source_id) : bookmark.book_source_id != null)
            return false;
        if (parameter != null ? !parameter.equals(bookmark.parameter) : bookmark.parameter != null)
            return false;
        if (extra_parameter != null ? !extra_parameter.equals(bookmark.extra_parameter) : bookmark.extra_parameter != null)
            return false;

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + sequence;
        result = 31 * result + offset;
        result = 31 * result + sort;
        result = 31 * result + (int) (last_time ^ (last_time >>> 32));
        result = 31 * result + (chapter_name != null ? chapter_name.hashCode() : 0);
        result = 31 * result + (chapter_content != null ? chapter_content.hashCode() : 0);
        result = 31 * result + (book_id != null ? book_id.hashCode() : 0);
        result = 31 * result + (book_source_id != null ? book_source_id.hashCode() : 0);
        result = 31 * result + (parameter != null ? parameter.hashCode() : 0);
        result = 31 * result + (extra_parameter != null ? extra_parameter.hashCode() : 0);
        return result;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "Bookmark{" +
                "sequence=" + sequence +
                ", offset=" + offset +
                ", sort=" + sort +
                ", last_time=" + last_time +
                ", chapter_name='" + chapter_name + '\'' +
                ", chapter_content='" + chapter_content + '\'' +
                ", book_id='" + book_id + '\'' +
                ", book_source_id='" + book_source_id + '\'' +
                ", parameter='" + parameter + '\'' +
                ", extra_parameter='" + extra_parameter + '\'' +
                '}';
    }
}
