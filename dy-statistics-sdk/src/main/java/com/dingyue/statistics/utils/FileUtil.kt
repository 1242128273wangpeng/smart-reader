package com.dingyue.statistics.utils

import android.os.Environment

import com.dingyue.statistics.common.GlobalContext
import com.dingyue.statistics.log.AppLog

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

object FileUtil {

    private val TAG = FileUtil::class.java.name
    private val BUFFER_SIZE = 8192

    private fun fileIsExist(filePath: String?): Boolean {
        if (filePath.isNullOrBlank()) {
            return false
        }
        val f = File(filePath)
        return f.exists()
    }

    private fun readFile(filePath: String): InputStream? {
        var `is`: InputStream? = null
        if (fileIsExist(filePath)) {
            val f = File(filePath)
            try {
                `is` = FileInputStream(f)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
        return `is`
    }

    fun readBytes(filePath: String): ByteArray? {
        var data: ByteArray? = null
        val inputstream = readFile(filePath) ?: return null
        var bis:BufferedInputStream? = BufferedInputStream(inputstream)
        var outStream: ByteArrayOutputStream? = ByteArrayOutputStream()
        val buffer = ByteArray(BUFFER_SIZE)
        try {
            var len = bis?.read(buffer) ?: -1
            while ( len!= -1) {
                outStream?.write(buffer, 0, len)
                len = bis?.read(buffer) ?: -1
            }
            data = outStream?.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
            return data
        } finally {
            if (bis != null) {
                try {
                    bis.close()
                    bis = null
                } catch (e: IOException) { }
            }
            if (outStream != null) {
                try {
                    outStream.close()
                    outStream = null
                } catch (e: IOException) { }
            }
        }
        // 把outStream里的数据写入内存
        return data
    }


    fun writeByteFile(filePath: String, bytes: ByteArray): Boolean {
        var success = true
        val distFile = File(filePath)
        if (!distFile.parentFile.exists()) {
            try {
                distFile.parentFile.mkdirs()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        var bos: BufferedOutputStream? = null
        try {
            bos = BufferedOutputStream(FileOutputStream(filePath), BUFFER_SIZE)
            bos.write(bytes)
        } catch (e: Exception) {
            AppLog.e(TAG, "save $filePath failed!")
            success = false
            e.printStackTrace()
        } finally {
            if (bos != null) {
                try {
                    bos.close()
                    bos = null
                } catch (e: IOException) { }
            }
        }
        return success
    }

    /**
     * 追加文本日志
     *
     * @param fileName
     * @param log
     */
    fun appendLog(fileName: String, log: String) {
        var fos: FileOutputStream? = null
        try {
            val dir = File(Environment.getExternalStorageDirectory().absolutePath
                    + File.separator + "log" + File.separator + GlobalContext.getGlobalContext().packageName)
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            if (!file.exists()) {
                file.createNewFile()
            }
            fos = FileOutputStream(file, true)
            fos.write((log + "\n").toByteArray())
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }


}
