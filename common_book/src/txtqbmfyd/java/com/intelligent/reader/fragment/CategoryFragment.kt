package com.intelligent.reader.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.intelligent.reader.R
import kotlinx.android.synthetic.main.category_fragment_layout.*
import net.lzbook.kit.encrypt.URLBuilderIntterface
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.AppUtils

/**
 * @desc 书城-分类
 * @author lijun Lee
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/3/7 16:36
 */
class CategoryFragment : Fragment() {

    private lateinit var mCategoryPageAdapter: CategoryPageAdapter

    private val titles = arrayOf("男频", "女频")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.category_fragment_layout, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mCategoryPageAdapter = CategoryPageAdapter(fragmentManager)
        category_view_page.adapter = mCategoryPageAdapter
        category_view_page.setCurrentItem(0, false)
        tabstrip.setViewPager(category_view_page)
    }

    // 男频
    private val manCategoryFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "category_male")
        val uri = URLBuilderIntterface.WEB_CATEGORY.replace("{packageName}", AppUtils.getPackageName())
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
        val uri = URLBuilderIntterface.WEB_CATEGORY.replace("{packageName}", AppUtils.getPackageName())
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

        override fun getItemPosition(`object`: Any?): Int = PagerAdapter.POSITION_NONE

        override fun getPageTitle(position: Int): CharSequence = titles[position]
    }
}