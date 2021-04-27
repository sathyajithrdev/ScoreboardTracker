package com.saj.android.scoreboardtracker.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.saj.android.scoreboardtracker.extensions.deserialize
import com.saj.android.scoreboardtracker.model.Game
import com.saj.android.scoreboardtracker.model.User
import com.saj.android.scoreboardtracker.model.mappers.toDomain
import com.saj.android.scoreboardtracker.model.responses.UserScoreResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.util.*

class GameRepository : BaseRepository() {

    private val tag = "GameRepo"

    private val db = FirebaseFirestore.getInstance()

    @ExperimentalCoroutinesApi
    fun getUsersList(): Flow<List<User>> {
        return callbackFlow {
            db.collection("users")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.let { result ->
                            val users = result.map {
                                User(
                                    it["userId"].toString(),
                                    it["name"].toString(),
                                    it["profileUrl"].toString()
                                )
                            }
                            offer(users)
                            close()
                        }
                    } else {
                        Log.w(tag, "Error getting getUsersList.", task.exception)
                        close()
                    }
                }
            awaitClose { }
        }.flowOn(Dispatchers.IO)
    }

    fun getCurrentOnGoingGame(groupId: String, users: List<User>): Flow<Game> {
        return callbackFlow {
            db.collection("groups/$groupId/games").whereEqualTo("isCompleted", false)
                .limit(1)
                .addSnapshotListener { value, ex ->
                    if (ex == null) {
                        value?.let { result ->
                            result.firstOrNull()?.let { data ->
                                val game = Game(
                                    data.id,
                                    data["isCompleted"] as Boolean,
                                    data["winnerId"].toString(),
                                    data["looserId"].toString(),
                                    data["timeStamp"] as Timestamp? ?: Timestamp(
                                        GregorianCalendar(2000, 1, 1).time
                                    ),
                                    data["scoresJson"].toString()
                                        .deserialize<List<UserScoreResponse>>()
                                        .map { it.toDomain(users) }
                                )
                                offer(game)
                            }
                        }
                    } else {
                        Log.w(tag, "Error getting documents.", ex)
                        close()
                    }
                }
            awaitClose { }
        }.flowOn(Dispatchers.IO)
    }

    fun updateGameToServer(groupId: String, game: Game): Flow<Boolean> {
        return callbackFlow {
            game.timestamp = Timestamp.now()
            db.collection("groups/$groupId/games").document(game.gameId).update(
                game.getGameJsonMap()
            ).addOnCompleteListener { task ->
                offer(task.isSuccessful)
                close()
            }
            awaitClose { }
        }.flowOn(Dispatchers.IO)
    }

    fun addNewGameToServer(groupId: String, game: Game): Flow<Boolean> {
        return callbackFlow {
            game.timestamp = Timestamp.now()
            db.collection("groups/$groupId/games").document().set(
                game.getGameJsonMap()
            ).addOnCompleteListener { task ->
                offer(task.isSuccessful)
                close()
            }
            awaitClose { }
        }.flowOn(Dispatchers.IO)
    }

    fun getAllCompletedGames(groupId: String, users: List<User>): Flow<List<Game>> {
        return callbackFlow {
            db.collection("groups/$groupId/games").whereEqualTo("isCompleted", true)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.let { result ->
                            val games = result.map { data ->
                                Game(
                                    data.id,
                                    data["isCompleted"] as Boolean,
                                    data["winnerId"].toString(),
                                    data["looserId"].toString(),
                                    data["timeStamp"] as Timestamp? ?: Timestamp(
                                        GregorianCalendar(2000, 1, 1).time
                                    ),
                                    data["scoresJson"].toString()
                                        .deserialize<List<UserScoreResponse>>()
                                        .map { it.toDomain(users) }
                                )
                            }
                            offer(games)
                            close()
                        }
                    } else {
                        Log.w(tag, "Error getting documents.", task.exception)
                        close()
                    }
                }
            awaitClose { }
        }.flowOn(Dispatchers.IO)
    }

    fun getGroupId(): Flow<String> {
        return callbackFlow {
            db.collection("groups").limit(1)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result?.any() == true) {
                        task.result?.let { result ->
                            offer(result.first().id)
                            close()
                        }
                    } else {
                        Log.w(tag, "Error getting documents.", task.exception)
                        close()
                    }
                }
            awaitClose { }
        }.flowOn(Dispatchers.IO)
    }
}