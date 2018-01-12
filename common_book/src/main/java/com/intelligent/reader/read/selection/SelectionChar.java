package com.intelligent.reader.read.selection;

import android.graphics.Point;

/**
 * @author lijun Lee
 * @desc 被选字符
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/1/11 18:31
 */

public class SelectionChar {

    public char charData;

    public Boolean selected = false;

    public Point topLeftPosition;
    public Point topRightPosition;
    public Point bottomLeftPosition;
    public Point bottomRightPosition;

    public float charWidth = 0;

    public int index = 0;
}
