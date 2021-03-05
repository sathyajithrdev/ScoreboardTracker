package com.saj.android.scoreboardtracker.model

data class UserScore(var user: User, var scores: MutableList<Int?>) {
    fun getTotalScore() = scores.sumBy { it ?: 0 }
}