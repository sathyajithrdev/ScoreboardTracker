package com.saj.android.scoreboardtracker.data

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    private val serveSequenceJson = "serveSequenceJson"

    private val preferences: SharedPreferences =
        context.getSharedPreferences("scoreTracker", Context.MODE_PRIVATE)

    var serveSequenceJsonPref: String?
        get() = preferences.getString(serveSequenceJson, null)
        set(value) = preferences.edit().putString(serveSequenceJson, value).apply()
}