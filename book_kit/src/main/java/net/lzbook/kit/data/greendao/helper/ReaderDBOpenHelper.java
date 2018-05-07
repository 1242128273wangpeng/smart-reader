package net.lzbook.kit.data.greendao.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import net.lzbook.kit.data.greendao.dao.DaoMaster;

import org.greenrobot.greendao.database.Database;

/**
 * Desc ServerLogDao操作Helper类
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/1/5 0005 14:58
 */

public class ReaderDBOpenHelper extends DaoMaster.DevOpenHelper {
    public ReaderDBOpenHelper(Context context, String name) {
        super(context, name);
    }

    public ReaderDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
