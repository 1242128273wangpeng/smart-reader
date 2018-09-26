package net.lzbook.kit.bean;

/**
 * Created by yuchao on 2017/12/11 0011
 */

public class PagerDesc {
    private float top;
    private float left;
    private float right;
    private float bottom;

    public PagerDesc(float top, float left, float right, float bottom) {
        this.top = top;
        this.left = left;
        this.right = right;
        this.bottom = bottom;
    }

    public float getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public float getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public float getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public float getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }
}
