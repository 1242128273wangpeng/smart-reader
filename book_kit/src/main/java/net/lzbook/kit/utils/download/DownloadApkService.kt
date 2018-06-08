package net.lzbook.kit.utils.download

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.text.TextUtils
import android.widget.Toast
import net.lzbook.kit.utils.AppLog
import java.io.*
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class DownloadAPKService : Service() {

    private var downloadHashMap = HashMap<String, String>()

    private var receiver: BroadcastReceiver? = null

    private var downloading = false

    private var downloadFilePath: String? = null

    private var downloadUrl: String? = null

    private var downloadName: String? = null

    private var handler: DownloadHandler = DownloadHandler(this)

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, id: Int): Int {
        if (intent != null) {

            Toast.makeText(this, "即将开始下载应用", Toast.LENGTH_SHORT).show()

            val url = intent.getStringExtra("url")
            val name = intent.getStringExtra("name")

            if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(name)) {
                if (!this.downloading) {

                    this.downloading = true

                    downloadUrl = url
                    downloadName = name

                    this.handleDownloadAction(url)

                } else if (this.downloadHashMap.contains(url)) {
                    Toast.makeText(this, "正在下载，请稍后再试...", Toast.LENGTH_SHORT).show()
                } else {
                    this.downloadHashMap.put(url, name)
                }
            } else {
                Toast.makeText(this, "下载地址为空", Toast.LENGTH_SHORT).show()

                this.downloadNextTask()
            }
        }

        return Service.START_STICKY
    }

    override fun onDestroy() {
        if (this.receiver != null) {
            this.unregisterReceiver(this.receiver)
        }

        handler.removeCallbacksAndMessages(null)

        this.downloadHashMap.clear()

        super.onDestroy()
    }


    private fun downloadNextTask() {
        if (!this.downloadHashMap.isEmpty() && downloadHashMap.size > 0) {
            val intent = Intent(this.applicationContext, DownloadAPKService::class.java)

            val entries = downloadHashMap.keys

            val iterator = entries.iterator()

            if (iterator.hasNext()) {
                val url: String = iterator.next()

                if (!TextUtils.isEmpty(url)) {

                    intent.putExtra("url", url)

                    val name: String? = downloadHashMap[url]

                    if (name != null && !TextUtils.isEmpty(name)) {

                        intent.putExtra("name", name)

                        downloadHashMap.remove(url)

                        this.onStartCommand(intent, 0, 0)
                    }
                }
            }
        }
    }

    private fun handleDownloadAction(url: String) {
        object : Thread() {
            override fun run() {
                super.run()

                val message: Message = Message.obtain()

                val availableUrl = this@DownloadAPKService.recursiveTracePath(url)

                if ("mounted" == Environment.getExternalStorageState()) {

                    downloadFilePath = Environment.getExternalStorageDirectory().absolutePath + "/apk/" + downloadName + ".apk"

                    val file = File(downloadFilePath)

                    if (file.exists()) {
                        AppLog.e("DownloadAPKService", "APK已存在！")
                        message.what = 1
                        this@DownloadAPKService.handler.sendMessage(message)
                    } else {
                        message.what = 0
                        message.obj = availableUrl
                        this@DownloadAPKService.handler.sendMessage(message)
                    }
                } else {
                    AppLog.e("DownloadAPKService", "SD卡不可用！")
                    this@DownloadAPKService.downloading = false

                    message.what = 2
                    message.obj = "sd卡不可用"
                    this@DownloadAPKService.handler.sendMessage(message)
                }
            }
        }.start()
    }

    fun recursiveTracePath(path: String): String {
        val url: URL?
        var availableUrl = ""
        var httpURLConnection: HttpURLConnection? = null

        try {
            url = URL(path)
            httpURLConnection = url.openConnection() as HttpURLConnection
            httpURLConnection.readTimeout = 30000
            httpURLConnection.connectTimeout = 30000
            httpURLConnection.instanceFollowRedirects = false

            val responseCode = httpURLConnection.responseCode

            if (this.checkResponseCode(responseCode)) {
                var location: String? = httpURLConnection.getHeaderField("Location")

                if (location == null) {
                    location = httpURLConnection.getHeaderField("location")
                }

                if (!location!!.startsWith("http://") && !location.startsWith("https://")) {
                    val originalUrl = URL(path)
                    location = originalUrl.protocol + "://" + originalUrl.host + location
                }

                if (downloadHashMap.containsKey(path)) {
                    val name = downloadHashMap[path]

                    if (name != null && !TextUtils.isEmpty(name)) {
                        downloadHashMap.remove(path)
                        downloadHashMap.put(location, name)
                    }
                }

                return recursiveTracePath(location)
            }

            availableUrl = path
        } catch (malformedURLException: MalformedURLException) {
            AppLog.e("DownloadAPKService", malformedURLException.toString())
            this.downloading = false
            malformedURLException.printStackTrace()
        } catch (ioException: IOException) {
            AppLog.e("DownloadAPKService", ioException.toString())
            this.downloading = false
            ioException.printStackTrace()
        } catch (exception: Exception) {
            AppLog.e("DownloadAPKService", exception.toString())
            this.downloading = false
            exception.printStackTrace()
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect()
            }
        }

        return availableUrl
    }

    private fun checkResponseCode(code: Int): Boolean {
        return code == 301 || code == 302 || code == 303 || code == 307
    }


    private fun handleDownloadFinishAction() {
        val intent = Intent("android.intent.action.VIEW")

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

        intent.setDataAndType(Uri.fromFile(File(downloadFilePath)), "application/vnd.android.package-archive")

        this.startActivity(intent)

        this.downloading = false

        this.downloadNextTask()
    }

    private fun handleDownload(url: String) {
        AppLog.e("DownloadAPKService", "开始下载: " + url)

        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, "下载失败！", Toast.LENGTH_SHORT).show()
            return
        }
        val downloadManager = this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val request = DownloadManager.Request(Uri.parse(url))

        request.setMimeType("application/vnd.android.package-archive")
        request.setDestinationInExternalPublicDir("apk", downloadName + ".apk")

        downloadManager.enqueue(request)
    }

    private class DownloadHandler(downloadAPKService: DownloadAPKService) : Handler() {

        internal var reference: WeakReference<DownloadAPKService> = WeakReference(downloadAPKService)

        override fun handleMessage(message: Message) {
            val downloadApkService = reference.get()

            if (downloadApkService != null) {
                when (message.what) {
                    0 -> {
                        if (downloadApkService.receiver != null) {
                            downloadApkService.unregisterReceiver(downloadApkService.receiver)
                            downloadApkService.receiver = null
                        }

                        downloadApkService.receiver = object : BroadcastReceiver() {
                            override fun onReceive(context: Context, intent: Intent) {
                                downloadApkService.handleDownloadFinishAction()
                            }
                        }

                        downloadApkService.registerReceiver(downloadApkService.receiver, IntentFilter("android.intent.action.DOWNLOAD_COMPLETE"))

                        try {
                            downloadApkService.handleDownload(message.obj as String)
                        } catch (exception: Exception) {
                            downloadApkService.downloading = false
                            exception.printStackTrace()
                        }

                    }
                    1 -> downloadApkService.handleDownloadFinishAction()
                    2 -> Toast.makeText(downloadApkService, message.obj as String, Toast.LENGTH_SHORT).show()
                }
            }
            super.handleMessage(message)
        }
    }
}