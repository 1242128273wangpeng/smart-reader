package cn.txtzsydsq.reader.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import net.lzbook.kit.user.UserManager

/**
 * Created by xian on 2017/6/20.
 */
class WXEntryActivity:Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("WXEntryActivity onCreate")
        UserManager.handleIntent(this.intent)
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        println("WXEntryActivity onNewIntent")
        UserManager.handleIntent(intent)
        finish()
    }
}
