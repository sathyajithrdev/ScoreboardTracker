package com.saj.android.scoreboardtracker.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.ExperimentalMaterialApi
import androidx.lifecycle.ViewModelProvider
import com.saj.android.scoreboardtracker.R
import com.saj.android.scoreboardtracker.ui.base.BaseActivity

class MainActivity : BaseActivity() {

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // This app draws behind the system bars, so we want to handle fitting system windows

        setContent {
            ScoreboardApp(viewModel)
        }
        setObservers(viewModel)
    }

    private fun setObservers(viewModel: MainViewModel) {
        viewModel.uiState.observe(this, {
            when (it?.first) {
                MainViewModel.UIState.EnterScoreForAllSets -> showToast(getString(R.string.enter_score_for_all_set))
                MainViewModel.UIState.UserLostGame -> showToast(
                    getString(
                        R.string.user_lost_game_info,
                        it.second
                    )
                )
                MainViewModel.UIState.Error -> {
                    if (hasNetworkConnection()) {
                        showToast(getString(R.string.something_went_wrong))
                    } else {
                        showToast(getString(R.string.no_internet_connection))
                    }
                }
                MainViewModel.UIState.Loading,
                MainViewModel.UIState.Loaded,
                MainViewModel.UIState.FinishingGame,
                null -> {
                }
            }
        })

//        viewModel.completedGamesLiveData.observe(this, {
//            Log.e("Games", "Games size is ${it.size}")
//            saveTextFile(it.serialize())
//        })
    }

//    private fun saveTextFile(data: String) {
//        try {
//            val fileOutputStream: FileOutputStream =
//                openFileOutput("gameJson${System.currentTimeMillis()}.txt", Context.MODE_PRIVATE)
//            val outputWriter = OutputStreamWriter(fileOutputStream)
//            outputWriter.write(data)
//            outputWriter.close()
//            //display file saved message
//            Toast.makeText(baseContext, "File saved successfully!", Toast.LENGTH_SHORT).show()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
}
