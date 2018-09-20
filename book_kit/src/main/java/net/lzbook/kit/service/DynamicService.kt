package net.lzbook.kit.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import net.lzbook.kit.constants.DynConstants
import net.lzbook.kit.utils.dynamic.DynamicParameter
import net.lzbook.kit.utils.dynamic.DynamicUtil
import net.lzbook.kit.utils.dynamic.RxTimerUtil
import net.lzbook.kit.utils.logger.AppLog


/**
 * 动态参数后台服务
 * Created by yuchao on 2018/8/22 0022.
 */
class DynamicService : Service() {

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        AppLog.e("DynamicService : onCreate")

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AppLog.e("DynamicService : onStartCommand")

        RxTimerUtil.cancel()
        RxTimerUtil.interval(DynConstants.REQUEST_TIME, {
            try {
                DynamicParameter(applicationContext).requestCheck()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            AppLog.e("DynamicService ---------- to do check at ${System.currentTimeMillis()}")
        })


        return START_STICKY
    }

    override fun onDestroy() {
        AppLog.e("DynamicService : onDestroy")
        val restartIntent = Intent(this, DynamicService::class.java)
        startService(restartIntent)
        super.onDestroy()
    }

    companion object {
        @JvmStatic
        fun startDynaService(ctt: Context) {
            try {
                if (!DynamicUtil.isDynServiceWork()) {
                    val intent = Intent()
                    intent.setClass(ctt, DynamicService::class.java)
                    ctt.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun keepService(ctt: Context) {
            try {
                if (!DynamicUtil.isDynServiceWork()) {
                    DynamicParameter(ctt).requestCheck()
                    val intent = Intent()
                    intent.setClass(ctt, DynamicService::class.java)
                    ctt.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }


}