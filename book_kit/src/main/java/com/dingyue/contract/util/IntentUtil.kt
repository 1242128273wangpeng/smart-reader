package net.lzbook.kit.utils

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import java.io.Serializable

/**
 * Function：Intent工具类
 *
 * Created by JoannChen on 2018/4/28 0028 16:52
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
object IntentUtil {

    // 仅在使用协议页面进入可以打开调试模式
    var isFormDisclaimerPage = "isFormDisclaimerPage"

    /**
     * 传递对象
     *
     * @param activity Activity
     * @param clazz    Activity
     * @param key      键名
     * @param obj      对象，实现Serializable接口
     * @param isFinish 是否关闭
     */
    fun start(activity: Activity, clazz: Class<*>, key: String, obj: Any, isFinish: Boolean) {
        val intent = Intent(activity, clazz)
        val bundle = Bundle()
        bundle.putSerializable(key, obj as Serializable)
        intent.putExtras(bundle)
        activity.startActivity(intent)
        if (isFinish) {
            activity.finish()
        }
    }


    /**
     * Intent单一跳转
     *
     * @param activity Activity
     * @param clazz    Activity
     * @param isFinish 是否关闭
     */
    fun start(activity: Activity, clazz: Class<*>,isFinish: Boolean) {
        val intent = Intent(activity, clazz)
        activity.startActivity(intent)
        if (isFinish) {
            activity.finish()
        }
        activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    fun start(activity: Activity, clazz: Class<*>, key: String, value: String,isFinish: Boolean) {
        val intent = Intent(activity, clazz)
        intent.putExtra(key, value)
        activity.startActivity(intent)
        if (isFinish) {
            activity.finish()
        }
        activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    fun start(activity: Activity, clazz: Class<*>, key: String, value: Boolean,isFinish: Boolean) {
        val intent = Intent(activity, clazz)
        intent.putExtra(key, value)
        activity.startActivity(intent)
        if (isFinish) {
            activity.finish()
        }
        activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

}
