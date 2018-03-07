package net.lzbook.kit.data.bean;


import net.lzbook.kit.book.download.DownloadState;


public class BookTask {
    public Book book;
    public String book_id;


    public boolean isAutoState = false;
    public boolean isWifiAuto = false;
    public boolean isOldTaskProgress = false;

    public volatile DownloadState state;

    public int progress = 0;
    public int startSequence;
    public int beginSequence;
    public int endSequence;

    //下载章节进度位置
    public int shouldCacheCount = 0;
    //处理了多少章节
    public int processChapterCount = 0;

    public String start_chapterid;
    public String end_chapterid;
    //真实缓存了多少章节
    public int cache_chapters = 0;
    public long cache_times;
    public boolean isFullCache = false;

    public BookTask(Book book, DownloadState state, int startSequence, int endSequence) {
        if (book == null) {
            throw new IllegalArgumentException("book may not be null");
        } else {
            try {
                //避免换源时， 任务无法刷新的问题
                this.book = book.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                throw new IllegalArgumentException("book cant clone");
            }
            this.book_id = book.book_id;
            this.state = state;
            this.startSequence = startSequence;
            this.endSequence = endSequence;
        }
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof BookTask)) {
            return super.equals(o);
        }
        return this.book.book_id.equals(((BookTask) o).book.book_id);
    }

    public int hashCode() {
        return this.book.book_id.hashCode();
    }

    public String toString() {
        return super.toString();
    }
}
