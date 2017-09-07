package net.lzbook.kit.data.bean;

import net.lzbook.kit.data.UpdateCallBack;

import java.util.ArrayList;

public class BookUpdateTaskData {

    private static BookUpdateTaskData data;
    //小说更新来源
    public UpdateTaskFrom from = null;
    //更新的小说
    public ArrayList<Book> books;
    //更新结构的回调接口
    public UpdateCallBack mCallBack;
    private int hash;

    public static BookUpdateTaskData getData() {
        return data;
    }

    public static void setData(BookUpdateTaskData data) {
        BookUpdateTaskData.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BookUpdateTaskData) {
            BookUpdateTaskData ao = (BookUpdateTaskData) o;
            return from == ao.from;
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0) {
            h = (from == null) ? 0 : from.from() << 30;
            hash = h;
        }
        return h;
    }

    @Override
    public String toString() {
        return "BookUpdateTaskData{" +
                "from=" + from +
                ", books=" + books +
                ", hash=" + hash +
                ", mCallBack=" + mCallBack +
                '}';
    }

    public enum UpdateTaskFrom {
        FROM_SELF(0), FROM_BOOK_SHELF(1);
        private int from;

        UpdateTaskFrom(int f) {
            from = f;
        }

        public int from() {
            return from;
        }
    }
}
