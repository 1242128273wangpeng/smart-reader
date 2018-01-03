package net.lzbook.kit.data.bean;

import android.view.ViewGroup;

import net.lzbook.kit.constants.ReadConstants;

import android.graphics.Rect;
import android.text.TextUtils;

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
    private ArrayList<Float> mArrLenths = new ArrayList<>();

    private int sequence;

    private String chapterName;

    private boolean lastPage;

    private int position;

    private float indexY;

    private ViewGroup adView;

    public ArrayList<Float> getArrLenths() {
        return mArrLenths;
    }

    public void setArrLenths(ArrayList<Float> arrLenths) {
        this.mArrLenths = arrLenths;
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

    public NovelLineBean() {
    }

    public NovelLineBean(String lineContent, float lineLength, int completeLine, boolean lastIsPunct, ArrayList<Float> arrLenths) {
        this.lineContent = lineContent;
        this.lineLength = lineLength;
        this.type = completeLine;
        this.lastIsPunct = lastIsPunct;
        initDrawIndex(arrLenths);
    }

    private void initDrawIndex(ArrayList<Float> arrLenths) {
        if (arrLenths == null || arrLenths.isEmpty() || TextUtils.isEmpty(lineContent)){
            return;
        }
        int length = lineContent.length();
        int charNum;
        if (lastIsPunct) {
            charNum = length - 2;
        } else {
            charNum = length - 1;
        }
        float marg = (ReadConfig.INSTANCE.getMWidth() - lineLength) / charNum;
        float star;
        if (!mArrLenths.isEmpty()){
            mArrLenths.clear();
        }
        for (int i = 0; i < length; i++) {
            star = ReadConfig.INSTANCE.getMLineStart() + arrLenths.get(i) + marg * i;
            char c = lineContent.charAt(i);
            if (i == length - 1 && isEndPunct(c)) {
                Rect rect = new Rect();
                ReadConfig.INSTANCE.getMPaint().getTextBounds(String.valueOf(c), 0, 1, rect);
                star -= marg;
                star -= rect.left;
                star += (ReadConstants.chineseWth / 2 - rect.width()) / 2;
            }
            mArrLenths.add(star);
        }
    }

    private boolean isEndPunct(char ch) {
        boolean isInclude = false;
        for (char c : ReadConstants.endPuncts) {
            if (ch == c) {
                isInclude = true;
                break;
            }
        }
        return isInclude;
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

    public ViewGroup getAdView() {
        return adView;
    }

    public void setAdView(ViewGroup adView) {
        this.adView = adView;
    }

    @Override
    public String toString() {
        return "NovelLineBean{" +
                "lineContent=" + getLineContent() +
                '}';
    }

    public float getIndexY() {
        return indexY;
    }

    public void setIndexY(float indexY) {
        this.indexY = indexY;
    }
}
