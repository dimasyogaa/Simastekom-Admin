package com.yogadimas.simastekom.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.yogadimas.simastekom.api.ApiService
import com.yogadimas.simastekom.common.helper.getErrors
import com.yogadimas.simastekom.common.paging.Constant
import com.yogadimas.simastekom.common.paging.GenericPagingSource
import com.yogadimas.simastekom.common.paging.student.StudentAllPagingSource
import com.yogadimas.simastekom.common.paging.student.StudentSearchSortPagingSource
import com.yogadimas.simastekom.common.state.State
import com.yogadimas.simastekom.model.responses.IdentityAcademicData
import com.yogadimas.simastekom.model.responses.StudentData
import com.yogadimas.simastekom.model.responses.StudentIdentityParentData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AdminStudentRepository(private val apiService: ApiService) {

    /** Student */
    fun getAllStudents(
        token: String,
        sortDir: String,
        onError: (String) -> Unit,
    ): Flow<PagingData<StudentData>> {
        return Pager(
            config = PagingConfig(
                pageSize =  Constant.PAGE_SIZE,
//                prefetchDistance = 2, // Muat 2 halaman di depan
//                initialLoadSize = 16, // Jumlah item pada muatan pertama
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                StudentAllPagingSource(apiService, token, sortDir, onError)
            }
        ).flow
    }


    fun searchSortStudents(
        token: String,
        keyword: String?,
        sortBy: String?,
        sortDir: String?,
        onError: (String) -> Unit,
    ): Flow<PagingData<StudentData>> {
        return Pager(
            config = PagingConfig(
                pageSize =  Constant.PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                StudentSearchSortPagingSource(
                    apiService, token,
                    keyword, sortBy, sortDir,
                    onError
                )
            }
        ).flow
    }


    private val _studentState = MutableSharedFlow<State<StudentData>>()
    val studentState: SharedFlow<State<StudentData>> get() = _studentState.asSharedFlow()
    suspend fun getStudentById(token: String, id: String) {
        _studentState.emit(State.Loading)
        val response = runCatching { apiService.getStudentById(token, id) }

        response.onSuccess {

            if (it.isSuccessful && it.body() != null) {
                val data = it.body()?.studentData ?: StudentData()
                _studentState.emit(State.Success(data))
            } else {
                val errorResponse = it.errorBody()?.string().orEmpty()
                _studentState.emit(State.ErrorClient(getErrors(errorResponse)))
            }
        }.onFailure { exception ->
            _studentState.emit(State.ErrorServer(exception.message.toString()))
        }
    }

    suspend fun updateStudent(token: String, id: String, studentData: StudentData) {
        _studentState.emit(State.Loading)
        val response = runCatching { apiService.updateStudent(token, id, studentData) }

        response.onSuccess {
            val data = it.body()?.studentData ?: StudentData()
            if (it.isSuccessful) {
                _studentState.emit(State.Success(data))
            } else {
                val errorResponse = it.errorBody()?.string().orEmpty()
                _studentState.emit(State.ErrorClient(getErrors(errorResponse)))
            }
        }.onFailure { exception ->
            _studentState.emit(State.ErrorServer(exception.message.toString()))
        }
    }

    suspend fun addStudent(token: String, studentData: StudentData) {
        _studentState.emit(State.Loading)
        val response = runCatching { apiService.addStudent(token, studentData) }

        response.onSuccess {
            val data = it.body()?.studentData ?: StudentData()
            if (it.isSuccessful) {
                _studentState.emit(State.Success(data))
            } else {
                val errorResponse = it.errorBody()?.string().orEmpty()
                _studentState.emit(State.ErrorClient(getErrors(errorResponse)))
            }
        }.onFailure { exception ->
            _studentState.emit(State.ErrorServer(exception.message.toString()))
        }
    }

    suspend fun deleteStudent(token: String, id: String) {
        _studentState.emit(State.Loading)
        val response = runCatching { apiService.deleteStudent(token, id) }

        response.onSuccess {

            if (it.isSuccessful && it.body() != null) {
                val data = it.body()?.studentData ?: StudentData()
                _studentState.emit(State.Success(data))
            } else {
                val errorResponse = it.errorBody()?.string().orEmpty()
                _studentState.emit(State.ErrorClient(getErrors(errorResponse)))
            }
        }.onFailure { exception ->
            _studentState.emit(State.ErrorServer(exception.message.toString()))
        }
    }


    /** Identity Academic */
    fun getIdentitiesAcademic(
        token: String,
        keyword: String? = null,
        sortBy: String? = null,
        sortDir: String? = null,
        onError: (String) -> Unit,
    ): Flow<PagingData<IdentityAcademicData>> {
        return Pager(
            config = PagingConfig(
                pageSize =  Constant.PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                GenericPagingSource(
                    token = token,
                    keyword = keyword,
                    sortBy = sortBy,
                    sortDir = sortDir,
                    fetchData = { token, page, size, keyword, sortBy, sortDir ->
                        if (keyword.isNullOrEmpty() && sortBy == null) {
                            apiService.getAllIdentitiesAcademic(token, page, size)
                        } else {
                            apiService.searchSortIdentitiesAcademic(
                                token,
                                page,
                                size,
                                keyword,
                                sortBy,
                                sortDir
                            )
                        }
                    },
                    onError = onError
                )
            }
        ).flow
    }

    /*fun getAllIdentitiesAcademic(
        token: String,
        onError: (String) -> Unit
    ): Flow<PagingData<IdentityAcademicData>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                GenericPagingSource(
                    token = token,
                    fetchData = { token, page, size, _, _, _ ->
                        apiService.getAllIdentitiesAcademic(token, page, size)
                    },
                    onError = onError
                )
            }
        ).flow
    }


    fun searchSortIdentitiesAcademic(
        token: String,
        keyword: String?,
        sortBy: String?,
        sortDir: String?,
        onError: (String) -> Unit
    ): Flow<PagingData<IdentityAcademicData>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                GenericPagingSource(
                    token = token,
                    keyword = keyword,
                    sortBy = sortBy,
                    sortDir = sortDir,
                    fetchData = { token, page, size, keyword, sortBy, sortDir ->
                        apiService.searchSortIdentitiesAcademic(
                            token,
                            page,
                            size,
                            keyword,
                            sortBy,
                            sortDir
                        )
                    },
                    onError = onError
                )
            }
        ).flow
    }*/

    /** Identity Parent */
    fun getStudentIdentitiesParent(
        token: String,
        keyword: String? = null,
        sortBy: String? = null,
        sortDir: String? = null,
        onError: (String) -> Unit,
    ): Flow<PagingData<StudentIdentityParentData>> {
        return Pager(
            config = PagingConfig(
                pageSize =  Constant.PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                GenericPagingSource(
                    token = token,
                    keyword = keyword,
                    sortBy = sortBy,
                    sortDir = sortDir,
                    fetchData = { token, page, size, keyword, sortBy, sortDir ->
                        if (keyword.isNullOrEmpty() && sortBy == null) {
                            apiService.getAllStudentIdentitiesParent(token, page, size)
                        } else {
                            apiService.searchSortStudentIdentitiesParent(
                                token,
                                page,
                                size,
                                keyword,
                                sortBy,
                                sortDir
                            )
                        }
                    },
                    onError = onError
                )
            }
        ).flow
    }

    private val _studentIdentityParentState = MutableSharedFlow<State<StudentIdentityParentData>>()
    val studentIdentityParentState: SharedFlow<State<StudentIdentityParentData>> get() = _studentIdentityParentState.asSharedFlow()
    suspend fun getStudentIdentityParentById(token: String, id: String) {
        _studentIdentityParentState.emit(State.Loading)
        val response = runCatching { apiService.getStudentIdentityParentById(token, id) }
        response.onSuccess {
            if (it.isSuccessful && it.body() != null) {
                val data = it.body()?.studentIdentityParentData ?: StudentIdentityParentData()
                _studentIdentityParentState.emit(State.Success(data))
            } else {
                val errorResponse = it.errorBody()?.string().orEmpty()
                _studentIdentityParentState.emit(State.ErrorClient(getErrors(errorResponse)))
            }
        }.onFailure { exception ->
            _studentIdentityParentState.emit(State.ErrorServer(exception.message.toString()))
        }
    }

    suspend fun addStudentIdentityParent(token: String, data: StudentIdentityParentData) {
        _studentIdentityParentState.emit(State.Loading)
        val response = runCatching { apiService.addStudentIdentityParent(token, data) }

        response.onSuccess {
            val result = it.body()?.studentIdentityParentData ?: StudentIdentityParentData()
            if (it.isSuccessful) {
                _studentIdentityParentState.emit(State.Success(result))
            } else {
                val errorResponse = it.errorBody()?.string().orEmpty()
                _studentIdentityParentState.emit(State.ErrorClient(getErrors(errorResponse)))
            }
        }.onFailure { exception ->
            _studentIdentityParentState.emit(State.ErrorServer(exception.message.toString()))
        }
    }

    suspend fun updateStudentIdentityParent(token: String,id: String, data: StudentIdentityParentData,
    ) {
        _studentIdentityParentState.emit(State.Loading)
        val response = runCatching { apiService.updateStudentIdentityParent(token, id, data) }

        response.onSuccess {
            val result = it.body()?.studentIdentityParentData ?: StudentIdentityParentData()
            if (it.isSuccessful) {
                _studentIdentityParentState.emit(State.Success(result))
            } else {
                val errorResponse = it.errorBody()?.string().orEmpty()
                _studentIdentityParentState.emit(State.ErrorClient(getErrors(errorResponse)))
            }
        }.onFailure { exception ->
            _studentIdentityParentState.emit(State.ErrorServer(exception.message.toString()))
        }
    }

    suspend fun deleteStudentIdentityParent(token: String, id: String) {
        _studentIdentityParentState.emit(State.Loading)
        val response = runCatching { apiService.deleteStudentIdentityParent(token, id) }

        response.onSuccess {

            if (it.isSuccessful && it.body() != null) {
                val result = it.body()?.studentIdentityParentData ?: StudentIdentityParentData()
                _studentIdentityParentState.emit(State.Success(result))
            } else {
                val errorResponse = it.errorBody()?.string().orEmpty()
                _studentIdentityParentState.emit(State.ErrorClient(getErrors(errorResponse)))
            }
        }.onFailure { exception ->
            _studentIdentityParentState.emit(State.ErrorServer(exception.message.toString()))
        }
    }

    fun getStudentIdentityParentByIdLiveData(
        token: String, id: String
    ) : LiveData<State<StudentIdentityParentData>> = liveData {
        emit(State.Loading)
        try {
            val response = apiService.getStudentIdentityParentById(token, id)
            if (response.isSuccessful) {
                val result = response.body()?.studentIdentityParentData ?: StudentIdentityParentData()
                emit(State.Success(result))
            } else {
                val errorResponse = response.errorBody()?.string().orEmpty()
                emit(State.ErrorClient(getErrors(errorResponse)))
            }
        } catch (exception: Exception) {
            emit(State.ErrorServer(exception.message.toString()))
        }
    }

    fun updateStudentIdentityParentLiveData(
        token: String,
        id: String,
        data: StudentIdentityParentData
    ): LiveData<State<StudentIdentityParentData>> = liveData {
        emit(State.Loading)
        try {
            val response = apiService.updateStudentIdentityParent(token, id, data)
            if (response.isSuccessful) {
                val result = response.body()?.studentIdentityParentData ?: StudentIdentityParentData()
                emit(State.Success(result))
            } else {
                val errorResponse = response.errorBody()?.string().orEmpty()
                emit(State.ErrorClient(getErrors(errorResponse)))
            }
        } catch (exception: Exception) {
            emit(State.ErrorServer(exception.message.toString()))
        }
    }

    fun deleteStudentIdentityParentAddressLiveData(
        token: String,
        id: String,
    ): LiveData<State<StudentIdentityParentData>> = liveData {
        emit(State.Loading)
        try {
            val response = apiService.deleteStudentIdentityParentAddress(token, id)
            if (response.isSuccessful) {
                val result = response.body()?.studentIdentityParentData ?: StudentIdentityParentData()
                emit(State.Success(result))
            } else {
                val errorResponse = response.errorBody()?.string().orEmpty()
                emit(State.ErrorClient(getErrors(errorResponse)))
            }
        } catch (exception: Exception) {
            emit(State.ErrorServer(exception.message.toString()))
        }
    }




companion object {

    @Volatile
    private var instance: AdminStudentRepository? = null
    fun getInstance(
        apiService: ApiService,
    ): AdminStudentRepository =
        instance ?: synchronized(this) {
            instance ?: AdminStudentRepository(apiService)
        }.also { instance = it }
}

}