package com.intelligent.reader.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.intelligent.reader.R
import com.intelligent.reader.fragment.scroll.ScrollWebFragment
import com.intelligent.reader.util.scrollFragmentBundle
import kotlinx.android.synthetic.qbmfkkydq.frag_recommend_layout.*
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.web.WebViewIndex

/**
 * Date: 2018/7/19 11:52
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 推荐Fragment
 */
class RecommendFragment : Fragment() {

    var statusBarHeight = 0

    private val fragments: ArrayList<ScrollWebFragment> = ArrayList()

    private var visibleState = false
    private var initializeState = false

    private val fragmentSelection: ScrollWebFragment by lazy {
        scrollFragmentBundle("recommend", WebViewIndex.recommend)
    }

    private val fragmentMale: ScrollWebFragment by lazy {
        scrollFragmentBundle("recommendMale", WebViewIndex.recommend_male)
    }

    private val fragmentFemale: ScrollWebFragment by lazy {
        scrollFragmentBundle("recommendFemale", WebViewIndex.recommend_female)
    }

    private val fragmentFinish: ScrollWebFragment by lazy {
        scrollFragmentBundle("recommendFinish", WebViewIndex.recommend_finish)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarHeight = resources.getDimensionPixelSize(resources.getIdentifier("status_bar_height", "dimen", "android"))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.frag_recommend_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeState = true
        initializeView()
    }

    private fun initializeView() {
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

        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> fragmentSelection.checkViewVisibleState()
                    1 -> fragmentMale.checkViewVisibleState()
                    2 -> fragmentFemale.checkViewVisibleState()
                    3 -> fragmentFinish.checkViewVisibleState()
                }
            }
        })

        view_pager.currentItem = 0
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        visibleState = isVisibleToUser

        checkViewVisibleState()
    }

    private fun checkViewVisibleState() {
        if (initializeState && visibleState) {
            val index = view_pager.currentItem
            when (index) {
                0 -> fragmentSelection.checkViewVisibleState()
                1 -> fragmentMale.checkViewVisibleState()
                2 -> fragmentFemale.checkViewVisibleState()
                3 -> fragmentFinish.checkViewVisibleState()
            }
        }
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