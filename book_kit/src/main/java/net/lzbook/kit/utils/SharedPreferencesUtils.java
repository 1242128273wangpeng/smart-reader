package net.lzbook.kit.utils;

import android.content.SharedPreferences;

public class SharedPreferencesUtils {
    private SharedPreferences sp;

    public SharedPreferencesUtils(SharedPreferences sp) {
        this.sp = sp;
    }

    public void putBoolean(String key, boolean value) {
        sp.edit().putBoolean(key, value).apply();
    }

    public void putInt(String key, int value) {
        sp.edit().putInt(key, value).apply();
    }

    public void putString(String key, String value) {
        sp.edit().putString(key, value).apply();
    }

    public boolean getBoolean(String key) {
        return sp.getBoolean(key, false);
    }

    public int getInt(String key) {
        return sp.getInt(key, 0);
    }

    public String getString(String key) {
        return sp.getString(key, "");
    }
}

