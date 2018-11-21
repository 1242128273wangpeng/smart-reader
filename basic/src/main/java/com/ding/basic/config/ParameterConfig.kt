package com.ding.basic.config

import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils

/**
 * Desc 参数配置类，存放应用运行相关的参数，添加参数注意添加注释
 * Author crazylei
 * Mail crazylei911228@gmail.com
 * Date 2018/10/19 15:23
 */

object ParameterConfig {

    /***
     * 高德地图获取的地理位置信息
     * **/
    //城市信息
    var city = ""


    //地区编号
    var areaCode = ""


    //城市编号
    var cityCode = ""
        get() {
            return if (field.isNotEmpty()) {
                field
            } else {
                val value = SPUtils.loadSharedString(SPKey.LOCATION_CITY_CODE)
                field = if (value.isNotEmpty()) {
                    value
                } else {
                    ""
                }
                field
            }
        }
        set(value) {
            if (value.isNotEmpty()) {
                field = value
                SPUtils.insertSharedString(SPKey.LOCATION_CITY_CODE, value)
            }
        }


    //纬度信息
    var latitude = ""
        get() {
            return if (field.isNotEmpty()) {
                field
            } else {
                val value = SPUtils.loadSharedString(SPKey.LOCATION_LATITUDE)
                field = if (value.isNotEmpty()) {
                    value
                } else {
                    "0.0"
                }
                field
            }
        }
        set(value) {
            if (value.isNotEmpty()) {
                field = value
                SPUtils.insertSharedString(SPKey.LOCATION_LATITUDE, value)
            }
        }


    //经度信息
    var longitude = ""
        get() {
            return if (field.isNotEmpty()) {
                field
            } else {
                val value = SPUtils.loadSharedString(SPKey.LOCATION_LONGITUDE)
                field = if (value.isNotEmpty()) {
                    value
                } else {
                    "0.0"
                }
                field
            }
        }
        set(value) {
            if (value.isNotEmpty()) {
                field = value
                SPUtils.insertSharedString(SPKey.LOCATION_LONGITUDE, value)
            }
        }


    //详细地址
    var locationDetail = ""









    /***
     * 用户开屏选择男女频的数据
     * **/
    //男频标识
    var GENDER_BOY = 0x81

    //女频标识
    var GENDER_GIRL = 0x82

    //没有选男女的壳默认不传sex字段
    var GENDER_NONE = 0x83

    //默认标识（跳过选项）
    var GENDER_DEFAULT = 0x80

    //男女频数据
    var GENDER_TYPE = GENDER_NONE
}