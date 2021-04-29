package com.saj.android.scoreboardtracker.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.saj.android.scoreboardtracker.extensions.deserialize
import com.saj.android.scoreboardtracker.model.Game
import com.saj.android.scoreboardtracker.model.Resource
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
    fun getUsersList(): Flow<Resource<List<User>>> {
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
                            offer(Resource.success(users))
                            close()
                        }
                    } else {
                        offer(Resource.genericError("Error getting getUsersList."))
                        close()
                    }
                }
            awaitClose { }
        }.flowOn(Dispatchers.IO)
    }

    @ExperimentalCoroutinesApi
    fun getCurrentOnGoingGame(groupId: String, users: List<User>): Flow<Resource<Game>> {
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
                                offer(Resource.success(game))
                            }
                        }
                    } else {
                        offer(Resource.genericError("error getting current game"))
                        close()
                    }
                }
            awaitClose { }
        }.flowOn(Dispatchers.IO)
    }

    @ExperimentalCoroutinesApi
    fun updateGameToServer(groupId: String, game: Game): Flow<Resource<Boolean>> {
        return callbackFlow {
            game.timestamp = Timestamp.now()
            db.collection("groups/$groupId/games").document(game.gameId).update(
                game.getGameJsonMap()
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    offer(Resource.success(task.isSuccessful))
                } else {
                    offer(Resource.genericError<Boolean>("Update game failed"))
                }
                close()
            }
            awaitClose { }
        }.flowOn(Dispatchers.IO)
    }

    @ExperimentalCoroutinesApi
    fun addNewGameToServer(groupId: String, game: Game): Flow<Resource<Boolean>> {
        return callbackFlow {
            game.timestamp = Timestamp.now()
            db.collection("groups/$groupId/games").document().set(
                game.getGameJsonMap()
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    offer(Resource.success(task.isSuccessful))
                } else {
                    offer(Resource.genericError<Boolean>("add new game failed"))
                }
                close()
            }
            awaitClose { }
        }.flowOn(Dispatchers.IO)
    }

    @ExperimentalCoroutinesApi
    fun getAllCompletedGames(groupId: String, users: List<User>): Flow<Resource<List<Game>>> {
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
                            offer(Resource.success(games))
                            close()
                        }
                    } else {
                        offer(Resource.genericError("Get all completed games failed"))
                        close()
                    }
                }
            awaitClose { }
        }.flowOn(Dispatchers.IO)
    }

    @ExperimentalCoroutinesApi
    fun getGroupId(): Flow<Resource<String>> {
        return callbackFlow {
            db.collection("groups").limit(1)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result?.any() == true) {
                        task.result?.let { result ->
                            offer(Resource.success(result.first().id))
                            close()
                        }
                    } else {
                        offer(Resource.genericError("Error getting group id"))
                        close()
                    }
                }
            awaitClose { }
        }.flowOn(Dispatchers.IO)
    }
}