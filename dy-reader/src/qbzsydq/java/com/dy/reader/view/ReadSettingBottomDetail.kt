package com.dy.reader.view

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.os.Build
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
import com.dingyue.contract.util.preventClickShake
import com.dy.reader.R
import com.dy.reader.event.EventReaderConfig
import com.dy.reader.event.EventSetting
import com.dy.reader.page.Position
import com.dy.reader.presenter.ReadPresenter
import com.dy.reader.presenter.ReadSettingPresenter
import com.dy.reader.setting.ReaderSettings
import com.dy.reader.setting.ReaderStatus
import iyouqu.theme.ThemeHelper
import kotlinx.android.synthetic.qbzsydq.read_option_bottom.view.*
import kotlinx.android.synthetic.qbzsydq.read_option_detail.view.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.*
import org.greenrobot.eventbus.EventBus
import java.text.NumberFormat


/**
 * 阅读页阅读设置
 */
class ReadSettingBottomDetail : FrameLayout, View.OnClickListener, RadioGroup.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {

    val readerSettings = ReaderSettings.instance
    
    var presenter: ReadSettingPresenter? = null

//    private var autoBrightness: Boolean = false
    internal var isCustomReadingSpace: Boolean = false

    private var popUpInAnimation: Animation? = null
    private var popDownOutAnimation: Animation? = null
    private var lastIndex: Int? = null
    var currentThemeMode: String? = null

    var readPresenter:ReadPresenter? = null

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

//        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
//        this.readSettingHelper = ReadSettingHelper(context)


        this.addView(LayoutInflater.from(context).inflate(R.layout.read_option_bottom, null))
        this.addView(LayoutInflater.from(context).inflate(R.layout.read_option_detail, null))

        changeBottomSettingView(-1)

        popUpInAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_up_in)
        popDownOutAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_down_out)

        read_setting_brightness_progress!!.max = 235

        if(!readerSettings.isAutoBrightness) {
            setScreenBrightProgress()
        }
        setBrightnessBackground(readerSettings.isAutoBrightness)


        isCustomSpaceSet()
        initPageMode()
        setFontSize()

        read_landscape.isChecked = readerSettings.isLandscape
        read_full.isChecked = readerSettings.isFullScreenRead

        resetBtn(Constants.isSlideUp)

    }

    private fun resetBtn(isSlideUp: Boolean) {
        if (readerSettings.animation_mode == 3) {
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
                EventBus.getDefault().post(EventSetting(EventSetting.Type.DISMISS_TOP_MENU))
                read_setting_detail!!.visibility = View.VISIBLE
                novel_bottom_options!!.visibility = View.GONE

                read_setting_backdrop_group.setOnCheckedChangeListener(null)

                resetBtn(Constants.isSlideUp)

                if (readerSettings.readThemeMode == 61) {
                    read_setting_backdrop_group.clearCheck()
                } else {
                    setNovelMode(readerSettings.readThemeMode)
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
        val screenBrightness = readerSettings.screenBrightness
        if (screenBrightness >= 0) {
            setScreenBrightness(screenBrightness)
        } else {
            setScreenBrightness(20)
        }
    }

    private fun setScreenBrightness(brightness: Int) {
        readPresenter?.setScreenBrightness(brightness)
    }

    /**
     * 关闭系统亮度
     */
    private fun closeSystemLight() {
        setBrightnessBackground(false)
//        readPresenter?.stopAutoBrightness()

        readerSettings.isAutoBrightness = false

        setScreenBright()
        setScreenBrightProgress()
    }

    private fun openSystemLight() {
        setBrightnessBackground(true)

//        val edit = sharedPreferences!!.edit()
//        edit.putBoolean("auto_brightness", true)
//        edit.apply()

        readerSettings.isAutoBrightness = true
        readPresenter?.startAutoBrightness()

        val screenBrightness =  readerSettings.screenBrightness
        if (screenBrightness > 0) {
            read_setting_brightness_progress!!.progress = screenBrightness
        }
        read_setting_brightness_progress!!.progress = 0
    }




    private fun initPageMode() {
        if (readerSettings.animation_mode == 1) {
            read_setting_animation_group!!.check(R.id.read_animation_simulation)
        } else if (readerSettings.animation_mode == 2) {
            read_setting_animation_group!!.check(R.id.read_animation_translation)
        } else if (readerSettings.animation_mode == 3) {
            read_setting_animation_group!!.check(R.id.read_animation_updown)
        }else{
            read_setting_animation_group!!.check(R.id.read_animation_slide)
        }
    }


    private fun setScreenBrightProgress() {
        val screenBrightness = readerSettings.screenBrightness

        if (screenBrightness >= 0) {
            read_setting_brightness_progress!!.progress = screenBrightness
        } else {
            read_setting_brightness_progress!!.progress = 5
        }
    }

    fun showMenu(isShow: Boolean) {
        if (isShow) {
            read_setting_reduce_text!!.isEnabled = readerSettings.fontSize > 10
            read_setting_increase_text!!.isEnabled = readerSettings.fontSize < 30
            novel_bottom_options.visibility = View.VISIBLE
            novel_bottom_options.startAnimation(popUpInAnimation)

            refreshJumpPreBtnState(ReaderStatus.position.group)
            novel_jump_progress.max = ReaderStatus.chapterList.size - 1
            if (novel_jump_layout != null) {
                if (ReaderStatus.chapterList.size < 1 || ReaderStatus.position.group < 1) {
                    novel_jump_progress!!.progress = 0
                } else {
                    novel_jump_progress!!.progress = ReaderStatus.position.group
                }
                showChapterProgress()
            }

            if (ThemeHelper.getInstance(context).isNight) {
                txt_night.text = "白天"
                ibtn_night.setImageResource(R.drawable.read_option_day_selector)
            } else {
                txt_night.text = "夜间"
                ibtn_night.setImageResource(R.drawable.read_option_night_selector)
            }
            setFontSize()
        } else {
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
        if (ReaderStatus.position.group == -1) {
        } else {
            if (novel_hint_chapter != null) {
                novel_hint_chapter!!.text = if (TextUtils.isEmpty(ReaderStatus.chapterName)) "" else ReaderStatus.chapterName
            }
            if (novel_hint_sequence != null) {
                novel_hint_sequence!!.text = (ReaderStatus.position.group + 1).toString() + "/" + ReaderStatus.chapterCount + "章"
            }
        }

    }

    private fun initListener() {

        novel_jump_previous?.preventClickShake(this)


        novel_jump_next?.preventClickShake(this)

        novel_jump_progress?.setOnSeekBarChangeListener(this)

        novel_catalog?.preventClickShake(this)

        novel_feedback?.preventClickShake(this)

        novel_setting?.preventClickShake(this)

        novel_night?.preventClickShake(this)

        read_setting_backdrop_group?.setOnCheckedChangeListener(this)

        read_setting_row_spacing_group?.setOnCheckedChangeListener { id ->
            when (id) {
                R.id.read_spacing_0_2 -> {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_hangju_01)
                    val data = java.util.HashMap<String, String>()
                    data.put("type", "4")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.READGAP, data)
                    if (read_spacing_0_2!!.isChecked) {
                        readerSettings.readInterlineaSpace = 0.2f
                        setInterLinearSpaceMode()
                    }
                }
                R.id.read_spacing_0_5 -> {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_hangju_02)
                    val data = java.util.HashMap<String, String>()
                    data.put("type", "3")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.READGAP, data)
                    if (read_spacing_0_5!!.isChecked) {
                        readerSettings.readInterlineaSpace = 0.3f
                        setInterLinearSpaceMode()
                    }
                }
                R.id.read_spacing_1_0 -> {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_hangju_03)
                    val data = java.util.HashMap<String, String>()
                    data.put("type", "2")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.READGAP, data)
                    if (read_spacing_1_0!!.isChecked) {
                        readerSettings.readInterlineaSpace = 0.4f
                        setInterLinearSpaceMode()
                    }
                }
                R.id.read_spacing_1_5 -> {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_hangju_04)
                    val data = java.util.HashMap<String, String>()
                    data.put("type", "1")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.READGAP, data)
                    if (read_spacing_1_5!!.isChecked) {
                        readerSettings.readInterlineaSpace = 0.5f
                        setInterLinearSpaceMode()
                    }
                }
            }
        }

        read_setting_animation_group?.setOnCheckedChangeListener(this)

        read_setting_reduce_text?.preventClickShake(this)

        read_setting_increase_text?.preventClickShake(this)


        read_setting_brightness_progress?.setOnSeekBarChangeListener(this)

        read_setting_auto_power?.preventClickShake(this)

        read_landscape?.preventClickShake(this)

        read_autoRead?.preventClickShake(this)
        read_full?.preventClickShake(this)


    }

    override fun onClick(v: View) {
        if(!ReaderStatus.isMenuShow) {
            return
        }
        when (v.id) {

            R.id.novel_jump_previous -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_previous_chapter)
                //dismissNovelHintLayout();
                val position = Position(ReaderStatus.book.book_id, ReaderStatus.position.group - 1, 0)
                EventBus.getDefault().post(EventReaderConfig(ReaderSettings.ConfigType.CHAPTER_REFRESH, position))
                changeChapter(position.group)
                presenter?.jumpNextChapterLog(1)
            }
            R.id.novel_jump_next -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_next_chapter)
                //dismissNovelHintLayout();
                val position = Position(ReaderStatus.book.book_id, ReaderStatus.position.group + 1, 0)
                EventBus.getDefault().post(EventReaderConfig(ReaderSettings.ConfigType.CHAPTER_REFRESH, position))
                changeChapter(position.group)
                presenter?.jumpNextChapterLog(2)
            }
            R.id.novel_catalog -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_catalog_btn)
                EventBus.getDefault().post(EventSetting(EventSetting.Type.OPEN_CATALOG))
                presenter?.readCatalogLog()
            }

            R.id.novel_feedback -> {
                presenter?.readFeedBack()
            }

            R.id.novel_setting -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_setting_btn)
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.SET)
                changeBottomSettingView(SETTING_DETAIL)
            }
            R.id.novel_night//夜间模式
            -> {
                if (ThemeHelper.getInstance(context).isNight) {
                    txt_night.text = "夜间"
                    ibtn_night.setImageResource(R.drawable.read_option_night_selector)
                } else {
                    txt_night.text = "白天"
                    ibtn_night.setImageResource(R.drawable.read_option_day_selector)

                }
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_night_mode)
                presenter?.chageNightMode()
            }
            R.id.read_setting_reduce_text// 减小字号
            -> {
                if (ReaderStatus.position.group < 0) {
                    return
                }
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_font_size_smaller)
                decreaseFont()
            }
            R.id.read_setting_increase_text// 加大字号
            -> {
                if (ReaderStatus.position.group < 0) {
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
                EventBus.getDefault().post(EventSetting(EventSetting.Type.CHANGE_SCREEN_MODE))
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

                readerSettings.isAutoReading = true
                EventBus.getDefault().post(EventSetting(EventSetting.Type.MENU_STATE_CHANGE,false))
            }
            R.id.read_full -> {

                readerSettings.isFullScreenRead = true

                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_fullscreen_read_btn)
                readerSettings.isFullScreenRead = read_full.isChecked
                val data = java.util.HashMap<String, String>()
                if (readerSettings.isFullScreenRead) {
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

    private fun refreshJumpPreBtnState(sequence: Int) {
        if (sequence <= 0) {
            novel_jump_previous.isClickable = false
            novel_jump_previous.isEnabled = false
            novel_jump_previous.alpha = 0.3f
        } else {
            novel_jump_previous.isClickable = true
            novel_jump_previous.isEnabled = true
            novel_jump_previous.alpha = 1f
        }

        if (sequence == ReaderStatus.chapterList.size - 1) {
            novel_jump_next.isClickable = false
            novel_jump_next.isEnabled = false
            novel_jump_next.alpha = 0.3f
        } else {
            novel_jump_next.isClickable = true
            novel_jump_next.isEnabled = true
            novel_jump_next.alpha = 1f
        }
    }

    private fun changeSystemLight() {
        if (readerSettings.isAutoBrightness) {
            closeSystemLight()
        } else {
            openSystemLight()
        }
    }




    /**
     * 减小字体
     */
    private fun decreaseFont() {
        if (readerSettings.fontSize > 10) {
            if (readerSettings.fontSize == 30) {
                read_setting_increase_text!!.isEnabled = true
            }
            readerSettings.fontSize -= 2
            if (readerSettings.fontSize <= 10) {
                read_setting_reduce_text!!.isEnabled = false
            }
            setFontSize()
        }
        val data = java.util.HashMap<String, String>()
        data.put("type", "2")
        data.put("FONT", readerSettings.fontSize.toString())
        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.WORDSIZE, data)
    }

    /**
     * 增大字体
     */
    private fun increaseFont() {
        if (readerSettings.fontSize < 30) {
            if (readerSettings.fontSize == 10) {
                read_setting_reduce_text!!.isEnabled = true
            }
            readerSettings.fontSize += 2
            if (readerSettings.fontSize >= 30) {
                read_setting_increase_text!!.isEnabled = false
            }

            setFontSize()
        }
        val data = java.util.HashMap<String, String>()
        data.put("type", "1")
        data.put("FONT", readerSettings.fontSize.toString())
        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.WORDSIZE, data)
    }

    fun setFontSize() {
        if (read_setting_text_size != null) {
            read_setting_text_size!!.text = readerSettings.fontSize.toString()
        }
    }

    fun changeChapter(sequence: Int) {
        if (novel_jump_progress != null && novel_jump_progress!!.isShown && ReaderStatus.chapterCount - 1 != 0) {
            val index = Math.max(ReaderStatus.position.group, 0)
            novel_jump_progress!!.progress = index
        }
        showChapterProgress()
        refreshJumpPreBtnState(sequence)
    }

    /**
     * 初始化阅读模式字体
     */
    fun initShowCacheState() {
        // 设置阅读模式
        val content_mode = readerSettings.readThemeMode

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
        if (readerSettings.readThemeMode == 61) {
            presenter?.chageNightMode(index, false)
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
                readerSettings.readThemeMode = index
                presenter?.changeNight()
            }
            else -> Unit
        }
        if (index in 51..56) {
            readerSettings.readThemeMode = index
            readerSettings.readLightThemeMode = index
            presenter?.changeNight()
        }
    }

    private fun restoreBright() {
        setBrightnessBackground()
        val screenBrightness = readerSettings.screenBrightness
        if (readerSettings.isAutoBrightness) {
            openSystemLight()
        } else {
            if (screenBrightness >= 0) {
                read_setting_brightness_progress!!.progress = screenBrightness
                setScreenBrightness(screenBrightness)
            } else {
                read_setting_brightness_progress!!.progress = 5
                setScreenBrightness(20)
            }
        }
    }

    fun setBrightnessBackground(autoBrightness: Boolean) {
        readerSettings.isAutoBrightness = autoBrightness
        setBrightBtn()
    }

    private fun setBrightnessBackground() {
        setBrightnessBackground(readerSettings.isAutoBrightness)
    }

    fun setMode() {
        if (this.resources == null || (context as Activity).isFinishing)
            return
        setBrightBtn()

//        val prefetchThumb = resources.getDrawable(
//                ResourceUtil.getResourceId(context, Constants.DRAWABLE, "_sliderbar"))
//        prefetchThumb.bounds = Rect(0, 0, prefetchThumb.intrinsicWidth, prefetchThumb.intrinsicHeight)

        /*novel_auto_read.setBackgroundResource(ResourceUtil.getResourceId(context,
                Constants.DRAWABLE, "_autoread_switch_selector"));*/
    }

    private fun setBrightBtn() {
        read_setting_auto_power?.isChecked = readerSettings.isAutoBrightness
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

        readerSettings.animation_mode = mode
    }

    fun setInterLinearSpaceMode() {
        switchSpaceState()

        isCustomReadingSpace = false
    }

    // 根据页间距默认值判断是否为自定义间距
    private fun isCustomSpaceSet() {
        if (readerSettings.readInterlineaSpace == 0.2f || readerSettings.readInterlineaSpace == 0.3f || readerSettings.readInterlineaSpace == 0.4f || readerSettings.readInterlineaSpace == 0.5f) {
            if (readerSettings.readContentPageLeftSpace == 20 && readerSettings.readContentPageTopSpace == 45 && readerSettings.readParagraphSpace == 1.0f) {
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
        if (readerSettings.readInterlineaSpace == 0.2f) {
            read_setting_row_spacing_group!!.check(R.id.read_spacing_0_2)
        } else if (readerSettings.readInterlineaSpace == 0.3f) {
            read_setting_row_spacing_group!!.check(R.id.read_spacing_0_5)
        } else if (readerSettings.readInterlineaSpace == 0.4f) {
            read_setting_row_spacing_group!!.check(R.id.read_spacing_1_0)
        } else if (readerSettings.readInterlineaSpace == 0.5f) {
            read_setting_row_spacing_group!!.check(R.id.read_spacing_1_5)
        }
    }

    @Volatile
    var anim: ViewPropertyAnimator? = null

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser && seekBar.id == R.id.novel_jump_progress) {
            novel_hint_layout.visibility = View.VISIBLE
            novel_hint_layout.alpha = 1F
            anim?.cancel()
            anim = novel_hint_layout.animate()
            anim?.alpha(0F)
            anim?.duration = 1000
            anim?.startDelay = 1000
            anim?.start()
//            val resizeProgress = progress.times(ReaderStatus.chapterList.size).div(100)
            if (!ReaderStatus.chapterList.isEmpty()
                    && progress <= ReaderStatus.chapterList.size && progress >= 0) {
                novel_jump_previous.isClickable = true
                novel_jump_previous.isEnabled = true
                novel_jump_previous.alpha = 1f
//                ReaderStatus.novel_progress = resizeProgress
                changeBottomSettingView(SETTING_OPTION)
                if (progress == 0) {
                    novel_hint_chapter.text = ReaderStatus.chapterList[progress].name
                    novel_hint_sequence.text = progress.plus(1).toString() + "/" + ReaderStatus.chapterList.size
                } else if (progress == ReaderStatus.chapterList.size - 1) {
                    novel_hint_chapter.text = ReaderStatus.chapterList[ReaderStatus.chapterList.size - 1].name
                    novel_hint_sequence.text = ReaderStatus.chapterList.size.toString() + "/" + ReaderStatus.chapterList.size
                } else {
                    novel_hint_chapter.text = ReaderStatus.chapterList[progress - 1].name
                    novel_hint_sequence.text = progress.toString() + "/" + ReaderStatus.chapterList.size
                }
            }
//            ReaderStatus.position.group = progress
//            refreshJumpPreBtnState()
        } else if (fromUser && seekBar.id == R.id.read_setting_brightness_progress) {
            // 改变系统亮度按钮
            if (readerSettings.isAutoBrightness) {
                setBrightnessBackground(false)

//                readPresenter?.stopAutoBrightness()

                readerSettings.isAutoBrightness = false
            }

            val p = Math.max(20, seekBar.progress)
            setScreenBrightness(p)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}


    override fun onStopTrackingTouch(seekBar: SeekBar) {
        val numFormat = NumberFormat.getNumberInstance()
        numFormat.maximumFractionDigits = 2
        if (seekBar.id == R.id.novel_jump_progress) {
            if (seekBar.progress == ReaderStatus.position.group) {// 本章不跳
                return
            }
            var resizeProgress = when (seekBar.progress) {
                0 -> 0
                ReaderStatus.chapterList.size - 1 -> ReaderStatus.chapterList.size - 1
                else -> seekBar.progress - 1
            }
            AppLog.e("progress2", resizeProgress.toString())
            val position = Position(ReaderStatus.book.book_id, resizeProgress, 0)
            EventBus.getDefault().post(EventReaderConfig(ReaderSettings.ConfigType.CHAPTER_REFRESH, position))
            refreshJumpPreBtnState(position.group)
        } else if (seekBar.id == R.id.read_setting_brightness_progress) {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_ld_progress)

            readerSettings.screenBrightness = Math.max(20, seekBar.progress)

            val data = java.util.HashMap<String, String>()
            data.put("lightvalue", seekBar.progress.toString())
            StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.LIGHTEDIT, data)

        }
    }


    fun recycleResource() {

        this.detachAllViewsFromParent()

        if (read_setting_backdrop_group != null) {
            read_setting_backdrop_group!!.removeAllViews()
        }

        System.gc()
    }

    companion object {
        private val SETTING_OPTION = 1
        private val SETTING_DETAIL = SETTING_OPTION + 1
    }
}
