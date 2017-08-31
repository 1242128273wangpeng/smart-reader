package net.lzbook.kit.data;

import net.lzbook.kit.book.download.CallBackDownload;

public class NullCallBack implements CallBackDownload {

    @Override
    public void onChapterDownFailed(String gid, int sequence, String msg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onChapterDownFinish(String gid, int sequence) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onChapterDownStart(String gid, int sequence) {
        // TODO Auto-generated method stub

    }

    public void onOffLineFinish() {
    }

    public void onProgressUpdate(String gid, int progress) {
    }

    @Override
    public void onTaskFinish(String gid) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTaskStart(String gid) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onChapterDownFailedNeedLogin() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onChapterDownFailedNeedPay(String gid, int nid, int sequence) {
        // TODO Auto-generated method stub

    }


}
