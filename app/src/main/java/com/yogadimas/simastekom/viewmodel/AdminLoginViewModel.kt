package com.yogadimas.simastekom.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.yogadimas.simastekom.Helper.getErrors
import com.yogadimas.simastekom.api.ApiConfig
import com.yogadimas.simastekom.event.Event
import com.yogadimas.simastekom.model.AdminResponse
import com.yogadimas.simastekom.model.Data
import com.yogadimas.simastekom.model.Errors
import com.yogadimas.simastekom.progress.ProgressListener
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminLoginViewModel : ViewModel() {

    private val _data = MutableLiveData<Data>()
    val data: LiveData<Data> = _data

    private val _errors = MutableLiveData<Errors?>()
    val errors: LiveData<Errors?> = _errors

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _snackbarText = MutableLiveData<Event<String>>()
    val snackbarText: LiveData<Event<String>> = _snackbarText

    fun postLogin(nama_pengguna: String, password: String, progressListener: ProgressListener) {
        _isLoading.value = true
        val client = ApiConfig.getApiService(progressListener).postLoginAdmin(nama_pengguna, password)
        client.enqueue(object : Callback<AdminResponse> {
            override fun onResponse(call: Call<AdminResponse>, response: Response<AdminResponse>) {
                progressListener.onProgress(100)

                _isLoading.value = false
                if (response.isSuccessful) {
                    _data.value = response.body()?.data
                    Log.e("TAG", "_data: " + response.body()?.data)
                } else {
                    _errors.value = getErrors(response.errorBody()?.string().orEmpty())
                }
            }

            override fun onFailure(call: Call<AdminResponse>, t: Throwable) {

                progressListener.onProgress(100)
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }

        })

    }

}