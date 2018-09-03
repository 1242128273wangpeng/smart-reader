package net.lzbook.kit.user.bean

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Desc 用户名可编辑剩余天数
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/4/12 0012 15:59
 */
data class UserNameState(
        @SerializedName("left_days")
        val remainingDays: Int,

        @SerializedName("can_modify")
        val isCanBeModified: Int // 1 可编辑   0 不可编辑
): Serializable