package net.lzbook.kit.book.download

import java.io.File

/**
 * Created by xian on 17-6-7.
 */

fun delFile(file: File?){
    file?.deleteRecursively()
}