package com.saj.android.scoreboardtracker.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.saj.android.scoreboardtracker.data.GameRepository
import com.saj.android.scoreboardtracker.model.*
import com.saj.android.scoreboardtracker.ui.base.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class MainViewModel : BaseViewModel() {

    enum class UIState {
        EnterScoreForAllSets
    }

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

    private val _recentPerformanceLiveData = MutableLiveData<List<UserRecentPerformance>>()
    val recentPerformanceLiveData: LiveData<List<UserRecentPerformance>>
        get() = _recentPerformanceLiveData

    private val _winStatsLiveData = MutableLiveData<List<UserWinStats>>()
    val winStatsLiveData: LiveData<List<UserWinStats>>
        get() = _winStatsLiveData

    private val _groupId = MutableLiveData<String>()

    private val _canSaveGameLiveData = MutableLiveData(false)
    val canSaveGameLiveData: LiveData<Boolean>
        get() = _canSaveGameLiveData

    private val _uiState = MutableLiveData<UIState?>()
    val uiState: LiveData<UIState?>
        get() = _uiState

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
            gameRepository.getUsersList().distinctUntilChanged()
                .collect {
                    _usersLiveData.value = it
                    getCompletedGames(groupId, it)
                    getCurrentOnGoingGame(groupId, it)
                }
        }
    }

    fun updateCurrentGameScore(userId: String, index: Int, score: Int?) {
        _groupId.value?.let { groupId ->
            _onGoingGameLiveData.value?.let { game ->
                game.userScores.firstOrNull { userScore -> userScore.user.userId == userId }?.let {
                    if (it.scores[index] != score) {
                        it.scores[index] = score
                        updateGame(groupId, game)
                    }
                }
            }
        }
    }

    fun onFinishGame() {
        if (validateCurrentGame()) {

        }
    }

    private fun updateGame(groupId: String, game: Game) {
        viewModelScope.launch {
            gameRepository.updateGameToServer(groupId, game)
                .collect { isSuccess ->
                    Log.e("MainViewModel", "Update to server is $isSuccess")
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
                populateUserResultData(it)
                populateUserWinStats(it)
            }
        }
    }

    private fun populateUserResultData(games: List<Game>) {
        val sortedGames = games.sortedByDescending { g -> g.timestamp }
        val recentGames = sortedGames.take(8)
        val userPerformances = mutableListOf<UserRecentPerformance>()
        _usersLiveData.value?.forEach { user ->
            userPerformances.add(
                UserRecentPerformance(
                    user,
                    getGameResultStatus(user, recentGames)
                )
            )
        }
        _recentPerformanceLiveData.value = userPerformances
    }

    private fun populateUserWinStats(games: List<Game>) {
        val userWinStats = mutableListOf<UserWinStats>()
        _usersLiveData.value?.forEach { user ->
            val winStats = getGameResultStatus(user, games)
            userWinStats.add(
                UserWinStats(
                    user,
                    winStats.count { it == GameResultStatus.Winner },
                    winStats.count { it == GameResultStatus.Loser },
                    winStats.count { it == GameResultStatus.Neutral })
            )
        }
        _winStatsLiveData.value = userWinStats
    }


    private fun getGameResultStatus(user: User, games: List<Game>): List<GameResultStatus> {
        return games.map {
            when {
                it.winnerId == user.userId -> GameResultStatus.Winner
                it.loserId == user.userId -> GameResultStatus.Loser
                else -> GameResultStatus.Neutral
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

    private fun validateCurrentGame(): Boolean {
        return onGoingGameLiveData.value?.let { game ->
            val isValid = game.userScores.all { userScore -> userScore.scores.all { it != null } }
            if (!isValid) {
                _uiState.value = UIState.EnterScoreForAllSets
            }
            isValid
        } ?: false
    }
}