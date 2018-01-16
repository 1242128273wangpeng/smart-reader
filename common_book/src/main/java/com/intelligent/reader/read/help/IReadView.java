package com.intelligent.reader.read.help;

import com.intelligent.reader.read.mode.ReadInfo;

import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.NovelLineBean;
import net.lzbook.kit.data.bean.ReadViewEnums;

import java.util.ArrayList;

/**
 * 子容器接口
 * Created by wt on 2017/12/13.
 */

public interface IReadView {
    /**
     * 入口 调用初始化操作：首次进入、字体大小改变、行间距改变、换源、跳章
     *
     * @param mReadInfo 阅读信息
     */
    void entrance(ReadInfo mReadInfo);

    /**
     * 设置背景颜色
     */
    void setBackground();

    /**
     * 设置 IReadView 实现 View 的变化监听
     *
     * @param mReadPageChange 监听对象
     */
    void setIReadPageChange(IReadPageChange mReadPageChange);

    void onRedrawPage();

    /**
     * 跳章
     *
     * @param sequence 章序号
     */
    void onJumpChapter(int sequence);

    /**
     * 切换动画
     *
     * @param animation
     */
    void onAnimationChange(ReadViewEnums.Animation animation);

    /**
     * Horizontal事件向仿真分发
     *
     * @param mHorizontalEvent event
     */
    void setHorizontalEventListener(HorizontalEvent mHorizontalEvent);
}
