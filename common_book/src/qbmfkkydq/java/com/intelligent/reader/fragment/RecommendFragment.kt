package com.intelligent.reader.fragment

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.widget.NestedScrollView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ding.basic.request.RequestService
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.intelligent.reader.R
import com.intelligent.reader.fragment.scroll.ScrollWebFragment
import kotlinx.android.synthetic.qbmfkkydq.frag_recommend_layout.*
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.AppUtils

/**
 * Date: 2018/7/19 11:52
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 推荐Fragment
 */
class RecommendFragment : Fragment() {


    var statusBarHeight=0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarHeight=getResources().getDimensionPixelSize(getResources().getIdentifier("status_bar_height", "dimen", "android"));
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.frag_recommend_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    val fragments: ArrayList<ScrollWebFragment> = ArrayList()

    private fun initView() {


        ll_search_layout.post{
            if(statusBarHeight==0){
                statusBarHeight=AppUtils.dip2px(context,20f)
            }
            val params:LinearLayout.LayoutParams= ll_search_layout.layoutParams as LinearLayout.LayoutParams;
            params.topMargin=params.topMargin+statusBarHeight;
        }

        view_pager.offscreenPageLimit = 4

        ll_search_layout.setOnClickListener {
            RouterUtil.navigation(requireActivity(), RouterConfig.SEARCH_BOOK_ACTIVITY)
        }
        val adapter = VPAdapter(childFragmentManager)
        view_pager.adapter = adapter

        val fragmentSelection = ScrollWebFragment()
        fragmentSelection.arguments = getBundle(//精选
                RequestService.WEB_RECOMMEND_H5.replace("{packageName}", AppUtils.getPackageName()))
        fragmentSelection.setViewPagerViewGroup(view_pager)

        val fragmentMale = ScrollWebFragment()
        fragmentMale.arguments = getBundle(//男频
                RequestService.WEB_RECOMMEND_H5_BOY.replace("{packageName}", AppUtils.getPackageName()))
        fragmentMale.setViewPagerViewGroup(view_pager)

        val fragmentFemale = ScrollWebFragment()
        fragmentFemale.arguments = getBundle(//女频
                RequestService.WEB_RECOMMEND_H5_Girl.replace("{packageName}", AppUtils.getPackageName()))
        fragmentFemale.setViewPagerViewGroup(view_pager)

        val fragmentFinish = ScrollWebFragment()
        fragmentFinish.arguments = getBundle(//完本
                RequestService.WEB_RECOMMEND_H5_Finish.replace("{packageName}", AppUtils.getPackageName()))
        fragmentFinish.setViewPagerViewGroup(view_pager)

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


    private fun getBundle(url: String): Bundle {
        val bundle = Bundle();
        val map = HashMap<String, String>()
        bundle.putString("url", UrlUtils.buildWebUrl(url, map))
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