package net.lzbook.kit.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.support.annotation.AttrRes
import android.support.v4.app.Fragment
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.Animation
import android.widget.TextView
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.RequestSubscriber
import com.dingyue.contract.util.SharedPreUtil
import com.umeng.message.PushAgent
import com.umeng.message.entity.UMessage
import de.greenrobot.event.EventBus
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.regex.Pattern

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
        param.forEachIndexed { index, any ->
            builder.append(any.toString() + if (index == param.size - 1) "" else " | ")
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

fun View.antiShakeClick(callback: (View) -> Unit) {
    this.setOnClickListener {
        if (isClickable) {
            callback.invoke(it)
            postDelayed({
                isClickable = true
            }, 200)
        }

        isClickable = false
    }
}

fun View.antiShakeClick(listener: View.OnClickListener) {
    this.setOnClickListener {
        if (isClickable) {
            listener.onClick(it)
            postDelayed({
                isClickable = true
            }, 200)
        }

        isClickable = false
    }
}

fun PushAgent.updateTags(context: Context, udid: String, callback: (Boolean) -> Unit) {
    loge("更新用户标签")
    RequestRepositoryFactory.loadRequestRepositoryFactory(context)
            .requestPushTags(udid, object : RequestSubscriber<ArrayList<String>>() {
                override fun requestResult(result: ArrayList<String>?) {
                    loge("获取用户新标签成功")
                    deleteOldTags { isDelete ->
                        if (!isDelete){
                            callback.invoke(false)
                            return@deleteOldTags
                        }
                        if (result?.isNotEmpty() == true) {
                            val addTags = result.toTypedArray()
                            loge("tags: $addTags")
                            tagManager.addTags({ isAdd, addResult ->
                                loge("添加用户新标签: $isAdd",
                                        "addResult: $addResult")
                                callback.invoke(isAdd)
                            }, addTags)
                        } else {
                            loge("添加用户新标签: 空")
                            callback.invoke(true)
                        }
                    }
                }

                override fun requestError(message: String) {
                    loge("获取用户新标签失败: error: $message")
                    callback.invoke(false)
                }
            })
}

private fun PushAgent.deleteOldTags(callback: (isDelete: Boolean) -> Unit) {
    tagManager.getTags { isGet, allTags ->
        if (!isGet) {
            callback.invoke(false)
            loge("获取用户旧标签失败")
            return@getTags
        }
        loge("用户旧标签 $allTags, size: ${allTags.size}")
        if (allTags?.isNotEmpty() == true && allTags[0]?.isNotEmpty() == true) {
            tagManager.deleteTags({ isDelete, deleteResult ->
                loge("删除用户旧标签: $isDelete", "result: $deleteResult")
                callback.invoke(isDelete)
            }, allTags.toTypedArray())
        } else {
            loge("用户旧标签为空")
            callback.invoke(true)
        }
    }

}

fun Activity.openPushSetting() {
    val intent = Intent()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
        intent.putExtra("app_package", packageName)
        intent.putExtra("app_uid", applicationInfo.uid)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra("android.provider.extra.APP_PACKAGE", packageName)
        }
    } else {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
        intent.data = Uri.fromParts("package", packageName, null)
    }
    startActivity(intent)
}

fun Activity.isShouldShowPushSettingDialog(): Boolean {
    val isNotifyEnable = NotificationManagerCompat.from(this)
            .areNotificationsEnabled()
    if (isNotifyEnable) return false
    val shareKey = SharedPreUtil.PUSH_LATEST_SHOW_SETTING_DIALOG_TIME
    val share = SharedPreUtil(SharedPreUtil.SHARE_DEFAULT)
    val latestShowTime = share
            .getLong(shareKey, 0)
    val currentTime = System.currentTimeMillis()
    val time = currentTime - latestShowTime
    return if (time > 3 * 24 * 60 * 60 * 1000) {
        share.putLong(shareKey, currentTime)
        true
    } else {
        false
    }
}

fun Context.openPushActivity(msg: UMessage) {
    val intent = Intent()
    intent.putPushExtra(msg)
    loge("umsg.activity: ${msg.activity}")
    intent.setClassName(this, msg.activity)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

private fun Intent.putPushExtra(msg: UMessage) {
    if (msg.extra != null) {
        val it = msg.extra.entries.iterator()

        while (it.hasNext()) {
            val entry = it.next() as MutableMap.MutableEntry<*, *>
            val key = entry.key as String
            val value = entry.value as String
            putExtra(key, value)
        }
    }
    putExtra(IS_FROM_PUSH, true)
}

@JvmField
val IS_FROM_PUSH = "is_from_push"

fun String.isNumeric(): Boolean {
    if (this.isEmpty()) return false
    val pattern = Pattern.compile("[0-9]*")
    val isNum = pattern.matcher(this)
    return isNum.matches()
}