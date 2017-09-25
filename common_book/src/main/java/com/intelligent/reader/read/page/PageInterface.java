package com.intelligent.reader.read.page;

import com.dingyueads.sdk.Bean.Novel;
import com.intelligent.reader.read.help.CallBack;
import com.intelligent.reader.read.help.IReadDataFactory;
import com.intelligent.reader.read.help.NovelHelper;

import net.lzbook.kit.data.bean.ReadStatus;

import android.app.Activity;
import android.view.KeyEvent;

public interface PageInterface {

    void init(Activity activity, ReadStatus readStatus, NovelHelper novelHelper);

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

    void setReadFactory(IReadDataFactory factory);

    void setFirstPage(boolean firstPage);

    void setisAutoMenuShowing(boolean isShowing);

    boolean setKeyEvent(KeyEvent event);

    void loadNatvieAd();

    void setOnOperationClickListener(OnOperationClickListener onOperationClickListener);

    Novel getCurrentNovel();

    void removeAdView();

    interface OnOperationClickListener {
        void onOriginClick();

        void onTransCodingClick();
    }
}
