package com.intelligent.reader.read.help;

import net.lzbook.kit.data.bean.ReadViewEnums;

import android.view.KeyEvent;

/**
 * 子容器接口
 * Created by wt on 2017/12/13.
 */

public interface IReadView {
    /**
     * 入口 调用初始化操作：首次进入、字体大小改变、行间距改变、换源、跳章
     *
     */
    void entrance();

    /**
     * 设置 IReadView 实现 View 的变化监听
     *
     * @param mReadPageChange 监听对象
     */
    void setIReadPageChange(IReadPageChange mReadPageChange);

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

    boolean onKeyEvent(KeyEvent event);
}
