package com.intelligent.reader.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import net.lzbook.kit.utils.user.UserManager
import net.lzbook.kit.utils.user.UserManagerV4

/**
 * Created by xian on 2017/6/20.
 */
class WXEntryActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("WXEntryActivity onCreate")
        UserManager.handleIntent(this.intent)
        UserManagerV4.handleIntent(this.intent)
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        println("WXEntryActivity onNewIntent")
        UserManager.handleIntent(intent)
        UserManagerV4.handleIntent(this.intent)
        finish()
    }
}
