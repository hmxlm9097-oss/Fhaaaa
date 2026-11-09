package com.example.data.system

import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * RFC 6238 HMAC-SHA1 TOTP generator & verifier for real 2FA authenticator binding.
 */
object TotpAuthenticator {
    private const val BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun generateSecretBase32(byteCount: Int = 20): String {
        val bytes = ByteArray(byteCount)
        SecureRandom().nextBytes(bytes)
        return encodeBase32(bytes)
    }

    fun buildOtpAuthUri(accountEmail: String, secretBase32: String, issuer: String = "H4XML Cloud"): String {
        val cleanLabel = accountEmail.ifBlank { "operator@h4xml.cloud" }
        return "otpauth://totp/$issuer:$cleanLabel?secret=$secretBase32&issuer=$issuer&algorithm=SHA1&digits=6&period=30"
    }

    fun currentCode(secretBase32: String, timeMillis: Long = System.currentTimeMillis()): String {
        if (secretBase32.isBlank()) return "------"
        val step = timeMillis / 30_000L
        return generateTotpForCounter(secretBase32, step)
    }

    fun remainingSeconds(timeMillis: Long = System.currentTimeMillis()): Int {
        val elapsedInStep = ((timeMillis / 1000L) % 30L).toInt()
        return 30 - elapsedInStep
    }

    fun verifyCode(secretBase32: String, candidateCode: String, timeMillis: Long = System.currentTimeMillis()): Boolean {
        val cleaned = candidateCode.trim()
        if (cleaned.length != 6 || secretBase32.isBlank()) return false
        val currentStep = timeMillis / 30_000L
        for (offset in -1L..1L) {
            if (generateTotpForCounter(secretBase32, currentStep + offset) == cleaned) {
                return true
            }
        }
        return false
    }

    private fun generateTotpForCounter(secretBase32: String, counter: Long): String {
        val keyBytes = decodeBase32(secretBase32)
        if (keyBytes.isEmpty()) return "000000"
        val msg = ByteBuffer.allocate(8).putLong(counter).array()
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(keyBytes, "HmacSHA1"))
        val hash = mac.doFinal(msg)
        val offset = hash[hash.size - 1].toInt() and 0x0F
        val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
            ((hash[offset + 1].toInt() and 0xFF) shl 16) or
            ((hash[offset + 2].toInt() and 0xFF) shl 8) or
            (hash[offset + 3].toInt() and 0xFF)
        val otp = binary % 1_000_000
        return String.format(Locale.US, "%06d", otp)
    }

    private fun encodeBase32(data: ByteArray): String {
        val sb = StringBuilder()
        var buffer = 0
        var bitsLeft = 0
        for (b in data) {
            buffer = (buffer shl 8) or (b.toInt() and 0xFF)
            bitsLeft += 8
            while (bitsLeft >= 5) {
                val index = (buffer shr (bitsLeft - 5)) and 0x1F
                sb.append(BASE32_ALPHABET[index])
                bitsLeft -= 5
            }
        }
        if (bitsLeft > 0) {
            val index = (buffer shl (5 - bitsLeft)) and 0x1F
            sb.append(BASE32_ALPHABET[index])
        }
        return sb.toString()
    }

    private fun decodeBase32(input: String): ByteArray {
        val clean = input.uppercase(Locale.US).replace("=", "").replace(" ", "")
        val out = ArrayList<Byte>()
        var buffer = 0
        var bitsLeft = 0
        for (c in clean) {
            val value = BASE32_ALPHABET.indexOf(c)
            if (value < 0) continue
            buffer = (buffer shl 5) or value
            bitsLeft += 5
            if (bitsLeft >= 8) {
                out.add(((buffer shr (bitsLeft - 8)) and 0xFF).toByte())
                bitsLeft -= 8
            }
        }
        return out.toByteArray()
    }
}
