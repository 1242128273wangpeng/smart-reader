package net.lzbook.kit.data.bean;

/**
 * Created by iyouqu on 2016/11/12.
 */
public class LogData {
    public String type;
    public String bookName;
    public String authorName;
    public String site;
    public String bookId;
    public String bookSourceId;

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;

        LogData that = (LogData) object;
        if (type != null ? !type.equals(that.type) : that.type != null)
            return false;
        if (bookName != null ? !bookName.equals(that.bookName) : that.bookName != null)
            return false;
        if (authorName != null ? !authorName.equals(that.authorName) : that.authorName != null)
            return false;
        if (site != null ? !site.equals(that.site) : that.site != null)
            return false;
        if (bookId != null ? !bookId.equals(that.bookId) : that.bookId != null)
            return false;
        if (bookSourceId != null ? !bookSourceId.equals(that.bookSourceId) : that.bookSourceId != null)
            return false;

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (bookName != null ? bookName.hashCode() : 0);
        result = 31 * result + (authorName != null ? authorName.hashCode() : 0);
        result = 31 * result + (site != null ? site.hashCode() : 0);
        result = 31 * result + (bookId != null ? bookId.hashCode() : 0);
        result = 31 * result + (bookSourceId != null ? bookSourceId.hashCode() : 0);
        return result;
    }
}
