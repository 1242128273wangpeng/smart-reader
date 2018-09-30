package com.intelligent.reader.util

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.intelligent.reader.R
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.antiShakeClick
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import java.util.*

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
    private val mTxtStepIn: TextView = view.findViewById(R.id.tv_step_in)
    private val mLlGenderSection: LinearLayout = view.findViewById(R.id.ll_section_icon)
    lateinit var mGenderSelectedListener :onGenderSelectedListener

    /*
     *动画持续时间
     * */
    val mImgTranDuration = 700L
    val mImgAlphaDuration = 300L
    val mTxtTranDuration = 300L
    val mTxtAlphaDuration = 1000L

    init {
        initListener()
    }

    private fun initListener() {
        mBoySection.antiShakeClick( View.OnClickListener {
            val data = HashMap<String, String>()
            data.put("type", "1")
            StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(), StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.PREFERENCE, data)
            selectBoy()
        })
        mGirlSection.antiShakeClick(View.OnClickListener {
            val data = HashMap<String, String>()
            data.put("type", "2")
            StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(), StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.PREFERENCE, data)
            selectGirl()
        })

    }


    private fun selectBoy() {
        mBoyTxtSelector.isSelected = true
        mBoySelector.isSelected = true
        mGirlSelector.isSelected = false
        mGirlTxtSelector.isSelected = false
        Constants.SGENDER = Constants.SBOY
        showSelectBoyAni()
    }

    private fun selectGirl() {
        mBoyTxtSelector.isSelected = false
        mBoySelector.isSelected = false
        mGirlSelector.isSelected = true
        mGirlTxtSelector.isSelected = true
        Constants.SGENDER = Constants.SGIRL
        showSelectGirlAni()
    }

    /**
     * 选择跳过的交互动画
     */
    fun jumpAnimation(){
        vanishIconAnimation(mBoySection,mGirlSection)
    }

    private fun showSelectBoyAni() {
        setIconAnimation(mBoySection,mGirlSection)
    }

    private fun showSelectGirlAni() {
        setIconAnimation(mGirlSection,mBoySection)
    }

    /**
     * 图片的动画（上下移动）
     */
    private fun setIconAnimation(tran: View , alpha:View){
        tran.isClickable = false
        alpha.isClickable = false
        alpha.alpha = 0.4f
        setTxtAnimation()
    }

    /**
     * 图片的消失
     */
     fun vanishIconAnimation(alpha1: View , alpha2:View){
        setTxtAnimation()
    }

    /**
     * 文字的动画
     */
    fun setTxtAnimation(){
        SPUtils.putDefaultSharedInt(SPKey.GENDER_TAG, Constants.SGENDER)
        mTxtStepIn.visibility = View.INVISIBLE
        mTxtDesc.visibility = View.INVISIBLE
        val txtAniSet = AnimatorSet()
        val titleAlphaAnimator = ObjectAnimator.ofFloat(mTxtTitle, "alpha", 1f, 0f)
        titleAlphaAnimator.duration = mTxtTranDuration
        val descAlphaAnimator = ObjectAnimator.ofFloat(mTxtDesc, "alpha", 1f, 0f)
        descAlphaAnimator.duration = mTxtTranDuration
        txtAniSet.playTogether(titleAlphaAnimator, descAlphaAnimator)
        txtAniSet.interpolator= DecelerateInterpolator()
        txtAniSet.start()
        mTxtLoading.visibility=View.VISIBLE
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
    fun setOnGenderSelectedListener(listen: onGenderSelectedListener){
        mGenderSelectedListener = listen
    }

    interface onGenderSelectedListener{
        fun genderSelected()
    }

}
