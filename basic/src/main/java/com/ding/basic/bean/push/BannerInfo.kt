package com.ding.basic.bean.push

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable

/**
 * Desc 弹窗活动信息
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/8/30 15:00
 */
data class BannerInfo(
        var tags: ArrayList<String>? = null, // 活动标签
        var url: String? = null, // 活动弹窗图片地址
        var updateMillSecs: Long = 0, //上次更新时间
        var isFromCache: Boolean = false
) : Serializable