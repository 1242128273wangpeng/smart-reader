package net.lzbook.kit.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v4.app.NotificationManagerCompat
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.push.BannerInfo
import com.ding.basic.bean.push.PushInfo
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.dingyue.statistics.DyStatService
import com.umeng.message.PushAgent
import com.umeng.message.entity.UMessage
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import java.util.*

/**
 * Desc Push功能-扩展
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/8/30 17:08
 */
fun PushAgent.updateTags(context: Context, udid: String): Flowable<String> {
    loge("更新用户标签")
    val pushTagsFlowable = RequestRepositoryFactory.loadRequestRepositoryFactory(context)
            .requestPushTags(udid)
            .subscribeOn(Schedulers.io())

    val bannerInfoFlowable = RequestRepositoryFactory.loadRequestRepositoryFactory(context)
            .requestBannerInfo()

    return pushTagsFlowable
            .doOnNext { pushInfo ->
                val tags = pushInfo.tags ?: return@doOnNext

                if (!pushInfo.isFromCache) {
                    SPUtils.editDefaultShared {
                        SPUtils.putDefaultSharedObject(PushInfo.KEY, pushInfo)
                    }
                }

                addPushTags(tags.toTypedArray())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(onNext = { isAdd ->
                            loge("更新标签: $isAdd")
                        }, onError = {
                            loge("更新标签失败")
                            it.printStackTrace()
                        })
            }
            .zipWith(bannerInfoFlowable, BiFunction<PushInfo, BannerInfo, String> { pushInfo, bannerInfo ->
                if (bannerInfo.hasShowed) {
                    loge("此次活动弹窗已经展示过")
                    ""
                } else {
                    loge("zip")
                    var result = ""
                    val pushTags = pushInfo.tags
                    val bannerTags = bannerInfo.tags
                    val bannerImgUrl = bannerInfo.url
                    if (pushTags?.isNotEmpty() == true && bannerTags?.isNotEmpty() == true
                            && bannerImgUrl?.isNotEmpty() == true) {
                        bannerTags.retainAll(pushTags) // 求交集
                        if (bannerTags.isNotEmpty()) {
                            result = bannerImgUrl
                        }
                    }
                    loge("zip result: $result")
                    SPUtils.editDefaultShared {
                        SPUtils.putDefaultSharedObject(BannerInfo.KEY, bannerInfo)
                    }
                    result
                }
            })
}

private fun PushAgent.addPushTags(pushTags: Array<String>?): Flowable<Boolean> {
    return getUTags()
            .flatMap { uTags ->
                deleteUTags(uTags)
            }
            .flatMap { isDelete ->
                if (isDelete) {
                    addUTags(pushTags)
                } else {
                    Flowable.error(Throwable("删除用户旧标签失败"))
                }
            }

}

private fun PushAgent.getUTags(): Flowable<Array<String>> {
    return Flowable.create<Array<String>>({ emitter ->
        tagManager.getTags { isGet, allTags ->
            if (!isGet) {
                emitter.onError(Throwable("获取用户旧标签失败"))
            } else {
                loge("用户旧标签 $allTags, size: ${allTags.size}")
                emitter.onNext(allTags.toTypedArray())
                emitter.onComplete()
            }
        }
    }, BackpressureStrategy.BUFFER)
}

private fun PushAgent.deleteUTags(tags: Array<String>?): Flowable<Boolean> {
    return Flowable.create<Boolean>({ emitter ->
        if (tags?.isNotEmpty() == true && tags[0].isNotEmpty() == true) {
            tagManager.deleteTags({ isDelete, deleteResult ->
                loge("删除用户旧标签: $isDelete", "result: $deleteResult")
                emitter.onNext(isDelete)
                emitter.onComplete()
            }, tags)
        } else {
            loge("用户旧标签为空")
            emitter.onNext(true)
            emitter.onComplete()
        }
    }, BackpressureStrategy.BUFFER)
}

private fun PushAgent.addUTags(tags: Array<String>?): Flowable<Boolean> {
    return Flowable.create<Boolean>({ emitter ->
        if (tags?.isNotEmpty() == true) {
            tagManager.addTags({ isAdd, addResult ->
                loge("添加用户新标签: $isAdd", "addResult: $addResult")
                emitter.onNext(isAdd)
                emitter.onComplete()
            }, tags)
        } else {
            loge("用户新标签为空")
            emitter.onNext(true)
            emitter.onComplete()
        }
    }, BackpressureStrategy.BUFFER)
}

fun Activity.openPushSetting() {
    val intent = Intent()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
        intent.putExtra("app_package", packageName)
        intent.putExtra("app_uid", applicationInfo.uid)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra("android.provider.extra.APP_PACKAGE", packageName)
        }
    } else {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
        intent.data = Uri.fromParts("package", packageName, null)
    }
    try {
        startActivity(intent)
        DyStatService.onEvent(EventPoint.MAIN_POPUPSET)
    } catch (e: Exception) {
        //打开设置界面
        startActivity(Intent(Settings.ACTION_SETTINGS))
    }
}

fun Activity.isShouldShowPushSettingDialog(): Boolean {
    val isNotifyEnable = NotificationManagerCompat.from(this)
            .areNotificationsEnabled()
    if (isNotifyEnable) return false
    val shareKey = SPKey.PUSH_LATEST_SHOW_SETTING_DIALOG_TIME
    val latestShowTime = SPUtils.getDefaultSharedLong(shareKey, 0)
    val currentTime = System.currentTimeMillis()
    val time = currentTime - latestShowTime
    return if (time > 3 * 24 * 60 * 60 * 1000) {
        SPUtils.putDefaultSharedLong(shareKey, currentTime)
        true
    } else {
        false
    }
}

fun Context.openPushActivity(msg: UMessage) {
    val intent = Intent()
    intent.putPushExtra(msg)
    loge("umsg.activity: ${msg.activity}")
    intent.setClassName(this, msg.activity)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        startActivity(intent)
    } catch (e: Exception) {
        if (this is Activity) {
            RouterUtil.navigation(this, RouterConfig.HOME_ACTIVITY)
        }
    }
    uploadPushLog(msg)
}

private fun Context.uploadPushLog(msg: UMessage) {
    val data = HashMap<String, String>()
    if (msg.activity == "com.intelligent.reader.activity.CoverPageActivity") {
        // 封面页打点
        if (msg.extra?.containsKey("book_id") == true) {
            data["bookid"] = msg.extra["book_id"] ?: ""
        }
        data["source"] = "PUSH"
        DyStatService.onEvent(EventPoint.BOOOKDETAIL_ENTER, data)
    } else if (msg.activity == "com.intelligent.reader.activity.FindBookDetail") {
        //H5 页面打点
        data["source"] = "PUSH"
        DyStatService.onEvent(EventPoint.BOOKLIST_ENTER, data)
    }
}

private fun Intent.putPushExtra(msg: UMessage) {
    if (msg.extra != null) {
        val it = msg.extra.entries.iterator()

        while (it.hasNext()) {
            val entry = it.next() as MutableMap.MutableEntry<*, *>
            val key = entry.key as String
            val value = entry.value as String
            putExtra(key, value)
        }
    }
    putExtra(IS_FROM_PUSH, true)
}

@JvmField
val IS_FROM_PUSH = "is_from_push"

@JvmField
val EVENT_UPDATE_TAG = "event_update_tag"