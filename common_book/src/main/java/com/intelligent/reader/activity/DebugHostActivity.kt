package com.intelligent.reader.activity

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.ding.basic.Config
import com.dingyue.contract.util.showToastMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intelligent.reader.R
import kotlinx.android.synthetic.main.activity_debug_host.*
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.constants.Constants


/**
 * <pre>
 * Function：host选择
 *
 * Created by JoannChen on 2018/4/19 0019 13:53
 * E-mail:yongzuo_chen@dingyuegroup.cn
 * </pre>
 */
class DebugHostActivity : Activity() {

    private val sp = BaseBookApplication.getGlobalContext().getSharedPreferences(Constants.SHAREDPREFERENCES_KEY, 0)
    private val editor = sp.edit()

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

                var type = when (intent.getStringExtra("type")) {
                    Constants.NOVEL_HOST -> {
                        Config.insertRequestAPIHost(et_input_host.text.toString())
                        Constants.NOVEL_HOST
                    }
                    Constants.WEBVIEW_HOST -> {
                        Config.insertWebViewHost(et_input_host.text.toString())
                        Constants.WEBVIEW_HOST
                    }
                    Constants.UNION_HOST -> {
                        Config.insertMicroAPIHost(et_input_host.text.toString())
                        Constants.UNION_HOST
                    }
                    Constants.CONTENT_HOST -> {
                        Config.insertContentAPIHost(et_input_host.text.toString())
                        Constants.CONTENT_HOST
                    }
                    else -> {
                        ""
                    }
                }

                editor.putString(type, et_input_host.text.toString())
                editor.apply()

                finish()
            }

        }

    }


    private fun getList(): ArrayList<String> {

        val json = sp.getString(Constants.HOST_LIST, "")
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

            editor.putString(Constants.HOST_LIST, Gson().toJson(list)).apply()
        }

        return list
    }


    /**
     * 添加host
     */
    private fun setHost(host: String) {
        list.add(1, host)
        mAdapter?.notifyDataSetChanged()

        editor.putString(Constants.HOST_LIST, Gson().toJson(list)).apply()
    }

    /**
     * 删除host
     * 先将sp中的字符串转为list，删除相对应的子条目，在存入sp中
     */
    private fun delHost(position: Int) {
        list.removeAt(position)
        mAdapter?.notifyDataSetChanged()

        val spList = Gson().fromJson<ArrayList<String>>(sp.getString(Constants.HOST_LIST, ""),
                object : TypeToken<List<String>>() {}.type)
        spList.removeAt(position)
        editor.putString(Constants.HOST_LIST, Gson().toJson(spList)).apply()

    }


}