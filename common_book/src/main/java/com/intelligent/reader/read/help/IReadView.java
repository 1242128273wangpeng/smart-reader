package com.intelligent.reader.read.help;

import android.app.Activity;

import com.intelligent.reader.reader.ReaderViewModel;

import net.lzbook.kit.data.bean.Chapter;

/**
 * 子容器接口
 * Created by wt on 2017/11/23.
 */

public interface IReadView {
    void init(Activity activity, NovelHelper novelHelper);

    void freshTime(CharSequence time);

    void freshBattery(float percent);

    void setTextColor(int color);

    void setBackground();

    void changeBatteryBg(int res);


    void getChapter(boolean needSavePage);

    void setReadViewModel(ReaderViewModel mReadViewModel);

    void onLoadChapter(int what, Chapter chapter);
}
