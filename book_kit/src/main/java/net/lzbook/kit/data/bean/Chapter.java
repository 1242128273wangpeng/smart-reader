package net.lzbook.kit.data.bean;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class Chapter implements Serializable {

    private static final long serialVersionUID = -7095136434459250965L;
    //章节获取操作是否成功
    public boolean isSuccess;
    //代替以前的sort，记录当前第几章
    public int sequence = -1;
    //是否自动切源
    public int flag;
    //章节名称列表

//    public ArrayList<NovelLineBean> chapterNameList;

    //升级后使用的字段
    //小说组id
    public int gid;
    //小说id
    public int nid;

    public long time;
    //小说来源站
    public int sort;
    //小说来源站
    public String site;
    //最佳目录的章节序号
    public int gsort;
    //本章内容源URL
    public String curl;
    public String curl1;
    //章节名
    public String chapter_name;
    //章节内容
    public String content;

    //新的字段
    public String book_chapter_md5;
    //获取的搜狗来源的小说相关信息
    public String cmd;
    public String book_id;
    //中间变量(用于存储搜狗md，百度bookID等字段)
    public String parameter;
    //预留字段
    public String extra_parameter;
    //接口链接
    public String api_url;
    //来源标识: 0：来源API，1：自有接口
    public int chapter_form;

    //临时字段，用于存储搜狗更新时间
    public String update_time;

    // 章节状态
    public Status status = Status.CONTENT_NORMAL;
    //章节字数
    public int word_count;
    //章节id
    public String chapter_id;
    public String book_source_id;
    public String chapter_status;

    public Chapter() {
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;

        Chapter chapter = (Chapter) object;

        if (isSuccess != chapter.isSuccess) return false;
        if (sequence != chapter.sequence) return false;
        if (flag != chapter.flag) return false;
        if (gid != chapter.gid) return false;
        if (nid != chapter.nid) return false;
        if (time != chapter.time) return false;
        if (sort != chapter.sort) return false;
        if (gsort != chapter.gsort) return false;
        if (chapter_form != chapter.chapter_form) return false;
//        if (chapterNameList != null ? !chapterNameList.equals(chapter.chapterNameList) : chapter.chapterNameList != null)
//            return false;
        if (site != null ? !site.equals(chapter.site) : chapter.site != null) return false;
        if (curl != null ? !curl.equals(chapter.curl) : chapter.curl != null) return false;
        if (curl1 != null ? !curl1.equals(chapter.curl1) : chapter.curl1 != null) return false;
        if (chapter_name != null ? !chapter_name.equals(chapter.chapter_name) : chapter.chapter_name != null)
            return false;
        if (content != null ? !content.equals(chapter.content) : chapter.content != null)
            return false;
        if (book_chapter_md5 != null ? !book_chapter_md5.equals(chapter.book_chapter_md5) : chapter.book_chapter_md5 != null)
            return false;
        if (cmd != null ? !cmd.equals(chapter.cmd) : chapter.cmd != null) return false;
        if (book_id != null ? !book_id.equals(chapter.book_id) : chapter.book_id != null)
            return false;
        if (parameter != null ? !parameter.equals(chapter.parameter) : chapter.parameter != null)
            return false;
        if (extra_parameter != null ? !extra_parameter.equals(chapter.extra_parameter) : chapter.extra_parameter != null)
            return false;
        if (api_url != null ? !api_url.equals(chapter.api_url) : chapter.api_url != null)
            return false;
        if (update_time != null ? !update_time.equals(chapter.update_time) : chapter.update_time != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (isSuccess ? 1 : 0);
        result = 31 * result + sequence;
        result = 31 * result + flag;
//        result = 31 * result + (chapterNameList != null ? chapterNameList.hashCode() : 0);
        result = 31 * result + gid;
        result = 31 * result + nid;
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + sort;
        result = 31 * result + (site != null ? site.hashCode() : 0);
        result = 31 * result + gsort;
        result = 31 * result + (curl != null ? curl.hashCode() : 0);
        result = 31 * result + (curl1 != null ? curl1.hashCode() : 0);
        result = 31 * result + (chapter_name != null ? chapter_name.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (book_chapter_md5 != null ? book_chapter_md5.hashCode() : 0);
        result = 31 * result + (cmd != null ? cmd.hashCode() : 0);
        result = 31 * result + (book_id != null ? book_id.hashCode() : 0);
        result = 31 * result + (parameter != null ? parameter.hashCode() : 0);
        result = 31 * result + (extra_parameter != null ? extra_parameter.hashCode() : 0);
        result = 31 * result + (api_url != null ? api_url.hashCode() : 0);
        result = 31 * result + chapter_form;
        result = 31 * result + (update_time != null ? update_time.hashCode() : 0);
        return result;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "Chapter{" +
                "isSuccess=" + isSuccess +
                ", sequence=" + sequence +
                ", flag=" + flag +
//                ", chapterNameList=" + chapterNameList +
                ", gid=" + gid +
                ", nid=" + nid +
                ", time=" + time +
                ", sort=" + sort +
                ", site='" + site + '\'' +
                ", gsort=" + gsort +
                ", curl='" + curl + '\'' +
                ", curl1='" + curl1 + '\'' +
                ", chapter_name='" + chapter_name + '\'' +
                ", content='" + content + '\'' +
                ", book_chapter_md5='" + book_chapter_md5 + '\'' +
                ", cmd='" + cmd + '\'' +
                ", book_id='" + book_id + '\'' +
                ", parameter='" + parameter + '\'' +
                ", extra_parameter='" + extra_parameter + '\'' +
                ", api_url='" + api_url + '\'' +
                ", chapter_form=" + chapter_form +
                ", update_time='" + update_time + '\'' +
                '}';
    }

    public enum Status {
        SOURCE_ERROR("源网站转换失败，请您更换来源或稍候重试！"),
        CONTENT_EMPTY("源网站内容为空，请您更换来源阅读！"),
        CONTENT_ERROR("源网站内容错误，请您更换来源阅读！"),
        CONTENT_NORMAL("内容正常");

        public String tips;

        private Status(String tips) {
            this.tips = tips;
        }
    }
}
