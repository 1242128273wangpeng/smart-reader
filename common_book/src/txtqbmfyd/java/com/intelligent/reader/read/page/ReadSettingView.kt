package com.intelligent.reader.read.page

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Build
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Toast
import com.dingyue.contract.util.showToastMessage
import com.intelligent.reader.R
import com.intelligent.reader.activity.ReadingActivity
import com.intelligent.reader.read.help.ReadSettingHelper
import com.intelligent.reader.read.mode.ReadState
import com.intelligent.reader.reader.ReaderViewModel
import com.intelligent.reader.widget.SignSeekBar
import iyouqu.theme.FrameActivity
import iyouqu.theme.ThemeHelper
import kotlinx.android.synthetic.txtqbmfyd.read_option_background.view.*
import kotlinx.android.synthetic.txtqbmfyd.read_option_bottom.view.*
import kotlinx.android.synthetic.txtqbmfyd.read_option_detail.view.*
import kotlinx.android.synthetic.txtqbmfyd.read_option_font.view.*
import kotlinx.android.synthetic.txtqbmfyd.read_option_mode.view.*
import kotlinx.android.synthetic.txtqbmfyd.read_option_reading_info.view.*
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.ReadConfig
import net.lzbook.kit.request.DataCache
import net.lzbook.kit.utils.*
import java.text.NumberFormat
import java.util.*


/**
 * 阅读页阅读设置
 */
class ReadSettingView : FrameLayout, View.OnClickListener, RadioGroup.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener, Observer {

    override fun update(o: Observable?, arg: Any?) {
        when (arg as String) {
            "FONT_SIZE" -> setFontSize()
        }
    }

    private var sharedPreferences: SharedPreferences? = null
    private var readSettingHelper: ReadSettingHelper? = null


    private var autoBrightness: Boolean = false
    internal var isCustomReadingSpace: Boolean = false


    private var listener: OnReadSettingListener? = null
    private var popUpInAnimation: Animation? = null
    private var popDownOutAnimation: Animation? = null
    private var mReaderViewModel: ReaderViewModel? = null
    private var lastIndex: Int? = null
    private var themeHelper: ThemeHelper? = null
    var currentThemeMode: String? = null

    private var time: Long = 0

    constructor(context: Context) : super(context) {
        initView()
        initListener()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
        initListener()
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
        initListener()
    }


    private fun initView() {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        this.readSettingHelper = ReadSettingHelper(context)

        val isAutoBrightness = sharedPreferences!!.getBoolean("auto_brightness", true)

        Constants.PAGE_MODE = sharedPreferences!!.getInt("page_mode", Constants.PAGE_MODE_DELAULT)

        autoBrightness = isAutoBrightness

        this.addView(LayoutInflater.from(context).inflate(R.layout.read_option_bottom, null))
        this.addView(LayoutInflater.from(context).inflate(R.layout.read_option_detail, null))

        function_layout.viewTreeObserver.addOnGlobalLayoutListener {
            function_layout_shadowView.layoutParams.height = function_layout.height
            function_layout.requestLayout()
        }

        changeBottomSettingView(-1)

        popUpInAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_up_in)
        popDownOutAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_down_out)

        read_setting_brightness_progress!!.max = 235

        setScreenBright()

        // 横竖屏切换 跟随系统亮度时,保持亮度一致
        if (autoBrightness) {
            openSystemLight()
        }

        setBrightnessBackground(autoBrightness)
        setScreenBrightProgress()

        val numberFormat = NumberFormat.getNumberInstance()
        numberFormat.maximumFractionDigits = 2

        try {
            ReadConfig.READ_INTERLINEAR_SPACE = sharedPreferences!!.getInt("read_interlinear_space", 3) * 0.1f
            ReadConfig.READ_INTERLINEAR_SPACE = java.lang.Float.valueOf(numberFormat.format(ReadConfig.READ_INTERLINEAR_SPACE.toDouble()))!!
            ReadConfig.READ_PARAGRAPH_SPACE = sharedPreferences!!.getInt("read_paragraph_space", 10) * 0.1f
            ReadConfig.READ_PARAGRAPH_SPACE = java.lang.Float.valueOf(numberFormat.format(ReadConfig.READ_PARAGRAPH_SPACE.toDouble()))!!
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }

        ReadConfig.READ_CONTENT_PAGE_TOP_SPACE = sharedPreferences!!.getInt("read_content_page_top_space", 45)
        ReadConfig.READ_CONTENT_PAGE_LEFT_SPACE = sharedPreferences!!.getInt("read_content_page_left_space", 20)

        // 老版本左右边距修正
        if (ReadConfig.READ_CONTENT_PAGE_LEFT_SPACE != 20) {
            ReadConfig.READ_CONTENT_PAGE_LEFT_SPACE = 20
            sharedPreferences!!.edit().putInt("read_content_page_left_space", 20).apply()
        }

        // 老版本行距修正
        if (!(ReadConfig.READ_INTERLINEAR_SPACE == 0.2f || ReadConfig.READ_INTERLINEAR_SPACE == 0.3f || ReadConfig.READ_INTERLINEAR_SPACE == 0.4f || ReadConfig.READ_INTERLINEAR_SPACE == 0.5f)) {
            ReadConfig.READ_INTERLINEAR_SPACE = 0.3f
            sharedPreferences!!.edit().putInt("read_interlinear_space", 3).apply()
        }

        ReadConfig.registObserver(this)

        isCustomSpaceSet()
        initPageMode()

        read_landscape.isChecked = ReadConfig.IS_LANDSCAPE
        read_full.isChecked = sharedPreferences!!.getBoolean("full_screen_read", false)

        resetBtn(Constants.isSlideUp)

        font_seekbar.configBuilder
                .min(10f)
                .max(30f)
                .sectionCount(10)
                //                .thumbColor(ContextCompat.getColor(getContext(), R.color.color_60))
//                .sectionTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .sectionTextSize(16)
                .setUnit("字号: ")
                //                .sectionTextPosition(SignSeekBar.TextPosition.BELOW_SECTION_MARK)
                .build()
        setFontSize()
    }

    private fun resetBtn(isSlideUp: Boolean) {
        if (Constants.PAGE_MODE == 3) {
            read_full.isEnabled = false
            read_full.isClickable = false
            read_full.alpha = 0.3f
        } else {
            read_full.isEnabled = true
            read_full.isClickable = true
            read_full.alpha = 1f
        }

        if (isSlideUp) {
            read_autoRead.isClickable = false
            read_autoRead.isEnabled = false
            read_autoRead.alpha = 0.3f
            Constants.isSlideUp = true
        } else {
            read_autoRead.isClickable = true
            read_autoRead.isEnabled = true
            read_autoRead.alpha = 1f
            Constants.isSlideUp
            Constants.isSlideUp = false
        }
    }

    private fun changeBottomSettingView(id: Int) {
        when (id) {
            SETTING_OPTION -> {
                novel_bottom_options!!.visibility = View.VISIBLE
                read_setting_detail!!.visibility = View.GONE
            }

            SETTING_DETAIL -> {
                if (context is ReadingActivity) {
                    (context as ReadingActivity).dismissTopMenu()
                }
                read_setting_detail!!.visibility = View.VISIBLE
                novel_bottom_options!!.visibility = View.GONE

                read_setting_backdrop_group.setOnCheckedChangeListener(null)

                resetBtn(Constants.isSlideUp)

                if (ReadConfig.MODE == 61) {
                    read_setting_backdrop_group.clearCheck()
                } else {
                    setNovelMode(ReadConfig.MODE)
                }
                read_setting_backdrop_group.setOnCheckedChangeListener(this)
            }

            else -> {
                read_setting_detail!!.visibility = View.GONE
                novel_bottom_options!!.visibility = View.GONE
            }
        }
    }

    //设置屏蔽亮度
    private fun setScreenBright() {
        val screenBrightness = sharedPreferences!!.getInt("screen_bright", -1)
        if (screenBrightness >= 0) {
            readSettingHelper?.setScreenBrightness(context as Activity, 20 + screenBrightness)
        } else if (FrameActivity.mSystemBrightness >= 20) {
            readSettingHelper?.setScreenBrightness(context as Activity, FrameActivity.mSystemBrightness)
        } else {
            readSettingHelper?.setScreenBrightness(context as Activity, 20)
        }
    }

    private fun openSystemLight() {
        setBrightnessBackground(true)

        val edit = sharedPreferences!!.edit()
        edit.putBoolean("auto_brightness", true)
        edit.apply()

        if (context is ReadingActivity) {
            (context as ReadingActivity).restoreBrightness()
        }

        val screenBrightness = sharedPreferences!!.getInt("screen_bright", 10)
        if (screenBrightness > 0) {
            read_setting_brightness_progress!!.progress = screenBrightness
        }
        read_setting_brightness_progress!!.progress = 0
    }

    private fun initPageMode() {
        if (Constants.PAGE_MODE == 0) {
            read_setting_animation_group!!.check(R.id.read_animation_slide)
        } else if (Constants.PAGE_MODE == 1) {
            read_setting_animation_group!!.check(R.id.read_animation_simulation)
        } else if (Constants.PAGE_MODE == 2) {
            read_setting_animation_group!!.check(R.id.read_animation_translation)
        } else if (Constants.PAGE_MODE == 3) {
            read_setting_animation_group!!.check(R.id.read_animation_updown)
        }
    }


    private fun setScreenBrightProgress() {
        val screenBrightness = sharedPreferences!!.getInt("screen_bright", -1)

        if (screenBrightness >= 0) {
            read_setting_brightness_progress!!.progress = screenBrightness
        } else if (FrameActivity.mSystemBrightness >= 20) {
            read_setting_brightness_progress!!.progress = FrameActivity.mSystemBrightness - 20
        } else {
            read_setting_brightness_progress!!.progress = 5
        }
    }

    fun showMenu(isShow: Boolean) {
        if (isShow) {
            font_reduce_iv.isEnabled = ReadConfig.FONT_SIZE > 10
            font_plus_iv.isEnabled = ReadConfig.FONT_SIZE < 30
            novel_bottom_options.visibility = View.VISIBLE
            novel_bottom_options.startAnimation(popUpInAnimation)

            refreshJumpPreBtnState()
            novel_jump_progress.max = ReadState.chapterList.size - 1
            if (ReadState.chapterList.size < 1 || ReadState.sequence < 1) {
                novel_jump_progress.progress = 0
            } else {
                novel_jump_progress.progress = ReadState.sequence
            }
            showChapterProgress()

            if (themeHelper!!.isNight) {
                night_mode_iv.setImageResource(R.drawable.icon_read_night)
            } else {
                night_mode_iv.setImageResource(R.drawable.icon_read_day)
            }
        } else {
            resetOptionLayout()
            if (novel_bottom_options != null && novel_bottom_options!!.isShown) {
                novel_bottom_options!!.startAnimation(popDownOutAnimation)
            }
            popDownOutAnimation?.onEnd {
                novel_bottom_options!!.visibility = View.GONE
            }
            read_setting_detail!!.visibility = View.GONE
        }
    }

    private fun showChapterProgress() {
        if (ReadState.sequence == -1) {
            setReadingChapterName("封面")
        } else {
            setReadingChapterName(if (TextUtils.isEmpty(ReadState.chapterName)) "" else ReadState.chapterName)
        }
    }

    private fun initListener() {
        novel_jump_previous.setOnClickListener(this)
        novel_jump_next.setOnClickListener(this)
        novel_jump_progress.setOnSeekBarChangeListener(this)
        novel_catalog.setOnClickListener(this)
        novel_background.setOnClickListener(this)
        novel_font.setOnClickListener(this)
        night_mode_iv.setOnClickListener(this)
        novel_read_mode.setOnClickListener(this)

        read_setting_row_spacing_group?.setOnCheckedChangeListener { id ->
            when (id) {
                R.id.read_spacing_0_2 -> {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_hangju_01)
                    val data = java.util.HashMap<String, String>()
                    data.put("type", "1")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.READGAP, data)
                    if (read_spacing_0_2.isChecked) {
                        ReadConfig.READ_INTERLINEAR_SPACE = 0.2f
                        setInterLinearSpaceMode()
                    }
                }
                R.id.read_spacing_0_5 -> {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_hangju_02)
                    val data = java.util.HashMap<String, String>()
                    data.put("type", "2")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.READGAP, data)
                    if (read_spacing_0_5.isChecked) {
                        ReadConfig.READ_INTERLINEAR_SPACE = 0.3f
                        setInterLinearSpaceMode()
                    }
                }
                R.id.read_spacing_1_0 -> {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_hangju_03)
                    val data = java.util.HashMap<String, String>()
                    data.put("type", "3")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.READGAP, data)
                    if (read_spacing_1_0.isChecked) {
                        ReadConfig.READ_INTERLINEAR_SPACE = 0.4f
                        setInterLinearSpaceMode()
                    }
                }
                R.id.read_spacing_1_5 -> {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_hangju_04)
                    val data = java.util.HashMap<String, String>()
                    data.put("type", "4")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.READGAP, data)
                    if (read_spacing_1_5.isChecked) {
                        ReadConfig.READ_INTERLINEAR_SPACE = 0.5f
                        setInterLinearSpaceMode()
                    }
                }
            }
        }

        read_setting_animation_group?.setOnCheckedChangeListener(this)

        font_reduce_iv?.setOnClickListener(this)

        font_plus_iv?.setOnClickListener(this)


        read_setting_brightness_progress?.setOnSeekBarChangeListener(this)

        read_setting_auto_power?.setOnClickListener(this)

        read_landscape?.setOnClickListener(this)

        read_autoRead?.setOnClickListener(this)
        read_full?.setOnClickListener(this)

        font_seekbar.setOnProgressChangedListener(object : SignSeekBar.OnProgressChangedListener {
            override fun onProgressChanged(signSeekBar: SignSeekBar?, progress: Int, progressFloat: Float, fromUser: Boolean) {
//                ReadConfig.FONT_SIZE = progressFloat.toInt()
//                readSettingHelper?.saveFontSize()
                setFontValue(progressFloat.toInt())
            }

            override fun getProgressOnActionUp(signSeekBar: SignSeekBar?, progress: Int, progressFloat: Float) {
//                ReadConfig.FONT_SIZE = progressFloat.toInt()
//                readSettingHelper?.saveFontSize()
                setFontValue(progressFloat.toInt())
            }

            override fun getProgressOnFinally(signSeekBar: SignSeekBar?, progress: Int, progressFloat: Float, fromUser: Boolean) {
//                ReadConfig.FONT_SIZE = progressFloat.toInt()
//                readSettingHelper?.saveFontSize()
                setFontValue(progressFloat.toInt())
            }
        })
    }

    private fun setFontValue(value: Int) {
        if (value % 2 == 0) {
            ReadConfig.FONT_SIZE = value
            readSettingHelper?.saveFontSize()
        }
    }

    override fun onClick(v: View) {
        if(ReadState.isMenuShow) {

            when (v.id) {

                R.id.novel_jump_previous -> {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_previous_chapter)
                    //dismissNovelHintLayout();

                    listener?.onJumpPreChapter()
                    refreshJumpPreBtnState()
                }
                R.id.novel_jump_next -> {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_next_chapter)
                    //dismissNovelHintLayout();

                    listener?.onJumpNextChapter()
                    refreshJumpPreBtnState()

                }
                R.id.novel_catalog -> {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_catalog_btn)
                    listener?.onReadCatalog()
                }

                R.id.novel_read_mode -> {
                    checkOptionLayout(R.id.novel_read_mode)
                }

                R.id.novel_background -> {
                    checkOptionLayout(R.id.novel_background)
                }

                R.id.novel_font -> {
                    checkOptionLayout(R.id.novel_font)
                }
                R.id.night_mode_iv//夜间模式
                -> {
                    if (themeHelper!!.isNight) {
                        night_mode_iv.setImageResource(R.drawable.icon_read_day)
                    } else {
                        night_mode_iv.setImageResource(R.drawable.icon_read_night)
                    }

                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_night_mode)
                    listener?.onChageNightMode()
                }
                R.id.font_reduce_iv// 减小字号
                -> {
                    if (ReadState.sequence < 0) {
                        return
                    }
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_font_size_smaller)
                    decreaseFont()
                }
                R.id.font_plus_iv// 加大字号
                -> {
                    if (ReadState.sequence < 0) {
                        return
                    }
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_font_size_bigger)
                    increaseFont()
                }
            /*case R.id.read_setting_save_power_layout:// 省电模式
            case R.id.read_setting_save_power:

                changeSavePowerMode();
                break;*/
                R.id.read_setting_save_power_layout, R.id.read_setting_auto_power// 跟随系统 更改按钮背景
                -> {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_ld_with_system)
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.SYSFOLLOW)
                    changeSystemLight()
                }
                R.id.read_landscape -> {
                    if (listener != null) {
                        listener!!.onChangeScreenMode()
                    }
                }
                R.id.read_autoRead -> {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_auto_read_btn)
                    val data = java.util.HashMap<String, String>()
                    if (Constants.isSlideUp) {
                        data.put("type", "2")
                    } else {
                        data.put("type", "1")
                    }
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.AUTOREAD, data)
                    listener?.onReadAuto()
                }
                R.id.read_full -> {

                    sharedPreferences?.edit()?.putBoolean("full_screen_read", read_full.isChecked)?.apply()
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_fullscreen_read_btn)
                    ReadConfig.FULL_SCREEN_READ = read_full.isChecked
                    val data = java.util.HashMap<String, String>()
                    if (ReadConfig.FULL_SCREEN_READ) {
                        data.put("type", "1")
                    } else {
                        data.put("type", "2")
                    }
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.FULLSCREENPAGEREAD, data)
                }
                else -> {
                }
            }
        }
    }

    /**
     * 当前章节名
     */
    private fun setReadingChapterName(title: String) {
        reading_foot_options_dividing.text = title
    }

    private fun checkOptionLayout(id: Int) {
        when (id) {
            R.id.novel_read_mode -> {
                novel_jump_layout.visibility = View.GONE
                novel_read_mode_layout.visibility = View.VISIBLE
                novel_read_font_layout.visibility = View.GONE
                novel_read_background_layout.visibility = View.GONE

                read_mode_iv.setImageResource(R.drawable.icon_read_mode_checked)
                bg_iv.setImageResource(R.drawable.icon_read_bg_normal)
                font_iv.setImageResource(R.drawable.icon_read_font_normal)
            }

            R.id.novel_background -> {
                novel_jump_layout.visibility = View.GONE
                novel_read_mode_layout.visibility = View.GONE
                novel_read_font_layout.visibility = View.GONE
                novel_read_background_layout.visibility = View.VISIBLE

                read_mode_iv.setImageResource(R.drawable.icon_read_mode_normal)
                bg_iv.setImageResource(R.drawable.icon_read_bg_checked)
                font_iv.setImageResource(R.drawable.icon_read_font_normal)
            }

            R.id.novel_font -> {
                novel_jump_layout.visibility = View.GONE
                novel_read_mode_layout.visibility = View.GONE
                novel_read_background_layout.visibility = View.GONE
                novel_read_font_layout.visibility = View.VISIBLE

                read_mode_iv.setImageResource(R.drawable.icon_read_mode_normal)
                bg_iv.setImageResource(R.drawable.icon_read_bg_normal)
                font_iv.setImageResource(R.drawable.icon_read_font_checked)
            }


        }
    }

    private fun resetOptionLayout() {
        novel_jump_layout.visibility = View.VISIBLE
        novel_read_mode_layout.visibility = View.GONE
        novel_read_font_layout.visibility = View.GONE
        novel_read_background_layout.visibility = View.GONE

        read_mode_iv.setImageResource(R.drawable.icon_read_mode_normal)
        bg_iv.setImageResource(R.drawable.icon_read_bg_normal)
        font_iv.setImageResource(R.drawable.icon_read_font_normal)

        function_layout_shadowView.layoutParams.height = 0
    }

    private fun refreshJumpPreBtnState() {
        if (ReadState.sequence <= 0) {
            novel_jump_previous.isClickable = false
            novel_jump_previous.isEnabled = false
        } else {
            novel_jump_previous.isClickable = true
            novel_jump_previous.isEnabled = true
        }

        if (ReadState.sequence == ReadState.chapterList.size - 1) {
            novel_jump_next.isClickable = false
            novel_jump_next.isEnabled = false
        } else {
            novel_jump_next.isClickable = true
            novel_jump_next.isEnabled = true
        }
        showChapterProgress()
    }

    private fun changeSystemLight() {
        if (autoBrightness) {
            closeSystemLight()
        } else {
            openSystemLight()
        }
    }

    /**
     * 设置当前亮度为最低
     */
    fun setSystemLightLowest() {
        readSettingHelper!!.setScreenBrightness(context as Activity?, 40)
        read_setting_brightness_progress!!.progress = 40
    }

//    private fun dismissNovelHintLayout() {
//        if (novel_hint_layout != null && novel_hint_layout!!.visibility != View.GONE) {
//            novel_hint_layout!!.visibility = View.GONE
//        }
//    }

    /**
     * 关闭系统亮度
     */
    private fun closeSystemLight() {
        setBrightnessBackground(false)
        if (context is ReadingActivity) {
            (context as ReadingActivity).setReaderDisplayBrightness()
        }

        readSettingHelper!!.closeBrightnessWithSystem()

        setScreenBright()
        setScreenBrightProgress()
    }

    /**
     * 减小字体
     */
    private fun decreaseFont() {
        if (ReadConfig.FONT_SIZE > 10) {
            if (ReadConfig.FONT_SIZE == 30) {
                font_plus_iv.isEnabled = true
            }
            ReadConfig.FONT_SIZE -= 2
            if (ReadConfig.FONT_SIZE <= 10) {
                font_reduce_iv.isEnabled = false
            }
            readSettingHelper?.saveFontSize()
            setFontSize()
        }
        val data = java.util.HashMap<String, String>()
        data.put("type", "2")
        data.put("FONT", ReadConfig.FONT_SIZE.toString())
        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.WORDSIZE, data)
    }

    /**
     * 增大字体
     */
    private fun increaseFont() {
        if (ReadConfig.FONT_SIZE < 30) {
            if (ReadConfig.FONT_SIZE == 10) {
                font_reduce_iv.isEnabled = true
            }
            ReadConfig.FONT_SIZE += 2
            if (ReadConfig.FONT_SIZE >= 30) {
                font_plus_iv.isEnabled = false
            }
            readSettingHelper!!.saveFontSize()

            setFontSize()
        }
        val data = java.util.HashMap<String, String>()
        data.put("type", "1")
        data.put("FONT", ReadConfig.FONT_SIZE.toString())
        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.WORDSIZE, data)
    }

    private fun setFontSize() {
//        if (read_setting_text_size != null) {
//            read_setting_text_size!!.text = ReadConfig.FONT_SIZE.toString()
//        }
        font_seekbar.setProgress(ReadConfig.FONT_SIZE.toFloat())
    }

    fun changeChapter() {
        if (novel_jump_progress != null && novel_jump_progress!!.isShown && ReadState.chapterCount - 1 != 0) {
            val index = Math.max(ReadState.sequence, 0)
            novel_jump_progress!!.progress = index * 100 / (ReadState.chapterCount - 1)
        }
        showChapterProgress()
        refreshJumpPreBtnState()
    }

    /**
     * 初始化阅读模式字体
     */
    fun initShowCacheState() {
        // 设置阅读模式
        val content_mode = sharedPreferences!!.getInt("content_mode", 51)

        AppLog.e("initShowCacheState", "initShowCacheState: " + content_mode)
        if ("night" == ResourceUtil.mode) {
            if (content_mode < 50) {
                setNovelMode(54)
            } else {
                setNovelMode(content_mode)
            }
        } else {
            setNovelMode(content_mode)
        }
        read_setting_backdrop_group.setOnCheckedChangeListener(this)
    }

    fun changePageBackgroundWrapper(index: Int) {
        if (ReadConfig.MODE == 61) {
            readSettingHelper?.setReadMode(index)
            sharedPreferences?.edit()?.putInt("current_light_mode", index)?.apply()
            listener?.onChageNightMode()
        } else {
            setNovelMode(index)
        }
        badiuStat(index)
    }

    private fun badiuStat(index: Int) {
        when (index) {
            51 -> StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_background_01)
            52 -> StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_background_02)
            53 -> StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_background_03)
            54 -> StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_background_04)
            55 -> StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_background_05)
            56 -> StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_background_06)
            61 -> StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_background_08)
            else -> Unit
        }
    }

    fun setNovelMode(index: Int) {
        when (index) {
            51 -> read_setting_backdrop_group?.check(R.id.read_backdrop_first)
            52 -> read_setting_backdrop_group?.check(R.id.read_backdrop_second)
            53 -> read_setting_backdrop_group?.check(R.id.read_backdrop_third)
            54 -> read_setting_backdrop_group?.check(R.id.read_backdrop_fourth)
            55 -> read_setting_backdrop_group?.check(R.id.read_backdrop_fifth)
            56 -> read_setting_backdrop_group!!.check(R.id.read_backdrop_sixth)
            61 -> {
                restoreBright()
                readSettingHelper?.setReadMode(index)
                changeMode(index)
            }
            else -> Unit
        }
        if (index in 51..56) {
            readSettingHelper?.setReadMode(index)
            changeMode(index)
        }
        read_setting_backdrop_group?.setOnCheckedChangeListener(this)
    }

    private fun restoreBright() {
        setBrightnessBackground()
        val screenBrightness = sharedPreferences!!.getInt("screen_bright", -1)
        if (autoBrightness) {
            openSystemLight()
        } else {
            if (screenBrightness >= 0) {
                read_setting_brightness_progress!!.progress = screenBrightness
                readSettingHelper!!.setScreenBrightness(context as Activity?, 20 + screenBrightness)
            } else if (FrameActivity.mSystemBrightness >= 20) {
                read_setting_brightness_progress!!.progress = FrameActivity.mSystemBrightness - 20
                readSettingHelper!!.setScreenBrightness(context as Activity?, FrameActivity.mSystemBrightness)
            } else {
                read_setting_brightness_progress!!.progress = 5
                readSettingHelper!!.setScreenBrightness(context as Activity?, 20)
            }
        }
    }

    fun setBrightnessBackground(autoBrightness: Boolean) {
        this.autoBrightness = autoBrightness
        setBrightBtn()
    }

    private fun setBrightnessBackground() {
        setBrightnessBackground(autoBrightness)
    }

    private fun changeMode(n: Int) {
        if (listener != null) {
            listener!!.onChangeMode(n)
        }
    }

    fun setMode() {
        if (this.resources == null || (context as Activity).isFinishing)
            return
        setBrightBtn()

        val prefetchThumb = resources.getDrawable(
                ResourceUtil.getResourceId(context, Constants.DRAWABLE, "_sliderbar"))
        prefetchThumb.bounds = Rect(0, 0, prefetchThumb.intrinsicWidth, prefetchThumb.intrinsicHeight)

        /*novel_auto_read.setBackgroundResource(ResourceUtil.getResourceId(context,
                Constants.DRAWABLE, "_autoread_switch_selector"));*/
    }

    private fun setBrightBtn() {
        read_setting_auto_power?.isChecked = autoBrightness
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {

        var current: Int = group.indexOfChild(group.findViewById(checkedId))
        when (checkedId) {
            R.id.read_backdrop_first -> {
                changePageBackgroundWrapper(51)
                if (current != lastIndex) {
                    val data = java.util.HashMap<String, String>()
                    data.put("type", "1")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.BACKGROUNDCOLOR, data)
                }
                lastIndex = current

            }
            R.id.read_backdrop_second -> {

                changePageBackgroundWrapper(52)
                if (current != lastIndex) {
                    val data = java.util.HashMap<String, String>()
                    data.put("type", "2")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.BACKGROUNDCOLOR, data)
                }
                lastIndex = current
            }
            R.id.read_backdrop_third -> {
                changePageBackgroundWrapper(53)
                if (current != lastIndex) {
                    val data = java.util.HashMap<String, String>()
                    data.put("type", "3")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.BACKGROUNDCOLOR, data)
                }
                lastIndex = current
            }
            R.id.read_backdrop_fourth -> {
                changePageBackgroundWrapper(54)
                if (current != lastIndex) {
                    val data = java.util.HashMap<String, String>()
                    data.put("type", "4")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.BACKGROUNDCOLOR, data)
                }
                lastIndex = current
            }
            R.id.read_backdrop_fifth -> {
                changePageBackgroundWrapper(55)
                if (current != lastIndex) {

                    val data = java.util.HashMap<String, String>()
                    data.put("type", "6")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.BACKGROUNDCOLOR, data)
                }
                lastIndex = current
            }
            R.id.read_backdrop_sixth -> {
                changePageBackgroundWrapper(56)
                if (current != lastIndex) {

                    val data = java.util.HashMap<String, String>()
                    data.put("type", "5")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.BACKGROUNDCOLOR, data)
                }
                lastIndex = current
            }

            R.id.read_animation_slide -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_flip_page_01)
                val data = java.util.HashMap<String, String>()
                data.put("type", "1")
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.PAGETURN, data)
                changePageMode(0)
                resetBtn(false)
            }
            R.id.read_animation_simulation -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_flip_page_02)
                val data = java.util.HashMap<String, String>()
                data.put("type", "3")
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.PAGETURN, data)
                changePageMode(1)
                resetBtn(false)
            }
            R.id.read_animation_translation -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_flip_page_03)
                val data = java.util.HashMap<String, String>()
                data.put("type", "2")
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.PAGETURN, data)
                changePageMode(2)
                resetBtn(false)
            }
            R.id.read_animation_updown -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_flip_page_04)
                val data = java.util.HashMap<String, String>()
                data.put("type", "4")
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.PAGETURN, data)
                changePageMode(3)
                resetBtn(true)
            }
            else -> {
            }
        }
    }

    /**
     * 0 滑动 1 仿真 2 平移 3 上下
     * mode
     */
    private fun changePageMode(mode: Int) {
        if (System.currentTimeMillis() - time < 500) {
            return
        }
        time = System.currentTimeMillis()
        readSettingHelper!!.savePageAnimation(mode)
        Constants.PAGE_MODE = mode
        listener?.changeAnimMode(mode)
    }

    fun setInterLinearSpaceMode() {
        switchSpaceState()

        readSettingHelper!!.saveLinearSpace()

        isCustomReadingSpace = false
    }

    // 根据页间距默认值判断是否为自定义间距
    private fun isCustomSpaceSet() {
        if (ReadConfig.READ_INTERLINEAR_SPACE == 0.2f || ReadConfig.READ_INTERLINEAR_SPACE == 0.3f || ReadConfig.READ_INTERLINEAR_SPACE == 0.4f || ReadConfig.READ_INTERLINEAR_SPACE == 0.5f) {
            if (ReadConfig.READ_CONTENT_PAGE_LEFT_SPACE == 20 && ReadConfig.READ_CONTENT_PAGE_TOP_SPACE == 45 && ReadConfig.READ_PARAGRAPH_SPACE == 1.0f) {
                isCustomReadingSpace = false
                switchSpaceState()
            } else {
                isCustomReadingSpace = true
                read_setting_row_spacing_group!!.clearCheck()
            }
        } else {
            isCustomReadingSpace = true
            read_setting_row_spacing_group!!.clearCheck()
        }
    }

    // 单选切换行间距
    private fun switchSpaceState() {
        if (ReadConfig.READ_INTERLINEAR_SPACE == 0.2f) {
            read_setting_row_spacing_group!!.check(R.id.read_spacing_0_2)
            readSettingHelper!!.setRowSpacing(2)

        } else if (ReadConfig.READ_INTERLINEAR_SPACE == 0.3f) {
            read_setting_row_spacing_group!!.check(R.id.read_spacing_0_5)
            readSettingHelper!!.setRowSpacing(3)

        } else if (ReadConfig.READ_INTERLINEAR_SPACE == 0.4f) {
            read_setting_row_spacing_group!!.check(R.id.read_spacing_1_0)
            readSettingHelper!!.setRowSpacing(4)

        } else if (ReadConfig.READ_INTERLINEAR_SPACE == 0.5f) {
            read_setting_row_spacing_group!!.check(R.id.read_spacing_1_5)
            readSettingHelper!!.setRowSpacing(5)

        }
    }


    fun setDataFactory(factory: ReaderViewModel, themeHelper: ThemeHelper) {
        this.mReaderViewModel = factory
        this.themeHelper = themeHelper

    }

    @Volatile
    var anim: ViewPropertyAnimator? = null

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser && seekBar.id == R.id.novel_jump_progress) {
//            novel_hint_layout.visibility = View.VISIBLE
//            novel_hint_layout.alpha = 1F
            anim?.cancel()
//            anim = novel_hint_layout.animate()
            anim?.alpha(0F)
            anim?.duration = 1000
            anim?.startDelay = 1000
            anim?.start()
//            val resizeProgress = progress.times(ReadState.chapterList.size).div(100)
            if (!ReadState.chapterList.isEmpty()
                    && progress <= ReadState.chapterList.size && progress >= 0) {
                novel_jump_previous.isClickable = true
                novel_jump_previous.isEnabled = true
                novel_jump_previous.alpha = 1f
//                ReadState.novel_progress = resizeProgress
                changeBottomSettingView(SETTING_OPTION)
                AppLog.e("progress1", progress.toString())
                if (progress == 0) {
                    setReadingChapterName(ReadState.chapterList[progress].chapter_name)
                } else if (progress == ReadState.chapterList.size - 1) {
                    setReadingChapterName(ReadState.chapterList[ReadState.chapterList.size - 1].chapter_name)
                } else {
                    setReadingChapterName(ReadState.chapterList[progress - 1].chapter_name)
                }
            }
//            ReadState.sequence = progress
            refreshJumpPreBtnState()
        } else if (fromUser && seekBar.id == R.id.read_setting_brightness_progress) {
            // 改变系统亮度按钮
            if (autoBrightness) {
                setBrightnessBackground(false)
                if (context is ReadingActivity) {
                    (context as ReadingActivity).setReaderDisplayBrightness()
                }

                readSettingHelper!!.closeAutoBrightness()

                val screenBrightness = sharedPreferences!!.getInt("screen_bright", -1)

                if (screenBrightness >= 0) {
                    read_setting_brightness_progress!!.progress = screenBrightness
                    readSettingHelper?.setScreenBrightness(context as Activity?, 20 + screenBrightness)
                } else if (FrameActivity.mSystemBrightness >= 20) {
                    read_setting_brightness_progress!!.progress = FrameActivity.mSystemBrightness - 20
                    readSettingHelper?.setScreenBrightness(context as Activity?, FrameActivity.mSystemBrightness)
                } else {
                    read_setting_brightness_progress!!.progress = 0
                    readSettingHelper?.setScreenBrightness(context as Activity?, 20)
                }
            }

            val p = 20 + seekBar.progress
            readSettingHelper!!.setScreenBrightness(context as Activity?, p)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    fun getQGChapterId(sequence: Int): String? {
        for (chapter in ReadState.chapterList) {
            if (chapter.sequence == sequence) {
                return chapter.chapter_id
            }
        }
        return null
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        val numFormat = NumberFormat.getNumberInstance()
        numFormat.maximumFractionDigits = 2
        if (seekBar.id == R.id.novel_jump_progress) {
            if (seekBar.progress == ReadState.sequence) {// 本章不跳
                return
            }
            var resizeProgress = when (seekBar.progress) {
                0 -> 0
                ReadState.chapterList.size - 1 -> ReadState.chapterList.size - 1
                else -> seekBar.progress - 1
            }
            AppLog.e("progress2", resizeProgress.toString())
            var offset = 0
            if (Constants.QG_SOURCE == ReadState.book.site) {
                val chapterId = getQGChapterId(resizeProgress)
                val b = com.quduquxie.network.DataCache.isChapterExists(chapterId, ReadState.book_id)
                if (b) {
                    if (listener != null) {
                        listener!!.onJumpChapter(resizeProgress, offset)
                    }
                } else {
                    if (NetWorkUtils.getNetWorkType(BaseBookApplication.getGlobalContext()) == NetWorkUtils.NETWORK_NONE) {
                        context.showToastMessage(R.string.net_error)
                        return
                    } else {
                        if (listener != null) {
                            listener!!.onJumpChapter(resizeProgress, offset)
                        }
                    }
                }
            } else {
                if (DataCache.isChapterExists(ReadState.currentChapter)) {
                    if (listener != null) {
                        listener!!.onJumpChapter(resizeProgress, offset)
                    }
                } else {
                    if (NetWorkUtils.getNetWorkType(BaseBookApplication.getGlobalContext()) == NetWorkUtils.NETWORK_NONE) {
                        context.showToastMessage(R.string.net_error)
                        return
                    } else {
                        if (listener != null) {
                            listener!!.onJumpChapter(resizeProgress, offset)
                        }
                    }
                }
            }
            refreshJumpPreBtnState()
        } else if (seekBar.id == R.id.read_setting_brightness_progress) {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_ld_progress)
            readSettingHelper!!.saveBrightness(seekBar.progress)

            val data = java.util.HashMap<String, String>()
            data.put("lightvalue", seekBar.progress.toString())
            StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.LIGHTEDIT, data)

        }
    }

    fun setBrightProgressBar(progress: Int) {
        read_setting_brightness_progress!!.progress = progress
    }

    fun recycleResource() {

        this.detachAllViewsFromParent()

        if (this.mReaderViewModel != null) {
            this.mReaderViewModel = null
        }

        if (read_setting_backdrop_group != null) {
            read_setting_backdrop_group!!.removeAllViews()
        }

        System.gc()
    }

    fun setOnReadSettingListener(listener: OnReadSettingListener) {
        this.listener = listener
    }

    interface OnReadSettingListener {
        fun onReadCatalog()

        fun onReadChangeSource()

        fun onReadCache()

        fun onReadAuto()

        fun onChangeMode(mode: Int)

        fun onChangeScreenMode()


        fun onJumpChapter(sequence: Int, offset: Int)

        fun onJumpPreChapter()

        fun onJumpNextChapter()

        fun onReadFeedBack()

        fun onChageNightMode()

        fun changeAnimMode(mode: Int)
    }

    companion object {
        private val SETTING_OPTION = 1
        private val SETTING_DETAIL = SETTING_OPTION + 1
    }
}
