package com.intelligent.reader.read.page

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewPropertyAnimator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.RadioGroup
import android.widget.RadioGroup.OnCheckedChangeListener
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import com.intelligent.reader.R
import com.intelligent.reader.activity.ReadingActivity
import com.intelligent.reader.read.help.IReadDataFactory
import com.intelligent.reader.read.help.ReadSettingHelper
import iyouqu.theme.FrameActivity
import iyouqu.theme.ThemeHelper
import kotlinx.android.synthetic.zsmfqbxs.read_option_bottom.view.*
import kotlinx.android.synthetic.zsmfqbxs.read_option_detail.view.*
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.ReadStatus
import net.lzbook.kit.request.DataCache
import net.lzbook.kit.utils.*
import java.text.NumberFormat


/**
 * 阅读页阅读设置
 */
class ReadSettingView : FrameLayout, View.OnClickListener, RadioGroup.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {

    private var sharedPreferences: SharedPreferences? = null
    private var readSettingHelper: ReadSettingHelper? = null


    private var autoBrightness: Boolean = false
    internal var isCustomReadingSpace: Boolean = false


    private var listener: OnReadSettingListener? = null
    private var popUpInAnimation: Animation? = null
    private var popDownOutAnimation: Animation? = null
    private var dataFactory: IReadDataFactory? = null
    private var readStatus: ReadStatus? = null
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

        Constants.PAGE_MODE = sharedPreferences!!.getInt("page_mode", 0)

        if (isAutoBrightness) {
            autoBrightness = true
        } else {
            autoBrightness = false
        }

        this.addView(LayoutInflater.from(context).inflate(R.layout.read_option_bottom, null))
        this.addView(LayoutInflater.from(context).inflate(R.layout.read_option_detail, null))

        changeBottomSettingView(-1)

        popUpInAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_up_in)
        popDownOutAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_down_out)

        read_setting_brightness_progress!!.max = 235

        setScreenBright()

        // 横竖屏切换 跟随系统亮度时,保持亮度一致
        if (autoBrightness) {
            openSystemLight()
        }

        val numberFormat = NumberFormat.getNumberInstance()
        numberFormat.maximumFractionDigits = 2

        try {
            Constants.READ_INTERLINEAR_SPACE = sharedPreferences!!.getInt("read_interlinear_space", 3) * 0.1f
            Constants.READ_INTERLINEAR_SPACE = java.lang.Float.valueOf(numberFormat.format(Constants.READ_INTERLINEAR_SPACE.toDouble()))!!
            Constants.READ_PARAGRAPH_SPACE = sharedPreferences!!.getInt("read_paragraph_space", 10) * 0.1f
            Constants.READ_PARAGRAPH_SPACE = java.lang.Float.valueOf(numberFormat.format(Constants.READ_PARAGRAPH_SPACE.toDouble()))!!
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }

        Constants.READ_CONTENT_PAGE_TOP_SPACE = sharedPreferences!!.getInt("read_content_page_top_space", 45)
        Constants.READ_CONTENT_PAGE_LEFT_SPACE = sharedPreferences!!.getInt("read_content_page_left_space", 20)

        // 老版本左右边距修正
        if (Constants.READ_CONTENT_PAGE_LEFT_SPACE != 20) {
            Constants.READ_CONTENT_PAGE_LEFT_SPACE = 20
            sharedPreferences!!.edit().putInt("read_content_page_left_space", 20).apply()
        }

        // 老版本行距修正
        if (!(Constants.READ_INTERLINEAR_SPACE == 0.2f || Constants.READ_INTERLINEAR_SPACE == 0.3f || Constants.READ_INTERLINEAR_SPACE == 0.4f || Constants.READ_INTERLINEAR_SPACE == 0.5f)) {
            Constants.READ_INTERLINEAR_SPACE = 0.3f
            sharedPreferences!!.edit().putInt("read_interlinear_space", 3).apply()
        }

        isCustomSpaceSet()
        initPageMode()
        setFontSize()

        if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            read_landscape.isChecked = true
        } else {
            read_landscape.isChecked = false
        }
        read_full.isChecked = sharedPreferences!!.getBoolean("full_screen_read", false)

        resetBtn()

    }

    private fun resetBtn() {
        if (Constants.PAGE_MODE == 3) {
            read_full.isEnabled = false
            read_full.isClickable = false
            read_full.alpha = 0.3f
        } else {
            read_full.isEnabled = true
            read_full.isClickable = true
            read_full.alpha = 1f
        }

        if (Constants.isSlideUp) {
            read_autoRead.isClickable = false
            read_autoRead.isEnabled = false
            read_autoRead.alpha = 0.3f
        } else {
            read_autoRead.isClickable = true
            read_autoRead.isEnabled = true
            read_autoRead.alpha = 1f
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

                resetBtn()

                if (Constants.MODE == 61) {
                    read_setting_backdrop_group.clearCheck()
                } else {
                    setNovelMode(Constants.MODE)
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
            readSettingHelper!!.setScreenBrightness(context as Activity?, 20 + screenBrightness)
        } else if (ReadingActivity.mSystemBrightness >= 20) {
            readSettingHelper!!.setScreenBrightness(context as Activity?, ReadingActivity.mSystemBrightness)
        } else {
            readSettingHelper!!.setScreenBrightness(context as Activity?, 20)
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
        } else if (ReadingActivity.mSystemBrightness >= 20) {
            read_setting_brightness_progress!!.progress = ReadingActivity.mSystemBrightness - 20
        } else {
            read_setting_brightness_progress!!.progress = 5
        }
    }

    fun showSetMenu(show: Boolean) {
        if (show) {
            if (Constants.FONT_SIZE > 10) {
                read_setting_reduce_text!!.isEnabled = true
            } else {
                read_setting_reduce_text!!.isEnabled = false
            }
            if (Constants.FONT_SIZE < 30) {
                read_setting_increase_text!!.isEnabled = true
            } else {
                read_setting_increase_text!!.isEnabled = false
            }
            novel_bottom_options.visibility = View.VISIBLE
            novel_bottom_options.startAnimation(popUpInAnimation)


            refreshJumpPreBtnState()

            if (novel_jump_layout != null) {
                if (readStatus!!.chapterCount - 1 <= 0 || readStatus!!.chapterCount - 1 < readStatus!!.sequence) {
                    novel_jump_progress!!.progress = 0
                } else {
                    val index = Math.max(readStatus!!.sequence, 0)
                    novel_jump_progress!!.progress = index * 100 / (readStatus!!.chapterCount - 1)
                }
                showChapterProgress()
            }


            if (themeHelper!!.isNight()) {
                txt_night.text = "白天"
                ibtn_night.setImageResource(R.drawable.read_option_day_selector)
            } else {
                txt_night.text = "夜间"
                ibtn_night.setImageResource(R.drawable.read_option_night_selector)
            }

        } else {
            if (novel_bottom_options != null && novel_bottom_options!!.isShown) {
                novel_bottom_options!!.startAnimation(popDownOutAnimation)
            }
            popDownOutAnimation!!.onEnd {
                novel_bottom_options!!.visibility = View.GONE
            }
            read_setting_detail!!.visibility = View.GONE
            //dismissNovelHintLayout();
        }
    }

    private fun showChapterProgress() {
        if (readStatus!!.sequence == -1) {
        } else {
            if (novel_hint_chapter != null) {
                novel_hint_chapter!!.text = if (TextUtils.isEmpty(readStatus!!.chapterName)) "" else readStatus!!.chapterName
            }
            if (novel_hint_sequence != null) {
                novel_hint_sequence!!.text = (readStatus!!.sequence + 1).toString() + "/" + readStatus!!.chapterCount + "章"
            }
        }

    }

    private fun initListener() {

        novel_jump_previous?.setOnClickListener(this)


        novel_jump_next?.setOnClickListener(this)

        novel_jump_progress?.setOnSeekBarChangeListener(this)

        novel_catalog?.setOnClickListener(this)

        novel_setting?.setOnClickListener(this)

        novel_night?.setOnClickListener(this)

        read_setting_backdrop_group?.setOnCheckedChangeListener(this)

        read_setting_row_spacing_group?.setOnCheckedChangeListener { id ->
            when (id) {
                R.id.read_spacing_0_2 -> {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_hangju_01)
                    val data = java.util.HashMap<String, String>()
                    data.put("type", "4")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.READGAP, data)
                    if (read_spacing_0_2!!.isChecked) {
                        Constants.READ_INTERLINEAR_SPACE = 0.2f
                        setInterLinearSpaceMode()
                    }
                }
                R.id.read_spacing_0_5 -> {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_hangju_02)
                    val data = java.util.HashMap<String, String>()
                    data.put("type", "3")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.READGAP, data)
                    if (read_spacing_0_5!!.isChecked) {
                        Constants.READ_INTERLINEAR_SPACE = 0.3f
                        setInterLinearSpaceMode()
                    }
                }
                R.id.read_spacing_1_0 -> {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_hangju_03)
                    val data = java.util.HashMap<String, String>()
                    data.put("type", "2")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.READGAP, data)
                    if (read_spacing_1_0!!.isChecked) {
                        Constants.READ_INTERLINEAR_SPACE = 0.4f
                        setInterLinearSpaceMode()
                    }
                }
                R.id.read_spacing_1_5 -> {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_hangju_04)
                    val data = java.util.HashMap<String, String>()
                    data.put("type", "1")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.READGAP, data)
                    if (read_spacing_1_5!!.isChecked) {
                        Constants.READ_INTERLINEAR_SPACE = 0.5f
                        setInterLinearSpaceMode()
                    }
                }
            }
        }

        read_setting_animation_group?.setOnCheckedChangeListener(this)

        read_setting_reduce_text?.setOnClickListener(this)

        read_setting_increase_text?.setOnClickListener(this)


        read_setting_brightness_progress?.setOnSeekBarChangeListener(this)

        read_setting_auto_power?.setOnClickListener(this)

        read_landscape?.setOnClickListener(this)

        read_autoRead?.setOnClickListener(this)
        read_full?.setOnClickListener(this)


    }

    override fun onClick(v: View) {
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

            R.id.novel_setting -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_setting_btn)
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.SET)
                changeBottomSettingView(SETTING_DETAIL)
            }
            R.id.novel_night//夜间模式
            -> {
                if (themeHelper!!.isNight()) {
                    txt_night.text = "夜间"
                    ibtn_night.setImageResource(R.drawable.read_option_night_selector)
                } else {
                    txt_night.text = "白天"
                    ibtn_night.setImageResource(R.drawable.read_option_day_selector)

                }
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_night_mode)
                listener?.onChageNightMode()
            }
            R.id.read_setting_reduce_text// 减小字号
            -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_font_size_smaller)
                decreaseFont()
            }
            R.id.read_setting_increase_text// 加大字号
            -> {
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
                Constants.FULL_SCREEN_READ = read_full.isChecked
                val data = java.util.HashMap<String, String>()
                if (Constants.FULL_SCREEN_READ) {
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

    private fun refreshJumpPreBtnState() {
        if (readStatus!!.sequence <= 0) {
            novel_jump_previous.isClickable = false
            novel_jump_previous.isEnabled = false
            novel_jump_previous.alpha = 0.3f
        } else {
            novel_jump_previous.isClickable = true
            novel_jump_previous.isEnabled = true
            novel_jump_previous.alpha = 1f
        }
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

    private fun dismissNovelHintLayout() {
        if (novel_hint_layout != null && novel_hint_layout!!.visibility != View.GONE) {
            novel_hint_layout!!.visibility = View.GONE
        }
    }

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
        if (Constants.FONT_SIZE > 10) {
            if (Constants.FONT_SIZE == 30) {
                read_setting_increase_text!!.isEnabled = true
            }
            Constants.FONT_SIZE -= 2
            if (Constants.FONT_SIZE <= 10) {
                read_setting_reduce_text!!.isEnabled = false
            }
            readSettingHelper!!.saveFontSize()

            setFontSize()

            val temp_offset = readStatus!!.offset
            if (listener != null) {
                listener!!.onRedrawPage()
            }
            readStatus!!.offset = temp_offset
        }
        val data = java.util.HashMap<String, String>()
        data.put("type", "2")
        data.put("FONT", Constants.FONT_SIZE.toString())
        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.WORDSIZE, data)
    }

    /**
     * 增大字体
     */
    private fun increaseFont() {
        if (Constants.FONT_SIZE < 30) {
            if (Constants.FONT_SIZE == 10) {
                read_setting_reduce_text!!.isEnabled = true
            }
            Constants.FONT_SIZE += 2
            if (Constants.FONT_SIZE >= 30) {
                read_setting_increase_text!!.isEnabled = false
            }
            readSettingHelper!!.saveFontSize()

            setFontSize()

            val temp_offset = readStatus!!.offset
            if (listener != null) {
                listener!!.onRedrawPage()
            }
            readStatus!!.offset = temp_offset
        }
        val data = java.util.HashMap<String, String>()
        data.put("type", "1")
        data.put("FONT", Constants.FONT_SIZE.toString())
        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.WORDSIZE, data)
    }

    private fun setFontSize() {
        if (read_setting_text_size != null) {
            read_setting_text_size!!.text = Constants.FONT_SIZE.toString()
        }
    }

    fun changeChapter() {
        if (novel_jump_progress != null && novel_jump_progress!!.isShown && readStatus!!.chapterCount - 1 != 0) {
            val index = Math.max(readStatus!!.sequence, 0)
            novel_jump_progress!!.progress = index * 100 / (readStatus!!.chapterCount - 1)
        }
        showChapterProgress()
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
    }

    fun changePageBackgroundWrapper(index: Int) {
        if (Constants.MODE == 61) {
            readSettingHelper!!.setReadMode(index)
            sharedPreferences?.edit()?.putInt("current_light_mode", index)?.apply()
            listener?.onChageNightMode()
        } else {
            setNovelMode(index)
        }

        badiuStat(index)
    }

    private fun badiuStat(index: Int) {
        when (index) {
            51 -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_background_01)
            }
            52 -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_background_02)
            }
            53 -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_background_03)
            }
            54 -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_background_04)
            }
            55 -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_background_05)
            }
            56 -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_background_06)
            }
            61 -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_background_08)
            }
            else -> {
            }
        }
    }

    fun setNovelMode(index: Int) {
        if (Constants.MODE == 61) {
            restoreBright()
        }


        when (index) {
            51 -> {

                readSettingHelper!!.setReadMode(index)
                changeMode(51)
                read_setting_backdrop_group!!.check(R.id.read_backdrop_first)
            }
            52 -> {

                readSettingHelper!!.setReadMode(index)
                changeMode(52)
                read_setting_backdrop_group!!.check(R.id.read_backdrop_second)
            }
            53 -> {

                readSettingHelper!!.setReadMode(index)
                changeMode(53)
                read_setting_backdrop_group!!.check(R.id.read_backdrop_third)
            }
            54 -> {

                readSettingHelper!!.setReadMode(index)
                changeMode(54)
                read_setting_backdrop_group!!.check(R.id.read_backdrop_fourth)
            }
            55 -> {

                readSettingHelper!!.setReadMode(index)
                changeMode(55)
                read_setting_backdrop_group!!.check(R.id.read_backdrop_fifth)
            }
            56 -> {

                readSettingHelper!!.setReadMode(index)
                changeMode(56)
                read_setting_backdrop_group!!.check(R.id.read_backdrop_sixth)
            }
            61 -> {

                readSettingHelper!!.setReadMode(index)
                changeMode(61)
            }
            else -> {
            }
        }
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

        val prefetchThumb = getResources().getDrawable(
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
            }
            R.id.read_animation_simulation -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_flip_page_02)
                val data = java.util.HashMap<String, String>()
                data.put("type", "3")
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.PAGETURN, data)
                changePageMode(1)
            }
            R.id.read_animation_translation -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_flip_page_03)
                val data = java.util.HashMap<String, String>()
                data.put("type", "2")
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.PAGETURN, data)
                changePageMode(2)
            }
            R.id.read_animation_updown -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_flip_page_04)
                val data = java.util.HashMap<String, String>()
                data.put("type", "4")
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.PAGETURN, data)
                changePageMode(3)
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
        if (mode == 3 && Constants.PAGE_MODE != 3 || mode != 3 && Constants.PAGE_MODE == 3) {
            val intent = Intent(context, ReadingActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("book", readStatus!!.book)
            bundle.putInt("sequence", readStatus!!.sequence)
            bundle.putInt("nid", readStatus!!.nid)
            bundle.putInt("offset", readStatus!!.offset)
            bundle.putString("thememode", currentThemeMode)
            intent.putExtras(bundle)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

            context!!.startActivity(intent)
        }
        Constants.PAGE_MODE = mode
    }

    fun setInterLinearSpaceMode() {
        switchSpaceState()

        readSettingHelper!!.saveLinearSpace()

        isCustomReadingSpace = false

        val temp_offset = readStatus!!.offset
        // 重新绘制页面
        if (listener != null) {
            listener!!.onRedrawPage()
        }
        readStatus!!.offset = temp_offset
    }

    // 根据页间距默认值判断是否为自定义间距
    private fun isCustomSpaceSet() {
        if (Constants.READ_INTERLINEAR_SPACE == 0.2f || Constants.READ_INTERLINEAR_SPACE == 0.3f || Constants.READ_INTERLINEAR_SPACE == 0.4f || Constants.READ_INTERLINEAR_SPACE == 0.5f) {
            if (Constants.READ_CONTENT_PAGE_LEFT_SPACE == 20 && Constants.READ_CONTENT_PAGE_TOP_SPACE == 45 && Constants.READ_PARAGRAPH_SPACE == 1.0f) {
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
        if (Constants.READ_INTERLINEAR_SPACE == 0.2f) {
            read_setting_row_spacing_group!!.check(R.id.read_spacing_0_2)
            readSettingHelper!!.setRowSpacing(2)

        } else if (Constants.READ_INTERLINEAR_SPACE == 0.3f) {
            read_setting_row_spacing_group!!.check(R.id.read_spacing_0_5)
            readSettingHelper!!.setRowSpacing(3)

        } else if (Constants.READ_INTERLINEAR_SPACE == 0.4f) {
            read_setting_row_spacing_group!!.check(R.id.read_spacing_1_0)
            readSettingHelper!!.setRowSpacing(4)

        } else if (Constants.READ_INTERLINEAR_SPACE == 0.5f) {
            read_setting_row_spacing_group!!.check(R.id.read_spacing_1_5)
            readSettingHelper!!.setRowSpacing(5)

        }
    }


    fun setDataFactory(factory: IReadDataFactory, readStatus: ReadStatus, themeHelper: ThemeHelper) {
        this.dataFactory = factory
        this.readStatus = readStatus
        this.themeHelper = themeHelper

    }

    @Volatile
    var anim: ViewPropertyAnimator? = null

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser && seekBar.id == R.id.novel_jump_progress) {
            novel_hint_layout.visibility = View.VISIBLE
            novel_hint_layout.alpha = 1F
            anim?.cancel()


            anim = novel_hint_layout.animate()
            anim!!.alpha(0F)
            anim!!.duration = 1000
            anim!!.startDelay = 1000

            anim!!.start()

            val resizeProgress = progress * (readStatus!!.chapterCount - 1) / 100

            if (dataFactory!!.chapterList != null && !dataFactory!!.chapterList.isEmpty()
                    && resizeProgress < dataFactory!!.chapterList.size && resizeProgress >= 0) {
                readStatus!!.novel_progress = resizeProgress
                changeBottomSettingView(SETTING_OPTION)
                novel_hint_chapter!!.text = dataFactory!!.chapterList[resizeProgress].chapter_name
                novel_hint_sequence!!.text = (resizeProgress + 1).toString() + "/" + readStatus!!.chapterCount
            }

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
                    readSettingHelper!!.setScreenBrightness(context as Activity?, 20 + screenBrightness)
                } else if (ReadingActivity.mSystemBrightness >= 20) {
                    read_setting_brightness_progress!!.progress = ReadingActivity.mSystemBrightness - 20
                    readSettingHelper!!.setScreenBrightness(context as Activity?, ReadingActivity.mSystemBrightness)
                } else {
                    read_setting_brightness_progress!!.progress = 0
                    readSettingHelper!!.setScreenBrightness(context as Activity?, 20)
                }
            }

            val p = 20 + seekBar.progress
            readSettingHelper!!.setScreenBrightness(context as Activity?, p)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    fun getQGChapterId(sequence: Int): String? {
        for (chapter in dataFactory!!.chapterList) {
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
            if (readStatus!!.novel_progress == readStatus!!.sequence) {// 本章不跳
                return
            }
            if (Constants.QG_SOURCE == readStatus!!.book.site) {
                val chapterId = getQGChapterId(readStatus!!.novel_progress)
                val b = com.quduquxie.network.DataCache.isChapterExists(chapterId, readStatus!!.book_id)
                if (b) {
                    if (listener != null) {
                        listener!!.onJumpChapter()
                    }
                } else {
                    if (NetWorkUtils.getNetWorkType(BaseBookApplication.getGlobalContext()) == NetWorkUtils.NETWORK_NONE) {
                        Toast.makeText(context, R.string.net_error, Toast.LENGTH_SHORT).show()
                        return
                    } else {
                        if (listener != null) {
                            listener!!.onJumpChapter()
                        }
                    }
                }
            } else {
                if (DataCache.isChapterExists(readStatus!!.novel_progress, readStatus!!.book_id)) {
                    if (listener != null) {
                        listener!!.onJumpChapter()
                    }
                } else {
                    if (NetWorkUtils.getNetWorkType(BaseBookApplication.getGlobalContext()) == NetWorkUtils.NETWORK_NONE) {
                        Toast.makeText(context, R.string.net_error, Toast.LENGTH_SHORT).show()
                        return
                    } else {
                        if (listener != null) {
                            listener!!.onJumpChapter()
                        }
                    }
                }
            }
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

        if (this.dataFactory != null) {
            this.dataFactory = null
        }

        if (this.readStatus != null) {
            this.readStatus = null
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

        fun onRedrawPage()

        fun onJumpChapter()

        fun onJumpPreChapter()

        fun onJumpNextChapter()

        fun onReadFeedBack()

        fun onChageNightMode()
    }

    companion object {

        private val SETTING_OPTION = 1
        private val SETTING_DETAIL = SETTING_OPTION + 1
    }
}