package com.intelligent.reader.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ding.basic.request.RequestService
import com.dingyue.contract.util.SharedPreUtil
import com.intelligent.reader.R
import com.intelligent.reader.activity.SearchBookActivity
import kotlinx.android.synthetic.mfxsqbyd.category_fragment_layout.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.constants.Constants
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
    private val sharedPreUtil:SharedPreUtil = SharedPreUtil(SharedPreUtil.SHARE_DEFAULT)

    private val titles = arrayOf("男频", "女频")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.category_fragment_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        content_head_search.setOnClickListener {
            val intent = Intent(activity, SearchBookActivity::class.java)
            startActivity(intent)
            StartLogClickUtil.upLoadEventLog(activity,
                    StartLogClickUtil.CLASS_PAGE, StartLogClickUtil.QG_FL_SEARCH)
        }
        mCategoryPageAdapter = CategoryPageAdapter(childFragmentManager)
        category_view_page.adapter = mCategoryPageAdapter
        when(sharedPreUtil?.getInt(SharedPreUtil.GENDER_TAG)){
            Constants.SGIRL -> { category_view_page.setCurrentItem(1, false)}
            else -> {
                category_view_page.setCurrentItem(0, false)
            }
        }

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
        val data = HashMap<String, String>()
        data["type"] = if (position == 0) "1" else "2"
        StartLogClickUtil.upLoadEventLog(activity,
                StartLogClickUtil.CLASS_PAGE, StartLogClickUtil.SWITCHTAB, data)
    }

    // 男频
    private val manCategoryFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "category_male")
        val uri = RequestService.WEB_CATEGORY_MAN_H5.replace("{packageName}", AppUtils.getPackageName())
        val map = HashMap<String, String>()
//        map["type"] = "0"
        bundle.putString("url", UrlUtils.buildWebUrl(uri, map))
        fragment.arguments = bundle
        fragment
    }

    // 女频
    private val girlCategoryFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "category_female")
        val uri = RequestService.WEB_CATEGORY_WOMAN_H5.replace("{packageName}", AppUtils.getPackageName())
        val map = HashMap<String, String>()
//        map["type"] = "1"
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