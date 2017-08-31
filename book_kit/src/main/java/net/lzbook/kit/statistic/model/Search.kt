package net.lzbook.kit.statistic.model

import com.alibaba.fastjson.annotation.JSONField
import net.lzbook.kit.utils.NotProguard

/**
 * Created by xian on 2017/7/3.
 */
@NotProguard
class Search :IAliLogModel(){
    /*CANCEL取消  RETURN返回   COVER进入封面  BOOKSHELF加入书架*/
    enum class OP {
        CANCEL, RETURN, COVER, BOOKSHELF
    }

    override val key = "ZN_APP_ANROID_SEARCH"
    @JSONField(serialize=false, deserialize = false)
    override val project = "datastatistics"
    @JSONField(serialize=false, deserialize = false)
    override val logStore = "statistics_search"



    /*	青果数据,book_id book_source_id 相同*/
    var book_id: String? = null
    /*书源*/
    var book_source_id: String? = null
    /*用来区分是否是青果的*/
    var book_code: String? = null
    /*相关操作*/
    var op: String? = null
    /*序号*/
    var s_order: Long = 0
    /*搜索消耗时间*/
    var cost_time: Long = 0
    /*搜索关键字*/
    var keyword: String? = null

    override fun toString(): String {
        return "Search(key='$key', project='$project', logStore='$logStore', device_id=$device_id, udid=$udid, uid=$uid, app_package='$app_package', app_version=$app_version, app_version_code=$app_version_code, app_channel_id=$app_channel_id, book_id=$book_id, book_source_id=$book_source_id, book_code=$book_code, op=$op, s_order=$s_order, cost_time=$cost_time, timestamp=$timestamp, keyword=$keyword)"
    }



}