package com.example.storyapp

import androidx.lifecycle.*
import com.example.storyapp.data.SessionPreferences
import kotlinx.coroutines.launch

class MainViewModel(private val pref: SessionPreferences) : ViewModel() {
    fun getToken(): LiveData<String> {
        return pref.getToken().asLiveData()
    }

    fun saveToken(token: String) {
        viewModelScope.launch {
            pref.saveToken(token)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val pref: SessionPreferences) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(pref) as T
        }
    }
}