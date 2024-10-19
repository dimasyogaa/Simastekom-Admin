package com.yogadimas.simastekom.viewmodel.forgotpassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yogadimas.simastekom.api.ApiConfig
import com.yogadimas.simastekom.common.event.Event
import com.yogadimas.simastekom.common.helper.getErrors
import com.yogadimas.simastekom.model.responses.Errors
import com.yogadimas.simastekom.model.responses.UserData
import com.yogadimas.simastekom.model.responses.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordViewModel : ViewModel() {


    private val _userData = MutableLiveData<UserData>()
    val userData: LiveData<UserData> = _userData

    private val _errors = MutableLiveData<Event<Errors?>>()
    val errors: LiveData<Event<Errors?>> = _errors

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _snackbarText = MutableLiveData<Event<String>>()
    val errorsSnackbarText: LiveData<Event<String>> = _snackbarText


    fun resetPassword(email: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().resetPassword(email)
        client.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {

                _isLoading.value = false
                if (response.isSuccessful) {
                    _userData.value = response.body()?.userData
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }

        })

    }

    fun checkToken(email: String, token: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().resetPasswordCheckToken(email, token)
        client.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {

                _isLoading.value = false
                if (response.isSuccessful) {
                    _userData.value = response.body()?.userData
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }

        })
    }

    fun updatePassword(userId: String, userType: String, password: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().updateUserCurrentPassword(userId, userType, password)
        client.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _userData.value = response.body()?.userData
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }

        })
    }

}