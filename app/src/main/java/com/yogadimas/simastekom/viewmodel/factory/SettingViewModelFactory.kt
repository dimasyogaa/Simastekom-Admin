package com.yogadimas.simastekom.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yogadimas.simastekom.datastore.preferences.SettingPreferences
import com.yogadimas.simastekom.viewmodel.setting.SettingViewModel

class SettingViewModelFactory(private val pref: SettingPreferences) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingViewModel::class.java)) {
            return SettingViewModel(pref) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }

    companion object {
        @Volatile
        private var instance: SettingViewModelFactory? = null
        fun getInstance(
            settingPreferences: SettingPreferences,
        ): SettingViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: SettingViewModelFactory(settingPreferences)
            }.also { instance = it }
    }
}