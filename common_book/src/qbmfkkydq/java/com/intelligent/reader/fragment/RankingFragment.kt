package com.intelligent.reader.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ding.basic.request.RequestService
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.router.RouterUtil
import com.intelligent.reader.R
import kotlinx.android.synthetic.qbmfkkydq.frag_ranking_layout.*
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.AppUtils

/**
 * Date: 2018/7/19 11:52
 * Author: wanghuilin
 * Mail: huilin_wang@dingyuegroup.cn
 * Desc: 榜单Fragment
 */
class RankingFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.frag_ranking_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }


    private fun initView() {
        iv_search.setOnClickListener {
            RouterUtil.navigation(requireActivity(), RouterConfig.SEARCH_BOOK_ACTIVITY)
//            TODO 搜索点击打点
        }

        val webFragment = WebViewFragment();
        val bundle=Bundle();
        bundle.putString("url"
        ,UrlUtils.buildWebUrl(RequestService.WEB_RANK_V3.replace("{packageName}"
                , AppUtils.getPackageName()),HashMap()))

        webFragment.arguments = getBundle("0",//男频
                RequestService.WEB_RANK_V3.replace("{packageName}", AppUtils.getPackageName()))

        childFragmentManager.beginTransaction().replace(R.id.fl_content, webFragment).commit()

    }

    private fun getBundle(type: String, url: String): Bundle {
        val bundle = Bundle();
        val map = HashMap<String, String>()
        map["type"] = type
        bundle.putString("url", UrlUtils.buildWebUrl(url, map))
        return bundle
    }


}