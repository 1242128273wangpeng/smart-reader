package net.lzbook.kit.bean;


import net.lzbook.kit.bean.BookUpdateResult;

public interface UpdateCallBack {
    void onSuccess(BookUpdateResult result);

    void onException(Exception e);

}
