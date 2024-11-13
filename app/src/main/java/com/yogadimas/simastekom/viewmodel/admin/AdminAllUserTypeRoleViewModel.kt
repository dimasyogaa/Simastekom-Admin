package com.yogadimas.simastekom.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yogadimas.simastekom.common.state.State
import com.yogadimas.simastekom.model.responses.ProfilePictureData
import com.yogadimas.simastekom.repository.AdminAllUserTypeRoleRepository
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class AdminAllUserTypeRoleViewModel(
    private val repository: AdminAllUserTypeRoleRepository,
) :
    ViewModel() {

    val profilePictureState: SharedFlow<State<ProfilePictureData>> = repository.profilePictureState
    fun getProfilePicture(token: String, userType: String, userId: String) = viewModelScope.launch {
        repository.getProfilePicture(token, userType, userId)
    }

    fun setManipulationProfilePicture(
        token: String,
        userType: String,
        userId: String,
        profilePicture: MultipartBody.Part?,
        isDeleted: Boolean,
    ) = viewModelScope.launch {
        repository.setManipulationProfilePicture(token, userType, userId, profilePicture, isDeleted)
    }

}