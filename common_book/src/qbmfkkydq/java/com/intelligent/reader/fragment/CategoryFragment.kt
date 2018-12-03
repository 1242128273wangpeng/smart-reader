package com.intelligent.reader.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ding.basic.net.Config
import com.intelligent.reader.R
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.qbmfkkydq.frag_category_layout.*
import net.lzbook.kit.constants.ReplaceConstants

import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.web.WebResourceCache
import net.lzbook.kit.utils.web.WebViewIndex
import java.io.File

/**
 * Date: 2018/7/19 11:52
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 分类Fragment
 */
class CategoryFragment : Fragment() {

    private var visibleState = false
    private var initializeState = false

    private val fragmentMale: WebViewFragment by lazy {
        val fragment = WebViewFragment()

        val bundle = Bundle()
        bundle.putString("url", loadChildViewBundleUrl(WebViewIndex.category_male))

        fragment.arguments = bundle

        fragment
    }

    private val fragmentFemale: WebViewFragment by lazy {
        val fragment = WebViewFragment()

        val bundle = Bundle()
        bundle.putString("url", loadChildViewBundleUrl(WebViewIndex.category_female))

        fragment.arguments = bundle

        fragment
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.frag_category_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeView()

        initializeState = true
    }


    private fun initializeView() {
        iv_search.setOnClickListener {
            RouterUtil.navigation(requireActivity(), RouterConfig.SEARCH_BOOK_ACTIVITY)
        }
        val adapter = VPAdapter(childFragmentManager)
        view_pager.adapter = adapter
        val fragments: ArrayList<Fragment> = ArrayList()

        fragments.add(fragmentMale)
        fragments.add(fragmentFemale)

        val titles: ArrayList<String> = ArrayList()

        titles.add("男频")
        titles.add("女频")

        adapter.setData(fragments, titles)
        tab_layout.setupWithViewPager(view_pager)

        tablayout_indicator.setupWithTabLayout(tab_layout)
        tablayout_indicator.setupWithViewPager(view_pager)

        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                when(position) {
                    0 -> fragmentMale.checkViewVisibleState()
                    1 -> fragmentFemale.checkViewVisibleState()
                }
            }
        })
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        visibleState = isVisibleToUser

        checkViewVisibleState()
    }

    private fun checkViewVisibleState() {
        if (initializeState && visibleState) {
            val index = view_pager.currentItem
            when(index) {
                0 -> fragmentMale.checkViewVisibleState()
                1 -> fragmentFemale.checkViewVisibleState()
            }
        }
    }

    private fun loadChildViewBundleUrl(url: String): String {
        val webViewHost = Config.webViewBaseHost

        val filePath = webViewHost.replace(WebResourceCache.internetPath, ReplaceConstants.getReplaceConstants().APP_PATH_CACHE) + "/index.html"

        Logger.e("WebView地址: $webViewHost ${Config.webCacheAvailable}")

        return if (Config.webCacheAvailable) {
            "file://$filePath$url"
        } else {
            Config.webViewBaseHost + "/index.html" + url
        }
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