package net.lzbook.kit.presenter

import android.content.pm.PackageManager
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.Book
import com.ding.basic.bean.CoverCheckItem
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.bean.ReadConfig
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.presenter.base.IPresenter
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.NetWorkUtils
import net.lzbook.kit.utils.book.CheckNovelUpdHelper
import net.lzbook.kit.utils.book.DeleteBookHelper
import net.lzbook.kit.utils.book.LoadDataManager
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.view.HomeView
import okhttp3.MediaType
import okhttp3.RequestBody

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

        //初始化阅读页背景
        if (SPUtils.getDefaultSharedInt(SPKey.CONTENT_MODE) < 50) {
            Constants.MODE = 51
            ReadConfig.MODE = 51
            SPUtils.putDefaultSharedInt(SPKey.CONTENT_MODE, Constants.MODE)
            SPUtils.putDefaultSharedInt(SPKey.CURRENT_NIGHT_MODE, Constants.MODE)
        } else {
            Constants.MODE = SPUtils.getDefaultSharedInt(SPKey.CONTENT_MODE)
            ReadConfig.MODE = SPUtils.getDefaultSharedInt(SPKey.CONTENT_MODE)
        }

        val firstTime = SPUtils.getDefaultSharedLong(SPKey.HOME_TODAY_FIRST_OPEN_APP)
        val currentTime = System.currentTimeMillis()

        //判断用户是否是当日首次打开应用
        val result = AppUtils.isToday(firstTime, currentTime)

        if (result) {
            Constants.is_user_today_first = false
        } else {
            //用户首次打开，记录当前时间
            Constants.is_user_today_first = true
            SPUtils.putDefaultSharedLong(SPKey.HOME_TODAY_FIRST_OPEN_APP, currentTime)
            SPUtils.putDefaultSharedBoolean(SPKey.HOME_IS_UPLOAD, false)
            updateApplicationList()
            updateCoverBatch()
        }

        Constants.upload_userinformation = SPUtils.getDefaultSharedBoolean(SPKey.HOME_IS_UPLOAD)

        loadDataManager = LoadDataManager(BaseBookApplication.getGlobalContext())

        CheckNovelUpdHelper.delLocalNotify(BaseBookApplication.getGlobalContext())

        val deleteBookHelper = DeleteBookHelper(BaseBookApplication.getGlobalContext())
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
                SPUtils.putDefaultSharedBoolean(Constants.IS_UPLOAD, Constants.upload_userinformation)
            }
        }

    }

    /**
     * 每日一次 书籍信息批量检查
     */
    fun updateCoverBatch() {

        val books = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBooks()

        if (books != null) {
            val checkBody = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    loadUpdateParameters(books))

            //部分4.2 手机报 retrofit 动态代理问题 java.lang.reflect.UndeclaredThrowableException at $Proxy2.a(Native Method)
            try {
                RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestCoverBatch(checkBody)
            } catch (e: Exception) {
            }
        }
    }

    private fun loadUpdateParameters(books: List<Book>): String {
        val checkLists = ArrayList<CoverCheckItem>()

        var checkItem: CoverCheckItem

        for (i in books.indices) {
            val book = books[i]
            checkItem = CoverCheckItem()
            checkItem.book_id = book.book_id
            checkItem.book_source_id = book.book_source_id
            checkItem.book_chapter_id = book.book_chapter_id
            checkLists.add(checkItem)
        }

        return Gson().toJson(checkLists)
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
        Observable.create(ObservableOnSubscribe<List<String>> { emitter ->
            emitter.onNext(mutableListOf(AppUtils.scanLocalInstallAppList(packageManager), AppUtils.loadUserApplicationList(BaseBookApplication.getGlobalContext(), packageManager)))
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    StartLogClickUtil.upLoadApps(it[0], it[1])
                })
    }

}