package com.saj.android.scoreboardtracker.model.mappers

import com.saj.android.scoreboardtracker.model.User
import com.saj.android.scoreboardtracker.model.UserScore
import com.saj.android.scoreboardtracker.model.responses.UserScoreResponse

internal fun UserScoreResponse.toDomain(users: List<User>) =
    UserScore(users.first { u -> u.userId == userId }, scores)


internal fun UserScore.toResponse() = UserScoreResponse(user.userId, scores)