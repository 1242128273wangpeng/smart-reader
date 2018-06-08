package com.dy.media

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.support.v4.app.Fragment
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Desc 扩展方法
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/6/7 15:57
 */

private object BackgroundExecutor {
    private var executor: ExecutorService =
            Executors.newScheduledThreadPool(2 * Runtime.getRuntime().availableProcessors())

    fun <R> submit(task: () -> R): Future<R> = executor.submit(task)
}

fun <R> R.doAsync(task: R.() -> Unit): Future<Unit> {
    return BackgroundExecutor.submit {
        return@submit try {
            task()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun <R> R.uiThread(f: (R) -> Unit): Boolean {
    val ref = WeakReference(this).get() ?: return false
    if (ref is Activity && ref.isFinishing) return false
    if (ref is Fragment && ref.isDetached) return false
    if (mainThread == Thread.currentThread()) {
        f(ref)
    } else {
        msMainLooperHandler.post { f(ref) }
    }
    return true
}

@JvmField
val mainThread: Thread = Looper.getMainLooper().thread

@JvmField
val msMainLooperHandler = Handler(Looper.getMainLooper())