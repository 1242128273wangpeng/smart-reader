package com.dy.reader.activity

import android.os.Bundle

import com.alibaba.android.arouter.facade.annotation.Route
import com.dingyue.contract.router.RouterConfig
import com.dy.reader.R

import kotlinx.android.synthetic.qbmfrmxs.act_disclaimer.*

import net.lzbook.kit.appender_loghub.StartLogClickUtil

import java.util.HashMap

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
            StartLogClickUtil.upLoadEventLog(this@DisclaimerActivity, StartLogClickUtil.PROCTCOL_PAGE, StartLogClickUtil.BACK, data)
            finish()
        }
    }
}
