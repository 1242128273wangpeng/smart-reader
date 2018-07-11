package com.dy.reader.view

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.os.Build
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
import com.dy.reader.Reader
import com.dy.reader.event.EventReaderConfig
import com.dy.reader.event.EventSetting
import com.dy.reader.page.Position
import com.dy.reader.presenter.ReadPresenter
import com.dy.reader.presenter.ReadSettingPresenter
import com.dy.reader.setting.ReaderSettings
import com.dy.reader.setting.ReaderStatus
import iyouqu.theme.ThemeHelper
import kotlinx.android.synthetic.qbmfrmxs.reader_option_bottom.view.*
import kotlinx.android.synthetic.qbmfrmxs.reader_option_detail.view.*
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

    internal var isCustomReadingSpace: Boolean = false

    private var popUpInAnimation: Animation? = null
    private var popDownOutAnimation: Animation? = null
    private var lastIndex: Int = -1
    var currentThemeMode: String? = null

    var readPresenter: ReadPresenter? = null

    private var time: Long = 0

    private var lastProgress = 0

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
        this.addView(LayoutInflater.from(context).inflate(R.layout.reader_option_bottom, null))
        this.addView(LayoutInflater.from(context).inflate(R.layout.reader_option_detail, null))

        changeBottomSettingView(-1)

        popUpInAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_up_in)
        popDownOutAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_down_out)

        skbar_reader_brightness_change?.max = 235

        if (!readerSettings.isAutoBrightness) {
            setScreenBrightProgress()
        }
        setBrightnessBackground(readerSettings.isAutoBrightness)


        isCustomSpaceSet()
        initPageMode()
        setFontSize()

        ckb_reader_landscape.isChecked = readerSettings.isLandscape
        ckb_reader_full_screen.isChecked = readerSettings.isFullScreenRead

        resetBtn(Constants.isSlideUp)

    }

    private fun resetBtn(isSlideUp: Boolean) {
        if (readerSettings.animation_mode == 3) {
            ckb_reader_full_screen.isEnabled = false
            ckb_reader_full_screen.isClickable = false
            ckb_reader_full_screen.alpha = 0.3f
        } else {
            ckb_reader_full_screen.isEnabled = true
            ckb_reader_full_screen.isClickable = true
            ckb_reader_full_screen.alpha = 1f
        }

        if (isSlideUp) {
            ckb_reader_auto_read.isClickable = false
            ckb_reader_auto_read.isEnabled = false
            ckb_reader_auto_read.alpha = 0.3f
            Constants.isSlideUp = true
        } else {
            ckb_reader_auto_read.isClickable = true
            ckb_reader_auto_read.isEnabled = true
            ckb_reader_auto_read.alpha = 1f
            Constants.isSlideUp
            Constants.isSlideUp = false
        }
    }

    private fun changeBottomSettingView(id: Int) {
        when (id) {
            SETTING_OPTION -> {
                rl_reader_option_bottom?.visibility = View.VISIBLE
                ll_reader_setting_detail?.visibility = View.GONE
            }

            SETTING_DETAIL -> {
                EventBus.getDefault().post(EventSetting(EventSetting.Type.DISMISS_TOP_MENU))
                ll_reader_setting_detail?.visibility = View.VISIBLE
                rl_reader_option_bottom?.visibility = View.GONE

//                rg_reader_backdrop_group.setOnCheckedChangeListener(null)

                resetBtn(Constants.isSlideUp)

                if (readerSettings.readThemeMode == 61) {
                    rg_reader_backdrop_group.clearCheck()
                } else {
                    setNovelMode(readerSettings.readThemeMode)
                }
//                rg_reader_backdrop_group.setOnCheckedChangeListener(this)
            }

            else -> {
                ll_reader_setting_detail?.visibility = View.GONE
                rl_reader_option_bottom?.visibility = View.GONE
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

        readerSettings.isAutoBrightness = false

        setScreenBright()
        setScreenBrightProgress()
    }

    private fun openSystemLight() {
        setBrightnessBackground(true)

        readerSettings.isAutoBrightness = true
        readPresenter?.startAutoBrightness()

        val screenBrightness = readerSettings.screenBrightness
        if (screenBrightness > 0) {
            skbar_reader_brightness_change?.progress = screenBrightness
        }
        skbar_reader_brightness_change?.progress = 0
    }


    private fun initPageMode() {
        if (readerSettings.animation_mode == 1) {
            rg_reader_animation_group?.check(R.id.rbtn_reader_animation_simulation)
        } else if (readerSettings.animation_mode == 2) {
            rg_reader_animation_group?.check(R.id.rbtn_reader_animation_translation)
        } else if (readerSettings.animation_mode == 3) {
            rg_reader_animation_group?.check(R.id.rbtn_reader_animation_up_down)
        } else {
            rg_reader_animation_group?.check(R.id.rbtn_reader_animation_slide)
        }
    }


    private fun setScreenBrightProgress() {
        val screenBrightness = readerSettings.screenBrightness

        if (screenBrightness >= 0) {
            skbar_reader_brightness_change?.progress = screenBrightness
        } else {
            skbar_reader_brightness_change?.progress = 5
        }
    }

    fun showMenu(isShow: Boolean) {
        if (isShow) {
            img_reader_font_reduce?.isEnabled = readerSettings.fontSize > 10
            img_reader_font_increase?.isEnabled = readerSettings.fontSize < 30
            rl_reader_option_bottom.visibility = View.VISIBLE
            rl_reader_option_bottom.startAnimation(popUpInAnimation)

            refreshJumpPreBtnState(ReaderStatus.position.group)
            skbar_reader_chapter_change.max = ReaderStatus.chapterList.size - 1
            if (rl_reader_change_chapter != null) {
                if (ReaderStatus.chapterList.size < 1 || ReaderStatus.position.group < 1) {
                    skbar_reader_chapter_change?.progress = 0
                } else {
                    skbar_reader_chapter_change?.progress = ReaderStatus.position.group
                }
                showChapterProgress()
            }

            if (ThemeHelper.getInstance(context).isNight) {
                txt_reader_night.text = "白天"
                img_reader_night.isSelected = true
            } else {
                txt_reader_night.text = "夜间"
                img_reader_night.isSelected = false
            }
            setFontSize()
        } else {
            if (rl_reader_option_bottom != null && rl_reader_option_bottom!!.isShown) {
                rl_reader_option_bottom?.startAnimation(popDownOutAnimation)
            }
            popDownOutAnimation?.onEnd {
                rl_reader_option_bottom?.visibility = View.GONE
            }
            ll_reader_setting_detail?.visibility = View.GONE
        }
    }

    private fun showChapterProgress() {
        if (ReaderStatus.position.group == -1) {
        } else {
            txt_cur_chapter_name.text = ReaderStatus.chapterName
            if(ReaderStatus.chapterCount !=0){
                var value = (ReaderStatus.position.group * 100 / ReaderStatus.chapterCount * 100) / 100
                if (value <= 0) value = 1
                val percent = value.toString() + "%"
                txt_chapter_percent.text = percent
            }

        }

    }

    private fun initListener() {

        img_reader_chapter_previous?.preventClickShake(this)

        img_reader_chapter_next?.preventClickShake(this)

        skbar_reader_chapter_change?.setOnSeekBarChangeListener(this)

        rl_reader_catalog?.preventClickShake(this)

        rl_reader_setting?.preventClickShake(this)

        rl_reader_night?.preventClickShake(this)

        rg_reader_backdrop_group?.setOnCheckedChangeListener { id ->
            when (id) {
                R.id.rbtn_reader_backdrop_first -> {
                    changePageBackgroundWrapper(51)
                    if (id != lastIndex) {
                        val data = java.util.HashMap<String, String>()
                        data.put("type", "1")
                        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.BACKGROUNDCOLOR, data)
                    }
                    lastIndex = id
                }
                R.id.rbtn_reader_backdrop_second -> {
                    changePageBackgroundWrapper(52)
                    if (id != lastIndex) {
                        val data = java.util.HashMap<String, String>()
                        data.put("type", "2")
                        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.BACKGROUNDCOLOR, data)
                    }
                    lastIndex = id
                }
                R.id.rbtn_reader_backdrop_third -> {
                    changePageBackgroundWrapper(53)
                    if (id != lastIndex) {
                        val data = java.util.HashMap<String, String>()
                        data.put("type", "3")
                        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.BACKGROUNDCOLOR, data)
                    }
                    lastIndex = id
                }
                R.id.rbtn_reader_backdrop_fourth -> {
                    changePageBackgroundWrapper(54)
                    if (id != lastIndex) {
                        val data = java.util.HashMap<String, String>()
                        data.put("type", "4")
                        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.BACKGROUNDCOLOR, data)
                    }
                    lastIndex = id
                }
                R.id.rbtn_reader_backdrop_fifth -> {
                    changePageBackgroundWrapper(55)
                    if (id != lastIndex) {

                        val data = java.util.HashMap<String, String>()
                        data.put("type", "6")
                        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.BACKGROUNDCOLOR, data)
                    }
                    lastIndex = id
                }
                R.id.rbtn_reader_backdrop_sixth -> {
                    changePageBackgroundWrapper(56)
                    if (id != lastIndex) {

                        val data = java.util.HashMap<String, String>()
                        data.put("type", "5")
                        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.BACKGROUNDCOLOR, data)
                    }
                    lastIndex = id
                }
            }

        }

        rg_reader_spacing_group?.setOnCheckedChangeListener { id ->
            when (id) {
                R.id.rbtn_reader_spacing_0_2 -> {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_hangju_01)
                    val data = java.util.HashMap<String, String>()
                    data.put("type", "4")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.READGAP, data)
                    if (rbtn_reader_spacing_0_2!!.isChecked) {
                        readerSettings.readInterlineaSpace = 0.2f
                        setInterLinearSpaceMode()
                    }
                }
                R.id.rbtn_reader_spacing_0_5 -> {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_hangju_02)
                    val data = java.util.HashMap<String, String>()
                    data.put("type", "3")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.READGAP, data)
                    if (rbtn_reader_spacing_0_5!!.isChecked) {
                        readerSettings.readInterlineaSpace = 0.3f
                        setInterLinearSpaceMode()
                    }
                }
                R.id.rbtn_reader_spacing_1_0 -> {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_hangju_03)
                    val data = java.util.HashMap<String, String>()
                    data.put("type", "2")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.READGAP, data)
                    if (rbtn_reader_spacing_1_0!!.isChecked) {
                        readerSettings.readInterlineaSpace = 0.4f
                        setInterLinearSpaceMode()
                    }
                }
                R.id.rbtn_reader_spacing_1_5 -> {
                    StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_hangju_04)
                    val data = java.util.HashMap<String, String>()
                    data.put("type", "1")
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.READGAP, data)
                    if (rbtn_reader_spacing_1_5!!.isChecked) {
                        readerSettings.readInterlineaSpace = 0.5f
                        setInterLinearSpaceMode()
                    }
                }
            }
        }

        rg_reader_animation_group?.setOnCheckedChangeListener(this)

        img_reader_font_reduce?.preventClickShake(this)

        img_reader_font_increase?.preventClickShake(this)


        skbar_reader_brightness_change?.setOnSeekBarChangeListener(this)

        ckb_reader_brightness_system?.preventClickShake(this)

        ckb_reader_landscape?.preventClickShake(this)

        ckb_reader_auto_read?.preventClickShake(this)
        ckb_reader_full_screen?.preventClickShake(this)

        img_jump_back.preventClickShake(this)

    }

    override fun onClick(v: View) {
        if (!ReaderStatus.isMenuShow) {
            return
        }
        when (v.id) {

            R.id.img_reader_chapter_previous -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_previous_chapter)
                //dismissNovelHintLayout();
                val position = Position(ReaderStatus.book.book_id, ReaderStatus.position.group - 1, 0)
                EventBus.getDefault().post(EventReaderConfig(ReaderSettings.ConfigType.CHAPTER_REFRESH, position))
                changeChapter(position.group)
                presenter?.jumpNextChapterLog(1)
            }
            R.id.img_reader_chapter_next -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_next_chapter)
                //dismissNovelHintLayout();
                val position = Position(ReaderStatus.book.book_id, ReaderStatus.position.group + 1, 0)
                EventBus.getDefault().post(EventReaderConfig(ReaderSettings.ConfigType.CHAPTER_REFRESH, position))
                changeChapter(position.group)
                presenter?.jumpNextChapterLog(2)
            }
            R.id.rl_reader_catalog -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_catalog_btn)
                EventBus.getDefault().post(EventSetting(EventSetting.Type.OPEN_CATALOG))
                presenter?.readCatalogLog()
            }
            R.id.rl_reader_setting -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_setting_btn)
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.SET)
                changeBottomSettingView(SETTING_DETAIL)
            }
            R.id.rl_reader_night//夜间模式
            -> {
                if (ThemeHelper.getInstance(context).isNight) {
                    txt_reader_night.text = "夜间"
                    img_reader_night.isSelected = false
                } else {
                    txt_reader_night.text = "白天"
                    img_reader_night.isSelected = true
                }
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_night_mode)
                presenter?.chageNightMode()
            }
            R.id.img_reader_font_reduce// 减小字号
            -> {
                if (ReaderStatus.position.group < 0) {
                    return
                }
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_font_size_smaller)
                decreaseFont()
            }
            R.id.img_reader_font_increase// 加大字号
            -> {
                if (ReaderStatus.position.group < 0) {
                    return
                }
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_font_size_bigger)
                increaseFont()
            }
        /*case R.id.ll_reader_brightness_system:// 省电模式
            case R.id.read_setting_save_power:

                changeSavePowerMode();
                break;*/
            R.id.ll_reader_brightness_system, R.id.ckb_reader_brightness_system// 跟随系统 更改按钮背景
            -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_ld_with_system)
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.SYSFOLLOW)
                changeSystemLight()
            }
            R.id.ckb_reader_landscape -> {
                EventBus.getDefault().post(EventSetting(EventSetting.Type.CHANGE_SCREEN_MODE))
            }
            R.id.ckb_reader_auto_read -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_auto_read_btn)
                val data = java.util.HashMap<String, String>()
                if (Constants.isSlideUp) {
                    data.put("type", "2")
                } else {
                    data.put("type", "1")
                }
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.AUTOREAD, data)

                readerSettings.isAutoReading = true
                EventBus.getDefault().post(EventSetting(EventSetting.Type.MENU_STATE_CHANGE, false))
            }
            R.id.ckb_reader_full_screen -> {

                readerSettings.isFullScreenRead = true

                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_fullscreen_read_btn)
                readerSettings.isFullScreenRead = ckb_reader_full_screen.isChecked
                val data = java.util.HashMap<String, String>()
                if (readerSettings.isFullScreenRead) {
                    data.put("type", "1")
                } else {
                    data.put("type", "2")
                }
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.FULLSCREENPAGEREAD, data)
            }
            R.id.img_jump_back -> {

                val position = formatToPosition(lastProgress)
                if (position.group == ReaderStatus.position.group) { // 本章不跳
                    return
                }

                anim?.cancel()
                rl_jump_back.alpha = 1F
                anim = rl_jump_back.animate()
                anim?.alpha(0F)
                anim?.duration = 2000
                anim?.startDelay = 2000
                anim?.start()

                jumpChapter(lastProgress)
                if (lastProgress < 1 || lastProgress < 1) {
                    skbar_reader_chapter_change?.progress = 0
                } else {
                    skbar_reader_chapter_change?.progress = lastProgress
                }
                showChapterInfo(lastProgress)

                StartLogClickUtil.upLoadEventLog(context.applicationContext,
                        StartLogClickUtil.READPAGE_PAGE, StartLogClickUtil.PROGRESSCANCLE)

            }
            else -> {
            }
        }
    }

    private fun refreshJumpPreBtnState(sequence: Int) {
        if (sequence <= 0) {
            img_reader_chapter_previous.isClickable = false
            img_reader_chapter_previous.isEnabled = false
            img_reader_chapter_previous.alpha = 0.3f
        } else {
            img_reader_chapter_previous.isClickable = true
            img_reader_chapter_previous.isEnabled = true
            img_reader_chapter_previous.alpha = 1f
        }

        if (sequence == ReaderStatus.chapterList.size - 1) {
            img_reader_chapter_next.isClickable = false
            img_reader_chapter_next.isEnabled = false
            img_reader_chapter_next.alpha = 0.3f
        } else {
            img_reader_chapter_next.isClickable = true
            img_reader_chapter_next.isEnabled = true
            img_reader_chapter_next.alpha = 1f
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
                img_reader_font_increase?.isEnabled = true
            }
            readerSettings.fontSize -= 2
            if (readerSettings.fontSize <= 10) {
                img_reader_font_reduce?.isEnabled = false
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
                img_reader_font_reduce?.isEnabled = true
            }
            readerSettings.fontSize += 2
            if (readerSettings.fontSize >= 30) {
                img_reader_font_increase?.isEnabled = false
            }

            setFontSize()
        }
        val data = java.util.HashMap<String, String>()
        data.put("type", "1")
        data.put("FONT", readerSettings.fontSize.toString())
        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.WORDSIZE, data)
    }

    fun setFontSize() {
        if (txt_reader_font_size != null) {
            txt_reader_font_size?.text = readerSettings.fontSize.toString()
        }
    }

    fun changeChapter(sequence: Int) {
        if (skbar_reader_chapter_change != null && skbar_reader_chapter_change!!.isShown && ReaderStatus.chapterCount - 1 != 0) {
            val index = Math.max(ReaderStatus.position.group, 0)
            skbar_reader_chapter_change?.progress = index
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
            51 -> rg_reader_backdrop_group?.check(R.id.rbtn_reader_backdrop_first)
            52 -> rg_reader_backdrop_group?.check(R.id.rbtn_reader_backdrop_second)
            53 -> rg_reader_backdrop_group?.check(R.id.rbtn_reader_backdrop_third)
            54 -> rg_reader_backdrop_group?.check(R.id.rbtn_reader_backdrop_fourth)
            55 -> rg_reader_backdrop_group?.check(R.id.rbtn_reader_backdrop_fifth)
            56 -> rg_reader_backdrop_group?.check(R.id.rbtn_reader_backdrop_sixth)
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
                skbar_reader_brightness_change?.progress = screenBrightness
                setScreenBrightness(screenBrightness)
            } else {
                skbar_reader_brightness_change?.progress = 5
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
    }

    private fun setBrightBtn() {
        ckb_reader_brightness_system?.isChecked = readerSettings.isAutoBrightness
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        when (checkedId) {
            R.id.rbtn_reader_animation_slide -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_flip_page_01)
                val data = java.util.HashMap<String, String>()
                data.put("type", "1")
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.PAGETURN, data)
                changePageMode(0)
                resetBtn(false)
            }
            R.id.rbtn_reader_animation_simulation -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_flip_page_02)
                val data = java.util.HashMap<String, String>()
                data.put("type", "3")
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.PAGETURN, data)
                changePageMode(1)
                resetBtn(false)
            }
            R.id.rbtn_reader_animation_translation -> {
                StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_flip_page_03)
                val data = java.util.HashMap<String, String>()
                data.put("type", "2")
                StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.PAGETURN, data)
                changePageMode(2)
                resetBtn(false)
            }
            R.id.rbtn_reader_animation_up_down -> {
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
                rg_reader_spacing_group?.clearCheck()
            }
        } else {
            isCustomReadingSpace = true
            rg_reader_spacing_group?.clearCheck()
        }
    }

    // 单选切换行间距
    private fun switchSpaceState() {
        if (readerSettings.readInterlineaSpace == 0.2f) {
            rg_reader_spacing_group?.check(R.id.rbtn_reader_spacing_0_2)
        } else if (readerSettings.readInterlineaSpace == 0.3f) {
            rg_reader_spacing_group?.check(R.id.rbtn_reader_spacing_0_5)
        } else if (readerSettings.readInterlineaSpace == 0.4f) {
            rg_reader_spacing_group?.check(R.id.rbtn_reader_spacing_1_0)
        } else if (readerSettings.readInterlineaSpace == 0.5f) {
            rg_reader_spacing_group?.check(R.id.rbtn_reader_spacing_1_5)
        }
    }

    @Volatile
    var anim: ViewPropertyAnimator? = null

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser && seekBar.id == R.id.skbar_reader_chapter_change) {
            rl_jump_back.visibility = View.VISIBLE
            anim?.cancel()
            rl_jump_back.alpha = 1F
//            val resizeProgress = progress.times(ReaderStatus.chapterList.size).div(100)
            if (!ReaderStatus.chapterList.isEmpty()
                    && progress <= ReaderStatus.chapterList.size && progress >= 0) {
                img_reader_chapter_previous.isClickable = true
                img_reader_chapter_previous.isEnabled = true
                img_reader_chapter_previous.alpha = 1f
//                ReaderStatus.novel_progress = resizeProgress
                changeBottomSettingView(SETTING_OPTION)
                showChapterInfo(progress)
            }
//            ReaderStatus.position.group = progress
//            refreshJumpPreBtnState()
        } else if (fromUser && seekBar.id == R.id.skbar_reader_brightness_change) {
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

    private fun showChapterInfo(progress: Int) {
        if (progress == 0) {
            txt_cur_chapter_name.text = ReaderStatus.chapterList[progress].name
            txt_chapter_percent.text = "1%"
        } else if (progress == ReaderStatus.chapterList.size - 1) {
            txt_cur_chapter_name.text = ReaderStatus.chapterList[ReaderStatus.chapterList.size - 1].name
            val percent = "100%"
            txt_chapter_percent.text = percent
        } else {
            txt_cur_chapter_name.text = ReaderStatus.chapterList[progress - 1].name
            val percent = ((progress * 100 / ReaderStatus.chapterList.size * 100) / 100).toString() + "%"
            txt_chapter_percent.text = percent
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        lastProgress = if (lastProgress == 0) { // menu 重新 show 的时候，seekBar.progress 比正确值小 1
            seekBar.progress + 1
        } else {
            seekBar.progress
        }
    }


    override fun onStopTrackingTouch(seekBar: SeekBar) {
        val numFormat = NumberFormat.getNumberInstance()
        numFormat.maximumFractionDigits = 2
        if (seekBar.id == R.id.skbar_reader_chapter_change) {
            jumpChapter(seekBar.progress)
        } else if (seekBar.id == R.id.skbar_reader_brightness_change) {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.rb_click_ld_progress)

            readerSettings.screenBrightness = Math.max(20, seekBar.progress)

            val data = java.util.HashMap<String, String>()
            data.put("lightvalue", seekBar.progress.toString())
            StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.LIGHTEDIT, data)

        }
    }

    private fun jumpChapter(progress: Int) {
        val position = formatToPosition(progress)
        if (position.group == ReaderStatus.position.group) {// 本章不跳
            return
        }
        EventBus.getDefault().post(EventReaderConfig(ReaderSettings.ConfigType.CHAPTER_REFRESH, position))
        refreshJumpPreBtnState(position.group)
    }

    private fun formatToPosition(progress: Int): Position {
        val resizeProgress = when (progress) {
            0 -> 0
            ReaderStatus.chapterList.size - 1 -> ReaderStatus.chapterList.size - 1
            else -> progress - 1
        }
        AppLog.e("progress2", resizeProgress.toString())
        return Position(ReaderStatus.book.book_id, resizeProgress, 0)
    }


    fun recycleResource() {

        this.detachAllViewsFromParent()

        if (rg_reader_backdrop_group != null) {
            rg_reader_backdrop_group?.removeAllViews()
        }

        System.gc()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val totalWidth = rg_reader_backdrop_group.width
        val width = rbtn_reader_backdrop_first.width
        loge("totalWidth: $totalWidth  width:$width")
        val margin = (totalWidth - (width * 6)) / 5
        rbtn_reader_backdrop_first.setMarginRight(margin)
        rbtn_reader_backdrop_second.setMarginRight(margin)
        rbtn_reader_backdrop_third.setMarginRight(margin)
        rbtn_reader_backdrop_fourth.setMarginRight(margin)
        rbtn_reader_backdrop_fifth.setMarginRight(margin)
        rbtn_reader_backdrop_sixth.setMarginRight(margin)
    }

    private fun View.setMarginRight(rightMargin: Int) {
        val params = layoutParams
        if (params is MarginLayoutParams) {
            if(params.rightMargin != rightMargin) {
                params.rightMargin = rightMargin
                requestLayout()
            }
        }
    }

    companion object {
        private val SETTING_OPTION = 1
        private val SETTING_DETAIL = SETTING_OPTION + 1
    }
}