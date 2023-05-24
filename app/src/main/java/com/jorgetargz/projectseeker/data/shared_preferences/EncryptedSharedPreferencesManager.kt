package com.jorgetargz.projectseeker.data.shared_preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject


class EncryptedSharedPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val encryptedPreferencesFileName = "sharedPrefsProjectSeeker"
    private var masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private var prefs: SharedPreferences

    init {
        prefs = initializeEncryptedSharedPreferencesManager()
    }


    private fun initializeEncryptedSharedPreferencesManager(): SharedPreferences {
        return EncryptedSharedPreferences.create(
            encryptedPreferencesFileName,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun <T : Any?> set(key: String, value: T) {
        setValue(key, value)
    }

    fun getString(key: String, defaultValue: String?): String? {
        val value = getValue(key, defaultValue)
        value?.let {
            return value as String
        } ?: run {
            return null
        }
    }

    fun getInt(key: String, defaultValue: Int): Int {
        val value = getValue(key, defaultValue)
        return value as Int
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val value = getValue(key, defaultValue)
        return value as Boolean
    }

    fun getLong(key: String, defaultValue: Long): Long {
        val value = getValue(key, defaultValue)
        return value as Long
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        val value = getValue(key, defaultValue)
        return value as Float
    }

    private fun getValue(key: String, defaultValue: Any?): Any? {
        var value = prefs.all[key]
        value = value ?: defaultValue
        return value
    }

    fun contains(key: String): Boolean {
        return prefs.contains(key)
    }

    private fun edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.prefs.edit()
        operation(editor)
        editor.apply()
    }

    private fun setValue(key: String, value: Any?) {
        when (value) {
            is String? -> edit { it.putString(key, value) }
            is Int -> edit { it.putInt(key, value.toInt()) }
            is Boolean -> edit { it.putBoolean(key, value) }
            is Float -> edit { it.putFloat(key, value.toFloat()) }
            is Long -> edit { it.putLong(key, value.toLong()) }
            else -> {
                Timber.e("Unsupported Type: $value")
            }
        }
    }

    fun remove(key: String) {
        edit { it.remove(key) }
    }

    fun clear() {
        edit() { it.clear() }
    }
}