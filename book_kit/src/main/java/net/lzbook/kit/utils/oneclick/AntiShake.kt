package net.lzbook.kit.utils.oneclick

/**
 * Created by yuchao on 2017/12/25 0025.
 */
class AntiShake {
    private val utils = ArrayList<OneClickUtil>()

    fun check(o: Any?): Boolean {
        var flag: String? = null
        if (o == null)
            flag = Thread.currentThread().stackTrace[2].methodName
        else
            flag = o.toString()
        for (util in utils) {
            if (util.methodName == flag) {
                return util.check()
            }
        }
        val clickUtil = OneClickUtil(flag!!)
        utils.add(clickUtil)
        return clickUtil.check()
    }

    fun check(): Boolean {
        return check(null)
    }
}