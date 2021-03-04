package com.saj.android.scoreboardtracker.ui.screens

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.saj.android.scoreboardtracker.data.GameRepository
import com.saj.android.scoreboardtracker.model.User
import com.saj.android.scoreboardtracker.ui.base.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

class MainViewModel : BaseViewModel() {

    private val gameRepository = GameRepository()

    private val _usersLiveData = MutableLiveData<List<User>>()
    val userLiveData: LiveData<List<User>>
        get() = _usersLiveData

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
                .onCompletion {
                    getCompletedGames(groupId)
                }
                .collect {
                    _usersLiveData.value = it
                }

        }
    }

    private fun getCompletedGames(groupId: String) {
        viewModelScope.launch {
            gameRepository.getAllCompletedGames(groupId).collect {
                Log.e("GroupId", "First Game is ${it.first()}")
            }
        }
    }
}