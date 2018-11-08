package com.intelligent.reader.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ding.basic.request.RequestService
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.util.SharedPreUtil
import com.intelligent.reader.R
import com.intelligent.reader.activity.SettingActivity
import kotlinx.android.synthetic.zsmfqbxs.frag_bookstore.*
import kotlinx.android.synthetic.zsmfqbxs.view_home_header.*
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.NavigationBarStrip

/**
 * @desc 书城-分类
 * @author lijun Lee
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/3/7 16:36
 */
class BookStoreFragment : Fragment() {

    private lateinit var bookStoreAdapter: BookStoreAdapter
    private lateinit var sharedPreUtil: SharedPreUtil

    private val titles = object : ArrayList<String>() {
        init {
            add("精选")
            add("男频")
            add("女频")
            add("完结")
        }
    }

    val recommendFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "recommend")
        val uri = RequestService.WEB_RECOMMEND.replace("{packageName}", AppUtils.getPackageName())
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }

    private val recommendManFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "recommendMan")
        val uri = RequestService.WEB_RECOMMEND_MAN.replace("{packageName}", AppUtils.getPackageName())
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }

    private val recommendWomanFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "recommendWoman")
        val uri = RequestService.WEB_RECOMMEND_WOMAN.replace("{packageName}", AppUtils.getPackageName())
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }

    private val recommendFinishFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "recommendWoman")
        val uri = RequestService.WEB_RECOMMEND_FINISH.replace("{packageName}", AppUtils.getPackageName())
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.frag_bookstore, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreUtil = SharedPreUtil(SharedPreUtil.SHARE_DEFAULT)

        txt_header_title.text = "书城"

        vp_content.offscreenPageLimit = 4

        bookStoreAdapter = BookStoreAdapter(childFragmentManager, nbs_navigation, vp_content)

        bookStoreAdapter.insertNavigationTable(titles[0])

        bookStoreAdapter.insertNavigationTable(titles[1])

        bookStoreAdapter.insertNavigationTable(titles[2])

        bookStoreAdapter.insertNavigationTable(titles[3])

        img_header_setting.setOnClickListener {
            fp_header_point.visibility = View.GONE

            val parameter = java.util.HashMap<String, String>()
            when (vp_content.currentItem) {
                0 -> parameter["pk"] = "推荐"
                1 -> parameter["pk"] = "男频"
                2 -> parameter["pk"] = "女频"
                3 -> parameter["pk"] = "完结"
            }
            StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                    StartLogClickUtil.PAGE_HOME, StartLogClickUtil.ACTION_HOME_PERSONAL, parameter)

            startActivity(Intent(requireActivity(), SettingActivity::class.java))
        }

        img_header_search.setOnClickListener {
            RouterUtil.navigation(requireActivity(),
                    RouterConfig.SEARCH_BOOK_ACTIVITY)
        }

        img_header_cache.setOnClickListener {
            RouterUtil.navigation(requireActivity(),
                    RouterConfig.DOWNLOAD_MANAGER_ACTIVITY)

            val parameter = java.util.HashMap<String, String>()
            when (vp_content.currentItem) {
                0 -> parameter["pk"] = "推荐"
                1 -> parameter["pk"] = "男频"
                2 -> parameter["pk"] = "女频"
                3 -> parameter["pk"] = "完结"
            }
            StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                    StartLogClickUtil.PAGE_HOME, StartLogClickUtil.CACHEMANAGE, parameter)
        }

        vp_content.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                when(position) {
                    0 -> {
                        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(), StartLogClickUtil.PAGE_BOOKSTORE, StartLogClickUtil.ACTION_RECOMMEND)
                        sharedPreUtil.putString(SharedPreUtil.HOME_FINDBOOK_SEARCH, "recommend")
                    }
                    1 -> {
                        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(), StartLogClickUtil.PAGE_BOOKSTORE, StartLogClickUtil.ACTION_MALE)
                    }
                    2 -> {
                        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(), StartLogClickUtil.PAGE_BOOKSTORE, StartLogClickUtil.ACTION_FEMALE)
                    }
                    3 -> {
                        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(), StartLogClickUtil.PAGE_BOOKSTORE, StartLogClickUtil.ACTION_FINISH)
                    }
                }
            }
        })

        vp_content.setCurrentItem(0, false)
    }

    inner class BookStoreAdapter(fragmentManager: FragmentManager, private var navigationBarStrip: NavigationBarStrip, private var viewPager: ViewPager) : FragmentStatePagerAdapter(fragmentManager) {

        init {
            this.viewPager.adapter = this
            navigationBarStrip.insertViewPager(viewPager)
        }

        @JvmOverloads
        fun remove(index: Int = 0) {
            var removeIndex = index

            if (titles.isEmpty()) {
                return
            }

            if (index < 0) {
                removeIndex = 0
            }

            if (index >= titles.size) {
                removeIndex = titles.size - 1
            }

            titles.removeAt(removeIndex)
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return titles.size
        }

        override fun getItemPosition(any: Any): Int {
            return PagerAdapter.POSITION_NONE
        }

        override fun getItem(position: Int): Fragment? {
            return when (position) {
                0 -> recommendFragment
                1 -> recommendManFragment
                2 -> recommendWomanFragment
                3 -> recommendFinishFragment
                else -> null
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }

        fun insertNavigationTable(title: String) {
            if (TextUtils.isEmpty(title)) {
                return
            }

            navigationBarStrip.insertTitle(title)

            notifyDataSetChanged()
        }

        fun recycle() {
            viewPager.removeAllViews()
        }
    }
}