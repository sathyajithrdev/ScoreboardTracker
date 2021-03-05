package com.saj.android.scoreboardtracker.model.responses

import kotlinx.serialization.Serializable

@Serializable
data class UserScoreResponse(val userId: String, val scores: MutableList<Int?>)