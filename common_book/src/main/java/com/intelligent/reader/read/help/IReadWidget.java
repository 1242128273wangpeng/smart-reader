package com.intelligent.reader.read.help;

import com.intelligent.reader.read.mode.ReadInfo;

/**
 * 父容器接口
 * Created by wt on 2017/12/13.
 */

public interface IReadWidget {
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

    /**
     * 设置SettingView重画
     */
    void onRedrawPage();

    /**
     * 跳章
     *
     * @param sequence 章序号
     */
    void onJumpChapter(int sequence);

    void changeAnimMode(int mode);
}
