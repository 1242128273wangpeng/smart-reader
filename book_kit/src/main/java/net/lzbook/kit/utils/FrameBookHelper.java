package net.lzbook.kit.utils;

import android.app.Activity;
import android.content.Context;

public class FrameBookHelper {
    public FrameBookHelper(Context context, Activity activity) {
        CheckNovelUpdHelper.delLocalNotify(context);
        DeleteBookHelper helper = new DeleteBookHelper(context);
        helper.startPendingService();
    }

}