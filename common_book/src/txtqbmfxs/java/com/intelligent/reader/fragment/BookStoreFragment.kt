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


import com.intelligent.reader.R
import kotlinx.android.synthetic.txtqbmfxs.frag_bookstore.*


import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.Constants

import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.webview.UrlUtils

import java.util.HashMap

/**
 * Function：书城
 *
 * Created by JoannChen on 2018/6/16 0016 10:38
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
open class BookStoreFragment : Fragment() {

    private var currentPosition: Int = 0

    private var searchClickListener: SearchClickListener? = null

    private var sharedPreferences: SharedPreferences? = null

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
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(BaseBookApplication.getGlobalContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_bookstore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bookStoreAdapter = BookStoreAdapter(childFragmentManager)

        vp_book_store_content?.adapter = bookStoreAdapter
        vp_book_store_content?.offscreenPageLimit = 2

        initListener()

    }

    private fun initListener() {
        vp_book_store_content?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                currentPosition = position
                when (position) {
                    0 -> {
                        changeNavigationState(position)

                        if (searchClickListener != null) {
                            searchClickListener?.getCurrent(2)
                        }

                        sharedPreferences?.edit()?.putString(Constants.FINDBOOK_SEARCH,
                                "recommend")?.apply()
                    }
                    1 -> {
                        changeNavigationState(position)

                        if (searchClickListener != null) {
                            searchClickListener?.getCurrent(3)
                        }

                        sharedPreferences?.edit()?.putString(Constants.FINDBOOK_SEARCH, "top")?.apply()
                    }
                    2 -> {
                        changeNavigationState(position)

                        if (searchClickListener != null) {
                            searchClickListener?.getCurrent(4)
                        }

                        sharedPreferences?.edit()?.putString(Constants.FINDBOOK_SEARCH,
                                "class")?.apply()
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        ll_book_store_recommend?.setOnClickListener {
            refreshNavigationState(0)

            sharedPreferences?.edit()?.putString(Constants.FINDBOOK_SEARCH,
                    "recommend")?.apply()

            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.MAIN_PAGE,
                    StartLogClickUtil.RECOMMEND)

            if (searchClickListener != null) {
                searchClickListener?.getCurrent(2)
            }
        }

        ll_book_store_ranking?.setOnClickListener {
            refreshNavigationState(1)

            sharedPreferences?.edit()?.putString(Constants.FINDBOOK_SEARCH, "top")?.apply()

            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.TOP)

            if (searchClickListener != null) {
                searchClickListener?.getCurrent(3)
            }
        }

        ll_book_store_category?.setOnClickListener {
            refreshNavigationState(2)

            sharedPreferences?.edit()?.putString(Constants.FINDBOOK_SEARCH, "class")?.apply()

            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.CLASS)

            if (searchClickListener != null) {
                searchClickListener?.getCurrent(4)
            }
        }
    }

    private fun changeNavigationState(type: Int) {

        if (ll_book_store_recommend != null) ll_book_store_recommend?.isSelected = type == 0

        if (ll_book_store_ranking != null) {
            ll_book_store_ranking?.isSelected = type == 1
        }

        if (ll_book_store_category != null) {
            ll_book_store_category?.isSelected = type == 2
        }
    }

    private fun refreshNavigationState(position: Int) {
        if (currentPosition == position) {
            return
        }

        if (vp_book_store_content != null) {
            vp_book_store_content?.currentItem = position
        }

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
            return 3
        }

        override fun getItem(position: Int): Fragment? {
            return when (position) {
                0 -> {
                    recommendFragment
                }
                1 -> {
                    rankingFragment
                }
                2 -> {
                    categoryFragment
                }
                else -> null
            }
        }

        override fun getItemPosition(`object`: Any): Int {
            return PagerAdapter.POSITION_NONE
        }
    }
}