package com.yogadimas.simastekom.core.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

inline fun <reified T> T.toQueryMap(): Map<String, String> {
    val gson = Gson()
    val json = gson.toJson(this)
    val type = object : TypeToken<Map<String, Any?>>() {}.type
    val map: Map<String, Any?> = gson.fromJson(json, type)

    return map
        .filterValues { it != null }
        .mapValues { (_, value) ->
            when (value) {
                is Double -> value.toInt().toString()
                else -> value.toString()
            }
        }
}
