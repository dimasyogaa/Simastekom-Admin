package com.yogadimas.simastekom.viewmodel.admin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yogadimas.simastekom.api.ApiConfig
import com.yogadimas.simastekom.event.Event
import com.yogadimas.simastekom.helper.getErrors
import com.yogadimas.simastekom.model.responses.AdminData
import com.yogadimas.simastekom.model.responses.AdminResponse
import com.yogadimas.simastekom.model.responses.Errors
import com.yogadimas.simastekom.model.responses.IdentityAcademicData
import com.yogadimas.simastekom.model.responses.IdentityAcademicListResponse
import com.yogadimas.simastekom.model.responses.IdentityAcademicObjectResponse
import com.yogadimas.simastekom.model.responses.IdentityPersonalData
import com.yogadimas.simastekom.model.responses.IdentityPersonalResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminViewModel : ViewModel() {

    lateinit var token: String

    private val _adminData = MutableLiveData<Event<AdminData?>>()
    val adminData: LiveData<Event<AdminData?>> = _adminData

    private val _identityPersonal = MutableLiveData<Event<IdentityPersonalData?>>()
    val identityPersonal: LiveData<Event<IdentityPersonalData?>> = _identityPersonal

    private val _identityAcademicList = MutableLiveData<Event<List<IdentityAcademicData>?>>()
    val identityAcademicList: LiveData<Event<List<IdentityAcademicData>?>> = _identityAcademicList

    private val _identityAcademic = MutableLiveData<Event<IdentityAcademicData?>>()
    val identityAcademic: LiveData<Event<IdentityAcademicData?>> = _identityAcademic

    private val _errors = MutableLiveData<Event<Errors?>>()
    val errors: LiveData<Event<Errors?>> = _errors

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _snackbarText = MutableLiveData<Event<String>>()
    val errorsSnackbarText: LiveData<Event<String>> = _snackbarText

    fun login(id: String, password: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().login(id, password)
        client.enqueue(object : Callback<AdminResponse> {
            override fun onResponse(call: Call<AdminResponse>, response: Response<AdminResponse>) {

                _isLoading.value = false
                if (response.isSuccessful) {
                    _adminData.value = Event(response.body()?.adminData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<AdminResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }

        })

    }

    fun getAdminCurrent() {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getAdminCurrent(token)
        client.enqueue(object : Callback<AdminResponse> {
            override fun onResponse(call: Call<AdminResponse>, response: Response<AdminResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _adminData.value = Event(response.body()?.adminData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<AdminResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }

    fun updateAdminCurrent(
        profilePicture: MultipartBody.Part?,
        username: RequestBody,
        name: RequestBody,
        deletePhoto: RequestBody,
    ) {

        _isLoading.value = true
        val client =
            ApiConfig.getApiService()
                .updateAdminCurrent(token, profilePicture, username, name, deletePhoto)


        client.enqueue(object : Callback<AdminResponse> {
            override fun onResponse(call: Call<AdminResponse>, response: Response<AdminResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _adminData.value = Event(response.body()?.adminData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<AdminResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }

        })

    }

    fun getAdminPassword() {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getAdminPassword(token)
        client.enqueue(object : Callback<AdminResponse> {
            override fun onResponse(call: Call<AdminResponse>, response: Response<AdminResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _adminData.value = Event(response.body()?.adminData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<AdminResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }

    fun updateAdminCurrentPassword(password: String) {

        _isLoading.value = true
        val client =
            ApiConfig.getApiService()
                .updateAdminCurrentPassword(token, password)


        client.enqueue(object : Callback<AdminResponse> {
            override fun onResponse(call: Call<AdminResponse>, response: Response<AdminResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _adminData.value = Event(response.body()?.adminData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<AdminResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }

        })

    }

    fun getIdentityPersonal(userType: String, userId: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getIdentityPersonal(token, userType, userId)
        client.enqueue(object : Callback<IdentityPersonalResponse> {
            override fun onResponse(
                call: Call<IdentityPersonalResponse>,
                response: Response<IdentityPersonalResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _identityPersonal.value = Event(response.body()?.identityPersonalData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<IdentityPersonalResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }

    fun updateIdentityPersonal(
        userType: String, userId: String, identityPersonal: IdentityPersonalData,
    ) {

        _isLoading.value = true
        val client =
            ApiConfig.getApiService()
                .updateIdentityPersonal(token, userType, userId, identityPersonal)


        client.enqueue(object : Callback<IdentityPersonalResponse> {
            override fun onResponse(
                call: Call<IdentityPersonalResponse>,
                response: Response<IdentityPersonalResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _identityPersonal.value = Event(response.body()?.identityPersonalData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<IdentityPersonalResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }

        })

    }

    fun verifyEmail(userType: String, userId: String, email: String) {

        _isLoading.value = true
        val client =
            ApiConfig.getApiService()
                .verifyEmail(token, userType, userId, email)


        client.enqueue(object : Callback<IdentityPersonalResponse> {
            override fun onResponse(
                call: Call<IdentityPersonalResponse>,
                response: Response<IdentityPersonalResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _identityPersonal.value = Event(response.body()?.identityPersonalData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<IdentityPersonalResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }

        })

    }


    fun verifyEmailCheckToken(
        userType: String,
        userId: String,
        email: String,
        tokenVerifyEmail: String,
    ) {

        _isLoading.value = true
        val client =
            ApiConfig.getApiService()
                .verifyEmailCheckToken(token, userType, userId, email, tokenVerifyEmail)


        client.enqueue(object : Callback<IdentityPersonalResponse> {
            override fun onResponse(
                call: Call<IdentityPersonalResponse>,
                response: Response<IdentityPersonalResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _identityPersonal.value = Event(response.body()?.identityPersonalData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<IdentityPersonalResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }

        })

    }


    /** FACULTY */
    fun addFaculty(code: String, name: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().addFaculty(token, code, name)
        client.enqueue(object : Callback<IdentityAcademicObjectResponse> {
            override fun onResponse(
                call: Call<IdentityAcademicObjectResponse>,
                response: Response<IdentityAcademicObjectResponse>,
            ) {

                _isLoading.value = false
                if (response.isSuccessful) {
                    _identityAcademic.value = Event(response.body()?.identityAcademicData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<IdentityAcademicObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }

        })

    }
    fun getAllLFaculties() {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getAllFaculties(token)
        client.enqueue(object : Callback<IdentityAcademicListResponse> {
            override fun onResponse(
                call: Call<IdentityAcademicListResponse>,
                response: Response<IdentityAcademicListResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _identityAcademicList.value = Event(response.body()?.identityAcademicData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<IdentityAcademicListResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun getFacultyById(id: Int) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getFacultyById(token, id)
        client.enqueue(object : Callback<IdentityAcademicObjectResponse> {
            override fun onResponse(
                call: Call<IdentityAcademicObjectResponse>,
                response: Response<IdentityAcademicObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _identityAcademic.value = Event(response.body()?.identityAcademicData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<IdentityAcademicObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun updateFaculty(id: Int, identityAcademicData: IdentityAcademicData) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().updateFaculty(token, id, identityAcademicData)
        client.enqueue(object : Callback<IdentityAcademicObjectResponse> {
            override fun onResponse(
                call: Call<IdentityAcademicObjectResponse>,
                response: Response<IdentityAcademicObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _identityAcademic.value = Event(response.body()?.identityAcademicData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<IdentityAcademicObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun deleteFaculty(id: Int) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().deleteFaculty(token, id)
        client.enqueue(object : Callback<IdentityAcademicObjectResponse> {
            override fun onResponse(
                call: Call<IdentityAcademicObjectResponse>,
                response: Response<IdentityAcademicObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _identityAcademic.value = Event(response.body()?.identityAcademicData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<IdentityAcademicObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }


    /** LEVEL */
    fun addLevel(code: String, name: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().addLevel(token, code, name)
        client.enqueue(object : Callback<IdentityAcademicObjectResponse> {
            override fun onResponse(
                call: Call<IdentityAcademicObjectResponse>,
                response: Response<IdentityAcademicObjectResponse>,
            ) {

                _isLoading.value = false
                if (response.isSuccessful) {
                    _identityAcademic.value = Event(response.body()?.identityAcademicData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<IdentityAcademicObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }

        })

    }
    fun getAllLevels() {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getAllLevels(token)
        client.enqueue(object : Callback<IdentityAcademicListResponse> {
            override fun onResponse(
                call: Call<IdentityAcademicListResponse>,
                response: Response<IdentityAcademicListResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _identityAcademicList.value = Event(response.body()?.identityAcademicData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<IdentityAcademicListResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun searchSortLevel(keyword: String?, sortBy: String?, sortDir: String?) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().searchSortLevel(token, keyword, sortBy, sortDir)
        client.enqueue(object : Callback<IdentityAcademicListResponse> {
            override fun onResponse(
                call: Call<IdentityAcademicListResponse>,
                response: Response<IdentityAcademicListResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _identityAcademicList.value = Event(response.body()?.identityAcademicData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<IdentityAcademicListResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun getLevelById(id: Int) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getLevelById(token, id)
        client.enqueue(object : Callback<IdentityAcademicObjectResponse> {
            override fun onResponse(
                call: Call<IdentityAcademicObjectResponse>,
                response: Response<IdentityAcademicObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _identityAcademic.value = Event(response.body()?.identityAcademicData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<IdentityAcademicObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun updateLevel(id: Int, identityAcademicData: IdentityAcademicData) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().updateLevel(token, id, identityAcademicData)
        client.enqueue(object : Callback<IdentityAcademicObjectResponse> {
            override fun onResponse(
                call: Call<IdentityAcademicObjectResponse>,
                response: Response<IdentityAcademicObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _identityAcademic.value = Event(response.body()?.identityAcademicData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<IdentityAcademicObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun deleteLevel(id: Int) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().deleteLevel(token, id)
        client.enqueue(object : Callback<IdentityAcademicObjectResponse> {
            override fun onResponse(
                call: Call<IdentityAcademicObjectResponse>,
                response: Response<IdentityAcademicObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _identityAcademic.value = Event(response.body()?.identityAcademicData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<IdentityAcademicObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }


    /** MAJOR */
    fun addMajor(code: String, name: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().addMajor(token, code, name)
        client.enqueue(object : Callback<IdentityAcademicObjectResponse> {
            override fun onResponse(
                call: Call<IdentityAcademicObjectResponse>,
                response: Response<IdentityAcademicObjectResponse>,
            ) {

                _isLoading.value = false
                if (response.isSuccessful) {
                    _identityAcademic.value = Event(response.body()?.identityAcademicData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<IdentityAcademicObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }

        })

    }
    fun getAllMajors() {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getAllMajors(token)
        client.enqueue(object : Callback<IdentityAcademicListResponse> {
            override fun onResponse(
                call: Call<IdentityAcademicListResponse>,
                response: Response<IdentityAcademicListResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _identityAcademicList.value = Event(response.body()?.identityAcademicData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<IdentityAcademicListResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun searchSortMajor(keyword: String?, sortBy: String?, sortDir: String?) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().searchSortMajor(token, keyword, sortBy, sortDir)
        client.enqueue(object : Callback<IdentityAcademicListResponse> {
            override fun onResponse(
                call: Call<IdentityAcademicListResponse>,
                response: Response<IdentityAcademicListResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _identityAcademicList.value = Event(response.body()?.identityAcademicData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<IdentityAcademicListResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun getMajorById(id: Int) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getMajorById(token, id)
        client.enqueue(object : Callback<IdentityAcademicObjectResponse> {
            override fun onResponse(
                call: Call<IdentityAcademicObjectResponse>,
                response: Response<IdentityAcademicObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _identityAcademic.value = Event(response.body()?.identityAcademicData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<IdentityAcademicObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun updateMajor(id: Int, identityAcademicData: IdentityAcademicData) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().updateMajor(token, id, identityAcademicData)
        client.enqueue(object : Callback<IdentityAcademicObjectResponse> {
            override fun onResponse(
                call: Call<IdentityAcademicObjectResponse>,
                response: Response<IdentityAcademicObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _identityAcademic.value = Event(response.body()?.identityAcademicData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<IdentityAcademicObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun deleteMajor(id: Int) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().deleteMajor(token, id)
        client.enqueue(object : Callback<IdentityAcademicObjectResponse> {
            override fun onResponse(
                call: Call<IdentityAcademicObjectResponse>,
                response: Response<IdentityAcademicObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _identityAcademic.value = Event(response.body()?.identityAcademicData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<IdentityAcademicObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }


    fun logout() {
        _isLoading.value = true
        val client = ApiConfig.getApiService().logout(token)
        client.enqueue(object : Callback<AdminResponse> {
            override fun onResponse(call: Call<AdminResponse>, response: Response<AdminResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _adminData.value = Event(response.body()?.adminData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<AdminResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }

        })
    }

}