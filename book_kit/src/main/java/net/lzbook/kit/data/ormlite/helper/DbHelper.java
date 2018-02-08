package net.lzbook.kit.data.ormlite.helper;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.data.ormlite.bean.HistoryInfo;

import android.content.Context;

import java.util.List;


public class DbHelper extends OrmDatabaseHelper {

    private static final String DEF_DB_NAME = ReplaceConstants.getReplaceConstants().DATABASE_NAME;
    private static final int DB_VERSION = 16;

    private volatile static DbHelper mInstance = null;

    public static DbHelper getInstance() {
        if (mInstance == null || !mInstance.isOpen()) {
            mInstance = null;
            synchronized (DbHelper.class) {
                if (mInstance == null) {
                    mInstance = new DbHelper(BaseBookApplication.getGlobalContext());
                }
            }
        }

        return mInstance;
    }

    private DbHelper(Context context) {
        super(context, DEF_DB_NAME, null, DB_VERSION);
    }

    @Override
    public void createTables(List tables) {
        tables.add(HistoryInfo.class);
    }

    @Override
    public void updateTables(List tables) {
        tables.add(HistoryInfo.class);
    }
}
