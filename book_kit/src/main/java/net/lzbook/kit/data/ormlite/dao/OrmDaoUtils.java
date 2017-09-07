package net.lzbook.kit.data.ormlite.dao;


import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;

import net.lzbook.kit.data.ormlite.helper.OrmDatabaseHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public abstract class OrmDaoUtils<T> {

    private static final int INSERT = 1;
    private static final int UPDATE = 2;
    private static final int DELETE = 3;


    private OrmDatabaseHelper mHelper;
    private Dao mDao;

    public OrmDaoUtils(Class cls) throws SQLException {
        if (mHelper == null) {
            mHelper = getHelper();
        }
        this.mDao = mHelper.getDao(cls);
    }

    protected abstract OrmDatabaseHelper getHelper();

    public boolean isBookExist(String book_id) throws SQLException {
        long count = mDao.queryBuilder().where().eq("book_id", book_id).countOf();
        if (count > 0) {
            return true;
        }
        return false;
    }

    public long countOf() throws SQLException {
        return mDao.queryBuilder().countOf();
    }

    /**
     * 分页加载,按照指定字段降序排序后,取制定区间数据
     *
     * @param columnName 按该字段降序排序
     * @param startRow   区间开始
     * @param maxRow     区间结束
     */
    public List<T> queryDataForPagingLoad(String columnName, Long startRow, Long maxRow) {
        List<T> result = null;
        try {
            result = mDao.queryBuilder().orderBy(columnName, false).offset(startRow).limit(maxRow).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 查询，通过ID
     *
     * @param objId 查询数据的ID
     * @return 返回查询 Bean 数据，如果未查询到则返回 null
     */
    public T queryById(Integer objId) throws SQLException {
        return (T) mDao.queryForId(objId);
    }

    /**
     * 查询指定 Bean 所有数据
     *
     * @return 返回查询 Bean 数据，如果未查询到则返回 null
     */
    public List<T> queryAll() throws SQLException {
        return mDao.queryForAll();
    }

    /**
     * 查询，条件查询
     * <br/> 说明： 该方法要和 {@link OrmDaoUtils#query(QueryBuilder)} 方法组合使用
     * <br/> 在查询操作中，查询的条件最后都要 通过 {@link OrmDaoUtils#query(QueryBuilder)} 方法执行查询
     * <br/> 此方法 对应SQL ：SELECT * FROM `t_person` WHERE `id` = 2
     */
    public QueryBuilder queryBuilder(Map<String, Object> whereMap) throws SQLException {

        QueryBuilder<T, Integer> queryBuilder = mDao.queryBuilder();
        if (whereMap != null && !whereMap.isEmpty()) {
            Where<T, Integer> wheres = queryBuilder.where();
            Set<String> keys = whereMap.keySet();
            ArrayList<String> keyss = new ArrayList<String>();
            keyss.addAll(keys);
            for (int i = 0; i < keyss.size(); i++) {
                if (i == 0) {
                    wheres.eq(keyss.get(i), whereMap.get(keyss.get(i)));
                } else {
                    wheres.and().eq(keyss.get(i), whereMap.get(keyss.get(i)));
                }
            }
        }

        return queryBuilder;
    }

    /**
     * 升序或降序
     *
     * @param orderColumn 升序、降序字段
     * @param isASC       true 升序 ，false 降序
     */
    public QueryBuilder queryOrderBy(String orderColumn, boolean isASC) throws SQLException {
        return queryOrderBy(null, orderColumn, isASC);
    }

    /**
     *
     * @param isASC 参数 false 表示降序，true 表示升序。
     * @return
     * @throws SQLException
     */

    /**
     * 带条件 升序或降序查询
     *
     * @param queryBuilder 查询条件
     * @param orderColumn  升序、降序字段
     * @param isASC        true 升序 ，false 降序
     */
    public QueryBuilder queryOrderBy(QueryBuilder queryBuilder, String orderColumn, boolean isASC) throws SQLException {

        if (queryBuilder == null) {
            queryBuilder = mDao.queryBuilder();
        }

        if (orderColumn != null) {
            queryBuilder.orderBy(orderColumn, isASC);
        }
        return queryBuilder;
    }

    public List<T> query(QueryBuilder builder) throws SQLException {
        PreparedQuery<T> preparedQuery = builder.prepare();
        return mDao.query(preparedQuery);
    }


    /**
     * 新增数据
     */
    public int insert(T obj) throws SQLException {
        return mDao.create(obj);
    }

    /**
     * 批量添加数据
     */
    public int insert(Collection<T> objs) throws SQLException {
        return mDao.create(objs);
    }

    /**
     * 使用事务批量添加数据
     */
    public boolean insertInTransaction(final List<T> objs) throws SQLException {
        return baseTransaction(INSERT, objs);
    }


    /**
     * 删除数据
     */
    public void delete(T obj) throws SQLException {
        mDao.delete(obj);
    }

    /**
     * 删除指定表中,指定字段最小值对应的一条记录
     */
    public int deleteMiniData(String tableName, String columnName) throws SQLException {
        DeleteBuilder deleteBuilder = mDao.deleteBuilder();
        deleteBuilder.where().eq(columnName, mDao.queryRawValue("select min(" + columnName + ") from " + tableName));
        return deleteBuilder.delete();
    }

    /**
     * 批量删除数据
     */
    public int delete(Collection<T> objs) throws SQLException {
        return mDao.delete(objs);
    }

    /**
     * 批量删除数据
     */
    public int deleteAll() {
        try {
            return mDao.deleteBuilder().delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 通过ID 删除数据
     */
    public void deleteById(Integer objId) throws SQLException {
        mDao.deleteById(objId);
    }

    /**
     * 使用事务批量删除事务（实体）
     */
    public boolean deleteInTransaction(final List<T> objs) throws SQLException {
        return baseTransaction(DELETE, objs);
    }

    /**
     * 使用事务批量删除事务（ID）
     */
    public boolean deleteInTransactionById(final List<Integer> objIds) throws SQLException {
        Boolean b = TransactionManager.callInTransaction(mHelper.getConnectionSource(), new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    for (Integer objId : objIds) {
                        mDao.deleteById(objId);
                    }
                } catch (SQLException e) {
                    return false;
                }
                return true;
            }
        });
        return b;
    }


    /**
     * 修改数据
     */
    public int update(T obj) throws SQLException {
        return mDao.update(obj);
    }

    /**
     * 条件修改数据
     *
     * @param whereMap    修改条件
     * @param columnName  修改字段名
     * @param columnValue 修改内容
     */
    public int update(Map<String, Object> whereMap, String columnName, Object columnValue) throws SQLException {
        Map<String, Object> updateValue = new HashMap<String, Object>();
        updateValue.put(columnName, columnValue);
        return this.update(whereMap, updateValue);

    }


    /**
     * 条件修改数据
     */
    public int update(Map<String, Object> whereMap, Map<String, Object> updateValue) throws SQLException {
        UpdateBuilder updateBuilder = mDao.updateBuilder();
        if (updateValue != null && !updateValue.isEmpty()) {
            Set<String> valueSet = updateValue.keySet();
            for (String valueKey : valueSet) {
                updateBuilder.updateColumnValue(valueKey, updateValue.get(valueKey));
            }
        } else {
            return -1;
        }

        if (whereMap != null && !whereMap.isEmpty()) {
            Where<T, Long> where = updateBuilder.where();
            Set<String> whereKeySet = whereMap.keySet();
            List<String> keys = new ArrayList<String>();
            keys.addAll(whereKeySet);
            for (int i = 0; i < keys.size(); i++) {
                if (i == 0) {
                    where.eq(keys.get(i), whereMap.get(keys.get(i)));
                } else {
                    where.and().eq(keys.get(i), whereMap.get(keys.get(i)));
                }
            }
        }
        return mDao.update(updateBuilder.prepare());
    }

    /**
     * 创建或修改数据
     *
     * <br/> 如果没有数据就添加，如果数据存在就修改
     */
    public Dao.CreateOrUpdateStatus createOrUpdate(T obj) throws SQLException {
        return mDao.createOrUpdate(obj);
    }

    /**
     * 批量创建或修改数据
     *
     * <br/> 如果没有数据就添加，如果数据存在就修改
     */
    public Dao.CreateOrUpdateStatus createOrUpdate(Collection<T> objs) throws SQLException {
        return mDao.createOrUpdate(objs);
    }

    /**
     * 在事务中批量修改数据
     */
    public boolean updateInTransaction(final List<T> objs) throws SQLException {
        return baseTransaction(UPDATE, objs);
    }

    private boolean baseTransaction(final int type, final List<T> objs) throws SQLException {
        Boolean b = TransactionManager.callInTransaction(mHelper.getConnectionSource(), new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {

                    switch (type) {
                        case INSERT:
                        case UPDATE:
                            for (T obj : objs) {
                                mDao.createOrUpdate(obj);
                            }
                            break;

                        case DELETE:
                            for (T obj : objs) {
                                mDao.delete(obj);
                            }
                            break;
                    }
                } catch (SQLException e) {
                    return false;
                }
                return true;
            }
        });
        return b;
    }
}
