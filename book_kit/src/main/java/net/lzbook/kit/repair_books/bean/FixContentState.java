package net.lzbook.kit.repair_books.bean;

import java.util.ArrayList;

/**
 * Created by yuchao on 2017/11/2 0002.
 */

public class FixContentState {

    private ArrayList<Boolean> chapterMsgFixStates;
    private ArrayList<Boolean> chapterContFixStates;

    public FixContentState() {
        chapterMsgFixStates = new ArrayList<>();
        chapterContFixStates = new ArrayList<>();
    }

    public void addMsgState(boolean b) {
        if (chapterMsgFixStates != null) {
            chapterMsgFixStates.add(b);
        }
    }

    public void addContState(boolean b) {
        if (chapterContFixStates != null) {
            chapterContFixStates.add(b);
        }
    }

    /**
     * 判断章节目录修复完成, 且章节内容修复完成
     */
    public boolean getFixState() {
        if (chapterMsgFixStates != null && chapterContFixStates != null) {
            if (chapterMsgFixStates.isEmpty()) {
                return false;
            }
            for (boolean item : chapterMsgFixStates) {
                if (!item) {
                    return item;
                }
            }
            if (chapterContFixStates.isEmpty()) {
                return true;
            }
            for (boolean item : chapterContFixStates) {
                if (!item) {
                    return item;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 判断是否修复成功过章节内容
     */
    public boolean getSaveFixState() {
        if (chapterContFixStates != null) {
            if (chapterContFixStates.isEmpty()) {
                return false;
            }
            for (boolean item : chapterContFixStates) {
                if (!item) {
                    return item;
                }
            }
            return true;
        }
        return false;
    }
}
