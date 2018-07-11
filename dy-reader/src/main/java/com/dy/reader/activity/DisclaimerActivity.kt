package com.dy.reader.activity

import android.os.Bundle
import android.os.SystemClock
import com.alibaba.android.arouter.facade.annotation.Route
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dy.reader.R
import kotlinx.android.synthetic.main.act_disclaimer.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import java.util.HashMap

/**
 * Function：使用协议 / 转码声明
 *
 * Created by JoannChen on 2018/7/11 0011 17:25
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
@Route(path = RouterConfig.DISCLAIMER_ACTIVITY)
class DisclaimerActivity : iyouqu.theme.FrameActivity() {

    private var isFromReading = false

    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        setContentView(R.layout.act_disclaimer)

        if (intent != null) {

            isFromReading = intent.getBooleanExtra("isFromReading", false)

            if (isFromReading) {
                txt_title.text = resources.getString(R.string.translate_code)
                txt_content.text = resources.getString(R.string.translate_code_description)
            }
        }

        img_back.setOnClickListener {
            val data = HashMap<String, String>()
            data["type"] = "1"
            StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PROCTCOL_PAGE, StartLogClickUtil.BACK, data)
            finish()
        }

        txt_content.setOnClickListener {
            displayEggs()
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
}