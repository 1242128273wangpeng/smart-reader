package net.lzbook.kit.data.bean;

import java.io.Serializable;
import java.util.ArrayList;

public class SourceItem implements Serializable {
    //获取源操作是否成功
    public boolean success;
    //错误信息
    public String error_log;
    //错误参数
    public String params;
    //所有来源数量
    public int total;
    //小说id
    public String book_id;
//    //宜搜源列表
//    public List<Source> YSItems;
//    //搜狗源列表
//    public List<Source> SGItems;

    //源列表集合
    public ArrayList<Source> sourceList;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourceItem that = (SourceItem) o;

        if (success != that.success) return false;
        if (total != that.total) return false;
        if (error_log != null ? !error_log.equals(that.error_log) : that.error_log != null)
            return false;
        if (params != null ? !params.equals(that.params) : that.params != null) return false;
        if (book_id != null ? !book_id.equals(that.book_id) : that.book_id != null) return false;
        return sourceList != null ? sourceList.equals(that.sourceList) : that.sourceList == null;

    }

    @Override
    public int hashCode() {
        int result = (success ? 1 : 0);
        result = 31 * result + (error_log != null ? error_log.hashCode() : 0);
        result = 31 * result + (params != null ? params.hashCode() : 0);
        result = 31 * result + total;
        result = 31 * result + (book_id != null ? book_id.hashCode() : 0);
        result = 31 * result + (sourceList != null ? sourceList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SourceItem{" +
                "success=" + success +
                ", error_log='" + error_log + '\'' +
                ", params='" + params + '\'' +
                ", total=" + total +
                ", book_id='" + book_id + '\'' +
                ", sourceList=" + sourceList +
                '}';
    }
}
