package net.lzbook.kit.data;

import net.lzbook.kit.data.bean.BookUpdateResult;

public interface UpdateCallBack {
    void onSuccess(BookUpdateResult result);

    void onException(Exception e);

}
