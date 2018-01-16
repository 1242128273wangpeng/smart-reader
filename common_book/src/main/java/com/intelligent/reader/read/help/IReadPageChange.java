package com.intelligent.reader.read.help;

/**
 *
 * Created by wt on 2017/12/13.
 */

public interface IReadPageChange {

    /**
     * 显示菜单
     */
    void showMenu(Boolean isShow);

    /**
     * 跳转BookEnd
     */
    void goToBookOver();

    /**
     * 原网页点击事件
     */
    void onOriginClick();
    /**
     * 转码声明点击事件
     */
    void onTransCodingClick();
    /*
     * 打点统计
     */
    void addLog();
}
