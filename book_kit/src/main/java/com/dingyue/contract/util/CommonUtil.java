package com.dingyue.contract.util;

import android.support.annotation.StringRes;
import android.widget.Toast;

import net.lzbook.kit.app.BaseBookApplication;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

/**
 * Desc 公共方法
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/24 20:31
 */
public class CommonUtil {

    private static Toast mToast;

    /***
     * 展示Toast
     * **/
    public static void showToastMessage(@StringRes int id) {
        if (mToast == null) {
            mToast = Toast.makeText(BaseBookApplication.getGlobalContext(), id, Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            mToast.setText(id);
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.show();
        }
    }

    /***
     * 展示Toast
     * **/
    public static void showToastMessage(String message) {
        if (mToast == null) {
            mToast = Toast.makeText(BaseBookApplication.getGlobalContext(), message, Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            mToast.setText(message);
            mToast.setDuration(Toast.LENGTH_SHORT);
            mToast.show();
        }
    }

    /***
     * 延迟展示Toast
     * **/
    public static void showToastMessage(final String message, Long delay) {
        Observable.timer(delay, TimeUnit.MICROSECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        showToastMessage(message);
                    }
                });
    }

    /***
     * 延迟展示Toast
     * **/
    public static void showToastMessage(final @StringRes int id, Long delay) {
        Observable.timer(delay, TimeUnit.MICROSECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        showToastMessage(id);
                    }
                });
    }
}