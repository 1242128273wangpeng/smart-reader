package com.dy.reader.activity

import android.os.Bundle
import android.os.SystemClock
import com.alibaba.android.arouter.facade.annotation.Route
import com.baidu.mobstat.StatService
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dy.reader.R
import kotlinx.android.synthetic.main.act_disclaimer.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import java.util.*

/**
 * Function：使用协议 / 转码声明
 *
 * Created by JoannChen on 2018/7/11 0011 17:25
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
@Route(path = RouterConfig.DISCLAIMER_ACTIVITY)
class DisclaimerActivity : iyouqu.theme.FrameActivity() {


    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        setContentView(R.layout.act_disclaimer)
        // 使用协议
        txt_title.text = resources.getString(R.string.disclaimer_statement)
        txt_content.text = resources.getString(R.string.disclaimer_statement_description)

        // 阅读页转码声明
        val isFromReadingPage = intent.getBooleanExtra(RouterUtil.FROM_READING_PAGE, false)
        if (isFromReadingPage) {
            txt_title.text = resources.getString(R.string.translate_code)
            txt_content.text = resources.getString(R.string.translate_code_description)
        }


        // 登录页服务条款
        val isServicePolicy = intent.getBooleanExtra(RouterUtil.SERVICE_POLICY, false)
        if (isServicePolicy) {
            txt_title.text = resources.getString(R.string.login_service_policy)
            txt_content.text = resources.getString(R.string.service_policy_description)

        }

        // 登录页隐私条款
        val isPrivacyPolicy = intent.getBooleanExtra(RouterUtil.PRIVACY_POLICY, false)
        if (isPrivacyPolicy) {
            txt_title.text = resources.getString(R.string.login_privacy_policy)
            txt_content.text = resources.getString(R.string.privacy_policy_description)

        }


        img_back.setOnClickListener {
            val data = HashMap<String, String>()
            data["type"] = "1"
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PROCTCOL_PAGE, StartLogClickUtil.BACK, data)
            finish()
        }

        // 仅在使用协议页面进入可以打开调试模式
        val isFormDisclaimerPage = intent.getBooleanExtra(RouterUtil.FROM_DISCLAIMER_PAGE, false)
        if (isFormDisclaimerPage) {
            txt_content.setOnClickListener {
                displayEggs()
            }
        }

    }

    /**
     * 存放点击事件次数
     */
    private var mHits: LongArray? = null

    /**
     * 测试彩蛋
     */
    private fun displayEggs() {

        if (mHits == null) {
            mHits = LongArray(5) // 需要点击几次 就设置几
        }

        mHits?.let {
            //把从第二位至最后一位之间的数字复制到第一位至倒数第一位
            System.arraycopy(mHits, 1, mHits, 0, it.size - 1)

            //记录一个时间
            it[it.size - 1] = SystemClock.uptimeMillis()
            if (SystemClock.uptimeMillis() - it[0] <= 5000) {//5秒内连续点击。
                mHits = null    //这里说明一下，我们在进来以后需要还原状态，否则如果点击过快，第六次，第七次 都会不断进来触发该效果。重新开始计数即可
                RouterUtil.navigation(this, RouterConfig.DEBUG_ACTIVITY)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        StatService.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        StatService.onPause(this)
    }
}