package net.lzbook.kit.user.bean;

/**
 * 类描述：
 * 创建人：Zach
 * 智能书籍建造类
 */
public class QGBook extends Book {

    private QGBook(QGBook.Builder builder) {
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
    }

    public static class Builder {
        //最新更新时间
        public long updateTime;
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

        public QGBook build() {
            return new QGBook(this);
        }

        public QGBook.Builder bookId(String bookId) {
            this.bookId = bookId;
            return this;
        }

        public QGBook.Builder bookSourceId(String bookSourceId) {
            this.bookSourceId = bookSourceId;
            return this;
        }

        public QGBook.Builder bookName(String bookName) {
            this.bookName = bookName;
            return this;
        }

        public QGBook.Builder category(String category) {
            this.category = category;
            return this;
        }

        public QGBook.Builder author(String author) {
            this.author = author;
            return this;
        }

        public QGBook.Builder chapterCount(int chapterCount) {
            this.chapterCount = chapterCount;
            return this;
        }

        public QGBook.Builder lastChapterName(String lastChapterName) {
            this.lastChapterName = lastChapterName;
            return this;
        }

        public QGBook.Builder host(String host) {
            this.host = host;
            return this;
        }

        public QGBook.Builder imgUrl(String imgUrl) {
            this.imgUrl = imgUrl;
            return this;
        }

        public QGBook.Builder status(int status) {
            this.status = status;
            return this;
        }

        public QGBook.Builder updateTime(long updateTime) {
            this.updateTime = updateTime;
            return this;
        }
    }

}