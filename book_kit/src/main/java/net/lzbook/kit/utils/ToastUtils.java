package net.lzbook.kit.utils;

import net.lzbook.kit.app.BaseBookApplication;

import android.widget.Toast;

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