package com.yogadimas.simastekom.viewmodel.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.yogadimas.simastekom.datastore.preferences.AuthPreferences
import kotlinx.coroutines.launch

class AuthViewModel(private val preferences: AuthPreferences) : ViewModel() {


    fun saveUser(token: String?, userId: String?, userType: String?) {
        viewModelScope.launch {
            preferences.saveUser(token, userId, userType)
        }
    }

    fun getUser(): LiveData<Triple<String, String, String>> = preferences.getUser().asLiveData()

}