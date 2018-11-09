package com.intelligent.reader.util

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.intelligent.reader.R
import android.animation.AnimatorSet
import android.view.animation.DecelerateInterpolator
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils

import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.utils.antiShakeClick
import java.util.HashMap

/**
 * 类描述：男女选择帮助类
 * 创建人：Zach
 * 创建时间：2017/12/4 0004
 */

class GenderHelper(view: View) {

    private val img_gender_boy: ImageView = view.findViewById(R.id.img_gender_boy)
    private val img_gender_girl: ImageView = view.findViewById(R.id.img_gender_girl)
    private val img_gender_boy_check: View = view.findViewById(R.id.img_gender_boy_check)
    private val img_gender_girl_check: View = view.findViewById(R.id.img_gender_girl_check)
    private val rl_gender_boy: RelativeLayout = view.findViewById(R.id.rl_gender_boy)
    private val rl_gender_girl: RelativeLayout = view.findViewById(R.id.rl_gender_girl)
    private val txt_gender_loading: TextView = view.findViewById(R.id.txt_gender_loading)
    private val txt_gender_prompt: TextView = view.findViewById(R.id.txt_gender_prompt)
    private val txt_gender_description: TextView = view.findViewById(R.id.txt_gender_description)
    private val txt_gender_skip: TextView = view.findViewById(R.id.txt_gender_skip)

    lateinit var genderSelectedListener :GenderSelectedListener


    val mImgTranDuration = 700L
    val mImgAlphaDuration = 300L
    val mTxtTranDuration = 300L
    val mTxtAlphaDuration = 1000L

    init {
        initListener()
    }

    private fun initListener() {
        rl_gender_boy.antiShakeClick( View.OnClickListener {
            val data = HashMap<String, String>()
            data["type"] = "1"
            StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(), StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.PREFERENCE, data)
            selectBoy()
        })

        rl_gender_girl.antiShakeClick(View.OnClickListener {
            val data = HashMap<String, String>()
            data["type"] = "2"
            StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(), StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.PREFERENCE, data)
            selectGirl()
        })
    }


    private fun selectBoy() {
        Constants.SGENDER = Constants.SBOY

        img_gender_boy_check.isSelected = true
        img_gender_boy.isSelected = true

        img_gender_girl.isSelected = false
        img_gender_girl_check.isSelected = false

        showSelectBoyAni()
    }

    private fun selectGirl() {
        Constants.SGENDER = Constants.SGIRL

        img_gender_girl_check.isSelected = true
        img_gender_girl.isSelected = true

        img_gender_boy.isSelected = false
        img_gender_boy_check.isSelected = false

        showSelectGirlAni()
    }

    /**
     * 选择跳过的交互动画
     */
    fun jumpAnimation(){
        vanishIconAnimation(rl_gender_boy,rl_gender_girl)
    }

    private fun showSelectBoyAni() {
        setIconAnimation(rl_gender_boy,rl_gender_girl)
    }

    private fun showSelectGirlAni() {
        setIconAnimation(rl_gender_girl,rl_gender_boy)
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
    private fun vanishIconAnimation(alpha1: View , alpha2:View){
        setTxtAnimation()
    }

    /**
     * 文字的动画
     */
    private fun setTxtAnimation(){
        SPUtils.putDefaultSharedInt(SPKey.GENDER_TAG, Constants.SGENDER)
        txt_gender_skip.visibility = View.INVISIBLE
        txt_gender_description.visibility = View.INVISIBLE
        val txtAniSet = AnimatorSet()
        val titleAlphaAnimator = ObjectAnimator.ofFloat(txt_gender_prompt, "alpha", 1f, 0f)
        titleAlphaAnimator.duration = mTxtTranDuration
        val descAlphaAnimator = ObjectAnimator.ofFloat(txt_gender_description, "alpha", 1f, 0f)
        descAlphaAnimator.duration = mTxtTranDuration
        txtAniSet.playTogether(titleAlphaAnimator, descAlphaAnimator)
        txtAniSet.interpolator= DecelerateInterpolator()
        txtAniSet.start()
        txt_gender_loading.visibility=View.VISIBLE
        val loadingAnimation = ObjectAnimator.ofFloat(txt_gender_loading, "alpha", 0f, 1f)
        loadingAnimation.duration = mTxtAlphaDuration
        loadingAnimation.start()
        loadingAnimation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                genderSelectedListener?.genderSelected()
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
    fun insertGenderSelectedListener(listen: GenderSelectedListener){
        genderSelectedListener = listen
    }

    interface GenderSelectedListener{
        fun genderSelected()
    }
}
