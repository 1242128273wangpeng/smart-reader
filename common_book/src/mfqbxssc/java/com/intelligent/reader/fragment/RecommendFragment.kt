package com.intelligent.reader.fragment

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
import kotlinx.android.synthetic.mfqbxssc.frag_recommend.*
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
class RecommendFragment : Fragment() {

    private lateinit var bookStoreAdapter: BookStoreAdapter
    private lateinit var sharedPreUtil: SharedPreUtil

    private val titles = object : ArrayList<String>() {
        init {
            add("男生")
            add("女生")
        }
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.frag_recommend, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreUtil = SharedPreUtil(SharedPreUtil.SHARE_DEFAULT)

        bookStoreAdapter = BookStoreAdapter(childFragmentManager, nbs_navigation, vp_recommend_content)

        bookStoreAdapter.insertNavigationTable(titles[0])

        bookStoreAdapter.insertNavigationTable(titles[1])

        vp_recommend_content.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                val map = java.util.HashMap<String, String>()
                when (position) {
                    0 -> {
                        map["type"] = "男频"
                    }
                    1 -> {
                        map["type"] = "女频"
                    }
                }
                StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                        StartLogClickUtil.RECOMMEND, StartLogClickUtil.SWITCHTAB, map)

            }
        })

        view_recommend_header_search.setOnClickListener {
            StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                    StartLogClickUtil.RECOMMEND, StartLogClickUtil.SEARCH)

            RouterUtil.navigation(requireActivity(), RouterConfig.SEARCH_BOOK_ACTIVITY)
        }

        vp_recommend_content.setCurrentItem(0, false)
        val map = java.util.HashMap<String, String>()
        map["type"] = "男频"
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.RECOMMEND, StartLogClickUtil.SWITCHTAB, map)

    }

    inner class BookStoreAdapter(fragmentManager: FragmentManager, private var navigationBarStrip: NavigationBarStrip, private var viewPager: ViewPager) : FragmentStatePagerAdapter(fragmentManager) {

        init {
            this.viewPager.adapter = this
            navigationBarStrip.setViewPager(viewPager)
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
                0 -> {
                    recommendManFragment
                }
                1 -> {
                    recommendWomanFragment
                }
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

            navigationBarStrip.addTitle(title)

            notifyDataSetChanged()
        }

        fun recycle() {
            viewPager.removeAllViews()
        }
    }
}