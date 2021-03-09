package com.saj.android.scoreboardtracker.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.saj.android.scoreboardtracker.data.GameRepository
import com.saj.android.scoreboardtracker.model.Game
import com.saj.android.scoreboardtracker.model.Statistics
import com.saj.android.scoreboardtracker.model.User
import com.saj.android.scoreboardtracker.ui.base.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel : BaseViewModel() {

    private val gameRepository = GameRepository()

    private val _usersLiveData = MutableLiveData<List<User>>()
    val userLiveData: LiveData<List<User>>
        get() = _usersLiveData

    private val _completedGamesLiveData = MutableLiveData<List<Game>>()
    val completedGamesLiveData: LiveData<List<Game>>
        get() = _completedGamesLiveData

    private val _onGoingGameLiveData = MutableLiveData<Game>()
    val onGoingGameLiveData: LiveData<Game>
        get() = _onGoingGameLiveData

    private val _scoreStatisticsLiveData = MutableLiveData<List<Statistics>>()
    val scoreStatisticsLiveData: LiveData<List<Statistics>>
        get() = _scoreStatisticsLiveData

    private val _groupId = MutableLiveData<String>()


    init {
        viewModelScope.launch {
            gameRepository.getGroupId().collect {
                Log.e("GroupId", "Group id is $it")
                _groupId.value = it
                getUsersList(it)
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun getUsersList(groupId: String) {
        viewModelScope.launch {
            gameRepository.getUsersList()
                .collect {
                    _usersLiveData.value = it
                    getCompletedGames(groupId, it)
                    getCurrentOnGoingGame(groupId, it)
                }
        }
    }

    private fun populateScoreStatistics() {
        _usersLiveData.value?.let { user ->
            _completedGamesLiveData.value?.let { games ->
                populateScoreStatistics(user, games)
            }
        }
    }

    private fun populateScoreStatistics(users: List<User>, games: List<Game>) {
        val statistics = mutableListOf<Statistics>()
        games.groupBy { it.winnerId }.maxByOrNull { it.value.count() }?.let {
            statistics.add(
                Statistics(
                    "Max Wins",
                    it.value.count(),
                    users.firstOrNull { u -> u.userId == it.key }?.profileImageUrl ?: ""
                )
            )
        }

        games.flatMap { it.userScores }.minByOrNull { it.getTotalScore() }?.let {
            statistics.add(
                Statistics(
                    "Min Score",
                    it.scores.sumBy { s -> s ?: 0 },
                    users.firstOrNull { u -> u.userId == it.user.userId }?.profileImageUrl ?: ""
                )
            )
        }

        games.groupBy { it.loserId }.minByOrNull { it.value.count() }?.let {
            statistics.add(
                Statistics(
                    "Least Defeats",
                    it.value.count(),
                    users.firstOrNull { u -> u.userId == it.key }?.profileImageUrl ?: ""
                )
            )
        }

        games.groupBy { it.loserId }.maxByOrNull { it.value.count() }?.let {
            statistics.add(
                Statistics(
                    "Most Defeats",
                    it.value.count(),
                    users.firstOrNull { u -> u.userId == it.key }?.profileImageUrl ?: ""
                )
            )
        }
        _scoreStatisticsLiveData.value = statistics
    }


    private fun getCompletedGames(groupId: String, users: List<User>) {
        viewModelScope.launch {
            gameRepository.getAllCompletedGames(groupId, users).collect {
                _completedGamesLiveData.value = it.sortedByDescending { g -> g.timestamp }
                populateScoreStatistics()
            }
        }
    }

    private fun getCurrentOnGoingGame(groupId: String, users: List<User>) {
        viewModelScope.launch {
            gameRepository.getCurrentOnGoingGame(groupId, users).collect {
                _onGoingGameLiveData.value = it
            }
        }
    }
}