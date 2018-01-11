package net.lzbook.kit.data.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/11/1.
 */
public class CoverPage implements Serializable {
    public boolean success;
    public Object error_log;
    public Object params;

    @SerializedName("book_vo")
    public BookVoBean bookVo;
    public List<SourcesBean> sources;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoverPage coverPage = (CoverPage) o;

        if (success != coverPage.success) return false;
        if (error_log != null ? !error_log.equals(coverPage.error_log) : coverPage.error_log != null)
            return false;
        if (params != null ? !params.equals(coverPage.params) : coverPage.params != null)
            return false;
        if (bookVo != null ? !bookVo.equals(coverPage.bookVo) : coverPage.bookVo != null)
            return false;
        return sources != null ? sources.equals(coverPage.sources) : coverPage.sources == null;

    }

    @Override
    public int hashCode() {
        int result = (success ? 1 : 0);
        result = 31 * result + (error_log != null ? error_log.hashCode() : 0);
        result = 31 * result + (params != null ? params.hashCode() : 0);
        result = 31 * result + (bookVo != null ? bookVo.hashCode() : 0);
        result = 31 * result + (sources != null ? sources.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CoverPage{" +
                "success=" + success +
                ", error_log=" + error_log +
                ", params=" + params +
                ", bookVo=" + bookVo +
                ", sources=" + sources +
                '}';
    }

    public static class BookVoBean {
        //小说名
        public String name;
        //作者
        public String author;
        //描述
        public String desc;
        //分类
        public String labels;
        //图片链接
        public String img_url;
        ////获取自有数据来源时URL
        public String url;
        ////小说更新状态

        public int book_status;

        @SerializedName("status")
        public String bookStatus;

        public String book_id;
        public String book_source_id;
        public String host;
        public int dex;

        public String parameter;
        public String extra_parameter;
        public String last_chapter_name;

        @SerializedName("last_chapter")
        public LastChapter lastChapter;

        //章节序列号
        public int serial_number;
        public long update_time;
        public int word_count;
        public String wordCountDescp;
        public String readerCountDescp;
        public double score;


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BookVoBean that = (BookVoBean) o;

            if (book_status != (that.book_status)) return false;
            if (dex != that.dex) return false;
            if (serial_number != that.serial_number) return false;
            if (update_time != that.update_time) return false;
            if (word_count != that.word_count) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            if (author != null ? !author.equals(that.author) : that.author != null) return false;
            if (desc != null ? !desc.equals(that.desc) : that.desc != null) return false;
            if (labels != null ? !labels.equals(that.labels) : that.labels != null) return false;
            if (img_url != null ? !img_url.equals(that.img_url) : that.img_url != null)
                return false;
            if (url != null ? !url.equals(that.url) : that.url != null) return false;
            if (book_id != null ? !book_id.equals(that.book_id) : that.book_id != null)
                return false;
            if (book_source_id != null ? !book_source_id.equals(that.book_source_id) : that.book_source_id != null)
                return false;
            if (host != null ? !host.equals(that.host) : that.host != null) return false;
            if (parameter != null ? !parameter.equals(that.parameter) : that.parameter != null)
                return false;
            if (extra_parameter != null ? !extra_parameter.equals(that.extra_parameter) : that.extra_parameter != null)
                return false;
            return last_chapter_name != null ? last_chapter_name.equals(that.last_chapter_name) : that.last_chapter_name == null;

        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (author != null ? author.hashCode() : 0);
            result = 31 * result + (desc != null ? desc.hashCode() : 0);
            result = 31 * result + (labels != null ? labels.hashCode() : 0);
            result = 31 * result + (img_url != null ? img_url.hashCode() : 0);
            result = 31 * result + (url != null ? url.hashCode() : 0);
            result = 31 * result + book_status;
            result = 31 * result + (book_id != null ? book_id.hashCode() : 0);
            result = 31 * result + (book_source_id != null ? book_source_id.hashCode() : 0);
            result = 31 * result + (host != null ? host.hashCode() : 0);
            result = 31 * result + dex;
            result = 31 * result + (parameter != null ? parameter.hashCode() : 0);
            result = 31 * result + (extra_parameter != null ? extra_parameter.hashCode() : 0);
            result = 31 * result + (last_chapter_name != null ? last_chapter_name.hashCode() : 0);
            result = 31 * result + serial_number;
            result = 31 * result + (int) (update_time ^ (update_time >>> 32));
            result = 31 * result + word_count;
            return result;
        }

        @Override
        public String toString() {
            return "BookVoBean{" +
                    "name='" + name + '\'' +
                    ", author='" + author + '\'' +
                    ", desc='" + desc + '\'' +
                    ", labels='" + labels + '\'' +
                    ", img_url='" + img_url + '\'' +
                    ", url='" + url + '\'' +
                    ", status=" + book_status +
                    ", book_id='" + book_id + '\'' +
                    ", book_source_id='" + book_source_id + '\'' +
                    ", host='" + host + '\'' +
                    ", dex=" + dex +
                    ", parameter='" + parameter + '\'' +
                    ", extra_parameter='" + extra_parameter + '\'' +
                    ", last_chapter_name='" + last_chapter_name + '\'' +
                    ", serial_number=" + serial_number +
                    ", update_time=" + update_time +
                    ", word_count=" + word_count +
                    ", wordCountDescp=" + wordCountDescp +
                    ", readerCountDescp=" + readerCountDescp +
                    ", score=" + score +
                    '}';
        }
    }

    public static class SourcesBean {
        public String book_id;
        public String book_source_id;
        public String host;
        public String url;
        public String terminal;
        public HashMap<String, String> source;
        public long update_time;
        public String last_chapter_name;
        public Object wordCount;

        public BookSourceVOBean bookSourceVO;
        public LastChapterBeanX last_chapter;
        public int dex;
        public String wordCountDescp;
        public String readerCountDescp;
        public double score;
        public String labels;
        private Object last_vipChapter;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SourcesBean that = (SourcesBean) o;

            if (update_time != that.update_time) return false;
            if (dex != that.dex) return false;
            if (book_id != null ? !book_id.equals(that.book_id) : that.book_id != null)
                return false;
            if (book_source_id != null ? !book_source_id.equals(that.book_source_id) : that.book_source_id != null)
                return false;
            if (host != null ? !host.equals(that.host) : that.host != null) return false;
            if (url != null ? !url.equals(that.url) : that.url != null) return false;
            if (terminal != null ? !terminal.equals(that.terminal) : that.terminal != null)
                return false;
            if (source != null ? !source.equals(that.source) : that.source != null) return false;
            if (last_chapter_name != null ? !last_chapter_name.equals(that.last_chapter_name) : that.last_chapter_name != null)
                return false;
            return wordCount != null ? wordCount.equals(that.wordCount) : that.wordCount == null;

        }

        @Override
        public int hashCode() {
            int result = book_id != null ? book_id.hashCode() : 0;
            result = 31 * result + (book_source_id != null ? book_source_id.hashCode() : 0);
            result = 31 * result + (host != null ? host.hashCode() : 0);
            result = 31 * result + (url != null ? url.hashCode() : 0);
            result = 31 * result + (terminal != null ? terminal.hashCode() : 0);
            result = 31 * result + (source != null ? source.hashCode() : 0);
            result = 31 * result + (int) (update_time ^ (update_time >>> 32));
            result = 31 * result + (last_chapter_name != null ? last_chapter_name.hashCode() : 0);
            result = 31 * result + (wordCount != null ? wordCount.hashCode() : 0);
            result = 31 * result + dex;
            return result;
        }

        @Override
        public String toString() {
            return "SourcesBean{" +
                    "book_id='" + book_id + '\'' +
                    ", book_source_id='" + book_source_id + '\'' +
                    ", host='" + host + '\'' +
                    ", url=" + url +
                    ", terminal='" + terminal + '\'' +
                    ", source=" + source +
                    ", update_time=" + update_time +
                    ", last_chapter_name='" + last_chapter_name + '\'' +
                    ", wordCount=" + wordCount +
                    ", dex=" + dex +
                    '}';
        }
    }

    public static class BookSourceVOBean {

        public String id;
        public String bookId;
        public String bookName;
        public String authorName;
        public String description;
        public String serialStatus;
        public String label;
        public String sourceImageUrl;
        public String imageId;
        public AttributeBean attribute;
        public String host;
        public String url;
        public String terminal;
        public String bookChapterMd5;
        public String bookChapterId;
        public String lastChapterId;
        public String lastChapterName;
        public long lastSerialNumber;
        public long v;
        public long updateTime;
        public long createTime;
        public int chapterCount;
        public int chapterBlankCount;
        public int chapterShortCount;
        public int wordCount;
        public String wordCountDescp;
        public int readerCount;
        public String readerCountDescp;
        public int dex;
        public int hot;
        public double score;
        public int vip;
        public double chapterPrice;
        public double bookprice;
        public double selfPrice;
        public double selfBookPrice;

        public static class AttributeBean {
            private String gid;
            private String nid;

            public String getGid() {
                return gid;
            }

            public void setGid(String gid) {
                this.gid = gid;
            }

            public String getNid() {
                return nid;
            }

            public void setNid(String nid) {
                this.nid = nid;
            }
        }
    }

    public static class LastChapterBeanX {

        public IdBean id;
        public Object bookChapterId;
        public String name;
        public int serialNumber;
        public String host;
        public String url;
        public String url1;
        public String terminal;
        public String status;
        public String bookChapterMd5;
        public long updateTime;
        public long createTime;
        public String bookSourceId;
        public int wordCount;

        public static class IdBean {

            public int timestamp;
            public int machineIdentifier;
            public int processIdentifier;
            public int counter;
            public int timeSecond;
            public long time;
            public long date;

        }
    }

    public class LastChapter {

        private String id;
        private String name;
        private long update_time;
        private int serial_number;

        public int getSerial_number() {
            return serial_number;
        }

        public void setSerial_number(int serial_number) {
            this.serial_number = serial_number;
        }

        public long getUpdate_time() {
            return update_time;
        }

        public void setUpdate_time(long update_time) {
            this.update_time = update_time;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LastChapter that = (LastChapter) o;

            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            return name != null ? name.equals(that.name) : that.name == null;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }
    }
}
