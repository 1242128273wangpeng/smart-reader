package com.intelligent.reader.read.help;

import android.view.View;
import com.intelligent.reader.read.mode.ReadInfo;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.NovelLineBean;

import java.util.ArrayList;

/**
 * 子容器接口
 * Created by wt on 2017/12/13.
 */

public interface IReadView {
    /**
     * 入口 调用初始化操作：首次进入、字体大小改变、行间距改变、换源、跳章
     * @param mReadInfo 阅读信息
     */
    void entrance(ReadInfo mReadInfo);

    /**
     * 设置时间
     * @param time 时间
     */
    void freshTime(CharSequence time);
    /**
     * 设置电池
     * @param percent 电量
     */
    void freshBattery(float percent);
    /**
     * 设置背景颜色
     */
    void setBackground();
    /**
     * 设置阅读信息
     * @param mReadInfo 新阅读信息
     */
    void setReadInfo(ReadInfo mReadInfo);

    /**
     * 设置 IReadView 实现 View 的变化监听
     * @param mReadPageChange 监听对象
     */
    void setIReadPageChange(IReadPageChange mReadPageChange);

    /**
     * 设置返回章节
     * @param chapterList 分页后章节内容对象
     */
    void setLoadChapter(int msg,Chapter chapter,ArrayList<ArrayList<NovelLineBean>> chapterList);

    /**
     * 设置返回广告
     * @param view 广告view
     */
    void setLoadAd(View view);

//    /**
//     * 设置字体颜色
//     * @param color
//     */
//    void setTextColor(int color);

    void onRedrawPage();
}
