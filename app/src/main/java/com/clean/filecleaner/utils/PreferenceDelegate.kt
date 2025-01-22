package com.clean.filecleaner.utils

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PreferenceDelegate<T>(
    private val preferences: SharedPreferences,
    private val defaultValue: T
) : ReadWriteProperty<Any?, T> {

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return when (defaultValue) {
            is Boolean -> preferences.getBoolean(property.name, defaultValue) as T
            is Int -> preferences.getInt(property.name, defaultValue) as T
            is Long -> preferences.getLong(property.name, defaultValue) as T
            is Float -> preferences.getFloat(property.name, defaultValue) as T
            is String -> preferences.getString(property.name, defaultValue) as T
            is Double -> preferences.getFloat(property.name, defaultValue.toFloat()) as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        with(preferences.edit()) {
            when (value) {
                is Boolean -> putBoolean(property.name, value)
                is Int -> putInt(property.name, value)
                is Long -> putLong(property.name, value)
                is Float -> putFloat(property.name, value)
                is String -> putString(property.name, value)
                is Double -> putFloat(property.name, value.toFloat())
                else -> throw IllegalArgumentException("Unsupported type")
            }
            apply()
        }
    }
}

fun <T> SharedPreferences.prefDelegate(defaultValue: T): ReadWriteProperty<Any?, T> {
    return PreferenceDelegate(this, defaultValue)
}