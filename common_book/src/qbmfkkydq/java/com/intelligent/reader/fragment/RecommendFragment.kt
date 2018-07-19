package com.intelligent.reader.fragment

import android.content.res.Resources
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ding.basic.request.RequestService
import com.dingyue.bookshelf.BookShelfLogger
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.intelligent.reader.R
import kotlinx.android.synthetic.qbmfkkydq.frag_recommend_layout.*
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.AppUtils
import java.lang.reflect.Field

/**
 * Date: 2018/7/19 11:52
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 推荐Fragment
 */
class RecommendFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.frag_recommend_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }


    private fun initView() {
        ll_search_layout.setOnClickListener {
            RouterUtil.navigation(requireActivity(), RouterConfig.SEARCH_BOOK_ACTIVITY)
//            TODO 搜索点击打点
        }
        val adapter = VPAdapter(childFragmentManager)
        view_pager.adapter = adapter
        val fragments: ArrayList<Fragment> = ArrayList()
        val fragmentSelection = WebViewFragment()
        fragmentSelection.arguments = getBundle("0",//精选
                RequestService.WEB_CATEGORY_V3.replace("{packageName}", AppUtils.getPackageName()))

        val fragmentMale = WebViewFragment()
        fragmentMale.arguments = getBundle("0",//男频
                RequestService.WEB_CATEGORY_V3.replace("{packageName}", AppUtils.getPackageName()))


        val fragmentFemale = WebViewFragment()
        fragmentFemale.arguments = getBundle("1",//女频
                RequestService.WEB_CATEGORY_V3.replace("{packageName}", AppUtils.getPackageName()))


        val fragmentFinish = WebViewFragment()
        fragmentFinish.arguments = getBundle("1",//完本
                RequestService.WEB_CATEGORY_V3.replace("{packageName}", AppUtils.getPackageName()))

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

    }


    private fun getBundle(type: String, url: String): Bundle {
        val bundle = Bundle();
        val map = HashMap<String, String>()
        map["type"] = type
        bundle.putString("url", UrlUtils.buildWebUrl(url, map))
        return bundle
    }


    inner class VPAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
        private val mFragments: ArrayList<Fragment> = ArrayList()
        private val mTitles: ArrayList<String> = ArrayList()

        fun setData(fragment: ArrayList<Fragment>, titles: ArrayList<String>) {
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