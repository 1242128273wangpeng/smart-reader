package com.ding.basic.bean

import java.io.Serializable

/**
 * Desc 弹窗活动信息
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/8/30 15:00
 */
data class BannerInfo(
        var tags: ArrayList<String>? = null, // 活动标签
        var url: String? = null // 活动弹窗图片地址
) : Serializable