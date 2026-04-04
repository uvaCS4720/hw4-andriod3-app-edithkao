package edu.nd.pmcburne.hello

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.nd.pmcburne.hello.data.AppDatabase
import edu.nd.pmcburne.hello.data.PlacemarkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.get(application)
    private val repository = PlacemarkRepository(db.locationDao())

    private val _filterTag = MutableStateFlow("core")
    val filterTag = _filterTag.asStateFlow()

    val tags = repository.tags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val locations = _filterTag
        .flatMapLatest { tag -> repository.locationsForTag(tag) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            try {
                repository.loadFromInternet()
            } catch (e: Exception) {
                // Network failed — app uses whatever is already in the database
            }
        }
    }

    fun setFilterTag(tag: String) {
        _filterTag.value = tag
    }
}
