package com.catsapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.catsapp.model.mapToCatModel
import com.catsapp.model.repository.CatsRepository
import com.catsapp.utils.NetworkConnectivityProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val UPDATE_CATS_DELAY = 300000L // every 5 minutes
private const val TAG = "MainViewModel"

class MainViewModel(private val repository: CatsRepository) : ViewModel() {
    private var periodicCatsRetrieveJob: Job? = null

    private val networkStatus: StateFlow<Boolean> =
        NetworkConnectivityProvider.isConnected.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            true
        )

    init {
        setupCollectors()
    }

    private fun setupCollectors() {
        viewModelScope.launch {
            networkStatus.collect { status ->
                Log.d(TAG, "setupCollectors | status=$status")
                if (status) {
                    startPeriodicUpdates()
                } else {
                    stopPeriodicUpdates()
                }
            }
        }
    }

    fun startPeriodicUpdates() {
        if (periodicCatsRetrieveJob?.isActive == true || !networkStatus.value) return

        periodicCatsRetrieveJob = viewModelScope.launch {
            while (isActive) {
                Log.d(TAG, "startPeriodicUpdates | starting delay")
                delay(UPDATE_CATS_DELAY)
                Log.d(TAG, "startPeriodicUpdates | fetching data")
                val cats = repository.fetchCatsFromApi()
                if (!cats.isNullOrEmpty()) {
                    Log.d(TAG, "startPeriodicUpdates | saving to database")
                    repository.saveCatsToDb(
                        cats = cats.map { it.mapToCatModel() },
                        isFromPeriodicUpdate = true
                    )

                }
            }
        }
    }

    private fun stopPeriodicUpdates() {
        Log.d(TAG, "startPeriodicUpdates | canceling periodic update")
        periodicCatsRetrieveJob?.cancel()
    }

    companion object {

        /**
         * Factory for creating instances of [MainViewModel].
         */
        val Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                MainViewModel(CatsRepository.getInstance(application.applicationContext))
            }
        }
    }
}