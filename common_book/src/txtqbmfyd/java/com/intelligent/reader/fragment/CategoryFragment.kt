package com.intelligent.reader.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dingyue.statistics.DyStatService
import com.intelligent.reader.R
import kotlinx.android.synthetic.txtqbmfyd.category_fragment_layout.*
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.logger.HomeLogger
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.webview.UrlUtils

/**
 * @desc 书城-分类
 * @author lijun Lee
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/3/7 16:36
 */
class CategoryFragment : Fragment() {

    private lateinit var mCategoryPageAdapter: CategoryPageAdapter

    private val titles = arrayOf("男频", "女频")

    // webview精选页面
    private val WEB_CATEGORY_BOY = "/h5/{packageName}/categoryBoy"
    // webview排行页面
    private val WEB_CATEGORY_GIRL = "/h5/{packageName}/categoryGirl"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.category_fragment_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        content_head_search.setOnClickListener {
            HomeLogger.uploadHomeSearch(4)
            RouterUtil.navigation(requireActivity(), RouterConfig.SEARCH_BOOK_ACTIVITY)
        }
        mCategoryPageAdapter = CategoryPageAdapter(childFragmentManager)
        category_view_page.adapter = mCategoryPageAdapter
        category_view_page.setCurrentItem(0, false)
        tabstrip.setViewPager(category_view_page)
        category_view_page.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                uploadTabSwitchLog(position)
            }

        })
        uploadTabSwitchLog(0)
    }

    private fun uploadTabSwitchLog(position: Int) {
        DyStatService.onEvent(EventPoint.CLASS_SWITCHTAB, mapOf("type" to if (position == 0) "1" else "2"))
    }

    // 男频
    private val manCategoryFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "category_male")
        val uri = WEB_CATEGORY_BOY.replace("{packageName}", AppUtils.getPackageName())
        val map = HashMap<String, String>()
        map["type"] = "0"
        bundle.putString("url", UrlUtils.buildWebUrl(uri, map))
        fragment.arguments = bundle
        fragment
    }

    // 女频
    private val girlCategoryFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "category_female")
        val uri = WEB_CATEGORY_GIRL.replace("{packageName}", AppUtils.getPackageName())
        val map = HashMap<String, String>()
        map["type"] = "1"
        bundle.putString("url", UrlUtils.buildWebUrl(uri, map))
        fragment.arguments = bundle
        fragment
    }

    private inner class CategoryPageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getCount() = 2

        override fun getItem(position: Int): Fragment = when (position) {
            0 -> manCategoryFragment
            1 -> girlCategoryFragment
            else -> manCategoryFragment
        }

        override fun getItemPosition(`object`: Any): Int = PagerAdapter.POSITION_NONE

        override fun getPageTitle(position: Int): CharSequence = titles[position]
    }
}