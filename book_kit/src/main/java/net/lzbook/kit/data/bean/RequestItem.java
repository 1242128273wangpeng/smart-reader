package net.lzbook.kit.data.bean;

import java.io.Serializable;

public class RequestItem implements Serializable {
    //小说book_id
    public String book_id;
    //小说id
//    public int nid;
    //小说来源站id
    public String book_source_id;
    //小说的来源信息
    public String host;
    //小说书名
    public String name;
    //小说作者名
    public String author;
    //中间变量(用于存储搜狗md，百度bookID等字段)
    public String parameter;


    //统计打点 当前页面是来自书籍封面/书架/上一页翻页
    public int fromType;
    //统计当前阅读的小说是来自青果还是智能 打点统计
    public int channel_code;

    //预留字段
    public String extra_parameter;
    // 0 正常增量更新；
    // 1 标签出更新：
    // 2 末端处更新：
    // 3 全部刷新：
    // 4 不刷新。
    public int update_type = -1;
    public int dex = -1;
    public long last_checkupdatetime;
    public long last_updateSucessTime;

    public static RequestItem fromBook(Book book) {
        RequestItem requestItem = new RequestItem();

        requestItem.host = book.site;
        requestItem.book_id = book.book_id;
        requestItem.book_source_id = book.book_source_id;
        requestItem.author = book.author;
        requestItem.dex = book.dex;
        requestItem.name = book.name;
        requestItem.parameter = book.parameter;
        requestItem.extra_parameter = book.extra_parameter;
        requestItem.last_checkupdatetime = book.last_checkupdatetime;
        requestItem.last_updateSucessTime = book.last_updateSucessTime;

        return requestItem;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestItem that = (RequestItem) o;

        if (update_type != that.update_type) return false;
        if (book_id != null ? !book_id.equals(that.book_id) : that.book_id != null) return false;
        if (book_source_id != null ? !book_source_id.equals(that.book_source_id) : that.book_source_id != null)
            return false;
        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (author != null ? !author.equals(that.author) : that.author != null) return false;
        if (parameter != null ? !parameter.equals(that.parameter) : that.parameter != null)
            return false;
        return extra_parameter != null ? extra_parameter.equals(that.extra_parameter) : that.extra_parameter == null;

    }

    @Override
    public int hashCode() {
        int result = book_id != null ? book_id.hashCode() : 0;
        result = 31 * result + (book_source_id != null ? book_source_id.hashCode() : 0);
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (parameter != null ? parameter.hashCode() : 0);
        result = 31 * result + (extra_parameter != null ? extra_parameter.hashCode() : 0);
        result = 31 * result + update_type;
        return result;
    }

    @Override
    public String toString() {
        return "RequestItem{" +
                "book_id='" + book_id + '\'' +
                ", book_source_id='" + book_source_id + '\'' +
                ", host='" + host + '\'' +
                ", name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", parameter='" + parameter + '\'' +
                ", extra_parameter='" + extra_parameter + '\'' +
                ", update_type=" + update_type +
                '}';
    }
}
