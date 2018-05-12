package com.intelligent.reader.presenter.home

import android.content.pm.PackageManager
import android.preference.PreferenceManager
import com.intelligent.reader.app.BookApplication
import com.dingyue.contract.IPresenter
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
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(BookApplication.getGlobalContext())

        //初始化阅读页背景
        if (sharedPreferences.getInt("content_mode", 51) < 50) {
            Constants.MODE = 51
            ReadConfig.MODE = 51
            sharedPreferences.edit().putInt("content_mode", Constants.MODE).apply()
            sharedPreferences.edit().putInt("current_light_mode", Constants.MODE).apply()
        } else {
            Constants.MODE = sharedPreferences.getInt("content_mode", 51)
            ReadConfig.MODE = sharedPreferences.getInt("content_mode", 51)
        }

        val firstTime = sharedPreferences.getLong(Constants.TODAY_FIRST_OPEN_APP, 0)
        val currentTime = System.currentTimeMillis()

        //判断用户是否是当日首次打开应用
        val result = AppUtils.isToday(firstTime, currentTime)

        if (result) {
            Constants.is_user_today_first = false
        } else {
            //用户首次打开，记录当前时间
            Constants.is_user_today_first = true
            sharedPreferences.edit().putLong(Constants.TODAY_FIRST_OPEN_APP, currentTime).apply()
            sharedPreferences.edit().putBoolean(Constants.IS_UPLOAD, false).apply()
            updateApplicationList()
        }

        Constants.upload_userinformation = sharedPreferences.getBoolean(Constants.IS_UPLOAD, false)

        loadDataManager = LoadDataManager(BookApplication.getGlobalContext())

        CheckNovelUpdHelper.delLocalNotify(BookApplication.getGlobalContext())

        val deleteBookHelper = DeleteBookHelper(BookApplication.getGlobalContext())
        deleteBookHelper.startPendingService()
    }


    /***
     * 初始化下载服务
     * **/
    fun initDownloadService() {
        CacheManager.checkService()
    }

    /***
     * 更新书架信息，由于服务器端缓存的问题，可能造成默认接口添加的书籍，书籍状态不正确
     * **/
    fun updateBookShelf() {
        if (loadDataManager != null) {
            loadDataManager!!.updateShelfBooks()
        }
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