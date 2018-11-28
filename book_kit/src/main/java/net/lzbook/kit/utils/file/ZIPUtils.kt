package net.lzbook.kit.utils.file

import android.content.Context
import com.ding.basic.util.ReplaceConstants
import net.lzbook.kit.utils.web.WebResourceCache

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Desc 解压工具类
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/23 0019 12:05
 */
object ZIPUtils {

    /**
     * 解压普通文件夹的zip压缩文件到指定目录
     *
     * @param inputDirectory  输入目录
     * @param outputDirectory 输出目录
     * @param isReWrite       是否覆盖
     * @throws IOException
     */
    @Throws(IOException::class)
    fun unZipFolder(inputDirectory: String, outputDirectory: String, isReWrite: Boolean) {
        // 从普通目录打开压缩文件
        val fileInputStream = FileInputStream(inputDirectory)
        stream2File(fileInputStream, outputDirectory, isReWrite)
    }

    /**
     * 解压assets的zip压缩文件到指定目录
     *
     * @param context         上下文对象
     * @param assetName       压缩文件名
     * @param outputDirectory 输出目录
     * @param isReWrite       是否覆盖
     * @throws IOException
     */
    @Throws(IOException::class)
    fun unZipAssets(context: Context, assetName: String, outputDirectory: String, isReWrite: Boolean) {

        // 从assets打开压缩文件
        val inputStream = context.assets.open(assetName)
        stream2File(inputStream, outputDirectory, isReWrite)
    }


    @Throws(IOException::class)
    private fun stream2File(inputStream: InputStream, outputDirectory: String, isReWrite: Boolean) {
        // 创建解压目标目录
        var file: File

        // 打开压缩文件
        val zipInputStream = ZipInputStream(inputStream)
        // 读取一个进入点
        var zipEntry: ZipEntry? = zipInputStream.nextEntry
        // 使用1M buffer
        val buffer = ByteArray(1024 * 1024)
        // 如果进入点为空说明已经遍历完所有压缩包中文件和目录
        while (zipEntry != null) {
            if (zipEntry.isDirectory) {// 如果是一个目录
                file = File(outputDirectory + zipEntry.name)
                // 文件需要覆盖或者是文件不存在
                if (isReWrite || !file.exists()) {
                    file.mkdir()
                }
            } else {// 如果是文件

                file = File(outputDirectory + zipEntry.name)

                // 文件需要覆盖或者文件不存在，则解压文件
                if (isReWrite || !file.exists()) {
                    file.createNewFile()
                    val fileOutputStream = FileOutputStream(file)

                    var counts = 0
                    while ({ counts = zipInputStream.read(buffer);counts }() > 0) {
                        fileOutputStream.write(buffer, 0, counts)
                    }
                    fileOutputStream.close()
                }
            }
            // 定位到下一个文件入口
            zipEntry = zipInputStream.nextEntry
        }
        zipInputStream.close()
        inputStream.close()
    }
}


