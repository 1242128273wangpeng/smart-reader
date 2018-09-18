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
import com.ding.basic.net.service.RequestService
import com.dingyue.contract.util.SharedPreUtil
import com.intelligent.reader.R
import com.intelligent.reader.activity.SearchBookActivity
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.AppUtils

/**
 * @desc 书城-分类
 * @author lijun Lee
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/3/7 16:36
 */
class RecommendFragment : Fragment() {

    private lateinit var mRecommendPageAdapter: RecommendPageAdapter
    private val sharedPreUtil:SharedPreUtil = SharedPreUtil(SharedPreUtil.SHARE_DEFAULT)

    private val titles = arrayOf("男生", "女生")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.category_fragment_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rl_head_search.setOnClickListener {
            val intent = Intent(activity, SearchBookActivity::class.java)
            startActivity(intent)
            StartLogClickUtil.upLoadEventLog(activity,
                    StartLogClickUtil.RECOMMEND_PAGE, StartLogClickUtil.QG_FL_SEARCH)
        }
        mRecommendPageAdapter = RecommendPageAdapter(childFragmentManager)
        category_view_page.adapter = mRecommendPageAdapter
        tabstrip.setViewPager(category_view_page)
        if(sharedPreUtil?.getInt(SharedPreUtil.RECOMMEND_SELECT_SEX) == 0){
            category_view_page.setCurrentItem(0, true)
        }else{
            category_view_page.setCurrentItem(1,true)
        }


        category_view_page.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                uploadTabSwitchLog(position)
                if(position == 0){
                    sharedPreUtil.putInt(SharedPreUtil.RECOMMEND_SELECT_SEX,0)
                }else{
                    sharedPreUtil.putInt(SharedPreUtil.RECOMMEND_SELECT_SEX,1)
                }
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
    val manRecommendFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "recommend_male")
        val uri = RequestService.WEB_RECOMMEND_H5_BOY.replace("{packageName}", AppUtils.getPackageName())
        val map = HashMap<String, String>()
//        map["type"] = "0"
        bundle.putString("url", UrlUtils.buildWebUrl(uri, map))
        fragment.arguments = bundle
        fragment
    }

    // 女频
     val girlRecommendFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "recommend_female")
        val uri = RequestService.WEB_RECOMMEND_H5_Girl.replace("{packageName}", AppUtils.getPackageName())
        val map = HashMap<String, String>()
//        map["type"] = "1"
        bundle.putString("url", UrlUtils.buildWebUrl(uri, map))
        fragment.arguments = bundle
        fragment
    }

    private inner class RecommendPageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getCount() = 2

        override fun getItem(position: Int): Fragment = when (position) {
            0 -> manRecommendFragment
            1 -> girlRecommendFragment
            else -> manRecommendFragment
        }

        override fun getItemPosition(`object`: Any): Int = PagerAdapter.POSITION_NONE

        override fun getPageTitle(position: Int): CharSequence = titles[position]
    }
}