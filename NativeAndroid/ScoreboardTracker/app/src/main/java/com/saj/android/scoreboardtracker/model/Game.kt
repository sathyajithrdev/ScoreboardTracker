package com.saj.android.scoreboardtracker.model

import com.google.firebase.Timestamp


data class Game(
    var gameId: String,
    var isCompleted: Boolean,
    var winnerId: String,
    var loserId: String,
    var timestamp: Timestamp,
    var userScores: List<UserScore>
) {

    fun getDate() = timestamp.toDate()
}