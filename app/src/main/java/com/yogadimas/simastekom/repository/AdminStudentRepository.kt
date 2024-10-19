package com.yogadimas.simastekom.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.yogadimas.simastekom.api.ApiService
import com.yogadimas.simastekom.common.enums.Role
import com.yogadimas.simastekom.common.helper.getErrors
import com.yogadimas.simastekom.common.paging.GenericPagingSource
import com.yogadimas.simastekom.common.paging.student.StudentAllPagingSource
import com.yogadimas.simastekom.common.paging.student.StudentSearchSortPagingSource
import com.yogadimas.simastekom.common.state.State
import com.yogadimas.simastekom.model.responses.StudentData
import com.yogadimas.simastekom.model.responses.IdentityAcademicData
import com.yogadimas.simastekom.model.responses.IdentityPersonalData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AdminStudentRepository(private val apiService: ApiService) {

    /** Student */
    fun getAllStudents(
        token: String,
        sortBy: String,
        onError: (String) -> Unit
    ): Flow<PagingData<StudentData>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
//                prefetchDistance = 2, // Muat 2 halaman di depan
//                initialLoadSize = 16, // Jumlah item pada muatan pertama
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                StudentAllPagingSource(apiService, token, sortBy, onError)
            }
        ).flow
    }


    fun searchSortStudents(
        token: String,
        keyword: String?,
        sortBy: String?,
        sortDir: String?,
        onError: (String) -> Unit
    ): Flow<PagingData<StudentData>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
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

    /** Identity Personal */
    fun getIdentitiesPersonal(
        token: String,
        keyword: String? = null,
        sortBy: String? = null,
        sortDir: String? = null,
        onError: (String) -> Unit
    ): Flow<PagingData<IdentityPersonalData>> {
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
                        if (keyword.isNullOrEmpty() && sortBy == null) {
                            apiService.getAllIdentitiesPersonal(token, page, size,  Role.STUDENT.value)
                        } else {
                            apiService.searchSortIdentitiesPersonal(
                                token,
                                page,
                                size,
                                keyword,
                                sortBy,
                                sortDir,
                                Role.STUDENT.value
                            )
                        }
                    },
                    onError = onError
                )
            }
        ).flow
    }

    /** Identity Academic */
    fun getIdentitiesAcademic(
        token: String,
        keyword: String? = null,
        sortBy: String? = null,
        sortDir: String? = null,
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

    companion object {

        const val PAGE_SIZE = 5

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