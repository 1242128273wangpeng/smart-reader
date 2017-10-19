package net.lzbook.kit.data.ormlite.dao;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.data.ormlite.helper.DbHelper;
import net.lzbook.kit.data.ormlite.helper.OrmDatabaseHelper;

import java.sql.SQLException;

public class DaoUtils extends OrmDaoUtils {
    public DaoUtils(Class cls) throws SQLException {
        super(cls);
    }

    @Override
    protected OrmDatabaseHelper getHelper() {
        return DbHelper.getInstance();
    }
}
