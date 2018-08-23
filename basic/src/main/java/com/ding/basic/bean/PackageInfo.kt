package com.ding.basic.bean

import java.io.Serializable

/**
 * Created by Xian on 2017/12/19.
 */
class PackageInfo(val startIndex: Int, val endIndex: Int): Serializable {

    companion object {
        fun parse(path: String): PackageInfo? {
            //59141354953be80d2efcdbf9-59141354953be80d2efcdbfa-1-100-2-1513177435906
            try {
                val split = path.split("-")
                return PackageInfo(split[2].toInt(), split[3].toInt())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }
}