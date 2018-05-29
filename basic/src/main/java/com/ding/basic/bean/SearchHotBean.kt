package com.ding.basic.bean

import java.io.Serializable

class SearchHotBean : Serializable {


    /**
     * suc : 200
     * errCode : null
     * data : [{"wordType":0,"word":"异界大陆","sort":0},{"wordType":0,"word":"冰山","sort":1}]
     */

    var suc: String? = null
    var errCode: Any? = null
    var data: List<DataBean>? = null

    class DataBean(
            /**
             * wordType : 0
             * word : 异界大陆
             * sort : 0
             */


            var wordType: Int, var word: String?, var sort: Int) {

        override fun toString(): String {
            return "DataBean{" +
                    "wordType=" + wordType +
                    ", word='" + word + '\''.toString() +
                    ", sort=" + sort +
                    '}'.toString()
        }
    }

    override fun toString(): String {
        return "SearchHotBean{" +
                "suc='" + suc + '\''.toString() +
                ", errCode=" + errCode +
                ", data=" + data +
                '}'.toString()
    }
}
