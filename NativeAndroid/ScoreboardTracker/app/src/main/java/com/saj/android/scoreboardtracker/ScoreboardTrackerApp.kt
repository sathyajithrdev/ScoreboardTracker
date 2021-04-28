package com.saj.android.scoreboardtracker

import android.app.Application
import com.saj.android.scoreboardtracker.data.Prefs

class ScoreboardTrackerApp : Application() {

    companion object {
        lateinit var app: ScoreboardTrackerApp
        lateinit var prefs: Prefs
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        prefs = Prefs(applicationContext)
    }
}