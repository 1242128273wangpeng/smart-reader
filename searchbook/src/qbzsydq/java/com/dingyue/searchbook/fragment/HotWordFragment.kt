package com.dingyue.searchbook.fragment

import com.ding.basic.bean.HotWordBean
import com.dingyue.searchbook.R
import com.dingyue.searchbook.adapter.HotWordAdapter
import kotlinx.android.synthetic.qbzsydq.fragment_hotword.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.utils.StatServiceUtils
import java.util.*


/**
 * Desc 热词
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019 22:05
 */
class HotWordFragment : BaseHotWordFragment() {

    override fun setLayout(): Int = R.layout.fragment_hotword

    override fun showHotWordList(hotWordList: ArrayList<HotWordBean>) {

        hotWordAdapter = HotWordAdapter(hotWordList)
        gridView.adapter = hotWordAdapter

        onHotWordItemClick(hotWordList)
    }

    private fun onHotWordItemClick(hotWordList: ArrayList<HotWordBean>) {
        gridView.setOnItemClickListener { _, _, position, _ ->
            StatServiceUtils.statAppBtnClick(context,
                    StatServiceUtils.b_search_click_allhotword)

            val bean = hotWordList[position]
            val data = HashMap<String, String>()
            data.put("topicword", bean.keyword ?: "")
            data.put("rank", bean.sort.toString())
            data.put("type", bean.superscript ?: "")
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.TOPIC, data)
            hotWordPresenter.onKeyWord(bean.keyword)
            onResultListener?.onSuccess(bean.keyword ?: "")
        }
    }

}