package com.yogadimas.simastekom.viewmodel.singleevent

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.yogadimas.simastekom.datastore.preferences.SingleEventPreferences
import kotlinx.coroutines.launch

class SingleEventViewModel(private val preferences: SingleEventPreferences): ViewModel() {
    fun setServerError(serverError: Boolean) {
        viewModelScope.launch {
            preferences.setServerError(serverError)
        }
    }

    fun getServerError(): LiveData<Boolean> = preferences.getServerError().asLiveData()

}