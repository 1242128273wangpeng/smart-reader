package net.lzbook.kit.widget;

import net.lzbook.kit.R;

import android.app.Activity;
import android.app.Dialog;
import android.view.Gravity;
import android.view.InflateException;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;


public class MyDialog extends Dialog {
    private boolean isShow = false;

    public MyDialog(Activity activity, int layout) {
        super(activity, R.style.update_dialog);
        try {
            setContentView(layout);
        } catch (InflateException e) {
            e.printStackTrace();
        }
        setCanceledOnTouchOutside(false);
    }

    public MyDialog(Activity activity, int layout, int gravity) {
        this(activity, layout, gravity, false);
    }

    public MyDialog(Activity activity, int layout, int gravity, boolean isShow) {
        super(activity, R.style.update_dialog);
        try {
            setContentView(layout);
        } catch (InflateException e) {
            e.printStackTrace();
        }
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = gravity;
        window.setAttributes(params);
        if (isShow) {
            setCanceledOnTouchOutside(true);
            this.isShow = isShow;
        } else {
            setCanceledOnTouchOutside(false);
            this.isShow = isShow;
        }
    }

    public MyDialog(Activity activity, int width, int height, int layout, int style) {
        super(activity, style);
        try {
            setContentView(layout);
        } catch (InflateException e) {
            e.printStackTrace();
        }
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = width;
        params.height = height;
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
        setCanceledOnTouchOutside(true);
    }

    public MyDialog(Activity activity, View layout, int style, boolean CanceledOnTouchOutside) {
        super(activity, style);
        try {
            setContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } catch (InflateException e) {
            e.printStackTrace();
        }
        setCancelable(true);// 可以用“返回键”取消
        setCanceledOnTouchOutside(CanceledOnTouchOutside);
    }

    public MyDialog(Activity activity, View layout, int style) {
        this(activity, layout, style, true);
    }

    @Override
    public void onBackPressed() {
        if (isShow) {
        } else {
            super.onBackPressed();
        }
    }
}