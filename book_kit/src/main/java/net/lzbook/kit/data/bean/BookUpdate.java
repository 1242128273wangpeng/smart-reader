package net.lzbook.kit.data.bean;

import java.util.ArrayList;

public class BookUpdate {
    //更新章节数
    public int update_count;
    //小说组id
    public int gid;
    //更新时间
    public long last_time;
    //最新章节名称
    public String last_chapter_name;
    //章节列表
    public ArrayList<Chapter> chapterList;
    //最新章节序号
    public int last_sort;
    //最佳目录的章节序号
    public int gsort;

    //新的字段
    public String book_id;
    public String book_source_id;
    public String parameter;
    public String extra_parameter;
    //最新章节名对应的md5值
    public String check_md5;
    //小说名称
    public String book_name;

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;

        BookUpdate that = (BookUpdate) object;

        if (update_count != that.update_count) return false;
        if (gid != that.gid) return false;
        if (last_time != that.last_time) return false;
        if (last_sort != that.last_sort) return false;
        if (gsort != that.gsort) return false;
        if (last_chapter_name != null ? !last_chapter_name.equals(that.last_chapter_name) : that.last_chapter_name != null)
            return false;
        if (chapterList != null ? !chapterList.equals(that.chapterList) : that.chapterList != null)
            return false;
        if (book_id != null ? !book_id.equals(that.book_id) : that.book_id != null) return false;
        if (book_source_id != null ? !book_source_id.equals(that.book_source_id) : that.book_source_id != null)
            return false;
        if (parameter != null ? !parameter.equals(that.parameter) : that.parameter != null)
            return false;
        if (extra_parameter != null ? !extra_parameter.equals(that.extra_parameter) : that.extra_parameter != null)
            return false;
        if (check_md5 != null ? !check_md5.equals(that.check_md5) : that.check_md5 != null)
            return false;
        if (book_name != null ? !book_name.equals(that.book_name) : that.book_name != null)
            return false;

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + update_count;
        result = 31 * result + gid;
        result = 31 * result + (int) (last_time ^ (last_time >>> 32));
        result = 31 * result + (last_chapter_name != null ? last_chapter_name.hashCode() : 0);
        result = 31 * result + (chapterList != null ? chapterList.hashCode() : 0);
        result = 31 * result + last_sort;
        result = 31 * result + gsort;
        result = 31 * result + (book_id != null ? book_id.hashCode() : 0);
        result = 31 * result + (book_source_id != null ? book_source_id.hashCode() : 0);
        result = 31 * result + (parameter != null ? parameter.hashCode() : 0);
        result = 31 * result + (extra_parameter != null ? extra_parameter.hashCode() : 0);
        result = 31 * result + (check_md5 != null ? check_md5.hashCode() : 0);
        result = 31 * result + (book_name != null ? book_name.hashCode() : 0);
        return result;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "BookUpdate{" +
                "update_count=" + update_count +
                ", gid=" + gid +
                ", last_time=" + last_time +
                ", last_chapter_name='" + last_chapter_name + '\'' +
                ", chapterList=" + chapterList +
                ", last_sort=" + last_sort +
                ", gsort=" + gsort +
                ", book_id='" + book_id + '\'' +
                ", book_source_id='" + book_source_id + '\'' +
                ", parameter='" + parameter + '\'' +
                ", extra_parameter='" + extra_parameter + '\'' +
                ", check_md5='" + check_md5 + '\'' +
                ", book_name='" + book_name + '\'' +
                '}';
    }
}
