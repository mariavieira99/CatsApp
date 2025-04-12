package com.catsapp.ui.cats

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catsapp.model.Cat
import com.catsapp.model.repository.CatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CatsListViewModel(
    private val repository: CatsRepository = CatsRepository.getInstance()
) : ViewModel() {
    val catsState: MutableState<List<Cat>> = mutableStateOf(emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val cats = getCats()
            catsState.value = cats
        }
    }

    private suspend fun getCats(): List<Cat> {
        return repository.getCats()
    }
}