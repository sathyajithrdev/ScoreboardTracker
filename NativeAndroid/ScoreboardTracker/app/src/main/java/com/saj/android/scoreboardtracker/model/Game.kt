package com.saj.android.scoreboardtracker.model

import com.google.firebase.Timestamp
import com.saj.android.scoreboardtracker.extensions.nullIfEmpty
import com.saj.android.scoreboardtracker.extensions.serialize
import com.saj.android.scoreboardtracker.model.mappers.toResponse

data class Game(
    var gameId: String,
    var isCompleted: Boolean,
    var winnerId: String,
    var loserId: String,
    var timestamp: Timestamp = Timestamp.now(),
    var userScores: List<UserScore>
) {

    fun getDate() = timestamp.toDate()

    fun getGameJsonMap() = mapOf(
        "isCompleted" to isCompleted,
        "looserId" to loserId.nullIfEmpty(),
        "winnerId" to winnerId.nullIfEmpty(),
        "timeStamp" to timestamp,
        "scoresJson" to userScores.map { it.toResponse() }.serialize()
    )
}