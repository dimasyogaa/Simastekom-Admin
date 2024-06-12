package com.yogadimas.simastekom.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yogadimas.simastekom.datastore.preferences.SingleEventPreferences
import com.yogadimas.simastekom.viewmodel.singleevent.SingleEventViewModel

class SingleEventViewModelFactory (private val pref: SingleEventPreferences) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SingleEventViewModel::class.java)) {
            return SingleEventViewModel(pref) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }

    companion object {
        @Volatile
        private var instance: SingleEventViewModelFactory? = null
        fun getInstance(
            singleEventPreferences: SingleEventPreferences,
        ): SingleEventViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: SingleEventViewModelFactory(singleEventPreferences)
            }.also { instance = it }
    }

}