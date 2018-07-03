package net.lzbook.kit.utils.oneclick

/**
 * Created by yuchao on 2017/12/25 0025.
 */
class AntiShake {
    private val utils = ArrayList<OneClickUtil>()

    fun check(o: Any?): Boolean {
        val flag: String? = o?.toString() ?: Thread.currentThread().stackTrace[2].methodName

        utils.filter { it.methodName == flag }
                .forEach { return it.check() }
        val clickUtil = OneClickUtil(flag!!)
        utils.add(clickUtil)
        return clickUtil.check()
    }

    fun check(): Boolean {
        return check(null)
    }
}