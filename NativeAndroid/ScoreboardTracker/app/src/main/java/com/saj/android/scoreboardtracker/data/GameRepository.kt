package com.saj.android.scoreboardtracker.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.saj.android.scoreboardtracker.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

class GameRepository : BaseRepository() {

    val tag = "GameRepo"

    val db = FirebaseFirestore.getInstance()

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
                        Log.w(tag, "Error getting documents.", task.exception)
                        close()
                    }
                }
            awaitClose { }
        }.flowOn(Dispatchers.IO)
    }
}