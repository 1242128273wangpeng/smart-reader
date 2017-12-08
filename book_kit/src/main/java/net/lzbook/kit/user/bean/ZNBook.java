package net.lzbook.kit.user.bean;

import net.lzbook.kit.data.bean.Book;

/**
 * 类描述：
 * 创建人：Zach
 * 创建时间：智能书籍建造类
 */
public class ZNBook extends Book {

    private ZNBook(Builder builder) {
        book_id = builder.bookId;
        book_source_id = builder.bookSourceId;
        name = builder.bookName;
        category = builder.category;
        author = builder.author;
        chapter_count = builder.chapterCount;
        last_chapter_name = builder.lastChapterName;
        img_url = builder.imgUrl;
        status = builder.status;
        site = builder.host;
        last_updatetime_native = builder.updateTime;
        dex = builder.dex;
    }

    public static class Builder {
        //最新更新时间
        public long updateTime;
        public int dex;
        private String bookId;
        private String bookSourceId;
        private String bookName;
        private String category;
        private String author;
        private int chapterCount;
        private String lastChapterName;
        private String imgUrl;
        private int status = -1;
        private String host;

        public ZNBook build() {
            return new ZNBook(this);
        }

        public Builder bookId(String bookId) {
            this.bookId = bookId;
            return this;
        }

        public Builder bookSourceId(String bookSourceId) {
            this.bookSourceId = bookSourceId;
            return this;
        }

        public Builder bookName(String bookName) {
            this.bookName = bookName;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder chapterCount(int chapterCount) {
            this.chapterCount = chapterCount;
            return this;
        }

        public Builder lastChapterName(String lastChapterName) {
            this.lastChapterName = lastChapterName;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder imgUrl(String imgUrl) {
            this.imgUrl = imgUrl;
            return this;
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder dex(int dex) {
            this.dex = dex;
            return this;
        }

        public Builder updateTime(long updateTime) {
            this.updateTime = updateTime;
            return this;
        }
    }

}
