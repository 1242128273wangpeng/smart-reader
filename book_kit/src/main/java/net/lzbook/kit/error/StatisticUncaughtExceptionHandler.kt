package net.lzbook.kit.error

import android.util.Log
import com.alibaba.fastjson.JSONObject
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.OpenUDID
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class StatisticUncaughtExceptionHandler(val parent: Thread.UncaughtExceptionHandler) : Thread.UncaughtExceptionHandler {

    val lock = java.lang.Object()

    override fun uncaughtException(thread: Thread, exception: Throwable) {

        //防止死循环
        Thread.setDefaultUncaughtExceptionHandler(parent)


        val extJson = JSONObject()
        val stackTrace = StringWriter()
        exception.printStackTrace(PrintWriter(stackTrace))

        val mainJson = JSONObject()

        mainJson.put("pkg", AppUtils.getPackageName())
        mainJson.put("version", AppUtils.getVersionName())
        mainJson.put("cause", exception.toString())
        mainJson.put("type", exception.javaClass.name)
        mainJson.put("udid", OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()))
        mainJson.put("Crash:${thread.id}:${thread.name}", stackTrace.toString())

//        val stackJson = JSONObject()
//        val allStackTraces = Thread.getAllStackTraces()
//        for ((key, value) in allStackTraces) {
//            if (key.id == thread.id)
//                continue
//            stackJson.put("${key.id}:${key.name}", getStackTraceString(value))
//        }
//        mainJson.put("allstacks", stackJson)

        if (BaseBookApplication.getGlobalContext().readStatus != null) {
            extJson.put("readstatus", BaseBookApplication.getGlobalContext().readStatus.toString())
        } else {
            extJson.put("readstatus", "null")
        }
        mainJson.put("env", extJson)

//        Log.e("Crash", mainJson.toString())
//
//        File("/sdcard/crash.txt").writeText(mainJson.toJSONString())

        Thread() {
            StartLogClickUtil.sendDirectMsg("crash", mainJson.toString())

            StartLogClickUtil.sendDirectLog(StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.CRASH)
            synchronized(lock) {
                lock.notify()
            }
        }.start()

        synchronized(lock) {
            lock.wait(5000)
            //必须交给上级处理
            parent.uncaughtException(thread, exception)
        }
    }


    private fun getStackTraceString(trace: Array<StackTraceElement>): String {
        val stackTrace = StringWriter()
        val printWriter = PrintWriter(stackTrace)
        for (traceElement in trace)
            printWriter.println(traceElement.toString())

        return stackTrace.toString()

    }
}
