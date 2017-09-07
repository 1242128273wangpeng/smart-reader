package com.intelligent.reader.event;

/**
 * Created by Administrator on 2017\8\10 0010.
 */

public class DownLoaderToHome {
    private int tabPosition;

    public DownLoaderToHome(int tabPosition) {
        this.tabPosition = tabPosition;
    }


    public int getTabPosition() {
        return tabPosition;
    }

    public void setTabPosition(int tabPosition) {
        this.tabPosition = tabPosition;
    }
}
