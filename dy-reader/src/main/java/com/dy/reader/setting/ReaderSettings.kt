package com.dy.reader.setting

import android.content.Context
import android.graphics.*
import android.preference.PreferenceManager
import android.support.annotation.ColorInt
import android.support.annotation.RawRes
import android.support.v4.content.ContextCompat
import com.ding.basic.util.sp.SPKey
import com.dy.reader.R
import com.dy.reader.Reader
import com.dy.reader.event.EventReaderConfig
import com.dy.reader.helper.AppHelper
import com.dy.reader.page.GLReaderView
import com.dy.reader.service.FontDownLoadService
import com.dy.reader.util.ThemeUtil
import com.dy.reader.util.TypefaceUtil
import com.google.gson.GsonBuilder
import com.google.gson.InstanceCreator
import com.google.gson.annotations.SerializedName
import net.lzbook.kit.constants.Constants
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Type
import java.text.NumberFormat


/**
 * Created by xian on 18-3-24
 */
class ReaderSettings {

    companion object {

        /**
         * 阅读配置
         */
        const val READER_CONFIG = "reader_config"

        val instance = ReaderSettings()
        private val gson by lazy {
            GsonBuilder()
                    .registerTypeAdapter(ReaderSettings::class.java, GsonCreator())
                    .create()
        }
        private val preferences by lazy {
            Reader.context.getSharedPreferences(READER_CONFIG, Context.MODE_PRIVATE)
        }

        const val DEFAULT_BRIGHTNESS = 80
        const val NOT_SET_BRIGHTNESS = -1
    }

    private class GsonCreator : InstanceCreator<ReaderSettings> {
        override fun createInstance(type: Type?): ReaderSettings {
            return instance
        }
    }


    enum class ConfigType {
        ANIMATION,
        PAGE_REFRESH, CHAPTER_REFRESH, FONT_REFRESH, AUTO_PAUSE, AUTO_RESUME,
        GO_TO_BOOKEND, CHAPTER_SUCCESS, ADDSHLEF_SUCCESS,
        TITLE_COCLOR_REFRESH
    }


    private var needNotify = false
        get() {
            return ReaderStatus.isReady()
        }

    fun loadParams() {

        try {
            if (!preferences.contains(READER_CONFIG)) {
                initFromOld()
            } else {
                gson.fromJson(preferences.getString(READER_CONFIG, "{}"), ReaderSettings::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (fontSize == 0) {
            fontSize = 18
        }

        if (fontTypeface == 0) {
            fontTypeface = TypefaceUtil.TYPEFACE_SYSTEM
        }

        if (readThemeMode == 0) {
            readThemeMode = 51
        }

        if (animation == GLReaderView.AnimationType.AUTO) {
            animation_mode = lastAnimationMode
        }

        Constants.isSlideUp = animation_mode == 3
        Constants.isVolumeTurnover = isVolumeTurnover

        refreshModeParams()
        EventBus.getDefault().post(EventReaderConfig(ConfigType.TITLE_COCLOR_REFRESH))

        needNotify = true
    }

    private fun initFromOld() {
        val sp = Reader.context.getSharedPreferences("config", Context.MODE_PRIVATE)
        // 设置字体
        fontSize = sp.getInt("novel_font_size", 18)
        animation_mode = sp.getInt("page_mode", Constants.PAGE_MODE_DELAULT)
        isFullScreenRead = sp.getBoolean("full_screen_read", false)
        readThemeMode = sp.getInt("content_mode", 51)
        fontTypeface = TypefaceUtil.getTypefaceCode(sp.getString(SPKey.READER_TYPE_FACE,
                FontDownLoadService.FONT_DEFAULT))

        isVolumeTurnover = sp.getBoolean("sound_turnover", true)


        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Reader.context)
        val numberFormat = NumberFormat.getNumberInstance()
        numberFormat.maximumFractionDigits = 2

        isAutoBrightness = sharedPreferences.getBoolean("auto_brightness", true)
        isFullScreenRead = sharedPreferences.getBoolean("full_screen_read", false)
        screenDayBrightness = sharedPreferences.getInt("screen_bright", -1)
        readLightThemeMode = sharedPreferences.getInt("current_light_mode", 51)
        readThemeMode = sharedPreferences.getInt("content_mode", 51)

        try {
            readInterlineaSpace = sharedPreferences!!.getInt("read_interlinear_space", 3) * 0.1f
            readInterlineaSpace = java.lang.Float.valueOf(numberFormat.format(readInterlineaSpace.toDouble()))!!
            readParagraphSpace = sharedPreferences.getInt("read_paragraph_space", 10) * 0.1f
            readParagraphSpace = java.lang.Float.valueOf(numberFormat.format(readParagraphSpace.toDouble()))!!
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }

//        readContentPageTopSpace = sharedPreferences!!.getInt("read_content_page_top_space", 45)
//        readContentPageLeftSpace = sharedPreferences!!.getInt("read_content_page_left_space", 20)

        // 老版本左右边距修正
//        if (readContentPageLeftSpace != 20) {
//            readContentPageLeftSpace = 20
//        }

        // 老版本行距修正
        if (!(readInterlineaSpace == 0.2f || readInterlineaSpace == 0.3f || readInterlineaSpace == 0.4f || readInterlineaSpace == 0.5f)) {
            readInterlineaSpace = 0.3f
        }
    }


    fun save() {
        val editor = preferences.edit()

        val value = gson.toJson(instance)
        editor.putString(READER_CONFIG, value)
        editor.apply()
    }


    fun initValues() {

        try {
            if (!preferences.contains(READER_CONFIG)) {
                initFromOld()
            } else {
                gson.fromJson(preferences.getString(READER_CONFIG, "{}"), ReaderSettings::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (fontSize == 0) {
            fontSize = 18
        }

        if (fontTypeface == 0) {
            fontTypeface = TypefaceUtil.TYPEFACE_SYSTEM
        }

        if (animation == GLReaderView.AnimationType.AUTO) {
            animation_mode = lastAnimationMode
        }

        Constants.isSlideUp = animation_mode == 3
        Constants.isVolumeTurnover = isVolumeTurnover

        val editor = preferences.edit()
        editor.putString(READER_CONFIG, gson.toJson(instance))
        editor.apply()

    }


    fun clear() {
        save()
        needNotify = false
        kraftBitmap = null
    }

    @SerializedName(value = "animation")
    var animation = GLReaderView.AnimationType.CURL
        private set(value) {
            field = value
            if (needNotify)
                EventBus.getDefault().post(EventReaderConfig(ConfigType.ANIMATION))
        }

    @Transient
    private var lastAnimationMode = 0

    @Transient
    var isAutoReading = false
        set(value) {
            field = value
            lastAnimationMode = animation_mode
            if (value) {
                animation = GLReaderView.AnimationType.AUTO
            } else {
                animation_mode = lastAnimationMode
            }
        }

    /**
     * 0 滑动 1 仿真 2 平移 3 上下
     */
    @SerializedName(value = "animation_mode")
    var animation_mode = 1
        set(value) {
            field = value
            animation = when (value) {
                1 -> GLReaderView.AnimationType.CURL
                2 -> GLReaderView.AnimationType.TRANSLATION
                3 -> GLReaderView.AnimationType.LIST
                else -> GLReaderView.AnimationType.OVERLAP
            }
        }

    @SerializedName(value = "fontSize")
    var fontSize = 0
        set(value) {
            if (field != value) {
                field = value

                if (needNotify)
                    EventBus.getDefault().post(EventReaderConfig(ConfigType.FONT_REFRESH, ReaderStatus.position))
            }
        }

    @SerializedName(value = "fontTypeface")
    var fontTypeface = TypefaceUtil.TYPEFACE_SYSTEM
        set(value) {
            if (field != value) {
                field = value

                if (needNotify)
                    EventBus.getDefault().post(EventReaderConfig(ConfigType.FONT_REFRESH, ReaderStatus.position))
            }
        }

    @SerializedName(value = "isAutoBrightness")
    var isDayAutoBrightness = true

    @SerializedName(value = "isNightAutoBrightness")
    var isNightAutoBrightness = false

    @Transient
    var isAutoBrightness = false
        get() {
            return if (readThemeMode == 61) {
                isNightAutoBrightness
            } else {
                isDayAutoBrightness
            }
        }
        set(value) {
            if (readThemeMode == 61) {
                isNightAutoBrightness = value
            } else {
                isDayAutoBrightness = value
            }
            field = value
        }

    @SerializedName(value = "isVolumeTurnover")
    var isVolumeTurnover = true

    @SerializedName(value = "screenBrightness")
    var screenDayBrightness = NOT_SET_BRIGHTNESS

    @SerializedName(value = "screenNightBrightness")
    var screenNightBrightness = NOT_SET_BRIGHTNESS

    @Transient
    var screenBrightness = 0
        get() {
            return if (readThemeMode == 61) {
                screenNightBrightness
            } else {
                screenDayBrightness
            }
        }
        set(value) {
            if (readThemeMode == 61) {
                screenNightBrightness = value
            } else {
                screenDayBrightness = value
            }
            field = value
        }

    @SerializedName(value = "fontColor")
    @ColorInt
    var fontColor = Color.BLACK

    @SerializedName(value = "titleColor")
    @ColorInt
    var titleColor = Color.BLACK
        set(value) {
            if (field != value) {
                field = value
                if (needNotify)
                    EventBus.getDefault().post(EventReaderConfig(ConfigType.TITLE_COCLOR_REFRESH))
            }

        }

    @SerializedName(value = "backgroundColor")
    @ColorInt
    var backgroundColor = Color.GRAY
        private set(value) {
            field = value
            backgroundColoRed = Color.red(Color.GRAY) / 255F
            backgroundColorGreen = Color.green(Color.GRAY) / 255F
            backgroundColorBlue = Color.blue(Color.GRAY) / 255F
        }
    @Transient
    var backgroundColoRed = Color.red(Color.GRAY) / 255F
        private set
    @Transient
    var backgroundColorGreen = Color.green(Color.GRAY) / 255F
        private set
    @Transient
    var backgroundColorBlue = Color.blue(Color.GRAY) / 255F
        private set

    @Transient
    var kraftBitmap: Bitmap? = null
        private set
        get() {
            if (field == null) {
                field = readBitmap(R.raw.read_page_bg_default)
            }
            return field
        }

    @Transient
    var blueBitmapLandscape: Bitmap? = null
        private set
        get() {
            if (field == null) {
                field = createBlueBitmap()
            }
            return field
        }

    @Transient
    var blueBitmapPortrait: Bitmap? = null
        private set
        get() {
            if (field == null) {
                field = createBlueBitmap()
            }
            return field
        }
    @Transient
    var pinkBitmapLandscape: Bitmap? = null
        private set
        get() {
            if (field == null) {
                field = createPinkBitmap()
            }
            return field
        }

    @Transient
    var pinkBitmapPortrait: Bitmap? = null
        private set
        get() {
            if (field == null) {
                field = createPinkBitmap()
            }
            return field
        }

    @Transient
    var greenBitmapLandscape: Bitmap? = null
        private set
        get() {
            if (field == null) {
                field = createGreenBitmap()
            }
            return field
        }

    @Transient
    var greenBitmapPortrait: Bitmap? = null
        private set
        get() {
            if (field == null) {
                field = createGreenBitmap()
            }
            return field
        }


    @Transient
    var darkBitmapLandscape: Bitmap? = null
        private set
        get() {
            if (field == null) {
                field = createDarkBitmap()
            }
            return field
        }

    @Transient
    var darkBitmapPortrait: Bitmap? = null
        private set
        get() {
            if (field == null) {
                field = createDarkBitmap()
            }
            return field
        }

    @Transient
    var dimBitmapLandscape: Bitmap? = null
        private set
        get() {
            if (field == null) {
                field = createDimBitmap()
            }
            return field
        }

    @Transient
    var dimBitmapPortrait: Bitmap? = null
        private set
        get() {
            if (field == null) {
                field = createDimBitmap()
            }
            return field
        }

    //段间距
    @SerializedName(value = "readParagraphSpace")
    var readParagraphSpace = 1.0f

    //行间距
    @SerializedName(value = "readInterlineaSpace")
    var readInterlineaSpace = 0.3F
        set(value) {
            if (needNotify && field != value) {
                field = value
                if (needNotify)
                    EventBus.getDefault().post(EventReaderConfig(ConfigType.FONT_REFRESH, ReaderStatus.position))
            }
        }

    @SerializedName(value = "readLightThemeMode")
    var readLightThemeMode = 51

    //阅读模式 背景
    @SerializedName(value = "readThemeMode")
    var readThemeMode: Int = 0
        set(value) {
            if (field != value) {
                field = value

                refreshModeParams()

                if (needNotify)
                    EventBus.getDefault().post(EventReaderConfig(ConfigType.PAGE_REFRESH))
            }
        }

    private fun refreshModeParams() {
        kraftBitmap = null
        blueBitmapLandscape = null
        blueBitmapPortrait = null
        pinkBitmapLandscape = null
        pinkBitmapPortrait = null
        greenBitmapLandscape = null
        greenBitmapPortrait = null
        darkBitmapLandscape = null
        darkBitmapPortrait = null
        dimBitmapLandscape = null
        dimBitmapPortrait = null
        backgroundColor = ThemeUtil.getBackgroundColor(Reader.context.resources)

        titleColor = Color.BLACK
        titleColor = ThemeUtil.getTitleColor(Reader.context.resources)
        fontColor = ThemeUtil.getTextColor(Reader.context.resources)

    }

    private fun readBitmap(@RawRes bitmapId: Int): Bitmap? {
        var inputStream: InputStream? = null
        try {
            inputStream = Reader.context.resources.openRawResource(bitmapId)
            return BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun createBaseBitmap(): Bitmap {
        return Bitmap.createBitmap(AppHelper.screenWidth, AppHelper.screenHeight,
                Bitmap.Config.RGB_565)
    }

    private fun createBlueBitmap(): Bitmap? {
        val topBitmap = readBitmap(R.raw.read_page_bg_blue_up)
        val bottomBitmap = readBitmap(R.raw.read_page_bg_blue_down)
        if (topBitmap == null || bottomBitmap == null) return null
        val baseBitmap = createBaseBitmap()
        val bgColor = ContextCompat.getColor(Reader.context, R.color.reader_backdrop_blue)

        val canvas = Canvas(baseBitmap)
        canvas.drawColor(bgColor)

        val topMatrix = Matrix()
        val width = if (isLandscape) {
            canvas.height
        } else {
            canvas.width
        }
        val topScale = (0.74 * width).toFloat() / topBitmap.width
        topMatrix.postScale(topScale, topScale)
        canvas.drawBitmap(topBitmap, topMatrix, null)

        val bottomMatrix = Matrix()
        val bottomScale = (0.51 * width).toFloat() / bottomBitmap.width
        bottomMatrix.postScale(bottomScale, bottomScale)
        bottomMatrix.postTranslate(canvas.width - bottomBitmap.width.toFloat() * bottomScale,
                canvas.height - bottomBitmap.height.toFloat() * bottomScale)
        canvas.drawBitmap(bottomBitmap, bottomMatrix, null)

        return baseBitmap
    }

    private fun createPinkBitmap(): Bitmap? {
        val topBitmap = readBitmap(R.raw.read_page_bg_pink_up)
        val bottomBitmap = readBitmap(R.raw.read_page_bg_pink_down)
        if (topBitmap == null || bottomBitmap == null) return null
        val baseBitmap = createBaseBitmap()
        val bgColor = ContextCompat.getColor(Reader.context, R.color.reader_backdrop_pink)

        val canvas = Canvas(baseBitmap)
        canvas.drawColor(bgColor)

        val topMatrix = Matrix()
        val width = if (isLandscape) {
            canvas.height
        } else {
            canvas.width
        }
        val topScale = (0.291 * width).toFloat() / topBitmap.width
        topMatrix.postScale(topScale, topScale)
        topMatrix.postTranslate(canvas.width - topBitmap.width * topScale, 0F)
        canvas.drawBitmap(topBitmap, topMatrix, null)

        val bottomMatrix = Matrix()
        val bottomScale = (0.373 * width).toFloat() / bottomBitmap.width
        bottomMatrix.postScale(bottomScale, bottomScale)
        bottomMatrix.postTranslate(0F, canvas.height - bottomBitmap.height * bottomScale)
        canvas.drawBitmap(bottomBitmap, bottomMatrix, null)

        return baseBitmap
    }

    private fun createGreenBitmap(): Bitmap? {
        val bitmap = readBitmap(R.raw.read_page_bg_green) ?: return null
        val baseBitmap = createBaseBitmap()
        val bgColor = ContextCompat.getColor(Reader.context, R.color.reader_backdrop_green)

        val canvas = Canvas(baseBitmap)
        canvas.drawColor(bgColor)

        val matrix = Matrix()
        val height = if (isLandscape) {
            canvas.width
        } else {
            canvas.height
        }
        val scaleX = canvas.width.toFloat() / bitmap.width
        val scaleY = (0.291 * height).toFloat() / bitmap.height
        matrix.postScale(scaleX, scaleY)
        matrix.postTranslate(0F, canvas.height - bitmap.height * scaleY)
        canvas.drawBitmap(bitmap, matrix, null)

        return baseBitmap
    }

    private fun createDarkBitmap(): Bitmap? {
        val bitmap = readBitmap(R.raw.read_page_bg_dark) ?: return null
        val baseBitmap = createBaseBitmap()
        val bgColor = ContextCompat.getColor(Reader.context, R.color.reader_backdrop_dark)

        val canvas = Canvas(baseBitmap)
        canvas.drawColor(bgColor)

        val matrix = Matrix()
        val height = if (isLandscape) {
            canvas.width
        } else {
            canvas.height
        }
        val scaleX = canvas.width.toFloat() / bitmap.width
        val scaleY = (0.282 * height).toFloat() / bitmap.height
        matrix.postScale(scaleX, scaleY)
        matrix.postTranslate(0F, canvas.height - bitmap.height * scaleY)
        canvas.drawBitmap(bitmap, matrix, null)

        return baseBitmap
    }

    private fun createDimBitmap(): Bitmap? {
        val bitmap = readBitmap(R.raw.read_page_bg_dim) ?: return null
        val baseBitmap = createBaseBitmap()
        val bgColor = ContextCompat.getColor(Reader.context, R.color.reader_backdrop_dim)

        val canvas = Canvas(baseBitmap)
        canvas.drawColor(bgColor)

        val matrix = Matrix()
        val width = if (isLandscape) {
            canvas.height
        } else {
            canvas.width
        }
        val scale = (0.648 * width).toFloat() / bitmap.width
        matrix.postScale(scale, scale)
        matrix.postTranslate(canvas.width - bitmap.width * scale, 0F)
        canvas.drawBitmap(bitmap, matrix, null)

        return baseBitmap
    }

    //横竖屏切换
    @SerializedName(value = "isLandscape")
    var isLandscape = false

    @SerializedName(value = "autoReadSpeed")
    var autoReadSpeed = 15

    //全屏翻页
    @SerializedName(value = "isFullScreenRead")
    var isFullScreenRead = false

    // 阅读页书名字体大小
    @Transient
    val FONT_BOOKNAME_DEFAULT = 30
    // 阅读页默认字体大小
    @Transient
    val FONT_CHAPTER_DEFAULT = 18
    //阅读页面章节首页字体
    @Transient
    val FONT_CHAPTER_SIZE = 30
    //阅读页内容页左右边距
    @Transient
    val readContentPageLeftSpace = 20//4-20
    //阅读页内容上下页边距
    @Transient
    val readContentPageTopSpace = 45//20-40
    //内容宽度
    @Transient
    var mWidth: Float = 0.0f
    //内容起始绘制位置
    @Transient
    var mLineStart: Float = 0.0f
    // 绘制文字的画笔
    @Transient
    var mPaint: Paint? = null
    // 行间距
    var mLineSpace: Float = 0.0f

    // 文字行高度(包括文字高度和行间距)
    @Transient
    var mFontHeight: Float = 0.0f
    // 段间距
    @Transient
    var mDuan: Float = 0.0f
}