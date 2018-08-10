package net.lzbook.kit.utils;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.input.MultiInputStreamHelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.io.File;
import java.util.UUID;

public class OpenUDID {

    // 用户唯一标志
    public final static String PREF_KEY = "openuuid";
    public static final String COMMON_PREFS = "common_prefs";
    public static final String FILE_NAME = "uuid.text";
    private static String TAG = OpenUDID.class.getSimpleName();
    private static String _openUdid;

    public static void syncContext(Context mContext) {
        if (_openUdid == null) {
            SharedPreferences mPreferences = mContext.getSharedPreferences(COMMON_PREFS, Context.MODE_PRIVATE);
            String _keyInPref = mPreferences.getString(PREF_KEY, null);
            String filePath = ReplaceConstants.getReplaceConstants().APP_PATH_CACHE + FILE_NAME;
            if (_keyInPref == null) {
                File file = new File(filePath);
                if (file != null && file.exists()) {
                    byte[] bytes = FileUtils.readBytes(filePath);
                    _keyInPref = new String(MultiInputStreamHelper.encrypt(bytes));
                    //应用内存被清理后, 需要恢复id
                    mPreferences.edit().putString(PREF_KEY, _keyInPref).apply();
                }
            }

            if (_keyInPref == null) {
                _openUdid = getUniqueId(mContext);
                Editor e = mPreferences.edit();
                e.putString(PREF_KEY, _openUdid);
                e.apply();

                FileUtils.writeByteFile(filePath, MultiInputStreamHelper.encrypt(_openUdid.getBytes()));
            } else {
                _openUdid = _keyInPref;

            }

            if (!new File(filePath).exists()) {
                FileUtils.writeByteFile(filePath, MultiInputStreamHelper.encrypt(_openUdid.getBytes()));
            }
            AppLog.d(TAG, "_openUdid= " + _openUdid);
        }
    }

    public static String getOpenUDIDInContext(Context context) {
        syncContext(context);
        return _openUdid;
    }

    private static String getUniqueId(Context context) {
        String uuid = UUID.randomUUID().toString();
        uuid = uuid.replace("-", "");
        return uuid;
    }

    public static void saveUDIDToSD(Context context){
        SharedPreferences mPreferences = context.getSharedPreferences(COMMON_PREFS, Context.MODE_PRIVATE);
        String _keyInPref = mPreferences.getString(PREF_KEY, null);
        String filePath = ReplaceConstants.getReplaceConstants().APP_PATH_CACHE + FILE_NAME;

        if (_keyInPref == null) {
            _keyInPref = getUniqueId(context);
            Editor e = mPreferences.edit();
            e.putString(PREF_KEY, _openUdid);
            e.apply();
        }

        if (!new File(filePath).exists() && _keyInPref != null) {
            FileUtils.writeByteFile(filePath, MultiInputStreamHelper.encrypt(_openUdid.getBytes()));
            AppLog.d("OpenUDID", "UDID = {" +_openUdid+ "}");
        }
    }
}
