package com.remlocteam.remloc1.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object SecureField {

    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "RemLocFieldEncryptionKey"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_LENGTH = 12
    private const val TAG_LENGTH = 128
    private const val ENCRYPTED_PREFIX = "enc::"

    fun encrypt(value: String?): String? {
        if (value.isNullOrEmpty()) return value
        if (value.startsWith(ENCRYPTED_PREFIX)) return value

        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            val encrypted = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))

            val payload = ByteBuffer.allocate(IV_LENGTH + encrypted.size)
            payload.put(cipher.iv)
            payload.put(encrypted)

            ENCRYPTED_PREFIX + Base64.encodeToString(payload.array(), Base64.NO_WRAP)
        } catch (_: Exception) {
            value
        }
    }

    fun decrypt(value: String?): String? {
        if (value.isNullOrEmpty()) return value
        if (!value.startsWith(ENCRYPTED_PREFIX)) return value

        return try {
            val encoded = value.removePrefix(ENCRYPTED_PREFIX)
            val raw = Base64.decode(encoded, Base64.NO_WRAP)
            if (raw.size <= IV_LENGTH) return value

            val buffer = ByteBuffer.wrap(raw)
            val iv = ByteArray(IV_LENGTH)
            buffer.get(iv)
            val encrypted = ByteArray(buffer.remaining())
            buffer.get(encrypted)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_LENGTH, iv))
            val plain = cipher.doFinal(encrypted)
            String(plain, StandardCharsets.UTF_8)
        } catch (_: Exception) {
            value
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }
}
