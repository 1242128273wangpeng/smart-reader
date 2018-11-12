package com.intelligent.reader.util

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.ding.basic.config.ParameterConfig
import com.intelligent.reader.R
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.antiShakeClick
import net.lzbook.kit.utils.logger.AppLog


/**
 * 类描述：男女选择帮助类
 * 创建人：Zach
 * 创建时间：2017/12/4 0004
 */
class GenderHelper(view: View) {

    private val mBoySelector: ImageView = view.findViewById(R.id.iv_boy_sel)
    private val mGirlSelector: ImageView = view.findViewById(R.id.iv_girl_sel)
    private val mBoyTxtSelector: View = view.findViewById(R.id.tv_boy_sel)
    private val mGirlTxtSelector: View = view.findViewById(R.id.tv_girl_sel)
    private val mBoySection: RelativeLayout = view.findViewById(R.id.rl_section_boy)
    private val mGirlSection: RelativeLayout = view.findViewById(R.id.rl_section_girl)
    private val mTxtLoading: TextView = view.findViewById(R.id.tv_loading)
    private val mTxtTitle: TextView = view.findViewById(R.id.tv_title)
    private val mTxtDesc: TextView = view.findViewById(R.id.tv_desc)
    private val mTxtStepIn: View = view.findViewById(R.id.tv_step_in)
    private val mView = view
    lateinit var mGenderSelectedListener: onGenderSelectedListener
    var defaultSharedPreferences: SharedPreferences

    /**
     *动画持续时间
     */
    private val mImgTranDuration = 700L
    private val mImgAlphaDuration = 300L
    private val mTxtTranDuration = 300L
    private val mTxtAlphaDuration = 1000L

    init {
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mView.context)
        initListener()
//        switchDecorate()
    }

    private fun initListener() {
        mBoySection.antiShakeClick {
            selectBoy()
        }
        mGirlSection.antiShakeClick {
            selectGirl()
        }

    }

    private fun selectBoy() {
        mBoyTxtSelector.isSelected = true
        mBoySelector.isSelected = true
        mGirlSelector.isSelected = false
        mGirlTxtSelector.isSelected = false
        ParameterConfig.GENDER_TYPE = ParameterConfig.GENDER_BOY
        showSelectBoyAni()

    }

    private fun selectGirl() {
        mBoyTxtSelector.isSelected = false
        mBoySelector.isSelected = false
        mGirlSelector.isSelected = true
        mGirlTxtSelector.isSelected = true
        ParameterConfig.GENDER_TYPE = ParameterConfig.GENDER_GIRL
        showSelectGirlAni()
    }

    private fun showSelectBoyAni() {
        setIconAnimation(mBoySection, mGirlSection)
    }

    private fun showSelectGirlAni() {
        setIconAnimation(mGirlSection, mBoySection)
    }

    /**
     * 选择跳过的交互动画
     */
    fun jumpAnimation() {
        vanishIconAnimation(mBoySection, mGirlSection)
    }

    /**
     * 图片的消失
     */
    private fun vanishIconAnimation(alpha1: View, alpha2: View) {
        val iconAniSet = AnimatorSet()
        val alphaAnimator1 = ObjectAnimator.ofFloat(alpha1, "alpha", 1f, 0.3f)
        val alphaAnimator2 = ObjectAnimator.ofFloat(alpha2, "alpha", 1f, 0.3f)
        iconAniSet.playTogether(alphaAnimator1, alphaAnimator2)
        iconAniSet.duration = mImgAlphaDuration
        iconAniSet.start()
        alpha1.isClickable = false
        alpha2.isClickable = false
        setTxtAnimation()
    }

    /**
     * 图片的动画
     */
    private fun setIconAnimation(tran: View, alpha: View) {
        val iconAniSet = AnimatorSet()
        val distance = (AppUtils.width - tran.measuredWidth) / 2 - tran.left
        AppLog.e("distacnce", distance.toString())
        val translateAnimator = ObjectAnimator.ofFloat(tran, "translationX", 0f, distance.toFloat())
        translateAnimator.duration = mImgTranDuration
        translateAnimator.interpolator = AccelerateDecelerateInterpolator()
        val alphaAnimator = ObjectAnimator.ofFloat(alpha, "alpha", 1f, 0f)
        alphaAnimator.duration = mImgAlphaDuration
        iconAniSet.playTogether(translateAnimator, alphaAnimator)
        iconAniSet.start()
        tran.isClickable = false
        alpha.isClickable = false
        setTxtAnimation()
    }

    /**
     * 文字的动画
     */
    private fun setTxtAnimation() {
        defaultSharedPreferences.edit().putInt("gender", ParameterConfig.GENDER_TYPE).apply()
        mTxtStepIn.visibility = View.INVISIBLE
        val txtAniSet = AnimatorSet()
        val titleAlphaAnimator = ObjectAnimator.ofFloat(mTxtTitle, "alpha", 1f, 0f)
        titleAlphaAnimator.duration = mTxtTranDuration
        val descAlphaAnimator = ObjectAnimator.ofFloat(mTxtDesc, "alpha", 1f, 0f)
        descAlphaAnimator.duration = mTxtTranDuration
        txtAniSet.playTogether(titleAlphaAnimator, descAlphaAnimator)
        txtAniSet.interpolator = DecelerateInterpolator()
        txtAniSet.start()
        mTxtLoading.visibility = View.VISIBLE
        val loadingAnimation = ObjectAnimator.ofFloat(mTxtLoading, "alpha", 0f, 1f)
        loadingAnimation.duration = mTxtAlphaDuration
        loadingAnimation.start()
        loadingAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                mGenderSelectedListener?.genderSelected()
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }

        })
    }

    /**
     * 男女频点击后的回调
     */
    fun setOnGenderSelectedListener(listen: onGenderSelectedListener) {
        mGenderSelectedListener = listen
    }

    interface onGenderSelectedListener {
        fun genderSelected()
    }

}
