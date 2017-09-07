package net.lzbook.kit.data.bean;

import java.io.Serializable;
import java.util.ArrayList;

public class BookCover implements Serializable {
    private static final long serialVersionUID = -7657022718381241775L;

    public String name;
    public String desc;
    //小说更新状态
    public int status;
    //    //宜搜来源小说组id
//    public String gid;
//    //宜搜来源小说id
//    public String nid;
//    //搜狗来源小说id
//    public String md;
//    //搜狗来源小说中字段
//    public String id;
    //用于对应不同源的source信息
    public String parameter;
    public String extra_parameter;


    //小说分类
    public String category;
    //小说作者
    public String author;
    //封装小说源状态信息
    public String site;
    //自定义章节数的最后一章
    public int lastSort;
    //小说封面图片URL
    public String img_url;
    //宜搜来源最新更新时间
    public long last_time;
    //最新章节名称
    public String last_chapter_name;
    //新增小说ID
    public String book_id;
    //小说来源id
    public String book_source_id;
    //获取自有数据来源时URL
    public String url;
    //搜狗来源最新
    public String update_time;
    //根据作者推荐的小说
    public ArrayList<RecommendItem> author_recommends;
    //根据分类推荐的小说
    public ArrayList<RecommendItem> category_recommends;

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;

        BookCover bookCover = (BookCover) object;

        if (status != bookCover.status) return false;
        if (lastSort != bookCover.lastSort) return false;
        if (last_time != bookCover.last_time) return false;
        if (name != null ? !name.equals(bookCover.name) : bookCover.name != null) return false;
        if (desc != null ? !desc.equals(bookCover.desc) : bookCover.desc != null) return false;
//        if (gid != null ? !gid.equals(bookCover.gid) : bookCover.gid != null) return false;
//        if (nid != null ? !nid.equals(bookCover.nid) : bookCover.nid != null) return false;
//        if (md != null ? !md.equals(bookCover.md) : bookCover.md != null) return false;
//        if (id != null ? !id.equals(bookCover.id) : bookCover.id != null) return false;
        if (parameter != null ? !parameter.equals(bookCover.parameter) : bookCover.parameter != null)
            return false;
        if (extra_parameter != null ? !extra_parameter.equals(bookCover.extra_parameter) : bookCover.extra_parameter != null)
            return false;
        if (category != null ? !category.equals(bookCover.category) : bookCover.category != null)
            return false;
        if (author != null ? !author.equals(bookCover.author) : bookCover.author != null)
            return false;
        if (site != null ? !site.equals(bookCover.site) : bookCover.site != null) return false;
        if (img_url != null ? !img_url.equals(bookCover.img_url) : bookCover.img_url != null)
            return false;
        if (last_chapter_name != null ? !last_chapter_name.equals(bookCover.last_chapter_name) : bookCover.last_chapter_name != null)
            return false;
        if (book_id != null ? !book_id.equals(bookCover.book_id) : bookCover.book_id != null)
            return false;
        if (book_source_id != null ? !book_source_id.equals(bookCover.book_source_id) : bookCover.book_source_id != null)
            return false;
        if (url != null ? !url.equals(bookCover.url) : bookCover.url != null) return false;
        if (update_time != null ? !update_time.equals(bookCover.update_time) : bookCover.update_time != null)
            return false;
        if (author_recommends != null ? !author_recommends.equals(bookCover.author_recommends) : bookCover.author_recommends != null)
            return false;
        if (category_recommends != null ? !category_recommends.equals(bookCover.category_recommends) : bookCover.category_recommends != null)
            return false;

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (desc != null ? desc.hashCode() : 0);
        result = 31 * result + status;
//        result = 31 * result + (gid != null ? gid.hashCode() : 0);
//        result = 31 * result + (nid != null ? nid.hashCode() : 0);
//        result = 31 * result + (md != null ? md.hashCode() : 0);
//        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (parameter != null ? parameter.hashCode() : 0);
        result = 31 * result + (extra_parameter != null ? extra_parameter.hashCode() : 0);
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (site != null ? site.hashCode() : 0);
        result = 31 * result + lastSort;
        result = 31 * result + (img_url != null ? img_url.hashCode() : 0);
        result = 31 * result + (int) (last_time ^ (last_time >>> 32));
        result = 31 * result + (last_chapter_name != null ? last_chapter_name.hashCode() : 0);
        result = 31 * result + (book_id != null ? book_id.hashCode() : 0);
        result = 31 * result + (book_source_id != null ? book_source_id.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (update_time != null ? update_time.hashCode() : 0);
        result = 31 * result + (author_recommends != null ? author_recommends.hashCode() : 0);
        result = 31 * result + (category_recommends != null ? category_recommends.hashCode() : 0);
        return result;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "BookCover{" +
                "name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", status=" + status +
//                ", gid='" + gid + '\'' +
//                ", nid='" + nid + '\'' +
//                ", md='" + md + '\'' +
//                ", id='" + id + '\'' +
                ", parameter='" + parameter + '\'' +
                ", extra_parameter='" + extra_parameter + '\'' +
                ", category='" + category + '\'' +
                ", author='" + author + '\'' +
                ", site='" + site + '\'' +
                ", lastSort=" + lastSort +
                ", img_url='" + img_url + '\'' +
                ", last_time=" + last_time +
                ", last_chapter_name='" + last_chapter_name + '\'' +
                ", book_id='" + book_id + '\'' +
                ", book_source_id='" + book_source_id + '\'' +
                ", url='" + url + '\'' +
                ", update_time='" + update_time + '\'' +
                ", author_recommends=" + author_recommends +
                ", category_recommends=" + category_recommends +
                '}';
    }
}
