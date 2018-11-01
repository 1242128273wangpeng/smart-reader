package com.ding.basic.config

/**
 * Desc 参数配置类，存放应用运行相关的参数，添加参数注意添加注释
 * Author crazylei
 * Mail crazylei911228@gmail.com
 * Date 2018/10/19 15:23
 */

object ParameterConfig {

    /***
     * 用户开屏选择男女频的数据
     * **/
    //男频标识
    var GENDER_BOY = 0x81
    //女频标识
    var GENDER_GIRL = 0x82
    //默认标识（跳过选项）
    var GENDER_DEFAULT = 0x80
    //男女频数据
    var GENDER_TYPE = GENDER_DEFAULT
}