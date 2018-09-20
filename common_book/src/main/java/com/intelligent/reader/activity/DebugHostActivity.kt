package com.intelligent.reader.activity

import android.os.Bundle
import android.widget.ArrayAdapter
import com.ding.basic.Config
import net.lzbook.kit.utils.sp.SharedPreUtil
import net.lzbook.kit.utils.toast.showToastMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intelligent.reader.R
import net.lzbook.kit.base.activity.BaseCacheableActivity
import kotlinx.android.synthetic.main.activity_debug_host.*


/**
 * <pre>
 * Function：host选择
 *
 * Created by JoannChen on 2018/4/19 0019 13:53
 * E-mail:yongzuo_chen@dingyuegroup.cn
 * </pre>
 */
class DebugHostActivity : BaseCacheableActivity() {

    private val sp = SharedPreUtil(SharedPreUtil.SHARE_ONLINE_CONFIG)

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
            showToastMessage("删除成功！")
            true
        }

        // 保存按钮
        tv_save.setOnClickListener {

            if (et_input_host.text.isNotEmpty()) {

                if (!list.contains(et_input_host.text.toString())) {
                    setHost(et_input_host.text.toString())
                }

                val type = when (intent.getStringExtra("type")) {
                    SharedPreUtil.NOVEL_HOST -> {
                        Config.insertRequestAPIHost(et_input_host.text.toString())
                        SharedPreUtil.NOVEL_HOST
                    }
                    SharedPreUtil.WEBVIEW_HOST -> {
                        Config.insertWebViewHost(et_input_host.text.toString())
                        SharedPreUtil.WEBVIEW_HOST
                    }
                    SharedPreUtil.UNION_HOST -> {
                        Config.insertMicroAPIHost(et_input_host.text.toString())
                        SharedPreUtil.UNION_HOST
                    }
                    SharedPreUtil.CONTENT_HOST -> {
                        Config.insertContentAPIHost(et_input_host.text.toString())
                        SharedPreUtil.CONTENT_HOST
                    }
                    else -> {
                        ""
                    }
                }

                sp.putString(type, et_input_host.text.toString())

                finish()
            }

        }

    }


    private fun getList(): ArrayList<String> {

        val json = sp.getString(SharedPreUtil.HOST_LIST, "")
        if (json != "") {

            list = Gson().fromJson(json, object : TypeToken<List<String>>() {}.type)

            return list
        } else {

            list.add("http://8086.zn.bookapi.cn")
            list.add("https://unionapi.bookapi.cn")
            list.add("https://unioncontent.bookapi.cn")
            list.add("https://uniontest.bookapi.cn")

            sp.putString(SharedPreUtil.HOST_LIST, Gson().toJson(list))
        }

        return list
    }


    /**
     * 添加host
     */
    private fun setHost(host: String) {
        list.add(0, host)
        mAdapter?.notifyDataSetChanged()

        sp.putString(SharedPreUtil.HOST_LIST, Gson().toJson(list))
    }

    /**
     * 删除host
     * 先将sp中的字符串转为list，删除相对应的子条目，在存入sp中
     */
    private fun delHost(position: Int) {
        list.removeAt(position)
        mAdapter?.notifyDataSetChanged()

        val spList = Gson().fromJson<ArrayList<String>>(sp.getString(SharedPreUtil.HOST_LIST, ""),
                object : TypeToken<List<String>>() {}.type)
        spList.removeAt(position)
        sp.putString(SharedPreUtil.HOST_LIST, Gson().toJson(spList))

    }


}