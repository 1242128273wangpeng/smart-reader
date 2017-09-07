package net.lzbook.kit.data.bean;

/**
 * 项目名称：xzq
 * 类描述：
 * 创建人：Administrator
 * 创建时间：2017/8/11 0011
 */

public class BookEvent {

    public static String DEFAULTBOOK_UPDATED = "update_book";
    public static String PULL_BOOK_STATUS = "pull_book_status";

    private String mMsg;

    public BookEvent(String msg) {
        mMsg = msg;
    }

    public String getMsg() {
        return mMsg;
    }
}
