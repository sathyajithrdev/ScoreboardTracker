package com.saj.android.scoreboardtracker.ui.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.saj.android.scoreboardtracker.data.GameRepository
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

    @ExperimentalCoroutinesApi
    fun getUsersList() {
        viewModelScope.launch {
            gameRepository.getUsersList().collect {
                _usersLiveData.value = it
            }

        }
    }
}