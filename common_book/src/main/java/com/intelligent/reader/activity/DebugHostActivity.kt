package com.intelligent.reader.activity

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.dingyue.contract.util.SharedPreUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intelligent.reader.R
import kotlinx.android.synthetic.main.activity_debug_host.*
import net.lzbook.kit.request.UrlUtils


/**
 * <pre>
 * Function：host选择
 *
 * Created by JoannChen on 2018/4/19 0019 13:53
 * E-mail:yongzuo_chen@dingyuegroup.cn
 * </pre>
 */
class DebugHostActivity : Activity() {

    private val sharedPreUtil = SharedPreUtil(0)
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

        // 点击选择host
        lv_host.setOnItemClickListener { _, _, position, _ ->
            et_input_host.setText(list[position])
        }

        // 长按删除子条目
        lv_host.setOnItemLongClickListener { _, _, position, _ ->
            delHost(position)
            true
        }


        iv_back.setOnClickListener { finish() }

        // 保存按钮
        tv_save.setOnClickListener {

            if (et_input_host.text.isNotEmpty()) {

                if (!list.contains(et_input_host.text.toString())) {
                    setHost(et_input_host.text.toString())
                }

                UrlUtils.setApiUrl(et_input_host.text.toString())
                sharedPreUtil.putString(SharedPreUtil.API_URL, et_input_host.text.toString())
                sharedPreUtil.putString(SharedPreUtil.WEB_URL, et_input_host.text.toString())
                sharedPreUtil.putBoolean(SharedPreUtil.START_PARAMS, false)
                finish()
            }

        }

    }


    private fun getList(): ArrayList<String> {

        val json = sharedPreUtil.getString(SharedPreUtil.HOST_LIST)
        if (json != "") {

            list = Gson().fromJson(json, object : TypeToken<List<String>>() {}.type)

            return list
        } else {

            list.add("http://8086.zn.bookapi.cn")
            list.add("https://test.lumanman.cc")
            list.add("http://test5.api.bookapi.cn:8888")
            list.add("http://test5.api.bookapi.cn:8880")
            list.add("http://test5.api.bookapi.cn:8080")
            list.add("http://test5.api.bookapi.cn:8090")
            list.add("http://test5.api.bookapi.cn:8088")
            list.add("https://txt.bookapi.cn")
        }

        return list
    }


    /**
     * 添加host
     */
    private fun setHost(host: String) {
        list.add(1, host)
        mAdapter?.notifyDataSetChanged()

        sharedPreUtil.putString(SharedPreUtil.HOST_LIST, Gson().toJson(list))
    }

    /**
     * 删除host
     * 先将sp中的字符串转为list，删除相对应的子条目，在存入sp中
     */
    private fun delHost(position: Int) {
        list.removeAt(position)
        mAdapter?.notifyDataSetChanged()

        val spList = Gson().fromJson<ArrayList<String>>(sharedPreUtil.getString(SharedPreUtil.HOST_LIST),
                object : TypeToken<List<String>>() {}.type)
        spList.removeAt(position)
        sharedPreUtil.putString(SharedPreUtil.HOST_LIST, Gson().toJson(spList))

    }


}