package net.lzbook.kit.data.bean;

import java.util.ArrayList;

public class BatchChapter {
    //批量获取小说章节是否成功
    public boolean success;
    //返回的错误信息
    public String errorlog;
    //获取到的章节内容
    public ArrayList<Chapter> chapters;

    @java.lang.Override
    public java.lang.String toString() {
        return "BatchChapter{" +
                "success=" + success +
                ", errorlog='" + errorlog + '\'' +
                ", chapters=" + chapters +
                '}';
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;

        BatchChapter that = (BatchChapter) object;

        if (success != that.success) return false;
        if (errorlog != null ? !errorlog.equals(that.errorlog) : that.errorlog != null)
            return false;
        if (chapters != null ? !chapters.equals(that.chapters) : that.chapters != null)
            return false;

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (success ? 1 : 0);
        result = 31 * result + (errorlog != null ? errorlog.hashCode() : 0);
        result = 31 * result + (chapters != null ? chapters.hashCode() : 0);
        return result;
    }
}
