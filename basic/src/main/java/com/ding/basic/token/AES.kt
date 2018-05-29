package com.ding.basic.token

import java.io.UnsupportedEncodingException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException

import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AES {
    companion object {

        @Throws(Exception::class)
        fun encodeAES(value: String, key: String): String {
            val keyByteArray = key.toByteArray()
            val valueByteArray = value.toByteArray(charset("UTF-8"))
            val secretKeySpec = SecretKeySpec(keyByteArray, "AES")
            val parameter = "16-Bytes--String".toByteArray()
            val ivParameterSpec = IvParameterSpec(parameter)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(1, secretKeySpec, ivParameterSpec)
            val encodeBytes = cipher.doFinal(valueByteArray)
            val encoder = Base64.getEncoder()
            return encoder.encodeToString(encodeBytes)
        }

        @Throws(InvalidKeyException::class, NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidAlgorithmParameterException::class, IllegalBlockSizeException::class, BadPaddingException::class, UnsupportedEncodingException::class)
        fun decodeAES(content: String, key: String): String {
            val decoder = Base64.getDecoder()
            val decodeBytes = decoder.decode(content)
            val keyByteArray = key.toByteArray()
            val secretKeySpec = SecretKeySpec(keyByteArray, "AES")
            val parameter = "16-Bytes--String".toByteArray()
            val ivParameterSpec = IvParameterSpec(parameter)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(2, secretKeySpec, ivParameterSpec)
            val result = cipher.doFinal(decodeBytes)
            return String(result, Charsets.UTF_8)
        }
    }
}
