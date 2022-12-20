package com.qiscus.qiscusmultichannel.util

import android.util.Log
import org.json.JSONObject

fun String?.isValidJson(): Boolean {
    try {
        JSONObject(this)
    } catch (e: Exception) {
        Log.e("Extension","Exception : $e")
        return false
    }
    return true
}