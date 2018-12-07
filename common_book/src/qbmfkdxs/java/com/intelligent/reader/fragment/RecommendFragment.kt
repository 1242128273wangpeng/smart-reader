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
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.bean.RecommendCateListBean
import com.ding.basic.net.RequestSubscriber
import com.ding.basic.net.api.service.RequestService

import com.intelligent.reader.R
import com.intelligent.reader.util.fragmentBundle
import com.intelligent.reader.util.loadWebViewUrl
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.qbmfkdxs.frag_recommend.*

import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil

import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.router.RouterUtil
import net.lzbook.kit.utils.web.WebViewIndex

/**
 * @desc 书城-分类
 * @author lijun Lee
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/3/7 16:36
 */
class RecommendFragment : Fragment() {

    private var currentPosition = 0
    var isLoadDataSuccess = false
    private lateinit var mCategoryPageAdapter: CategoryPageAdapter

    private var recommendCateList: MutableList<RecommendCateListBean> = ArrayList()


    val categoryNames = "玄幻,现代言情"


    val recommendFragment: WebViewFragment by lazy {
        fragmentBundle("recommend", WebViewIndex.recommend_gender)
    }

    private val recommendManFragment: WebViewFragment by lazy {
        fragmentBundle("recommendMan", WebViewIndex.recommend_gender_male)
    }

    private val recommendWomanFragment: WebViewFragment by lazy {
        fragmentBundle("recommendWoman", WebViewIndex.recommend_gender_female)
    }

    private val recommendFinishFragment: WebViewFragment by lazy {
        fragmentBundle("recommendFinish", WebViewIndex.recommend_cate_finish)
    }

    private val recommendFantasyFragment: WebViewFragment by lazy {
        fragmentBundle("recommendFantasy", WebViewIndex.recommend_cate_fantasy)
    }

    private val recommendModernFragment: WebViewFragment by lazy {
        fragmentBundle("recommendModern", WebViewIndex.recommend_cate_modern)
    }

    private val fragmentList = arrayListOf(
            recommendFragment,
            recommendManFragment,
            recommendWomanFragment,
            recommendFinishFragment,
            recommendFantasyFragment,
            recommendModernFragment
    )


    private val titleList = arrayListOf(
            "精选", "男频", "女频", "完结", "玄幻", "现代言情"
    )


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.frag_recommend, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestRecommendCateList(AppUtils.getPackageName(), categoryNames, object : RequestSubscriber<ArrayList<RecommendCateListBean>>() {
            override fun requestResult(result: ArrayList<RecommendCateListBean>?) {
                isLoadDataSuccess = if (result != null && result.isNotEmpty()) {
                    recommendCateList.clear()
                    recommendCateList.addAll(result)
                    true
                } else {
                    false
                }
            }

            override fun requestError(message: String) {
                Logger.e("获取作者推荐异常！")
                isLoadDataSuccess = false
            }
        })

        mCategoryPageAdapter = CategoryPageAdapter(childFragmentManager)
        vp_recommend_content.adapter = mCategoryPageAdapter


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
                    bundle.putString("url", loadWebViewUrl(WebViewIndex.finish_Detail_finish))
                    bundle.putString("title", "完结")
                    RouterUtil.navigation(requireActivity(), RouterConfig.TABULATION_ACTIVITY, bundle)

                    StartLogClickUtil.upLoadEventLog(requireActivity(), StartLogClickUtil.FINISHRECOMMEND_PAGE, StartLogClickUtil.SEQUENCE)
                    val data = HashMap<String, String>()
                    data["pk"] = StartLogClickUtil.FINISHRECOMMEND_PAGE
                    StartLogClickUtil.upLoadEventLog(requireActivity(), StartLogClickUtil.CLASS_PAGE, StartLogClickUtil.ENTRYPAGE, data)

                }
                4 -> {

                    if (recommendCateList.size > 0) {
                        bundle.putString("url", loadWebViewUrl(WebViewIndex.finish_Detail_fantasy + recommendCateList[0].id))
                        bundle.putString("title", "玄幻")
                        RouterUtil.navigation(requireActivity(), RouterConfig.TABULATION_ACTIVITY, bundle)

                        StartLogClickUtil.upLoadEventLog(requireActivity(), StartLogClickUtil.XUANHUANRECOMMEND_PAGE, StartLogClickUtil.SEQUENCE)
                        val data = HashMap<String, String>()
                        data["pk"] = StartLogClickUtil.XUANHUANRECOMMEND_PAGE
                        StartLogClickUtil.upLoadEventLog(requireActivity(), StartLogClickUtil.CLASS_PAGE, StartLogClickUtil.ENTRYPAGE, data)
                    }
                }
                5 -> {
                    if (recommendCateList.size > 1) {
                        bundle.putString("url", loadWebViewUrl(WebViewIndex.finish_Detail_modern + recommendCateList[1].id))
                        bundle.putString("title", "现代言情")
                        RouterUtil.navigation(requireActivity(), RouterConfig.TABULATION_ACTIVITY, bundle)

                        StartLogClickUtil.upLoadEventLog(requireActivity(), StartLogClickUtil.CITYLOVERECOMMEND_PAGE, StartLogClickUtil.SEQUENCE)
                        val data = HashMap<String, String>()
                        data["pk"] = StartLogClickUtil.CITYLOVERECOMMEND_PAGE
                        StartLogClickUtil.upLoadEventLog(requireActivity(), StartLogClickUtil.CLASS_PAGE, StartLogClickUtil.ENTRYPAGE, data)
                    }
                }
            }
        }
    }

    private fun uploadTabSwitchLog(position: Int) {
        var type = "RECOMMEND"
        when (position) {
            0 -> {
                type = "RECOMMEND"
            }
            1 -> {
                type = "MALE"
            }
            2 -> {
                type = "FEMALE"
            }
            3 -> {
                type = "FINISH"
            }
            4 -> {
                type = "XUANHUANRECOMMEND"
            }
            5 -> {
                type = "CITYLOVERECOMMEND"
            }
        }
        StartLogClickUtil.upLoadEventLog(requireContext(), StartLogClickUtil.RECOMMEND_PAGE, type)
    }


    private inner class CategoryPageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getCount() = fragmentList.size

        override fun getItem(position: Int): Fragment? {
            return fragmentList[position]
        }

        override fun getItemPosition(`object`: Any): Int = PagerAdapter.POSITION_NONE

        override fun getPageTitle(position: Int): CharSequence = titleList[position]
    }
}