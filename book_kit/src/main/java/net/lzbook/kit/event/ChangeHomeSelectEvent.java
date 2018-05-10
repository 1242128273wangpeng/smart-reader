package net.lzbook.kit.event;

/**
 * Created by Administrator on 2017\8\22 0022.
 */

public class ChangeHomeSelectEvent {
    private boolean isSelectAll;

    public ChangeHomeSelectEvent(boolean isSelectAll) {
        this.isSelectAll = isSelectAll;
    }

    public boolean isSelectAll() {
        return isSelectAll;
    }


}
