package com.saj.android.scoreboardtracker.ui.base

import android.widget.Toast
import androidx.activity.ComponentActivity

open class BaseActivity : ComponentActivity() {

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}