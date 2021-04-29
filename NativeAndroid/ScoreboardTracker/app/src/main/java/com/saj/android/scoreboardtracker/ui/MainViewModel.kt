package com.saj.android.scoreboardtracker.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.saj.android.scoreboardtracker.ScoreboardTrackerApp
import com.saj.android.scoreboardtracker.data.GameRepository
import com.saj.android.scoreboardtracker.extensions.deserialize
import com.saj.android.scoreboardtracker.extensions.serialize
import com.saj.android.scoreboardtracker.model.*
import com.saj.android.scoreboardtracker.ui.base.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.floor

class MainViewModel : BaseViewModel() {

    private val tag = "MainViewModel"

    enum class UIState {
        EnterScoreForAllSets,
        UserLostGame,
        Loading,
        Loaded,
        FinishingGame,
        Error
    }

    private val gameRepository = GameRepository()

    private val _usersServeSequenceLiveData = MutableLiveData<List<UserServeSequence>>()
    val usersServeSequenceLiveData: LiveData<List<UserServeSequence>>
        get() = _usersServeSequenceLiveData

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

    private val _canFinishGameLiveData = MutableLiveData(false)
    val canFinishGameLiveData: LiveData<Boolean>
        get() = _canFinishGameLiveData

    private val _uiState = MutableLiveData<Pair<UIState, String>?>()
    val uiState: LiveData<Pair<UIState, String>?>
        get() = _uiState

    private val _winnerLiveData = MutableLiveData<Pair<Boolean, User?>>()
    val winnerLiveData: LiveData<Pair<Boolean, User?>>
        get() = _winnerLiveData

    private val _nextServerUserLiveData = MutableLiveData<String>()
    val nextServerUserLiveData: LiveData<String>
        get() = _nextServerUserLiveData

    private val _lastSetWinStatLiveData = MutableLiveData<String>()
    val lastSetWinStatLiveData: LiveData<String>
        get() = _lastSetWinStatLiveData

    init {
        _uiState.postValue(Pair(UIState.Loading, ""))
        viewModelScope.launch {
            gameRepository.getGroupId().collect {
                if (it.isSuccess()) {
                    it.data?.let { groupId ->
                        _groupId.value = groupId
                        getUsersList(groupId)
                    }
                } else {
                    _uiState.postValue(Pair(UIState.Error, ""))
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun getUsersList(groupId: String) {
        viewModelScope.launch {
            gameRepository.getUsersList().distinctUntilChanged()
                .collect {
                    if (it.isSuccess()) {
                        it.data?.let { users ->
                            _usersLiveData.value = users
                            refreshGameStatistics(groupId, users)
                            getCurrentOnGoingGame(groupId, users)
                            setServeSequenceData(users)

                        }
                    }
                }
        }
    }

    private fun setServeSequenceData(users: List<User>) {
        val sequenceData = mutableListOf<UserServeSequence>()
        val savedSequence = getServeSequenceData()
        users.forEach { user ->
            val index = savedSequence?.firstOrNull { it -> it.userId == user.userId }?.order
            sequenceData.add(UserServeSequence(user.userId, user.name, false, index))
        }
        _usersServeSequenceLiveData.postValue(sequenceData)
    }

    private fun getServeSequenceData() = try {
        ScoreboardTrackerApp.prefs.serveSequenceJsonPref?.deserialize<List<UserServeSequence>>()
    } catch (ex: Exception) {
        null
    }

    fun updateCurrentGameScore(userId: String, index: Int, score: Int?) {
        _groupId.value?.let { groupId ->
            _onGoingGameLiveData.value?.let { game ->
                game.userScores.firstOrNull { userScore -> userScore.user.userId == userId }?.let {
                    if (it.scores[index] != score) {
                        it.scores[index] = score
                        updateGame(groupId, game)
                        setCanFinishGameLiveData()
                        calculateNextUserToServe()
                        calculateLastSetWinStat()
                    }
                }
            }
        }
    }

    private fun setCanFinishGameLiveData() {
        _canFinishGameLiveData.postValue(isScoreEnteredForAllSets())
    }

    private fun calculateLastSetWinStat() {
        _onGoingGameLiveData.value?.let { game ->
            val scoreToStandMessage =
                if (game.userScores.all { it.scores.count { score -> score != null } == 6 }) {
                    val totalScores =
                        game.userScores.map {
                            Pair(
                                it.user,
                                it.scores.sumBy { score -> score ?: 0 })
                        }
                            .sortedBy { it.second }
                    val scoreToStand =
                        floor((totalScores[1].second - 1 - totalScores[0].second) / 2.0).toInt()

                    "${totalScores[0].first.name} will stand against ${totalScores[1].first.name} at $scoreToStand"

                } else {
                    ""
                }
            _lastSetWinStatLiveData.postValue(scoreToStandMessage)
        }
    }

    fun onFinishGame() {
        if (validateCurrentGame()) {
            _onGoingGameLiveData.value?.let { game ->
                _canFinishGameLiveData.postValue(false)
                _uiState.value = Pair(UIState.FinishingGame, "")
                val sortedData =
                    game.userScores.sortedByDescending { it.scores.count { score -> score == 0 } }
                        .sortedBy { it.scores.sumBy { score -> score ?: 0 } }
                val winner = sortedData.first().user
                val loser = sortedData.last().user
                _groupId.value?.let { groupId ->
                    updateCompletedGameResult(groupId, game, winner, loser)
                }
            }
        }
    }

    private fun updateCompletedGameResult(groupId: String, game: Game, winner: User, loser: User) {
        viewModelScope.launch {
            game.winnerId = winner.userId
            game.loserId = loser.userId
            game.isCompleted = true
            gameRepository.updateGameToServer(groupId, game).collect {
                if (it.isSuccess() && it.data == true) {
                    _uiState.value = Pair(UIState.Loaded, "")
                    _winnerLiveData.value = Pair(true, winner)
                    addNewGame(game, groupId)
                    delay(6000)
                    _winnerLiveData.postValue(Pair(false, null))
                    _canFinishGameLiveData.postValue(true)
                    _uiState.value = Pair(UIState.UserLostGame, "${loser.name}")
                } else {
                    _uiState.value = Pair(UIState.Error, "")
                    _canFinishGameLiveData.postValue(true)
                }
            }
        }
    }

    private suspend fun addNewGame(game: Game, groupId: String) {
        val newUserScores = mutableListOf<UserScore>()
        game.userScores.forEach { userScore ->
            newUserScores.add(UserScore(userScore.user, getSecretSevenInitialScore()))
        }
        val newGame = Game("", false, "", "", Timestamp.now(), newUserScores)

        gameRepository.addNewGameToServer(groupId, newGame).collect {
            recalculateServeSequence()
        }
    }

    private fun recalculateServeSequence() {
        _usersServeSequenceLiveData.value?.let { serveSequence ->
            if (serveSequence.any()) {
                val sortedSequence = serveSequence.sortedBy { it.order }
                sortedSequence.drop(1).forEachIndexed { index, it ->
                    it.order = index + 1
                }
                sortedSequence.first().order = serveSequence.size
                onSaveServeSequence()
            }
        }
    }

    private fun updateGame(groupId: String, game: Game) {
        viewModelScope.launch {
            gameRepository.updateGameToServer(groupId, game)
                .collect {
                    if (!it.isSuccess()) {
                        _uiState.postValue(Pair(UIState.Error, ""))
                    }
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

    private fun refreshGameStatistics(groupId: String, users: List<User>) {
        viewModelScope.launch {
            gameRepository.getAllCompletedGames(groupId, users).collect {
                if (it.isSuccess()) {
                    it.data?.let { games ->
                        _completedGamesLiveData.value =
                            games.sortedByDescending { g -> g.timestamp }
                        populateScoreStatistics()
                        populateUserResultData(games)
                        populateUserWinStats(games)
                    }
                }
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
            gameRepository.getCurrentOnGoingGame(groupId, users).distinctUntilChanged().collect {
                if (it.isSuccess()) {
                    _onGoingGameLiveData.value?.let { game ->
                        if (game.gameId != it.data?.gameId) {
                            refreshGameStatistics(groupId, users)
                        }
                    }
                    _onGoingGameLiveData.value = it.data
                    calculateNextUserToServe()
                    calculateLastSetWinStat()
                    setCanFinishGameLiveData()
                    _uiState.value = Pair(UIState.Loaded, "")
                }
            }
        }
    }

    private fun validateCurrentGame(): Boolean {
        val isValid = isScoreEnteredForAllSets()
        if (!isScoreEnteredForAllSets()) {
            _uiState.value = Pair(UIState.EnterScoreForAllSets, "")
        }
        return isValid
    }

    private fun isScoreEnteredForAllSets(): Boolean {
        return onGoingGameLiveData.value?.let { game ->
            game.userScores.all { userScore -> userScore.scores.all { it != null } }
        } ?: false
    }

    private fun getSecretSevenInitialScore(): MutableList<Int?> {
        val mutableList = mutableListOf<Int?>()
        for (i in 1..7) {
            mutableList.add(null)
        }
        return mutableList
    }

    fun onSaveServeSequence(): Boolean {
        if (validateServeSequence()) {
            _usersServeSequenceLiveData.value?.sortedBy { it.order }?.forEachIndexed { index, it ->
                it.order = index + 1
            }
            ScoreboardTrackerApp.prefs.serveSequenceJsonPref =
                _usersServeSequenceLiveData.value?.serialize()
            calculateNextUserToServe()
            return true
        }
        return false
    }

    private fun validateServeSequence(): Boolean {
        return _usersServeSequenceLiveData.value?.let { data ->
            data.all { it.order != null } && data.groupBy { it.order }.all { it.value.size == 1 }
        } ?: true
    }

    private fun calculateNextUserToServe() {
        _usersServeSequenceLiveData.value?.sortedBy { it.order }?.let { sequence ->
            _onGoingGameLiveData.value?.let { game ->
                val nextRoundIndex =
                    game.userScores.minOf { it.scores.count { score -> score != null } } + 1

                val userToServeIndex = with(nextRoundIndex % 3) {
                    if (this <= 0) 3 else this
                } - 1

                val noOfRounds = game.userScores.firstOrNull()?.scores?.size ?: 0

                val nextServerUserId =
                    if (nextRoundIndex <= noOfRounds && userToServeIndex >= 0 && userToServeIndex < sequence.size) {
                        sequence[userToServeIndex].userId
                    } else {
                        ""
                    }
                _nextServerUserLiveData.postValue(nextServerUserId)
            }
        }
    }
}