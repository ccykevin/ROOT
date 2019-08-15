package com.kevincheng.appextensions

import android.content.SharedPreferences
import com.kevincheng.extensions.defaultSharedPreferences

class Preferences {
    companion object {
        private val sharedPreferences: SharedPreferences by lazy { App.context.defaultSharedPreferences }

        fun get(): SharedPreferences = sharedPreferences

        fun getBoolean(resId: Int, defValue: Boolean): Boolean {
            return sharedPreferences.getBoolean(App.context.getString(resId), defValue)
        }

        fun getBoolean(resId: Int, defResId: Int): Boolean {
            return getBoolean(resId, App.context.resources.getBoolean(defResId))
        }

        fun getString(resId: Int, defValue: String?): String? {
            return sharedPreferences.getString(App.context.getString(resId), defValue)
        }

        fun getString(resId: Int, defResId: Int): String {
            return getString(resId, null) ?: App.context.getString(defResId)
        }
    }
}