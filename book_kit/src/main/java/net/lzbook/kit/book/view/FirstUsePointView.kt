package net.lzbook.kit.book.view

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.support.annotation.IdRes
import android.util.AttributeSet
import android.widget.TextView
import net.lzbook.kit.utils.log
import net.lzbook.kit.utils.registToEventBus
import net.lzbook.kit.utils.unregistFromEventBus
import java.util.*


/**
 * Created by xian on 2017/7/1.
 */
class FirstUsePointView : TextView {


    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun onEvent(event: ConsumeEvent) {
        if (event.id == id) {
            setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        registToEventBus(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        unregistFromEventBus(this)
    }

    override fun onPreDraw(): Boolean {
        if (FirstUseManager.getInstance(context).isFirstUse(id)) {
            if (background is ColorDrawable) {

                val gd = GradientDrawable()
                gd.setColor((background as ColorDrawable).color)

                val r = Math.min(width, height) / 2

                gd.cornerRadius = r.toFloat()

                setBackgroundDrawable(gd)
            }
        } else {
            setBackgroundColor(Color.TRANSPARENT)
        }
        return super.onPreDraw()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }
}

class FirstUseManager(val context: Context) {
    private val preferences: SharedPreferences

    init {
        preferences = context.getSharedPreferences("first_use_preferences", Context.MODE_PRIVATE)
        val version = preferences!!.getString("VERSION", "")
        val versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        if (!version.equals(versionName, true)) {
            val editor = preferences!!.edit()
            editor.clear()

            editor.putString("VERSION", versionName)
            val properties = Properties()
            properties.load(context.assets.open("config/first_use.properties"))
            val propertyNames = properties.propertyNames() as Enumeration<String>
            propertyNames
                    .toList().forEach {
                editor.putBoolean(it, properties[it].toString().toBoolean())
            }
            editor.apply()
        }
    }

    fun isFirstUse(@IdRes id: Int): Boolean {
        val key = context.resources.getResourceEntryName(id)
        if (key != null) {
            return isFirstUse(key)
        } else {
            return true
        }
    }

    fun isFirstUse(key: String): Boolean {
        return preferences.getBoolean(key, false)
    }

    fun onEvent(event: ConsumeEvent) {
        consumeFirstUse(event.id)
    }

    fun consumeFirstUse(@IdRes id: Int) {
        log("consumeFirstUse", id)

        val key = context.resources.getResourceEntryName(id)
        if (key != null) {
            consumeFirstUse(key)
        }
    }

    fun consumeFirstUse(key: String) {
        log("consumeFirstUse", key)
        if (preferences.getBoolean(key, false)) {
            val editor = preferences.edit()
            editor.putBoolean(key, false)
            editor.apply()
        }
    }


    companion object {
        @JvmStatic private var msFirstUserManager: FirstUseManager? = null
        @JvmStatic
        fun getInstance(context: Context): FirstUseManager {
            if (msFirstUserManager == null) {
                msFirstUserManager = FirstUseManager(context.applicationContext)
                registToEventBus(msFirstUserManager!!)
            }
            return msFirstUserManager!!
        }
    }
}

data class ConsumeEvent(@IdRes val id: Int)

