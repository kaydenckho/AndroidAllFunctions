package com.example.androidallfunctions.utils

import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class AES private constructor() {

    companion object{
        fun get() = AES()
    }

    private val ByteArray.asHexUpper: String
        inline get() {
            return this.joinToString(separator = "") {
                String.format("%02X", (it.toInt() and 0xFF))
            }
        }

    private val String.decodeHex: ByteArray
    inline get(){
        check(length % 2 == 0) { "Must have an even length" }
        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    fun genKey(): SecretKey{
        val keygen = KeyGenerator.getInstance("AES")
        keygen.init(256)
        return keygen.generateKey()
    }

    fun encrypt(plaintext:String, key: SecretKey) : Pair<String, String>{
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val ciphertext = cipher.doFinal(plaintext.toByteArray()).asHexUpper
        val iv = cipher.iv.asHexUpper
        return Pair(ciphertext, iv)
    }

    fun decrypt(ciphertext: String, key: SecretKey,  iv: String) : String{
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv.decodeHex))
        val plaintext = cipher.doFinal(ciphertext.decodeHex)
        return plaintext.decodeToString()
    }

    fun sha256(plaintext: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(plaintext.toByteArray()).asHexUpper
    }

}