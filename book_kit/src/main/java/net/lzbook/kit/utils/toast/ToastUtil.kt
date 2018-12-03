package net.lzbook.kit.utils.toast

import android.os.Handler
import android.os.Looper
import android.support.annotation.StringRes
import android.widget.Toast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.msDebuggAble
import net.lzbook.kit.utils.topsnackbar.TopSnackBarUtils
import java.util.concurrent.TimeUnit

object ToastUtil {
    private var mToast: Toast? = null
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
     */
    fun showToastMessage(@StringRes id: Int) {
        runOnMain {
            if ("cn.mfxsqbyd.reader" == AppUtils.getPackageName()) {
                TopSnackBarUtils.show(id)
            }else{
                if (mToast == null) {
                    mToast = Toast.makeText(BaseBookApplication.getGlobalContext(), id, Toast.LENGTH_SHORT)
                    mToast?.show()
                } else {
                    mToast?.setText(id)
                    mToast?.duration = Toast.LENGTH_SHORT
                    mToast?.show()
                }
            }

        }

    }

    /***
     * 展示Toast
     */
    fun showToastMessage(message: String) {
        runOnMain {
            if ("cn.mfxsqbyd.reader" == AppUtils.getPackageName()) {
                TopSnackBarUtils.show(message)
            }else {
                if (mToast == null) {
                    mToast = Toast.makeText(BaseBookApplication.getGlobalContext(), message, Toast.LENGTH_SHORT)
                    mToast?.show()
                } else {
                    mToast?.setText(message)
                    mToast?.duration = Toast.LENGTH_SHORT
                    mToast?.show()
                }
            }
        }

    }

    /***
     * 延迟展示Toast
     */
    fun showToastMessage(message: String, delay: Long?) {
        Observable.timer(delay!!, TimeUnit.MICROSECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showToastMessage(message) }
    }

    /***
     * 延迟展示Toast
     */
    fun showToastMessage(@StringRes id: Int, delay: Long?) {
        Observable.timer(delay!!, TimeUnit.MICROSECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showToastMessage(id) }
    }

    /**
     * 显示debug Toast
     */
    fun debugToastShort(msg: String) {
        if (!msDebuggAble) {
            return
        }
        showToastMessage(msg)
    }
}