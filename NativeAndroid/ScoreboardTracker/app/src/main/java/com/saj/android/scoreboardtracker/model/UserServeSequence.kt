package com.saj.android.scoreboardtracker.model

import kotlinx.serialization.Serializable

@Serializable
data class UserServeSequence(
    var userId: String,
    var userName: String,
    var isNextServer: Boolean,
    var order: Int?
)