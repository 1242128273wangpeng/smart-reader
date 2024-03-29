package net.lzbook.kit.ui.activity

import android.os.Bundle
import android.widget.ArrayAdapter
import com.ding.basic.net.Config
import com.ding.basic.net.api.ContentAPI
import com.ding.basic.net.api.MicroAPI
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_debug_host.*
import net.lzbook.kit.R
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity
import net.lzbook.kit.utils.toast.ToastUtil


/**
 * <pre>
 * Function：host选择
 *
 * Created by JoannChen on 2018/4/19 0019 13:53
 * E-mail:yongzuo_chen@dingyuegroup.cn
 * </pre>
 */
class DebugHostActivity : BaseCacheableActivity() {

    private var list = ArrayList<String>()
    private var mAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug_host)
        initData()
    }


    private fun initData() {
        getList()

        mAdapter = ArrayAdapter(this,
                android.R.layout.simple_dropdown_item_1line, list)
        lv_host.adapter = mAdapter

        iv_back.setOnClickListener { finish() }

        // 点击选择host
        lv_host.setOnItemClickListener { _, _, position, _ ->
            et_input_host.setText(list[position])
        }

        // 长按删除子条目
        lv_host.setOnItemLongClickListener { _, _, position, _ ->
            delHost(position)
            ToastUtil.showToastMessage("删除成功！")
            true
        }

        // 保存按钮
        tv_save.setOnClickListener {

            if (et_input_host.text.isNotEmpty()) {

                if (!list.contains(et_input_host.text.toString())) {
                    setHost(et_input_host.text.toString())
                }

                when (intent.getStringExtra("type")) {
                    SPKey.NOVEL_HOST -> {
                        Config.insertRequestAPIHost(et_input_host.text.toString())
                        SPUtils.putOnlineConfigSharedString(SPKey.NOVEL_HOST, et_input_host.text.toString())
                    }
                    SPKey.WEBVIEW_HOST -> {
                        Config.insertWebViewHost(et_input_host.text.toString())
                        SPUtils.putOnlineConfigSharedString(SPKey.WEBVIEW_HOST, et_input_host.text.toString())
                    }
                    SPKey.MICRO_AUTH_HOST -> {
                        MicroAPI.microHost = (et_input_host.text.toString())
                        MicroAPI.initMicroService()
                        SPUtils.insertPrivateSharedString(SPKey.MICRO_AUTH_HOST, et_input_host.text.toString())
                    }
                    SPKey.CONTENT_AUTH_HOST -> {
                        ContentAPI.contentHost = (et_input_host.text.toString())
                        ContentAPI.initContentService()
                        SPUtils.insertPrivateSharedString(SPKey.CONTENT_AUTH_HOST, et_input_host.text.toString())
                    }
                    SPKey.USER_TAG_HOST -> {
                        Config.insertUserTagHost(et_input_host.text.toString())
                    }
                    else -> {
                        ""
                    }
                }

                finish()
            }

        }

    }


    private fun getList(): ArrayList<String> {

        val json = SPUtils.getOnlineConfigSharedString(SPKey.HOST_LIST, "")
        if (json != "") {

            list = Gson().fromJson(json, object : TypeToken<List<String>>() {}.type)

            return list
        } else {

            list.add("http://119.254.159.100:8081")
            list.add("http://8011.uzn.bookapi.cn")
            list.add("http://8086.zn.bookapi.cn")
            list.add("https://uniontest.bookapi.cn")
            list.add("https://unionapi.bookapi.cn")
            list.add("https://unioncontent.bookapi.cn")

            SPUtils.putOnlineConfigSharedString(SPKey.HOST_LIST, Gson().toJson(list))
        }

        return list
    }


    /**
     * 添加host
     */
    private fun setHost(host: String) {
        list.add(0, host)
        mAdapter?.notifyDataSetChanged()

        SPUtils.putOnlineConfigSharedString(SPKey.HOST_LIST, Gson().toJson(list))
    }

    /**
     * 删除host
     * 先将sp中的字符串转为list，删除相对应的子条目，在存入sp中
     */
    private fun delHost(position: Int) {
        list.removeAt(position)
        mAdapter?.notifyDataSetChanged()

        val spList = Gson().fromJson<ArrayList<String>>(SPUtils.getOnlineConfigSharedString(SPKey.HOST_LIST, ""),
                object : TypeToken<List<String>>() {}.type)
        spList.removeAt(position)
        SPUtils.putOnlineConfigSharedString(SPKey.HOST_LIST, Gson().toJson(spList))

    }
}