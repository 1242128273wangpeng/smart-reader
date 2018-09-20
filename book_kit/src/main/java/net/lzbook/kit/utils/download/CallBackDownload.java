package net.lzbook.kit.utils.download;

public interface CallBackDownload {
    void onTaskStatusChange(String book_id);

    void onTaskFinish(String book_id);

    void onTaskFailed(String book_id, Throwable throwable);

    void onTaskProgressUpdate(String book_id);
}
