package net.lzbook.kit.repair_books.bean;

import java.util.ArrayList;

/**
 * Created by yuchao on 2017/11/2 0002.
 */

public class FixContentState {

    private ArrayList<Boolean> chapterFixStates;

    public FixContentState() {
        chapterFixStates = new ArrayList<>();
    }

    public void addState(boolean b) {
        if (chapterFixStates != null) {
            chapterFixStates.add(b);
        }
    }

    public boolean getFixState() {
        if (chapterFixStates != null) {
            if (chapterFixStates.isEmpty()) {
                return false;
            }
            for (boolean item : chapterFixStates) {
                if (!item) {
                    return item;
                }
            }
            return true;
        }
        return false;
    }
}
