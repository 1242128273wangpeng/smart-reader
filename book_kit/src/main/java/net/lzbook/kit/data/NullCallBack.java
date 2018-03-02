package net.lzbook.kit.data;

import net.lzbook.kit.book.download.CallBackDownload;

public class NullCallBack implements CallBackDownload {


    @Override
    public void onTaskStatusChange(String book_id) {

    }

    @Override
    public void onTaskFinish(String book_id) {

    }

    @Override
    public void onTaskFailed(String book_id, Throwable t) {

    }

    @Override
    public void onTaskProgressUpdate(String book_id) {

    }
}
