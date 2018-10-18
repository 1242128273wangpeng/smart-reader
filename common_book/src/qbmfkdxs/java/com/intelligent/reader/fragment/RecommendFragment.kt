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
import com.ding.basic.bean.RecommendCateListBean
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.RequestService
import com.ding.basic.request.RequestSubscriber
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.dingyue.contract.util.SharedPreUtil
import com.intelligent.reader.R
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.qbmfkdxs.frag_recommend.*
import net.lzbook.kit.app.BaseBookApplication
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

    private lateinit var sharedPreUtil: SharedPreUtil
    private var currentPosition = 0
    var isLoadDataSuccess = false
    private lateinit var mCategoryPageAdapter: CategoryPageAdapter
    private var recommendCateList: MutableList<RecommendCateListBean> = ArrayList()
    var listFragment: MutableList<WebViewFragment> = ArrayList()
    val categoryNames = "玄幻,现代言情"

    private val titles = object : ArrayList<String>() {
        init {
            add("精选")
            add("男频")
            add("女频")
            add("完结")
            add("玄幻")
            add("现代言情")
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

    val recommendManFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "recommendMan")
        val uri = RequestService.WEB_RECOMMEND_MAN.replace("{packageName}", AppUtils.getPackageName())
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }

    val recommendWomanFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "recommendWoman")
        val uri = RequestService.WEB_RECOMMEND_WOMAN.replace("{packageName}", AppUtils.getPackageName())
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }


    val recommendFinishFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "recommendFinish")
        val uri = RequestService.WEB_RECOMMEND_FINISH.replace("{packageName}", AppUtils.getPackageName())
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }
    val recommendFantasyFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "recommendFantasy")
        val uri = RequestService.WEB_RECOMMEND_FANTASY.replace("{packageName}", AppUtils.getPackageName())
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }
    val recommendModernFragment: WebViewFragment by lazy {
        val fragment = WebViewFragment()
        val bundle = Bundle()
        bundle.putString("type", "recommendModern")
        val uri = RequestService.WEB_RECOMMEND_MODERN.replace("{packageName}", AppUtils.getPackageName())
        bundle.putString("url", UrlUtils.buildWebUrl(uri, HashMap()))
        fragment.arguments = bundle
        fragment
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.frag_recommend, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestRecommendCateList(AppUtils.getPackageName(), categoryNames, object : RequestSubscriber<ArrayList<RecommendCateListBean>>() {
            override fun requestResult(result: ArrayList<RecommendCateListBean>?) {
                if (result != null && result.isNotEmpty()) {
                    recommendCateList.clear()
                    recommendCateList.addAll(result)
                    isLoadDataSuccess = true
                } else {
                    isLoadDataSuccess = false
                }
            }

            override fun requestError(message: String) {
                Logger.e("获取作者推荐异常！")
                isLoadDataSuccess = false
            }
        })

        mCategoryPageAdapter = CategoryPageAdapter(childFragmentManager)
        vp_recommend_content.adapter = mCategoryPageAdapter

        sharedPreUtil = SharedPreUtil(SharedPreUtil.SHARE_DEFAULT)

        tabstrip.setViewPager(vp_recommend_content)
        vp_recommend_content.offscreenPageLimit = 6
        vp_recommend_content.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                currentPosition = position
                uploadTabSwitchLog(position)
                when (position) {
                    0, 1, 2 -> {
                        txt_select.visibility = View.GONE
                    }
                    3, 4, 5 -> {
                        if (isLoadDataSuccess) {
                            txt_select.visibility = View.VISIBLE
                        } else {
                            txt_select.visibility = View.GONE
                        }
                    }
                }
            }
        })

        rl_head_recommend.setOnClickListener {
            RouterUtil.navigation(requireActivity(),
                    RouterConfig.SEARCH_BOOK_ACTIVITY)
            StartLogClickUtil.upLoadEventLog(requireActivity(),
                    StartLogClickUtil.RECOMMEND_PAGE, StartLogClickUtil.QG_TJY_SEARCH)
        }

        txt_select.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("from", "recommend")
            when (currentPosition) {

                3 -> {
                    bundle.putString("url", RequestService.WEB_RECOMMEND_FINISH_DETAIL.replace("{packageName}", AppUtils.getPackageName()) + "- -完结")
                    bundle.putString("title", "完结")
                    RouterUtil.navigation(requireActivity(), RouterConfig.TABULATION_ACTIVITY, bundle)
                }
                4 -> {

                    if (recommendCateList.size > 0) {
                        bundle.putString("url", RequestService.WEB_RECOMMEND_FANTASY_DETAIL.replace("{packageName}", AppUtils.getPackageName()) + "-玄幻-" + recommendCateList.get(0).id)
                        bundle.putString("title", "玄幻")
                        RouterUtil.navigation(requireActivity(), RouterConfig.TABULATION_ACTIVITY, bundle)
                    }
                }
                5 -> {
                    if (recommendCateList.size > 1) {
                        bundle.putString("url", RequestService.WEB_RECOMMEND_MODERN_DETAIL.replace("{packageName}", AppUtils.getPackageName()) + "-现代言情-" + recommendCateList.get(1).id)
                        bundle.putString("title", "现代言情")
                        RouterUtil.navigation(requireActivity(), RouterConfig.TABULATION_ACTIVITY, bundle)
                    }
                }
            }
        }
    }

    private fun uploadTabSwitchLog(position: Int) {
        val data = HashMap<String, String>()
        data["type"] = if (position == 0) "1" else "2"
        StartLogClickUtil.upLoadEventLog(activity,
                StartLogClickUtil.CLASS_PAGE, StartLogClickUtil.SWITCHTAB, data)
    }

    private inner class CategoryPageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment? {
            return when (position) {
                0 -> recommendFragment
                1 -> recommendManFragment
                2 -> recommendWomanFragment
                3 -> recommendFinishFragment
                4 -> recommendFantasyFragment
                5 -> recommendModernFragment
                else -> recommendFragment
            }
        }

        override fun getCount() = 6


        override fun getItemPosition(`object`: Any): Int = PagerAdapter.POSITION_NONE

        override fun getPageTitle(position: Int): CharSequence = titles[position]
    }
}