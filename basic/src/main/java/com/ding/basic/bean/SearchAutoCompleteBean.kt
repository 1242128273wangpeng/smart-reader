package com.ding.basic.bean

import java.io.Serializable

class SearchAutoCompleteBean : Serializable {


    /**
     * suc : 200
     * errCode : null
     * data : {"label":[{"suggest":"大主宰","wordtype":"label","pv":"9235"}],"name":[{"suggest":"大主宰","wordtype":"name","pv":"1372678"},{"suggest":"大主宰外传","wordtype":"name","pv":"517"},{"suggest":"混沌大主宰","wordtype":"name","pv":"479"},{"suggest":"斗破苍穹之大主宰","wordtype":"name","pv":"330"},{"suggest":"大主宰天蚕土豆","wordtype":"name","pv":"301"},{"suggest":"神通大主宰","wordtype":"name","pv":"297"},{"suggest":"诸天大主宰","wordtype":"name","pv":"233"},{"suggest":"大主宰_沦陷的书生","wordtype":"name","pv":"212"},{"suggest":"隋唐大主宰","wordtype":"name","pv":"211"},{"suggest":"大主宰之萧玄传奇","wordtype":"name","pv":"165"},{"suggest":"武动乾坤之大主宰","wordtype":"name","pv":"111"},{"suggest":"斗破大主宰","wordtype":"name","pv":"110"},{"suggest":"NPC大主宰","wordtype":"name","pv":"82"},{"suggest":"傲世大主宰","wordtype":"name","pv":"77"},{"suggest":"召唤大主宰","wordtype":"name","pv":"50"}],"authors":[{"suggest":"大主宰","wordtype":"author","pv":"2"},{"suggest":"幕后大主宰","wordtype":"author","pv":"1"}]}
     */

    var suc: String? = null
    var errCode: Any? = null
    var data: DataBean? = null

    class DataBean {
        var label: List<LabelBean>? = null
        var name: List<NameBean>? = null
        var authors: List<AuthorsBean>? = null

        class LabelBean {
            /**
             * suggest : 大主宰
             * wordtype : label
             * pv : 9235
             */

            var suggest: String? = null
            var wordtype: String? = null
            var pv: String? = null

            override fun toString(): String {
                return "LabelBean{" +
                        "suggest='" + suggest + '\''.toString() +
                        ", wordtype='" + wordtype + '\''.toString() +
                        ", pv='" + pv + '\''.toString() +
                        '}'.toString()
            }
        }

        class NameBean {
            /**
             * suggest : 大主宰
             * wordtype : name
             * pv : 1372678
             */

            var suggest: String? = null
            var wordtype: String? = null
            var pv: String? = null

            override fun toString(): String {
                return "NameBean{" +
                        "suggest='" + suggest + '\''.toString() +
                        ", wordtype='" + wordtype + '\''.toString() +
                        ", pv='" + pv + '\''.toString() +
                        '}'.toString()
            }
        }

        class AuthorsBean {
            /**
             * suggest : 大主宰
             * wordtype : author
             * pv : 2
             */

            var suggest: String? = null
            var wordtype: String? = null
            var pv: String? = null

            override fun toString(): String {
                return "AuthorsBean{" +
                        "suggest='" + suggest + '\''.toString() +
                        ", wordtype='" + wordtype + '\''.toString() +
                        ", pv='" + pv + '\''.toString() +
                        '}'.toString()
            }
        }
    }

    override fun toString(): String {
        return "SearchAutoCompleteBean{" +
                "suc='" + suc + '\''.toString() +
                ", errCode=" + errCode +
                ", data=" + data +
                '}'.toString()
    }
}
