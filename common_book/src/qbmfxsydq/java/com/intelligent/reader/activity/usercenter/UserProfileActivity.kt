package com.intelligent.reader.activity.usercenter

import android.os.Bundle
import com.intelligent.reader.R
import iyouqu.theme.BaseCacheableActivity

/**
 * Date: 2018/7/27 18:10
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 个人中心
 */
class UserProfileActivity : BaseCacheableActivity() {
    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        setContentView(R.layout.act_user_profile)
    }
}