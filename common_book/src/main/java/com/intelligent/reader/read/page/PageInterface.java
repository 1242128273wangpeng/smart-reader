package com.intelligent.reader.read.page;

import com.intelligent.reader.read.help.CallBack;
import com.intelligent.reader.read.help.NovelHelper;
import com.intelligent.reader.reader.ReaderViewModel;


import android.app.Activity;
import android.view.KeyEvent;

public interface PageInterface {

    void init(Activity activity, NovelHelper novelHelper);

    void freshTime(CharSequence time);

    void freshBattery(float percent);

    void drawNextPage();

    void drawCurrentPage();

    void setTextColor(int color);

    void changeBatteryBg(int res);

    void setBackground();

    void setPageBackColor(int color);

    void refreshCurrentPage();

    void tryTurnPrePage();

    void onAnimationFinish();

    void setCallBack(CallBack callBack);

    void clear();

    boolean isAutoReadMode();

    void startAutoRead();

    void exitAutoRead();

    void exitAutoReadNoCancel();

    void tryResumeAutoRead();

    void resumeAutoRead();

    void pauseAutoRead();

    void getChapter(boolean needSavePage);

    void getPreChapter();

    void getNextChapter();

    void setViewModel(ReaderViewModel mReaderViewModel);

    void setFirstPage(boolean firstPage);

    void setisAutoMenuShowing(boolean isShowing);

    boolean setKeyEvent(KeyEvent event);

    void setOnOperationClickListener(OnOperationClickListener onOperationClickListener);

    interface OnOperationClickListener {
        void onOriginClick();

        void onTransCodingClick();
    }
}
