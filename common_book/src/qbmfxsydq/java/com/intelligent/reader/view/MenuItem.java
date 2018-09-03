package com.intelligent.reader.view;

/**
 * @author lijun Lee
 * @desc Bottom Menu Item
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/3/28 18:06
 */

public class MenuItem {

    private String title;

    private int color;

    public MenuItem(String title, int color) {
        this.title = title;
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
