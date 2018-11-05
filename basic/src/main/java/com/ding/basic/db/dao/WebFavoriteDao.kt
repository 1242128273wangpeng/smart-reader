package com.ding.basic.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.ding.basic.bean.WebPageFavorite

/**
 * Desc 网页收藏dao
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/10/30 15:12
 */
@Dao
interface WebFavoriteDao : BaseDao<WebPageFavorite> {

    @Query("SELECT * FROM web_page_favorite ORDER BY create_time DESC")
    fun getAll(): List<WebPageFavorite>?

    @Query("SELECT * FROM web_page_favorite WHERE id = :id")
    fun getById(id: Int): WebPageFavorite?

    @Query("DELETE FROM web_page_favorite WHERE id= :id")
    fun deleteById(id: Int): Int

    @Query("DELETE FROM web_page_favorite")
    fun deleteAll(): Int

    @Query("SELECT COUNT(*) FROM web_page_favorite")
    fun getCount(): Int

    @Query("SELECT * FROM web_page_favorite WHERE title = :title AND web_link = :web_link")
    fun getByTitleAndLink(title: String,web_link: String): List<WebPageFavorite>?

}