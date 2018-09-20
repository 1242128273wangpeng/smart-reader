package net.lzbook.kit.utils.toast

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.support.annotation.StringRes
import android.view.View
import android.widget.Toast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import net.lzbook.kit.utils.msDebuggAble
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/24 20:30
 */

@JvmField
val mainLooperHandler = Handler(Looper.getMainLooper())

/***
 * 主线程运行
 * **/
fun runOnMain(run: () -> Unit) {
    mainLooperHandler.post {
        run.invoke()
    }
}

/***
 * 展示Toast
 * **/
fun Context.showToastMessage(@StringRes id: Int) {
    runOnMain {
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show()
    }
}

/***
 * 展示Toast
 * **/
fun Context.showToastMessage(message: String) {
    runOnMain {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

/**
 * 登录页面 debug
 */
fun Context.debugToastShort(msg: String?) {
    if (!msDebuggAble) {
        return
    }
    runOnMain {
        Toast.makeText(this, "$msg", Toast.LENGTH_SHORT).show()
    }

}

/***
 * 延迟展示Toast
 * **/
fun Context.showToastMessage(@StringRes id: Int, delay: Long) {
    Observable.timer(delay, TimeUnit.MICROSECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                showToastMessage(id)
            }
}

/***
 * 延迟展示Toast
 * **/
fun Context.showToastMessage(message: String, delay: Long) {
    Observable.timer(delay, TimeUnit.MICROSECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                showToastMessage(message)
            }
}

fun View.preventClickShake(listener: View.OnClickListener) {
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

fun Bitmap.bitmapTransformByteArray(): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)

    val result = byteArrayOutputStream.toByteArray()

    try {
        byteArrayOutputStream.close()
    } catch (exception: Exception) {
        exception.printStackTrace()
    }

    return result
}