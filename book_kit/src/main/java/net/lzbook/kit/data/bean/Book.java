package net.lzbook.kit.data.bean;

import android.text.TextUtils;

import java.io.Serializable;

public class Book implements Serializable, Comparable<Book>, Cloneable {
    //0:线上书籍
    public static final int TYPE_ONLINE = 0;
    private static final long serialVersionUID = -6628109520747996395L;
    //区分小说在书架上的位置是否要放置广告
    public int book_type;
    //原先小说组ID，保留用于替换book_id
    public int gid;
    //小说id
    public int nid;
    //记录书籍当前的阅读位置
    public int sequence = -2;
    //书签偏移量
    public int offset = -1;
    //定义的章节序号的最新序号
    public int last_sort;
    //最佳目录的章节序号
    public int gsort = -1;
    //更新状态
    public int update_status = -1;
    //获取小说数据失败的信息
    public String bad_nid;
    //添加到书架记录当前sequence的时间
    public long sequence_time;
    //是否添加书签
    public int readed;
    //初始化书籍信息时，加入到数据库的时间
    public long insert_time;
    //升级后使用的字段
    //小说名称
    public String name;
    //小说作者名
    public String author;
    //小说简介
    public String desc;
    //小说分类信息
    public String category;
    //小说封面页图片URL
    public String img_url;
    //判断小说当前更新状态
    public int status = -1;
    public String site;
    //小说最新章节名称
    public String last_chapter_name;
    //小说章节数量
    public int chapter_count;
    //本地保存最新更新时间
    public long last_updatetime_native;

    //新的字段
    //每本小说都有其唯一的book_id
    public String book_id;
    //小说来源id
    public String book_source_id;
    //最新章节对应MD5值
    public String last_chapter_md5;
    //最新章节对应的url
    public String last_chapter_url;
    public String last_chapter_url1;

    //中间变量(用于存储搜狗md，百度bookID等字段)
    public String parameter;
    //预留字段
    public String extra_parameter;
    //升级初始化状态：0:暂不更新来源，1:来源更新，2:小说可切换到WEB，3:小说可切换到源站，4:小说下线; 5:小说弹目录
    public int initialization_status = 0;

    //用来标识书籍是最近阅读的
    public boolean isLastRead = false;

    public int dex = -1;
    //最后检查更新的时间
    public long last_checkupdatetime = 0;
    //最后最后一次本地更新成功的时间, 与最后一章节中的时间有区别
    public long last_updateSucessTime;

    //上一次更新到的章节序号，为了和青果适配而新增的字段
    public int chapters_update_index;
    public String readPersonNum;

    public int list_version = -1;
    public int c_version = -1;



    public int item_type = 0;
    public int item_position = -1;


    public static boolean isOnlineType(int type) {
        return type == TYPE_ONLINE;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Book)) {
            return false;
        } else {
            Book book = (Book) o;
            if (!TextUtils.isEmpty(book.book_id)) {
                return book.book_id.equals(this.book_id);// 只要book_id相同就认为是同一个对象
            }
            return false;
        }

    }

    @Override
    public int hashCode() {
        return gid;
    }

    @Override
    public int compareTo(Book another) {
        return this.sequence_time == another.sequence_time ? 0 : (this.sequence_time < another.sequence_time ? 1 : -1);
    }

    @Override
    public Book clone() throws CloneNotSupportedException {
        return (Book) super.clone();
    }

    @Override
    public String toString() {
        return "Book {" +
                " book_id : " + book_id +
                " book_source_id : " + book_source_id +
                " name : " + name +
                " author : " + author +
                " parameter : " + parameter +
                " extra_parameter : " + extra_parameter +
                " site : " + site +
                " chapter_count: " + chapter_count +
                " sequence: " + sequence +
                " }";
    }

}
