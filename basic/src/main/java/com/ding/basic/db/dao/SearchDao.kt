package com.ding.basic.db.dao

import android.arch.persistence.room.*
import com.ding.basic.bean.Chapter
import com.ding.basic.bean.SearchRecommendBook
import io.reactivex.Flowable

/**
 * Created by yuchao on 2018/3/16 0016.
 */
@Dao
interface SearchDao : BaseDao<SearchRecommendBook.DataBean> {

    /************************** 增 ****************************/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSearch(dataBean: SearchRecommendBook.DataBean): Long

    /************************** 删 ****************************/

    @Query("DELETE FROM search_recommend")
    fun deleteAllSearchs()

    /************************** 改 ****************************/


    /************************** 查 ****************************/

    @Query("SELECT * FROM search_recommend")
    fun querySearchs(): List<SearchRecommendBook.DataBean>

}