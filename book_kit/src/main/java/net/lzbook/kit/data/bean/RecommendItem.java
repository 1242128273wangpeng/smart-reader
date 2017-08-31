package net.lzbook.kit.data.bean;

import java.io.Serializable;

public class RecommendItem implements Serializable {
    private static final long serialVersionUID = 4304272593554818688L;
    //推荐种类
    public String classes;
    //小说组id
    public int gid;
    //小说ID
    public int nid;
    //小说作者
    public String author;
    //最新章节序号
    public int last_sort;
    //最新章节名
    public String last_chapter_name;
    //小说名
    public String novel_name;
    public int sub_count;
    //
    public String cover_url;
    //最新更新时间
    public long last_update_time;

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;

        RecommendItem that = (RecommendItem) object;

        if (gid != that.gid) return false;
        if (nid != that.nid) return false;
        if (last_sort != that.last_sort) return false;
        if (sub_count != that.sub_count) return false;
        if (last_update_time != that.last_update_time) return false;
        if (classes != null ? !classes.equals(that.classes) : that.classes != null) return false;
        if (author != null ? !author.equals(that.author) : that.author != null) return false;
        if (last_chapter_name != null ? !last_chapter_name.equals(that.last_chapter_name) : that.last_chapter_name != null)
            return false;
        if (novel_name != null ? !novel_name.equals(that.novel_name) : that.novel_name != null)
            return false;
        if (cover_url != null ? !cover_url.equals(that.cover_url) : that.cover_url != null)
            return false;

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (classes != null ? classes.hashCode() : 0);
        result = 31 * result + gid;
        result = 31 * result + nid;
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + last_sort;
        result = 31 * result + (last_chapter_name != null ? last_chapter_name.hashCode() : 0);
        result = 31 * result + (novel_name != null ? novel_name.hashCode() : 0);
        result = 31 * result + sub_count;
        result = 31 * result + (cover_url != null ? cover_url.hashCode() : 0);
        result = 31 * result + (int) (last_update_time ^ (last_update_time >>> 32));
        return result;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "RecommendItem{" +
                "classes='" + classes + '\'' +
                ", gid=" + gid +
                ", nid=" + nid +
                ", author='" + author + '\'' +
                ", last_sort=" + last_sort +
                ", last_chapter_name='" + last_chapter_name + '\'' +
                ", novel_name='" + novel_name + '\'' +
                ", sub_count=" + sub_count +
                ", cover_url='" + cover_url + '\'' +
                ", last_update_time=" + last_update_time +
                '}';
    }
}
