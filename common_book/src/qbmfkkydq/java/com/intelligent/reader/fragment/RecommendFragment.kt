package com.intelligent.reader.fragment

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ding.basic.net.Config
import com.intelligent.reader.R
import com.intelligent.reader.fragment.scroll.ScrollWebFragment
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.qbmfkkydq.frag_recommend_layout.*
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.web.WebViewIndex
import java.io.File

/**
 * Date: 2018/7/19 11:52
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 推荐Fragment
 */
class RecommendFragment : Fragment() {

    var statusBarHeight = 0

    private val fragments: ArrayList<ScrollWebFragment> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarHeight = resources.getDimensionPixelSize(resources.getIdentifier("status_bar_height", "dimen", "android"))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.frag_recommend_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }


    private fun initView() {
        if (statusBarHeight == 0) {
            statusBarHeight = AppUtils.dip2px(requireContext(), 20f)
        }

        val params = ll_search_layout?.layoutParams as LinearLayout.LayoutParams?
        params?.topMargin = (params?.topMargin
                ?: AppUtils.dip2px(requireContext(), 4f)) + statusBarHeight

        view_pager.offscreenPageLimit = 4

        ll_search_layout?.setOnClickListener {
            RouterUtil.navigation(requireActivity(), RouterConfig.SEARCH_BOOK_ACTIVITY)
        }
        val adapter = VPAdapter(childFragmentManager)
        view_pager.adapter = adapter

        val webViewHost = Config.webViewBaseHost
        Logger.e("WebView地址: $webViewHost")

        val filePath = ReplaceConstants.getReplaceConstants().APP_PATH_CACHE + "web/" + Config.webViewTimeTemp + "/index.html"
        val localFileExist = File(filePath).exists()

        val fragmentSelection = ScrollWebFragment()
        if (localFileExist) {
            fragmentSelection.arguments = getBundle("file://${ReplaceConstants.getReplaceConstants().APP_PATH_CACHE}/web/${Config.webViewTimeTemp}${WebViewIndex.recommend}", "recommend")
        } else {
            fragmentSelection.arguments = getBundle(Config.webViewBaseHost + WebViewIndex.recommend, "recommend")
        }

        val fragmentMale = ScrollWebFragment()
        if (localFileExist) {
            fragmentMale.arguments = getBundle("file://${ReplaceConstants.getReplaceConstants().APP_PATH_CACHE}/web/${Config.webViewTimeTemp}${WebViewIndex.recommend_male}", "recommendMale")
        } else {
            fragmentMale.arguments = getBundle(Config.webViewBaseHost + WebViewIndex.recommend_male, "recommendMale")
        }

        val fragmentFemale = ScrollWebFragment()
        if (localFileExist) {
            fragmentFemale.arguments = getBundle("file://${ReplaceConstants.getReplaceConstants().APP_PATH_CACHE}/web/${Config.webViewTimeTemp}${WebViewIndex.recommend_female}", "recommendFemale")
        } else {
            fragmentFemale.arguments = getBundle(Config.webViewBaseHost + WebViewIndex.recommend_female, "recommendFemale")
        }

        val fragmentFinish = ScrollWebFragment()
        if (localFileExist) {
            fragmentFinish.arguments = getBundle("file://${ReplaceConstants.getReplaceConstants().APP_PATH_CACHE}/web/${Config.webViewTimeTemp}${WebViewIndex.recommend_finish}", "recommendFinish")
        } else {
            fragmentFinish.arguments = getBundle(Config.webViewBaseHost + WebViewIndex.recommend_finish, "recommendFinish")
        }

        fragments.clear()
        fragments.add(fragmentSelection)
        fragments.add(fragmentMale)
        fragments.add(fragmentFemale)
        fragments.add(fragmentFinish)

        val titles: ArrayList<String> = ArrayList()
        titles.add("精选")
        titles.add("男频")
        titles.add("女频")
        titles.add("完本")

        adapter.setData(fragments, titles)

        tab_layout.setupWithViewPager(view_pager)

        tablayout_indicator.setupWithTabLayout(tab_layout)
        tablayout_indicator.setupWithViewPager(view_pager)

        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
            }
        })
    }


    private fun getBundle(url: String, type: String): Bundle {
        val bundle = Bundle()
        bundle.putString("url", url)
        bundle.putString("type", type)
        return bundle
    }


    inner class VPAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
        private val mFragments: ArrayList<ScrollWebFragment> = ArrayList()
        private val mTitles: ArrayList<String> = ArrayList()

        fun setData(fragment: ArrayList<ScrollWebFragment>, titles: ArrayList<String>) {
            mFragments.clear()
            mFragments.addAll(fragment)
            mTitles.clear()
            mTitles.addAll(titles)
            notifyDataSetChanged()

        }

        override fun getItem(position: Int): Fragment {
            return mFragments[position]
        }

        override fun getCount(): Int {
            return mFragments.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mTitles[position]
        }
    }
}