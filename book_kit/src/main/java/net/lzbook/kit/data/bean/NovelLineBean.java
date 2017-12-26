package net.lzbook.kit.data.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by yuchao on 2017/9/15 0015.
 */

public class NovelLineBean implements Serializable {
    private static final long serialVersionUID = 9173402120406347893L;
    private String lineContent;
    private float lineLength;
    //type:0-不是完整行 1-完整行
    private int type = -1;
    private boolean lastIsPunct;
    private ArrayList<Float> arrLenths;

    private int sequence;

    private String chapterName;

    private boolean lastPage;

    private int position;

    public ArrayList<Float> getArrLenths() {
        return arrLenths;
    }

    public void setArrLenths(ArrayList<Float> arrLenths) {
        this.arrLenths = arrLenths;
    }

    public boolean isLastIsPunct() {
        return lastIsPunct;
    }

    public void setLastIsPunct(boolean lastIsPunct) {
        this.lastIsPunct = lastIsPunct;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public NovelLineBean(){}

    public NovelLineBean(String lineContent, float lineLength, int completeLine, boolean lastIsPunct, ArrayList<Float> arrLenths) {
        this.lineContent = lineContent;
        this.lineLength = lineLength;
        this.type = completeLine;
        this.lastIsPunct = lastIsPunct;
        this.arrLenths = arrLenths;
    }

    public String getLineContent() {
        return lineContent;
    }

    public void setLineContent(String lineContent) {
        this.lineContent = lineContent;
    }

    public float getLineLength() {
        return lineLength;
    }

    public void setLineLength(float lineLength) {
        this.lineLength = lineLength;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public boolean isLastPage() {
        return lastPage;
    }

    public void setLastPage(boolean lastPage) {
        this.lastPage = lastPage;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "NovelLineBean{" +
                "lineContent=" + getLineContent() +
                '}';
    }
}
