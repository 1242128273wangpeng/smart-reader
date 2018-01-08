package com.intelligent.reader.util;

import android.content.Context;
import android.content.res.Resources;
import android.view.WindowManager;

/**
 * Desc px sp dp单位转换
 * Author lijun Lee
 * Mail jun_li@dingyuegroup.cn
 * Data 2017/9/14 18:12
 */

public class DisplayUtils {
    /**
     * convert px to its equivalent dp
     * <p>
     * 将px转换为与之相等的dp
     */
    public static int px2dp(Resources resources, float pxValue) {
        final float scale = resources.getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    /**
     * convert dp to its equivalent px
     * <p>
     * 将dp转换为与之相等的px
     */
    public static int dp2px(Resources resources, float dipValue) {
        final float scale = resources.getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }


    /**
     * convert px to its equivalent sp
     * <p>
     * 将px转换为sp
     */
    public static int px2sp(Resources resources, float pxValue) {
        final float fontScale = resources.getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }


    /**
     * convert sp to its equivalent px
     * <p>
     * 将sp转换为px
     */
    public static int sp2px(Resources resources, float spValue) {
        final float fontScale = resources.getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 获取屏幕高
     *
     * @return
     */
    @SuppressWarnings("deprecation")
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int height = wm.getDefaultDisplay().getHeight();
        return height;
    }
    /**
     * 获取屏幕宽
     *
     * @return
     */
    @SuppressWarnings("deprecation")
    public static int getScreenWight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        return width;
    }
}
