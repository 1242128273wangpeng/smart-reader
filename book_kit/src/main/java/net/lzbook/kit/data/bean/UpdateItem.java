package net.lzbook.kit.data.bean;

import java.io.Serializable;

public class UpdateItem implements Serializable {
    //请求升级小说id
    public String book_id;
    //小说来源站id
    public String book_source_id;
    //是否升级
    public boolean update;
    //升级数量
    public int update_count;
    //升级时间
    public long update_time;

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;

        UpdateItem that = (UpdateItem) object;

        if (update != that.update) return false;
        if (update_count != that.update_count) return false;
        if (update_time != that.update_time) return false;
        if (book_id != null ? !book_id.equals(that.book_id) : that.book_id != null) return false;
        if (book_source_id != null ? !book_source_id.equals(that.book_source_id) : that.book_source_id != null)
            return false;

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (book_id != null ? book_id.hashCode() : 0);
        result = 31 * result + (book_source_id != null ? book_source_id.hashCode() : 0);
        result = 31 * result + (update ? 1 : 0);
        result = 31 * result + update_count;
        result = 31 * result + (int) (update_time ^ (update_time >>> 32));
        return result;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "UpdateItem{" +
                "book_id='" + book_id + '\'' +
                ", book_source_id='" + book_source_id + '\'' +
                ", update=" + update +
                ", update_count=" + update_count +
                ", update_time=" + update_time +
                '}';
    }
}
