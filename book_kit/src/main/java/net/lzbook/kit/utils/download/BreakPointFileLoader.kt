package net.lzbook.kit.utils.download

import android.util.Log
import net.lzbook.kit.utils.msDebuggAble
import org.apache.http.HttpStatus
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by xian on 17-6-5.
 */

var HTTP_CONNECTION_TIMEOUT = 10000
var HTTP_READ_TIMEOUT = 30000

var RETRY_TIMES = 3

private fun log(str: String, vararg param: Any?) {
    if (msDebuggAble) {
        var builder = StringBuilder()
        builder.append(str)
        param.forEach {
            builder.append(" | " + it.toString())
        }
        Log.d("loader", builder.toString())
    }
}

class BreakPointFileLoader(val id: String, val url: String, val file: File, val error: ((id: String, msg: String) -> Unit)? = null, val progress: ((id: String, progress: Int) -> Boolean)? = null, val finish: ((id: String) -> Unit)? = null) {

    var index = 0
    var fileLength = 0
    var etag: String? = null

    val statusFile = File(file.parentFile, id)
    val status = Properties()

    var retry = 0

    var stoped = false

    var connection: HttpURLConnection? = null

    var isDownloading = AtomicBoolean(false)

    val lock = Object()

    init {

        var oldFileName: String? = null

        statusFile.parentFile.mkdirs()

        if (file.exists() && statusFile.exists()) {
            var inputStream: InputStream? = null
            try {
                val inputStream = statusFile.inputStream()
                status.load(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                inputStream?.close()
            }
            index = status.getProperty("index", "0").toInt()
            fileLength = status.getProperty("length", "0").toInt()
            oldFileName = status.getProperty("name")
            etag = status.getProperty("etag")
        }

        if (oldFileName != null && !file.name.equals(oldFileName)) {
            resetStatus()
        }

        log("init", fileLength, index, etag)
    }

    private fun resetStatus() {
        log("resetStatus")
        status.clear()
        statusFile.delete()
        statusFile.createNewFile()

        index = 0
        fileLength = 0
        etag = null
    }

    fun load() {
        isDownloading.set(true)
        try {
            if (index > 0 && checkEtag()) {
                if (index == fileLength) {
                    log("load", "文件上次已经下载完成了")
                    isDownloading.set(false)
                    finish?.invoke(id)
                    return
                } else {
                    loadPartial()
                }
            } else {
                log("load", "文件上次已经下载完成了, 但是文件改变了")
                resetStatus()
                loadFull()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            storeStatus()
            retry(e.message ?: e.toString())
        }

    }

    fun pause() {
        log("pause")
        stoped = true
        if (isDownloading.get()) {
            synchronized(lock) {
                try {
                    log("pause waitting start")
                    lock.wait()
                    log("pause waitting end")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun delete() {
        log("delete")
        stoped = true

        if (isDownloading.get()) {
            synchronized(lock) {
                try {
                    lock.wait()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        file.delete()
        statusFile.delete()
    }


    private fun createConnection(): HttpURLConnection {
        connection?.disconnect()

        connection = URL(url).openConnection() as HttpURLConnection
        connection!!.connectTimeout = HTTP_CONNECTION_TIMEOUT
        connection!!.readTimeout = HTTP_READ_TIMEOUT
        connection!!.doInput = true
        return connection!!
    }


    fun checkEtag(): Boolean {
        val connection = createConnection()
        connection.connect()
        val field = connection.getHeaderField("Etag")
        log("checkEtag", etag ?: "null", field)
        return etag?.equals(field, true) ?: false
    }


    fun loadPartial() {

        val connection = createConnection()

        connection.addRequestProperty("Range", "bytes=$index-")

        log("loadPartial headers", connection.requestProperties)

        connection.connect()

        log("loadPartial headers", connection.headerFields)

        when (connection.responseCode) {
            HttpStatus.SC_PARTIAL_CONTENT -> {
                if (etag?.equals(connection.getHeaderField("Etag"), true) ?: false) {
                    val randomAccessFile = RandomAccessFile(file, "rwd")
                    randomAccessFile.seek(index.toLong())
                    var startTime = System.currentTimeMillis()
                    var flag = -1
                    var buf = ByteArray(1024 * 64)
                    flag = connection.inputStream.read(buf)
                    while (!stoped && flag != -1) {
                        randomAccessFile.write(buf, 0, flag)
                        index += flag
                        status.setProperty("index", "$index")

                        var ret = progress?.invoke(id, (index.toFloat() / fileLength * 100).toInt()) ?: true
                        if (!ret) {
                            stoped = true
                            break
                        }

                        flag = connection.inputStream.read(buf)

                        if (System.currentTimeMillis() - startTime > 200) {
                            storeStatus()
                            startTime = System.currentTimeMillis()
                        }
                    }


                    randomAccessFile.close()
                    connection.inputStream.close()
                    connection.disconnect()

                    //保存状态
                    storeStatus()

                    if (!stoped) {
                        if (index == status.getProperty("length").toInt()) {
                            isDownloading.set(false)
                            finish?.invoke(id)
                        } else {
                            //需要从头来了
                            resetStatus()
                            retry("complete but size not match")
                        }
                    } else {
                        notifyStoped()
                    }


                } else {
                    //缓存已经失效
                    log("缓存已经失效", etag, connection.getHeaderField("Etag"))
                    resetStatus()
                    retry("缓存已经失效")
                }
            }

            HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE -> {
                //服务器不支持断点
                resetStatus()
                retry("服务器不支持断点")
            }

            else -> {
                //状态吗错误
                retry("状态码错误")
            }
        }
    }

    fun loadFull() {
        val connection = createConnection()
        connection.connect()
        log("loadFull headers", connection.headerFields)
        when (connection.responseCode) {
            HttpStatus.SC_OK -> {
                fileLength = connection.contentLength
                status.setProperty("name", file.name)
                status.setProperty("etag", connection.getHeaderField("Etag"))
                status.setProperty("length", connection.contentLength.toString())

                storeStatus()


                var startTime = System.currentTimeMillis()

                var out = file.outputStream()
                var flag = -1
                var buf = ByteArray(1024 * 64)
                flag = connection.inputStream.read(buf)
                while (!stoped && flag != -1) {
                    out.write(buf, 0, flag)
                    index += flag
                    status.setProperty("index", "$index")

                    var ret = progress?.invoke(id, (index.toFloat() / fileLength * 100).toInt()) ?: true
                    if (!ret) {
                        stoped = true
                        break
                    }

                    flag = connection.inputStream.read(buf)

                    if (System.currentTimeMillis() - startTime > 200) {
                        storeStatus()
                        startTime = System.currentTimeMillis()
                    }
                }
                out.close()
                connection.inputStream.close()
                connection.disconnect()

                storeStatus()

                if (!stoped) {
                    if (index == status.getProperty("length").toInt()) {
                        isDownloading.set(false)
                        finish?.invoke(id)
                    }
                } else {
                    notifyStoped()
                }
            }

            else -> {
                retry("responeCode:${connection.responseCode}, msg:${connection.responseMessage}")
            }
        }

    }

    private fun storeStatus() {
        var outputStream: OutputStream? = null
        try {
            outputStream = statusFile.outputStream()
            status.store(outputStream, "${Date()}")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                outputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun retry(msg: String) {
        if (!stoped) {
            retry++
            if (retry < RETRY_TIMES) {
                log("retry $retry : $msg")
                load()
            } else {
                isDownloading.set(false)
                error?.invoke(id, msg)
            }
        } else {
            notifyStoped()
        }
    }

    private fun notifyStoped() {
        Exception("notifyStoped").printStackTrace()
        isDownloading.set(false)
        synchronized(lock) {
            lock.notify()
        }
    }


}