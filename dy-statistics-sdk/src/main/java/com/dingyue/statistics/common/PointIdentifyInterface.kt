package com.dingyue.statistics.common

/**
 * Desc 点位标识接口
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/8/29 14:55
 */
interface PointIdentifyInterface {

    /**
     * 获取页面标识
     */
    fun getPageCode(): String

    /**
     * 获取点位标识
     */
    fun getIdentificationCode(): String
}