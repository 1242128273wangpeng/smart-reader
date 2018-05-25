package net.lzbook.kit.utils

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.support.annotation.AttrRes
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.Animation
import android.widget.TextView
import de.greenrobot.event.EventBus
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Created by xian on 2017/6/21.
 */

@JvmField
var msDebuggAble = false

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

fun Any.runOnMain(run: () -> Unit) {
    msMainLooperHandler.post {
        run.invoke()
    }
}

fun Any.runOnMainDelayed(delay: Long, run: () -> Unit) {
    msMainLooperHandler.postDelayed({ run.invoke() }, delay)
}

enum class LOG_LEVEL {
    DEBUG, INFO, ERR;
}

fun Any.log(str: String, vararg param: Any?) {
    if (msDebuggAble) {
        var builder = StringBuilder()
        builder.append(str)
        param.forEach {
            builder.append(" | " + it.toString())
        }

        Log.d(this.javaClass.simpleName, builder.toString())
    }
}

fun logWithLevel(obj: Any, level: LOG_LEVEL, param: List<Any?>) {
    if (msDebuggAble || level == LOG_LEVEL.ERR) {
        var builder = StringBuilder()
        param?.forEach {
            builder.append(it.toString() + " | ")
        }

        when (level) {
            LOG_LEVEL.DEBUG -> {
                Log.d(obj.javaClass.name, builder.toString())
            }
            LOG_LEVEL.INFO -> {
                Log.i(obj.javaClass.name, builder.toString())
            }
            LOG_LEVEL.ERR -> {
                Log.e(obj.javaClass.name, builder.toString())
            }
        }

    }
}

fun Any.logd(vararg param: Any) {
    logWithLevel(this, LOG_LEVEL.DEBUG, param.asList())
}

fun Any.logi(vararg param: Any) {
    logWithLevel(this, LOG_LEVEL.INFO, param.asList())
}

fun Any.loge(vararg param: Any) {
    logWithLevel(this, LOG_LEVEL.ERR, param.asList())
}

fun Any?.toMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    if (this != null) {
        val fields = this.javaClass.declaredFields
        fields.forEach {
            if (!it.name.equals("Companion")) {
                it.isAccessible = true
                map.put(it.name, it.get(this)?.toString() ?: "")
            }
        }
    }
    return map
}

fun <T> Observable<T>.subscribekt(onNext: ((t: T) -> Unit)? = null, onError: ((t: Throwable) -> Unit)? = null): Disposable {
    return this.subscribe(io.reactivex.functions.Consumer { t ->
        onNext?.invoke(t)
    }, io.reactivex.functions.Consumer { t ->
        onError?.invoke(t)
    })
}

fun EventBus.safeRegist(obj: Any) {
    if (!isRegistered(obj))
        register(obj)
}

fun EventBus.safeUnregist(obj: Any) {
    if (isRegistered(obj))
        unregister(obj)
}

fun Any.postEventToBus(obj: Any) {
    EventBus.getDefault().post(obj)
}

fun View.idName(): String {
    return context.resources.getResourceEntryName(id)
}

fun Animation.onEnd(callback: () -> Unit) {
    this.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {

        }

        override fun onAnimationEnd(animation: Animation?) {
            callback.invoke()
        }

        override fun onAnimationStart(animation: Animation?) {

        }

    })
}

/**
 * theme 中未找到返回 -1
 */
fun Context.attrColor(@AttrRes attr: Int): Int {
    val typeValue = TypedValue()
    val b = this.theme.resolveAttribute(attr, typeValue, true)
    if (b) {
        if (typeValue.type == TypedValue.TYPE_REFERENCE) {
            return resources.getColor(typeValue.resourceId)
        } else {
            return typeValue.data
        }
    } else {
        return -1
    }
}

fun Context.attrDrawable(@AttrRes attr: Int): Drawable? {
    val typeValue = TypedValue()
    val b = this.theme.resolveAttribute(attr, typeValue, true)

    if (b) {
        if (typeValue.type == TypedValue.TYPE_REFERENCE) {
            return resources.getDrawable(typeValue.resourceId)
        } else if (typeValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && typeValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            return ColorDrawable(typeValue.data)
        }
    }

    return null
}

fun TextView.resolveTextColor(@AttrRes attr: Int) {
    val attrColor = this.context.attrColor(attr)
    if (attrColor != -1) {
        this.setTextColor(attrColor)

    }
}