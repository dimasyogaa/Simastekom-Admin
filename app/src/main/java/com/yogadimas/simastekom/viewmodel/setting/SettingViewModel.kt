package com.yogadimas.simastekom.viewmodel.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.yogadimas.simastekom.common.datastore.preferences.SettingPreferences
import kotlinx.coroutines.launch

class SettingViewModel(private val preferences: SettingPreferences) : ViewModel() {


    fun saveSetting(
        toggleIdentityPersonal: Boolean,
        togglePlaceDateBirth: Boolean,
        toggleAddressHome: Boolean,
    ) {
        viewModelScope.launch {
            preferences.saveSetting(toggleIdentityPersonal, togglePlaceDateBirth, toggleAddressHome)
        }
    }

    fun getSetting(): LiveData<Triple<Boolean, Boolean, Boolean>> =
        preferences.getSetting().asLiveData()

}