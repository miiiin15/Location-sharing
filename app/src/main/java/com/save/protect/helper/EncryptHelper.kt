package kr.co.essb.app.data.helper

import java.math.BigInteger
import java.security.MessageDigest

object EncryptHelper {
    fun getSHA256(input: String): String {

        var toReturn: String = ""
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.reset()
            digest.update(input.toByteArray(charset("utf8")))
            toReturn = String.format("%064x", BigInteger(1, digest.digest()))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return toReturn
    }

    fun getSHA512(input: String): String {

        var toReturn: String = ""
        try {
            val digest = MessageDigest.getInstance("SHA-512")
            digest.reset()
            digest.update(input.toByteArray(charset("utf8")))
            toReturn = String.format("%0128x", BigInteger(1, digest.digest()))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return toReturn
    }

}