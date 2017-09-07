package net.lzbook.kit.data.bean;

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
        public int status;

        public String book_id;
        public String book_source_id;
        public String host;
        public int dex;

        public String parameter;
        public String extra_parameter;
        public String last_chapter_name;

        //章节数
        public int serial_number;
        public long update_time;
        public int word_count;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BookVoBean that = (BookVoBean) o;

            if (status != that.status) return false;
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
            result = 31 * result + status;
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
                    ", status=" + status +
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
                    '}';
        }
    }

    public static class SourcesBean {
        public String book_id;
        public String book_source_id;
        public String host;
        public Object url;
        public String terminal;
        public HashMap<String, String> source;
        public long update_time;
        public String last_chapter_name;
        public Object wordCount;
        public int dex;

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
}
