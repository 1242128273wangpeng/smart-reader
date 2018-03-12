package com.intelligent.reader.event;

/**
 * Desc EventBus 从下载管理页面跳转到书城
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/9 0002 09:48
 */

public class DownloadManagerToHome {
    private int tabPosition;

    public DownloadManagerToHome(int tabPosition) {
        this.tabPosition = tabPosition;
    }


    public int getTabPosition() {
        return tabPosition;
    }

    public void setTabPosition(int tabPosition) {
        this.tabPosition = tabPosition;
    }
}
