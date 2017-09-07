package com.intelligent.reader.util;

/**
 * Created by Administrator on 2016/8/27.
 */
public class EventBookStore {
    public static final String BOOKSTORE = "type_event";
    public static final int TYPE_TO_BOOKSTORE = 1;
    public static final int TYPE_TO_BOOKSHELF = 0;
    public static final int TYPE_ERROR = 2;
    public static final int TYPE_TO_SWITCH_THEME = 0;
    private int type;

    public EventBookStore(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
