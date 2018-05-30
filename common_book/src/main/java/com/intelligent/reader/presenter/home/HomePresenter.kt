package com.intelligent.reader.presenter.home

import android.content.pm.PackageManager
import android.preference.PreferenceManager
import com.intelligent.reader.app.BookApplication
import com.dingyue.contract.IPresenter
import com.dingyue.contract.util.SharedPreUtil
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.ReadConfig
import net.lzbook.kit.utils.*

/**
 * Desc HomeActivity - presenter
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/2/28 0028 11:12
 */
class HomePresenter(override var view: HomeView?, var packageManager: PackageManager) : IPresenter<HomeView> {

    private var loadDataManager: LoadDataManager? = null

    /***
     * 初始化参数
     * **/
    fun initParameters() {
        val sharePreUtil = SharedPreUtil(SharedPreUtil.SHARE_DEFAULT)
//        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(BookApplication.getGlobalContext())

        //初始化阅读页背景
        if (sharePreUtil.getInt(SharedPreUtil.CONTENT_MODE) < 50) {
            Constants.MODE = 51
            ReadConfig.MODE = 51
            sharePreUtil.putInt(SharedPreUtil.CONTENT_MODE, Constants.MODE)
            sharePreUtil.putInt(SharedPreUtil.CURRENT_NIGHT_MODE, Constants.MODE)
        } else {
            Constants.MODE = sharePreUtil.getInt(SharedPreUtil.CONTENT_MODE)
            ReadConfig.MODE = sharePreUtil.getInt(SharedPreUtil.CONTENT_MODE)
        }

        val firstTime = sharePreUtil.getLong(SharedPreUtil.HOME_TODAY_FIRST_OPEN_APP)
        val currentTime = System.currentTimeMillis()

        //判断用户是否是当日首次打开应用
        val result = AppUtils.isToday(firstTime, currentTime)

        if (result) {
            Constants.is_user_today_first = false
        } else {
            //用户首次打开，记录当前时间
            Constants.is_user_today_first = true
            sharePreUtil.putLong(SharedPreUtil.HOME_TODAY_FIRST_OPEN_APP, currentTime)
            sharePreUtil.putBoolean(SharedPreUtil.HOME_IS_UPLOAD, false)
            updateApplicationList()
        }

        Constants.upload_userinformation = sharePreUtil.getBoolean(SharedPreUtil.HOME_IS_UPLOAD)

        loadDataManager = LoadDataManager(BookApplication.getGlobalContext())

        CheckNovelUpdHelper.delLocalNotify(BookApplication.getGlobalContext())

        val deleteBookHelper = DeleteBookHelper(BookApplication.getGlobalContext())
        deleteBookHelper.startPendingService()
        val premVersionCode = Constants.preVersionCode
        val currentVersionCode = AppUtils.getVersionCode()

        if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
            //
            if (!Constants.upload_userinformation || premVersionCode != currentVersionCode) {
                // 获取用户基础数据
                StartLogClickUtil.sendZnUserLog()
                Constants.upload_userinformation = true
                Constants.preVersionCode = currentVersionCode
                sharePreUtil.putBoolean(Constants.IS_UPLOAD, Constants.upload_userinformation)
            }
        }

    }


    /***
     * 初始化下载服务
     * **/
    fun initDownloadService() {
        CacheManager.checkService()
    }

    /***
     * 上传用户应用列表
     * **/
    private fun updateApplicationList() {
        Observable.create(ObservableOnSubscribe<String> { emitter ->
            emitter.onNext(AppUtils.scanLocalInstallAppList(packageManager))
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = { message ->
                    StartLogClickUtil.upLoadApps(message)
                })
    }
}