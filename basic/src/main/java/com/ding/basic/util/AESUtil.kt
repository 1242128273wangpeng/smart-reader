package com.ding.basic.util

import android.util.Base64

import com.orhanobut.logger.Logger

import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

import java.nio.charset.Charset
import java.security.NoSuchAlgorithmException

object AESUtil {

    private val parameterSpec = IvParameterSpec(convertString("123456789dingyue"))

    private const val transform = "AES/CBC/PKCS5Padding"

    private var cipher: Cipher? = null

    init {
        try {
            cipher = Cipher.getInstance(transform)
        } catch (exception: NoSuchAlgorithmException) {
            exception.printStackTrace()
        } catch (exception: NoSuchPaddingException) {
            exception.printStackTrace()
        }
    }

    /***
     * 加密
     * **/
    fun encrypt(message: String, key: String?): String? {
        if (key == null) {
            Logger.e("鉴权加密：Key为空！")
            return null
        }

        if (key.length != 16) {
            Logger.e("鉴权加密：Key长度不为16位！")
            return null
        }

        val keyBytes = convertString(key)

        val secretKeySpec = SecretKeySpec(keyBytes, "AES")

        try {
            cipher!!.init(Cipher.ENCRYPT_MODE, secretKeySpec, parameterSpec)

            val encrypted = cipher!!.doFinal(convertString(message))

            return Base64.encodeToString(encrypted, Base64.DEFAULT)

        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        return ""
    }

    /***
     * 解密
     * **/
    fun decrypt(auth: String, key: String?): String? {
        try {
            if (key == null) {
                Logger.e("鉴权解密：Key为空！")
                return null
            }

            if (key.length != 16) {
                Logger.e("鉴权解密：Key长度不为16位！")
                return null
            }

            val keyBytes = convertString(key)

            val secretKeySpec = SecretKeySpec(keyBytes, "AES")

            cipher!!.init(Cipher.DECRYPT_MODE, secretKeySpec, parameterSpec)

            val encryptedBytes = Base64.decode(auth, Base64.DEFAULT)

            try {
                val result = cipher!!.doFinal(encryptedBytes)
                return String(result)
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        return ""
    }

    /***
     * 将字符串转为UTF-8字节组
     */
    private fun convertString(input: String): ByteArray {
        return input.toByteArray(Charset.forName("UTF-8"))
    }


    //    private void check() {
    //        Map<String, String[]> parameterMap = new TreeMap<>(request.getParameterMap());
    ////验签
    //        StringBuilder sb = new StringBuilder();
    //        parameterMap.entrySet().forEach(stringEntry -> {
    //            if (!StringUtils.equals(stringEntry.getKey(), "sign")) {
    //                sb.append(stringEntry.getKey());
    //                sb.append("=");
    //                sb.append(stringEntry.getValue()[0]);
    //            }
    //        });
    //        sb.append("privateKey=");
    //        sb.append(privateKey);
    //
    //
    //        String sign = DigestUtils.md5Hex(sb.toString());
    //    }


    //    public static void main(String[] args) {
    //        String s = "aWo1MEY5vZgvIP+NNhb2bU6MpVvg7wND+dedeKhM47o2sLppZdYmeMT/ZPblKjn2KA6HtqJ8RnXq7KzvE+59kjFTWT7JWK3krutnh3L4O011AxrndvTDtF3qqoqE05zJRLTXbv7CZgQ6w6COkzkspg==";
    //        System.out.println(decrypt(s, "wangpeng12345678"));
    //    }
}
