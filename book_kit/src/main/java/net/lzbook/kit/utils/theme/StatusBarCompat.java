package net.lzbook.kit.utils.theme;


import net.lzbook.kit.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import net.lzbook.kit.utils.theme.statusbar.StatusBarFontHelper;


public class StatusBarCompat {
    private static final int INVALID_VAL = -1;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatuColor(Activity activity, int statusColor) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (statusColor != INVALID_VAL) {
                activity.getWindow().setStatusBarColor(statusColor);
            }
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
            View statusBarView = new View(activity);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    getStatusBarHeight(activity));
            statusBarView.setBackgroundColor(statusColor);
            contentView.addView(statusBarView, lp);
        }

    }

    public static void compat(Activity activity) {
        compat(activity, R.color.color_statusBar);
    }

    public static void compat(Activity activity, int resid) {

        int whiteColor = activity.getResources().getColor(R.color.color_white);
        setStatuColor(activity, resid);
        if (resid == whiteColor) {
            if (StatusBarFontHelper.setStatusBarMode(activity, true) == 0) {
                int grayColor = activity.getResources().getColor(R.color.color_gray_070707);
                setStatuColor(activity, grayColor);
            }
        } else {
            StatusBarFontHelper.setStatusBarMode(activity, false);
        }
    }


    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}