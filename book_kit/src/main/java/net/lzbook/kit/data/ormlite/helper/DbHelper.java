package net.lzbook.kit.data.ormlite.helper;

import android.content.Context;

import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.data.ormlite.bean.HistoryInfo;

import java.util.List;


public class DbHelper extends OrmDatabaseHelper {

    private static final String DEF_DB_NAME = ReplaceConstants.getReplaceConstants().DATABASE_NAME;
    private static final int DB_VERSION = 15;


    public DbHelper(Context context) {
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
