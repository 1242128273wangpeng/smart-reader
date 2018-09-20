package net.lzbook.kit.bean;

public class ChapterErrorBean {
    public String bookSourceId;
    public String bookName;
    public String author;
    public String bookChapterId;
    public String chapterId;
    public String chapterName;
    public String channelCode;
    public int serial;
    //章节host
    public String host;
    //错误类型，1-书籍无法阅读，2-缓存失败， 3-网络无法连接，4-空章乱码，5-重复内容或章节错误
    public int type;


    @Override
    public String toString() {
        return "ChapterErrorBean{" +
                "bookSourceId='" + bookSourceId + '\'' +
                ", bookName='" + bookName + '\'' +
                ", author='" + author + '\'' +
                ", bookChapterId='" + bookChapterId + '\'' +
                ", chapterId='" + chapterId + '\'' +
                ", chapterName='" + chapterName + '\'' +
                ", channelCode='" + channelCode + '\'' +
                ", serial=" + serial +
                ", host='" + host + '\'' +
                ", type=" + type +
                '}';
    }
}
