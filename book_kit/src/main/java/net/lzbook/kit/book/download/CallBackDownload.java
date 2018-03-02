package net.lzbook.kit.book.download;

public interface CallBackDownload {
    void onTaskStatusChange(String book_id);

    void onTaskFinish(String book_id);
    void onTaskFailed(String book_id, Throwable t);

    void onTaskProgressUpdate(String book_id);
}
