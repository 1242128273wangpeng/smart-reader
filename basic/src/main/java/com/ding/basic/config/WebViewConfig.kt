package com.ding.basic.config

import com.ding.basic.net.Config


/**
 * Desc：WebView配置管理类
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/12/6 0006 15:42
 */
object WebViewConfig {

    /**
     * WebView静态压缩包路径
     */
    var zipPath = ""

    /**
     * WebView加载路径
     */
    var urlPath = ""

    /**
     * webViewBaseHost默认地址，带时间戳
     */
    var urlPathTimeTemp = ""

    private fun timeTemp(): String {
        return zipPath.split("/")[1].split(".")[0]
    }

    @JvmStatic
    fun initWebViewConfig() {
        when (Config.loadRequestParameter("packageName")) {

            "cc.quanben.novel" -> { //五步替
                zipPath = "qbmfxsydq/2018.zip"
            }

            "cc.kdqbxs.reader" -> { //快读替
                zipPath = "mfqbxssc/2018.zip"
            }

            "cc.mianfeinovel" -> { //阅微替
                zipPath = "zsmfqbxs/2018.zip"
            }

            "cn.qbzsydsq.reader" -> { //全本追书阅读器
                zipPath = "qbzsydq/2018.zip"
            }

            "cc.lianzainovel" -> { //鸿雁替
                zipPath = "txtqbmfxs/2018.zip"
            }

            "cc.quanbennovel" -> { //今日多看
                zipPath = "qbmfkdxs/201812132009.zip"
                urlPath = "https://sta-ccquanbennovel.zhuishuwang.com/cc-quanbennovel/"
            }

            "cc.remennovel" -> { //智胜电子书替
                zipPath = "txtqbdzs/2018.zip"
            }

            "cn.txtqbmfyd.reader" -> { //新壳1
                zipPath = "txtqbmfyd/2018.zip"
            }

            "cn.mfxsqbyd.reader" -> { //新壳2
                zipPath = "mfxsqbyd/2018.zip"
            }

            "cn.qbmfrmxs.reader" -> { //新壳3
                zipPath = "qbmfrmxs/2018.zip"
            }

            "cn.qbmfkkydq.reader" -> { //新壳4
                zipPath = "qbmfkkydq/201812051021.zip"
                urlPath = "https://sta-cnqbmfkkydqreader.bookapi.cn/cn-qbmfkkydq-reader/"
            }
        }
        urlPathTimeTemp = "$urlPath${timeTemp()}"
    }


}