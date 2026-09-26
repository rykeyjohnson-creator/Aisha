package com.example.jarvisalexa.aisha.companion

import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

object CompanionCrypto {

    private const val IV_SIZE = 12
    private const val TAG_SIZE = 128

    private fun key(secret: String): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(secret.toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(digest, "AES")
    }

    fun encrypt(request: CompanionRequest, secret: String): String {
        require(secret.isNotBlank()) { "Companion secret missing" }

        val json = JSONObject().apply {
            put("id", request.id)
            put("action", request.action)
            put("timestamp", request.timestamp)

            val p = JSONObject()
            request.params.forEach { (k, v) -> p.put(k, v) }
            put("params", p)
        }.toString()

        val iv = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.ENCRYPT_MODE,
            key(secret),
            GCMParameterSpec(TAG_SIZE, iv)
        )

        val encrypted = cipher.doFinal(
            json.toByteArray(StandardCharsets.UTF_8)
        )

        return Base64.encodeToString(iv + encrypted, Base64.NO_WRAP)
    }

    fun decrypt(payload: String, secret: String): String {
        require(secret.isNotBlank()) { "Companion secret missing" }

        val raw = Base64.decode(payload, Base64.NO_WRAP)
        require(raw.size > IV_SIZE) { "Invalid Companion payload" }

        val iv = raw.copyOfRange(0, IV_SIZE)
        val encrypted = raw.copyOfRange(IV_SIZE, raw.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            key(secret),
            GCMParameterSpec(TAG_SIZE, iv)
        )

        return String(
            cipher.doFinal(encrypted),
            StandardCharsets.UTF_8
        )
    }
}
