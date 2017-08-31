package net.lzbook.kit.utils;

import android.widget.Toast;

import net.lzbook.kit.app.BaseBookApplication;

public class ToastUtils {
    private static Toast mToast;

    public static void showToastNoRepeat(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(BaseBookApplication.getGlobalContext(), text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }
}