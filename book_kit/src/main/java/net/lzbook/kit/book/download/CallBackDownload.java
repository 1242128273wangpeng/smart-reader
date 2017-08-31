package net.lzbook.kit.book.download;

public interface CallBackDownload {
    void onTaskStart(String book_id);

    void onChapterDownStart(String book_id, int sequence);

    void onChapterDownFinish(String book_id, int sequence);

    void onChapterDownFailed(String book_id, int sequence, String msg);

    void onChapterDownFailedNeedLogin();

    void onChapterDownFailedNeedPay(String book_id, int nid, int sequence);

    void onTaskFinish(String book_id);

    void onProgressUpdate(String book_id, int progress);

    void onOffLineFinish();
}
