package com.ding.basic.util;

import com.ding.basic.Config;

import android.content.res.Resources;
import android.util.Log;

/**
 * 动态获取资源id
 */
public class ResourceUtil {

    public static String getStringById(int id) {
        if (Config.INSTANCE.getContext() == null){
            Log.e("basic 异常!!!", " 请检查basic model是否被初始化!!!");
            return "";
        }
        Resources res = Config.INSTANCE.getContext().getResources();
        return res.getString(id);
    }
}
