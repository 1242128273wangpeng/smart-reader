package net.lzbook.kit.dynamic.utils

import android.app.ActivityManager
import android.content.Context
import net.lzbook.kit.dynamic.service.DynamicService
import net.lzbook.kit.app.BaseBookApplication


/**
 * Created by yuchao on 2018/8/22 0022.
 */
object DynamicUtil {


    fun isDynServiceWork(): Boolean{
        return isServiceWork(BaseBookApplication.getGlobalContext(), DynamicService::class.java.name)
    }

    /**
     * 判断某个服务是否正在运行
     *
     * @param ctt 上下文
     * @param serviceName 是包名+服务的类名
     * @return true代表正在运行，false代表服务没有正在运行
     */
    fun isServiceWork(ctt: Context, serviceName: String): Boolean {
        try {
            val myAM = ctt.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val myList = myAM.getRunningServices(400)
            if (myList.size <= 0) {
                return false
            }
            for (i in myList.indices) {
                val mName = myList[i].service.className.toString()
                if (mName == serviceName) {
                    return true
                }
            }
        } catch (e: Exception) {
        }
        return false
    }
}