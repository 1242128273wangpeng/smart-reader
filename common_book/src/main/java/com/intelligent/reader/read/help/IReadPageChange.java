package com.intelligent.reader.read.help;

import net.lzbook.kit.data.bean.ReadViewEnums;

/**
 *
 * Created by wt on 2017/12/13.
 */

public interface IReadPageChange {

    /**
     * 加载章节
     * @param type 加载类型
     * @param sequence 章节序号
     * @param isShowLoadPage 是否显示LoadPageView
     * @param
     */
    void onLoadChapter(ReadViewEnums.MsgType type, int sequence,boolean isShowLoadPage,ReadViewEnums.PageIndex pageIndex);
    /**
     * 显示菜单
     */
    void showMenu(Boolean isShow);

    /**
     * 加载广告
     */
    void loadAD();

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
}
