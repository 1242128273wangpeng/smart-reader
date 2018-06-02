package net.lzbook.kit.data.bean;

import com.ding.basic.bean.Bookmark;

/**
 * 书签事件
 */
public class EventBookmark {
    //删除书签
    public static int type_delete = 1;
    //书签事件类型
    private int type = 0;
    //书签实体
    private Bookmark bookmark;

    public EventBookmark(int type) {
        this.type = type;
    }

    public EventBookmark(int type, Bookmark bookmark) {
        this.type = type;
        this.bookmark = bookmark;
    }

    public Bookmark getBookmark() {
        return bookmark;
    }

    public int getType() {
        return type;
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;

        EventBookmark that = (EventBookmark) object;

        if (type != that.type) return false;
        if (bookmark != null ? !bookmark.equals(that.bookmark) : that.bookmark != null)
            return false;

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + type;
        result = 31 * result + (bookmark != null ? bookmark.hashCode() : 0);
        return result;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "EventBookmark{" +
                "type=" + type +
                ", bookmark=" + bookmark +
                '}';
    }
}
