package net.lzbook.kit.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.input.MultiInputStreamHelper;

import java.io.File;
import java.util.UUID;

public class OpenUDID {

    private static String TAG = OpenUDID.class.getSimpleName();

    private static String _openUdid;
    private static long _time;

    // 用户唯一标志
    public final static String PREF_KEY = "openuuid";
    public final static String TIME = "time";
    public static final String COMMON_PREFS = "common_prefs";


    public static void syncContext(Context mContext) {
        if (_openUdid == null) {
            SharedPreferences mPreferences = mContext.getSharedPreferences(COMMON_PREFS, Context.MODE_PRIVATE);
            String _keyInPref = mPreferences.getString(PREF_KEY, null);
            String filePath = ReplaceConstants.getReplaceConstants().APP_PATH_CACHE + "uuid.text";
            if (_keyInPref == null) {
                File file = new File(filePath);
                if (file != null && file.exists()) {
                    byte[] bytes = FileUtils.readBytes(filePath);
                    _keyInPref = new String(MultiInputStreamHelper.encrypt(bytes));
                }
            }
            long _keyTime = mPreferences.getLong(TIME, 0);

            if (_keyInPref == null || _keyTime == 0) {
                _openUdid = getUniqueId(mContext);
                _time = System.currentTimeMillis();
                Editor e = mPreferences.edit();
                e.putString(PREF_KEY, _openUdid);
                e.putLong(TIME, _time);
                e.apply();
                FileUtils.writeByteFile(filePath, MultiInputStreamHelper.encrypt(_openUdid.getBytes()));
            } else {
                _openUdid = _keyInPref;
                _time = _keyTime;
            }
            AppLog.d(TAG, "_openUdid= " + _openUdid);
        }
    }

    public static String getOpenUDIDInContext(Context context) {
        syncContext(context);
        return _openUdid;
    }

    public static long getTime() {
        return _time;
    }

    private static String getUniqueId(Context context) {
        String uuid = UUID.randomUUID().toString();
        uuid = uuid.replace("-", "");
        return uuid;
    }
}
