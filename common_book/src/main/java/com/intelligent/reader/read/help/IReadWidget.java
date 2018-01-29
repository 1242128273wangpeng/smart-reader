package com.intelligent.reader.read.help;

/**
 * 父容器接口
 * Created by wt on 2017/12/13.
 */

public interface IReadWidget {
    /**
     * 入口 调用初始化操作：首次进入、字体大小改变、行间距改变、换源、跳章
     */
    void entrance();

    /**
     * 设置 IReadView 实现 View 的变化监听
     *
     * @param mReadPageChange 监听对象
     */
    void setIReadPageChange(IReadPageChange mReadPageChange);

    void changeAnimMode(int mode);

    void onResume();

    void onPause();

    void onDestroy();

    void startAutoRead();

    void stopAutoRead();

    boolean isAutoRead();
}
