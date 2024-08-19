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
import com.yogadimas.simastekom.model.responses.CampusData
import com.yogadimas.simastekom.model.responses.CampusListResponse
import com.yogadimas.simastekom.model.responses.CampusObjectResponse
import com.yogadimas.simastekom.model.responses.Errors
import com.yogadimas.simastekom.model.responses.IdentityAcademicData
import com.yogadimas.simastekom.model.responses.IdentityAcademicListResponse
import com.yogadimas.simastekom.model.responses.IdentityAcademicObjectResponse
import com.yogadimas.simastekom.model.responses.IdentityPersonalData
import com.yogadimas.simastekom.model.responses.IdentityPersonalResponse
import com.yogadimas.simastekom.model.responses.NameData
import com.yogadimas.simastekom.model.responses.NameListResponse
import com.yogadimas.simastekom.model.responses.NameObjectResponse
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

    private val _campusList = MutableLiveData<Event<List<CampusData>?>>()
    val campusList: LiveData<Event<List<CampusData>?>> = _campusList

    private val _campus = MutableLiveData<Event<CampusData?>>()
    val campus: LiveData<Event<CampusData?>> = _campus

    private val _nameList = MutableLiveData<Event<List<NameData>?>>()
    val nameList: LiveData<Event<List<NameData>?>> = _nameList

    private val _name = MutableLiveData<Event<NameData?>>()
    val name: LiveData<Event<NameData?>> = _name

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

    /** EMPLOYMENT STATUS */
    fun addEmploymentStatus(nameData: NameData) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().addEmploymentStatus(token, nameData)
        client.enqueue(object : Callback<NameObjectResponse> {
            override fun onResponse(
                call: Call<NameObjectResponse>,
                response: Response<NameObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _name.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<NameObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }

    fun getAllEmploymentStatuses() {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getAllEmploymentStatus(token)
        client.enqueue(object : Callback<NameListResponse> {
            override fun onResponse(
                call: Call<NameListResponse>,
                response: Response<NameListResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _nameList.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<NameListResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }

    fun searchSortEmploymentStatus(keyword: String?, sortBy: String?, sortDir: String?) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().searchSortEmploymentStatus(token, keyword, sortBy, sortDir)
        client.enqueue(object : Callback<NameListResponse> {
            override fun onResponse(
                call: Call<NameListResponse>,
                response: Response<NameListResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _nameList.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<NameListResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }

    fun getEmploymentStatusById(id: Int) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getEmploymentStatusById(token, id)
        client.enqueue(object : Callback<NameObjectResponse> {
            override fun onResponse(
                call: Call<NameObjectResponse>,
                response: Response<NameObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _name.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<NameObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }

    fun updateEmploymentStatus(id: Int, nameData: NameData) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().updateEmploymentStatus(token, id, nameData)
        client.enqueue(object : Callback<NameObjectResponse> {
            override fun onResponse(
                call: Call<NameObjectResponse>,
                response: Response<NameObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _name.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<NameObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }

    fun deleteEmploymentStatus(id: Int) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().deleteEmploymentStatus(token, id)
        client.enqueue(object : Callback<NameObjectResponse> {
            override fun onResponse(
                call: Call<NameObjectResponse>,
                response: Response<NameObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _name.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<NameObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }

    /** STUDENT STATUS */
    fun addStudentStatus(nameData: NameData) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().addStudentStatus(token, nameData) // Mengubah dari addClassSession menjadi addStudentStatus
        client.enqueue(object : Callback<NameObjectResponse> {
            override fun onResponse(
                call: Call<NameObjectResponse>,
                response: Response<NameObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _name.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<NameObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun getAllStudentStatuses() {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getAllStudentStatus(token) // Mengubah dari getAllClassSessions menjadi getAllStudentStatuses
        client.enqueue(object : Callback<NameListResponse> {
            override fun onResponse(
                call: Call<NameListResponse>,
                response: Response<NameListResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _nameList.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<NameListResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun searchSortStudentStatus(keyword: String?, sortBy: String?, sortDir: String?) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().searchSortStudentStatus(token, keyword, sortBy, sortDir) // Mengubah dari searchSortClassSession menjadi searchSortStudentStatus
        client.enqueue(object : Callback<NameListResponse> {
            override fun onResponse(
                call: Call<NameListResponse>,
                response: Response<NameListResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _nameList.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<NameListResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun getStudentStatusById(id: Int) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getStudentStatusById(token, id) // Mengubah dari getClassSessionById menjadi getStudentStatusById
        client.enqueue(object : Callback<NameObjectResponse> {
            override fun onResponse(
                call: Call<NameObjectResponse>,
                response: Response<NameObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _name.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<NameObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun updateStudentStatus(id: Int, nameData: NameData) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().updateStudentStatus(token, id, nameData) // Mengubah dari updateClassSession menjadi updateStudentStatus
        client.enqueue(object : Callback<NameObjectResponse> {
            override fun onResponse(
                call: Call<NameObjectResponse>,
                response: Response<NameObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _name.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<NameObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun deleteStudentStatus(id: Int) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().deleteStudentStatus(token, id) // Mengubah dari deleteClassSession menjadi deleteStudentStatus
        client.enqueue(object : Callback<NameObjectResponse> {
            override fun onResponse(
                call: Call<NameObjectResponse>,
                response: Response<NameObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _name.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<NameObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }

    /** LECTURE METHOD */
    fun addLectureMethod(nameData: NameData) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().addLectureMethod(token, nameData)
        client.enqueue(object : Callback<NameObjectResponse> {
            override fun onResponse(
                call: Call<NameObjectResponse>,
                response: Response<NameObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _name.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<NameObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun getAllLectureMethods() {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getAllLectureMethods(token)
        client.enqueue(object : Callback<NameListResponse> {
            override fun onResponse(
                call: Call<NameListResponse>,
                response: Response<NameListResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _nameList.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<NameListResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun searchSortLectureMethod(keyword: String?, sortBy: String?, sortDir: String?) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().searchSortLectureMethod(token, keyword, sortBy, sortDir)
        client.enqueue(object : Callback<NameListResponse> {
            override fun onResponse(
                call: Call<NameListResponse>,
                response: Response<NameListResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _nameList.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<NameListResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun getLectureMethodById(id: Int) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getLectureMethodById(token, id)
        client.enqueue(object : Callback<NameObjectResponse> {
            override fun onResponse(
                call: Call<NameObjectResponse>,
                response: Response<NameObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _name.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<NameObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun updateLectureMethod(id: Int, nameData: NameData) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().updateLectureMethod(token, id, nameData)
        client.enqueue(object : Callback<NameObjectResponse> {
            override fun onResponse(
                call: Call<NameObjectResponse>,
                response: Response<NameObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _name.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<NameObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun deleteLectureMethod(id: Int) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().deleteLectureMethod(token, id)
        client.enqueue(object : Callback<NameObjectResponse> {
            override fun onResponse(
                call: Call<NameObjectResponse>,
                response: Response<NameObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _name.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<NameObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }



    /** SEMESTER */
    fun addSemester(nameData: NameData) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().addSemester(token, nameData)
        client.enqueue(object : Callback<NameObjectResponse> {
            override fun onResponse(
                call: Call<NameObjectResponse>,
                response: Response<NameObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _name.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<NameObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }

        })
    }
    fun getAllSemesters() {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getAllSemesters(token)
        client.enqueue(object : Callback<NameListResponse> {
            override fun onResponse(
                call: Call<NameListResponse>,
                response: Response<NameListResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _nameList.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<NameListResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun searchSortSemester(keyword: String?, sortBy: String?, sortDir: String?) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().searchSortSemester(token, keyword, sortBy, sortDir)
        client.enqueue(object : Callback<NameListResponse> {
            override fun onResponse(
                call: Call<NameListResponse>,
                response: Response<NameListResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _nameList.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<NameListResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun getSemesterById(id: Int) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getSemesterById(token, id)
        client.enqueue(object : Callback<NameObjectResponse> {
            override fun onResponse(
                call: Call<NameObjectResponse>,
                response: Response<NameObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _name.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<NameObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun updateSemester(id: Int, nameData: NameData) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().updateSemester(token, id, nameData)
        client.enqueue(object : Callback<NameObjectResponse> {
            override fun onResponse(
                call: Call<NameObjectResponse>,
                response: Response<NameObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _name.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<NameObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun deleteSemester(id: Int) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().deleteSemester(token, id)
        client.enqueue(object : Callback<NameObjectResponse> {
            override fun onResponse(
                call: Call<NameObjectResponse>,
                response: Response<NameObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _name.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<NameObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }


    /** CLASS SESSION */
    fun addClassSession(nameData: NameData) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().addClassSession(token, nameData)
        client.enqueue(object : Callback<NameObjectResponse> {
            override fun onResponse(
                call: Call<NameObjectResponse>,
                response: Response<NameObjectResponse>,
            ) {

                _isLoading.value = false
                if (response.isSuccessful) {
                    _name.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<NameObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }

        })

    }
    fun getAllClassSessions() {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getAllClassSession(token)
        client.enqueue(object : Callback<NameListResponse> {
            override fun onResponse(
                call: Call<NameListResponse>,
                response: Response<NameListResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _nameList.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<NameListResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun searchSortClassSession(keyword: String?, sortBy: String?, sortDir: String?) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().searchSortClassSession(token, keyword, sortBy, sortDir)
        client.enqueue(object : Callback<NameListResponse> {
            override fun onResponse(
                call: Call<NameListResponse>,
                response: Response<NameListResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _nameList.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<NameListResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun getClassSessionById(id: Int) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getClassSessionById(token, id)
        client.enqueue(object : Callback<NameObjectResponse> {
            override fun onResponse(
                call: Call<NameObjectResponse>,
                response: Response<NameObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _name.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<NameObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun updateClassSession(id: Int, nameData: NameData) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().updateClassSession(token, id, nameData)
        client.enqueue(object : Callback<NameObjectResponse> {
            override fun onResponse(
                call: Call<NameObjectResponse>,
                response: Response<NameObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _name.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<NameObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun deleteClassSession(id: Int) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().deleteClassSession(token, id)
        client.enqueue(object : Callback<NameObjectResponse> {
            override fun onResponse(
                call: Call<NameObjectResponse>,
                response: Response<NameObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _name.value = Event(response.body()?.nameData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<NameObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }

    /** CAMPUS */
    fun addCampus(campusData: CampusData) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().addCampus(token, campusData)
        client.enqueue(object : Callback<CampusObjectResponse> {
            override fun onResponse(
                call: Call<CampusObjectResponse>,
                response: Response<CampusObjectResponse>,
            ) {

                _isLoading.value = false
                if (response.isSuccessful) {
                    _campus.value = Event(response.body()?.campusData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))
                }
            }

            override fun onFailure(call: Call<CampusObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }

        })

    }
    fun getAllCampuses() {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getAllCampus(token)
        client.enqueue(object : Callback<CampusListResponse> {
            override fun onResponse(
                call: Call<CampusListResponse>,
                response: Response<CampusListResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _campusList.value = Event(response.body()?.campusData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<CampusListResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun searchSortCampus(keyword: String?, sortBy: String?, sortDir: String?) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().searchSortCampus(token, keyword, sortBy, sortDir)
        client.enqueue(object : Callback<CampusListResponse> {
            override fun onResponse(
                call: Call<CampusListResponse>,
                response: Response<CampusListResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _campusList.value = Event(response.body()?.campusData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<CampusListResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun getCampusById(id: Int) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getCampusById(token, id)
        client.enqueue(object : Callback<CampusObjectResponse> {
            override fun onResponse(
                call: Call<CampusObjectResponse>,
                response: Response<CampusObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _campus.value = Event(response.body()?.campusData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<CampusObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun updateCampus(id: Int, campusData: CampusData) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().updateCampus(token, id, campusData)
        client.enqueue(object : Callback<CampusObjectResponse> {
            override fun onResponse(
                call: Call<CampusObjectResponse>,
                response: Response<CampusObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _campus.value = Event(response.body()?.campusData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<CampusObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }
    fun deleteCampus(id: Int) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().deleteCampus(token, id)
        client.enqueue(object : Callback<CampusObjectResponse> {
            override fun onResponse(
                call: Call<CampusObjectResponse>,
                response: Response<CampusObjectResponse>,
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _campus.value = Event(response.body()?.campusData)
                } else {
                    _errors.value = Event(getErrors(response.errorBody()?.string().orEmpty()))

                }
            }

            override fun onFailure(call: Call<CampusObjectResponse>, t: Throwable) {
                _isLoading.value = false
                _snackbarText.value = Event(t.message.toString())
            }
        })
    }


    /** STUDY PROGRAM */
    fun addStudyProgram(identityAcademicData: IdentityAcademicData) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().addStudyProgram(token, identityAcademicData)
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
    fun getAllStudyPrograms() {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getAllStudyPrograms(token)
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
    fun searchSortStudyProgram(keyword: String?, sortBy: String?, sortDir: String?) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().searchSortStudyProgram(token, keyword, sortBy, sortDir)
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
    fun getStudyProgramById(id: Int) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getStudyProgramById(token, id)
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
    fun updateStudyProgram(id: Int, identityAcademicData: IdentityAcademicData) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().updateStudyProgram(token, id, identityAcademicData)
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
    fun deleteStudyProgram(id: Int) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().deleteStudyProgram(token, id)
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


    /** DEGREE */
    fun addDegree(code: String, name: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().addDegree(token, code, name)
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
    fun getAllDegrees() {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getAllDegrees(token)
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
    fun searchSortDegree(keyword: String?, sortBy: String?, sortDir: String?) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().searchSortDegree(token, keyword, sortBy, sortDir)
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
    fun getDegreeById(id: Int) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().getDegreeById(token, id)
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
    fun updateDegree(id: Int, identityAcademicData: IdentityAcademicData) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().updateDegree(token, id, identityAcademicData)
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
    fun deleteDegree(id: Int) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().deleteDegree(token, id)
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