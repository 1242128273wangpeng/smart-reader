package com.ding.basic.bean;

import org.jetbrains.annotations.NotNull;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;
import java.util.List;


/**
 * 搜索推荐模块  一期二期实体类一致
 * Created by zhenXiang on 2018\1\16 0016.
 */
public class SearchRecommendBook implements Serializable {

    private String respCode;
    private String message;
    private List<DataBean> data;

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    @Entity(tableName = "search_recommend")
    public static class DataBean {
        /**
         * id : 58bfe2a64ed3437e6b6b5a77
         * bookId : 57692e291b341116f66491d1
         * bookName : 万道剑尊
         * authorName : 打死都要钱
         * description : 下无双！他是独一无二的逆天君王，杀伐果断，杀尽世间一切该杀之人！他，更是掌控万道，亘古以来史上第一剑尊！
         * serialStatus : SERIALIZE
         * label : 玄幻魔法,历史军事,穿越,文学美文,玄幻,东方玄幻,玄幻奇幻
         * genre : 玄幻
         * subGenre : 东方玄幻
         * sourceImageUrl : http://qidian.qpic.cn/qdbimg/349573/1003414055/180
         * imageId : null
         * attribute : {"gid":"200008377","nid":"0"}
         * host : www.mantantd.cn
         * url :
         * terminal : WEB
         * bookChapterMd5 : new-sync
         * bookChapterId : 58bfe2a64ed3437e6b6b5a76
         * lastChapterId : 5a3a4f9485b1ce5665afd7b1
         * lastChapterName : 第二千七百八十一章 天虚宫
         * lastSerialNumber : 2792
         * v : 1597
         * updateTime : 1513770900786
         * createTime : 1488970406693
         * chapterCount : 2784
         * chapterBlankCount : null
         * chapterShortCount : 2450000
         * wordCount : null
         * wordCountDescp : 571.3万
         * readerCount : 380156
         * readerCountDescp : 38.0万
         * dex : 0
         * hot : null
         * score : 10
         * vip : 0
         * chapterPrice : 0
         * bookprice : 0
         * selfPrice : 0
         * selfBookPrice : 0
         */

        @ColumnInfo(name = "booksource_id")
        private String id;
        @PrimaryKey
        @NotNull
        @ColumnInfo(name = "book_id")
        private String bookId;
        @ColumnInfo(name = "book_name")
        private String bookName;
        @ColumnInfo(name = "author_name")
        private String authorName;
        @Ignore
        private String description;
        @ColumnInfo(name = "serial_status")
        private String serialStatus;
        @Ignore
        private String label;
        @ColumnInfo(name = "genre")
        private String genre;
        @Ignore
        private String subGenre;
        @ColumnInfo(name = "source_imageurl")
        private String sourceImageUrl;
        @Ignore
        private String imageId;
        @Ignore
        private AttributeBean attribute;
        @ColumnInfo(name = "host")
        private String host;
        @Ignore
        private String url;
        @Ignore
        private String terminal;
        @Ignore
        private String bookChapterMd5;
        @Ignore
        private String bookChapterId;
        @Ignore
        private String lastChapterId;
        @Ignore
        private String lastChapterName;
        @Ignore
        private long lastSerialNumber;
        @Ignore
        private long v;
        @Ignore
        private long updateTime;
        @Ignore
        private long createTime;
        @Ignore
        private int chapterCount;
        @Ignore
        private int chapterBlankCount;
        @Ignore
        private int chapterShortCount;
        @Ignore
        private int wordCount;
        @ColumnInfo(name = "word_count_descp")
        private String wordCountDescp;
        @Ignore
        private int readerCount;
        @ColumnInfo(name = "reader_count_descp")
        private String readerCountDescp;
        @Ignore
        private int dex;
        @Ignore
        private int hot;
        @ColumnInfo(name = "score")
        private double score;
        @Ignore
        private int vip;
        @Ignore
        private float chapterPrice;
        @Ignore
        private float bookprice;
        @Ignore
        private float selfPrice;
        @Ignore
        private float selfBookPrice;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getBookId() {
            return bookId;
        }

        public void setBookId(String bookId) {
            this.bookId = bookId;
        }

        public String getBookName() {
            return bookName;
        }

        public void setBookName(String bookName) {
            this.bookName = bookName;
        }

        public String getAuthorName() {
            return authorName;
        }

        public void setAuthorName(String authorName) {
            this.authorName = authorName;
        }


        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSerialStatus() {
            return serialStatus;
        }

        public void setSerialStatus(String serialStatus) {
            this.serialStatus = serialStatus;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getGenre() {
            return genre;
        }

        public void setGenre(String genre) {
            this.genre = genre;
        }

        public String getSubGenre() {
            return subGenre;
        }

        public void setSubGenre(String subGenre) {
            this.subGenre = subGenre;
        }

        public String getSourceImageUrl() {
            return sourceImageUrl;
        }

        public void setSourceImageUrl(String sourceImageUrl) {
            this.sourceImageUrl = sourceImageUrl;
        }


        public AttributeBean getAttribute() {
            return attribute;
        }

        public void setAttribute(AttributeBean attribute) {
            this.attribute = attribute;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getTerminal() {
            return terminal;
        }

        public void setTerminal(String terminal) {
            this.terminal = terminal;
        }

        public String getBookChapterMd5() {
            return bookChapterMd5;
        }

        public void setBookChapterMd5(String bookChapterMd5) {
            this.bookChapterMd5 = bookChapterMd5;
        }

        public String getBookChapterId() {
            return bookChapterId;
        }

        public void setBookChapterId(String bookChapterId) {
            this.bookChapterId = bookChapterId;
        }

        public String getLastChapterId() {
            return lastChapterId;
        }

        public void setLastChapterId(String lastChapterId) {
            this.lastChapterId = lastChapterId;
        }

        public String getLastChapterName() {
            return lastChapterName;
        }

        public void setLastChapterName(String lastChapterName) {
            this.lastChapterName = lastChapterName;
        }


        public long getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(long updateTime) {
            this.updateTime = updateTime;
        }

        public long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }

        public int getChapterCount() {
            return chapterCount;
        }

        public void setChapterCount(int chapterCount) {
            this.chapterCount = chapterCount;
        }

        public Object getChapterBlankCount() {
            return chapterBlankCount;
        }


        public void setChapterShortCount(int chapterShortCount) {
            this.chapterShortCount = chapterShortCount;
        }


        public String getWordCountDescp() {
            return wordCountDescp;
        }

        public void setWordCountDescp(String wordCountDescp) {
            this.wordCountDescp = wordCountDescp;
        }

        public int getReaderCount() {
            return readerCount;
        }

        public void setReaderCount(int readerCount) {
            this.readerCount = readerCount;
        }

        public String getReaderCountDescp() {
            return readerCountDescp;
        }

        public void setReaderCountDescp(String readerCountDescp) {
            this.readerCountDescp = readerCountDescp;
        }

        public int getDex() {
            return dex;
        }

        public void setDex(int dex) {
            this.dex = dex;
        }

        public Object getHot() {
            return hot;
        }

        @Ignore
        public void setScore(int score) {
            this.score = score;
        }

        public int getVip() {
            return vip;
        }

        public void setVip(int vip) {
            this.vip = vip;
        }

        public String getImageId() {
            return imageId;
        }

        public void setImageId(String imageId) {
            this.imageId = imageId;
        }

        public long getLastSerialNumber() {
            return lastSerialNumber;
        }

        public void setLastSerialNumber(long lastSerialNumber) {
            this.lastSerialNumber = lastSerialNumber;
        }

        public long getV() {
            return v;
        }

        public void setV(long v) {
            this.v = v;
        }

        public void setChapterBlankCount(int chapterBlankCount) {
            this.chapterBlankCount = chapterBlankCount;
        }

        public int getChapterShortCount() {
            return chapterShortCount;
        }

        public int getWordCount() {
            return wordCount;
        }

        public void setWordCount(int wordCount) {
            this.wordCount = wordCount;
        }

        public void setHot(int hot) {
            this.hot = hot;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public float getChapterPrice() {
            return chapterPrice;
        }

        public void setChapterPrice(float chapterPrice) {
            this.chapterPrice = chapterPrice;
        }

        public float getBookprice() {
            return bookprice;
        }

        public void setBookprice(float bookprice) {
            this.bookprice = bookprice;
        }

        public float getSelfPrice() {
            return selfPrice;
        }

        public void setSelfPrice(float selfPrice) {
            this.selfPrice = selfPrice;
        }

        public float getSelfBookPrice() {
            return selfBookPrice;
        }

        public void setSelfBookPrice(float selfBookPrice) {
            this.selfBookPrice = selfBookPrice;
        }

        public static class AttributeBean {
            /**
             * gid : 200008377
             * nid : 0
             */

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
}
