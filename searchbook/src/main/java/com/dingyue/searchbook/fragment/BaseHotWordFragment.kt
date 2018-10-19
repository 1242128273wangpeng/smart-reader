package com.dingyue.searchbook.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dingyue.searchbook.R
import com.dingyue.searchbook.adapter.HotWordAdapter
import com.dingyue.searchbook.interfaces.OnResultListener
import com.dingyue.searchbook.presenter.HotWordPresenter
import com.dingyue.searchbook.view.IHotWordView
import kotlinx.android.synthetic.main.fragment_base_hotword.*
import kotlinx.android.synthetic.main.fragment_base_hotword.view.*
import net.lzbook.kit.ui.widget.LoadingPage


/**
 * Desc：搜索热词Fragment父类
 *
 * 【换一换功能】：
 * 阅微替、五步替、智胜电子书替、新壳4
 *
 * 【热词列表使用瀑布流】：
 * 五步替、智胜电子书替
 *
 * 【不展示推荐功能】：
 * 今日多看、全本追书、新壳1
 *
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/10/19 0019 15:20
 */
abstract class BaseHotWordFragment : Fragment(), IHotWordView {

    abstract fun setLayout(): Int

    open fun initView() {} // 子类需要时重写

    private var loadingPage: LoadingPage? = null

    var hotWordAdapter: HotWordAdapter? = null

    var onResultListener: OnResultListener<String>? = null

    val hotWordPresenter: HotWordPresenter by lazy {
        HotWordPresenter(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_base_hotword, container, false)
        val hotWordView = LayoutInflater.from(requireContext()).inflate(setLayout(), null, false)
        view.search_result_main.addView(hotWordView)
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hotWordPresenter.onCreate()
        hotWordPresenter.loadHotWordData()
        hotWordPresenter.loadRecommendData()

        initView()

    }

    override fun showLoading() {
        hideLoading()
        loadingPage = LoadingPage(requireActivity(), search_result_main, LoadingPage.setting_result)
    }

    override fun hideLoading() {
        loadingPage?.onSuccessGone()
    }

    override fun onDestroy() {
        super.onDestroy()
        hotWordPresenter.onDestroy()
    }
}