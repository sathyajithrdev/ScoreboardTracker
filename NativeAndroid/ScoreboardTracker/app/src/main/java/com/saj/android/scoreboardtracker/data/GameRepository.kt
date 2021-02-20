package com.saj.android.scoreboardtracker.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class GameRepository : BaseRepository() {

    val tag = "GameRepo"

    val db = FirebaseFirestore.getInstance()

    fun getGameList() {
        db.collection("users")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.let {
                        for (document in it) {
                            Log.d(tag, document.id + " => " + document.data)
                        }
                    }
                } else {
                    Log.w(tag, "Error getting documents.", task.exception)
                }
            }
    }
}