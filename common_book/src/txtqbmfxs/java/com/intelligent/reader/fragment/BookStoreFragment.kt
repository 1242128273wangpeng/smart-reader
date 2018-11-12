package com.intelligent.reader.fragment


import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ding.basic.net.api.service.RequestService
import com.dingyue.statistics.DyStatService
import com.intelligent.reader.R
import kotlinx.android.synthetic.txtqbmfxs.frag_bookstore.*
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.loge
import net.lzbook.kit.utils.webview.UrlUtils
import java.util.*

/**
 * Function：书城
 *
 * Created by JoannChen on 2018/6/16 0016 10:38
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
open class BookStoreFragment : Fragment() {

    private var currentPosition: Int = 0

    private val recommendPage = 0
    private val rankingPage = 1
    private val bookListPage = 2
    private val categoryPage = 3


    private var searchClickListener: SearchClickListener? = null

    private var sp: SharedPreferences? = null

    private val recommendFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "recommend")
        val uri = RequestService.WEB_RECOMMEND.replace("{packageName}", AppUtils.getPackageName())
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }

    private val rankingFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "rank")
        val uri = RequestService.WEB_RANKING.replace("{packageName}", AppUtils.getPackageName())
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }

    private val bookListFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "booklist")
        val uri = RequestService.WEB_BOOKLIST.replace("{packageName}", AppUtils.getPackageName())
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }


    private val categoryFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "category")
        val uri = RequestService.WEB_CATEGORY.replace("{packageName}", AppUtils.getPackageName())
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sp = PreferenceManager.getDefaultSharedPreferences(BaseBookApplication.getGlobalContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_bookstore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bookStoreAdapter = BookStoreAdapter(childFragmentManager)

        vp_book_store_content?.adapter = bookStoreAdapter
        vp_book_store_content?.offscreenPageLimit = 2

        //屏蔽北京、上海的用户
        loge("CityCode:${Constants.cityCode}")
        tv_book_store_list.visibility = if ("010" == Constants.cityCode || "021" == Constants.cityCode)
            View.GONE else View.VISIBLE

        initListener()

    }

    private fun initListener() {
        vp_book_store_content?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                currentPosition = position
                when (position) {
                    recommendPage -> {
                        searchClickListener?.getCurrent(2)
                        sp?.edit()?.putString(Constants.FINDBOOK_SEARCH, "recommend")?.apply()
                    }
                    rankingPage -> {
                        searchClickListener?.getCurrent(3)
                        sp?.edit()?.putString(Constants.FINDBOOK_SEARCH, "top")?.apply()
                    }
                    bookListPage -> {
                        searchClickListener?.getCurrent(5)
                        sp?.edit()?.putString(Constants.FINDBOOK_SEARCH, "booklist")?.apply()
                    }
                    categoryPage -> {
                        searchClickListener?.getCurrent(4)
                        sp?.edit()?.putString(Constants.FINDBOOK_SEARCH, "class")?.apply()
                    }
                }
                changeNavigationState(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        tv_book_store_recommend?.setOnClickListener {
            refreshNavigationState(recommendPage)

            sp?.edit()?.putString(Constants.FINDBOOK_SEARCH, "recommend")?.apply()

            DyStatService.onEvent(EventPoint.MAIN_RECOMMEND)

            searchClickListener?.getCurrent(2)
        }

        tv_book_store_ranking?.setOnClickListener {
            refreshNavigationState(rankingPage)

            sp?.edit()?.putString(Constants.FINDBOOK_SEARCH, "top")?.apply()

            DyStatService.onEvent(EventPoint.MAIN_TOP)

            searchClickListener?.getCurrent(3)
        }

        tv_book_store_list?.setOnClickListener {
            refreshNavigationState(bookListPage)

            sp?.edit()?.putString(Constants.FINDBOOK_SEARCH, "booklist")?.apply()

            DyStatService.onEvent(EventPoint.MAIN_BOOK_LIST)

            searchClickListener?.getCurrent(2)
        }

        tv_book_store_category?.setOnClickListener {
            refreshNavigationState(categoryPage)

            sp?.edit()?.putString(Constants.FINDBOOK_SEARCH, "class")?.apply()

            DyStatService.onEvent(EventPoint.MAIN_CLASS)

            searchClickListener?.getCurrent(4)
        }
    }

    private fun changeNavigationState(type: Int) {
        tv_book_store_recommend?.isSelected = type == recommendPage
        tv_book_store_ranking?.isSelected = type == rankingPage
        tv_book_store_list?.isSelected = type == bookListPage
        tv_book_store_category?.isSelected = type == categoryPage
    }

    private fun refreshNavigationState(position: Int) {
        if (currentPosition == position) {
            return
        }
        vp_book_store_content?.currentItem = position

        currentPosition = position

        changeNavigationState(currentPosition)
    }

    override fun onResume() {
        super.onResume()
        changeNavigationState(currentPosition)
    }

    fun setOnBottomClickListener(searchClickListener: SearchClickListener) {
        this.searchClickListener = searchClickListener
    }

    //大数据 青果搜索打点用
    interface SearchClickListener {
        fun getCurrent(position: Int)
    }

    /**
     * ViewPager的Adapter
     */
    protected inner class BookStoreAdapter internal constructor(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        override fun getCount(): Int {
            return 4
        }

        override fun getItem(position: Int): Fragment? {
            return when (position) {
                recommendPage -> recommendFragment
                rankingPage -> rankingFragment
                bookListPage -> bookListFragment
                categoryPage -> categoryFragment
                else -> null
            }
        }

        override fun getItemPosition(`object`: Any): Int {
            return PagerAdapter.POSITION_NONE
        }
    }
}