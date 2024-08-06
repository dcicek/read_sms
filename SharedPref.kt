package com.example.getsms

import android.content.Context
import android.content.SharedPreferences

class SharedPrefManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    fun save(key: String, value: String) {
        val editor = prefs.edit()
        editor.putString(key, value)
        val success = editor.commit()
        if (success) {
            println("Veri başarıyla kaydedildi.")
        } else {
            println("Veri kaydedilirken bir hata oluştu.")
        }
    }

    fun read(key: String): String {
        return try {
            val value = prefs.getString(key, "") ?: ""
            println("Okunan veri: $value")
            value
        } catch (e: Exception) {
            println("Veri okunurken bir hata oluştu: ${e.message}")
            ""
        }
    }
}
