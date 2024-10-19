package com.yogadimas.simastekom.common.paging.student

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yogadimas.simastekom.api.ApiService
import com.yogadimas.simastekom.model.responses.StudentData
import com.yogadimas.simastekom.repository.AdminStudentRepository.Companion.PAGE_SIZE


class StudentAllPagingSource(
    private val apiService: ApiService,
    private val token: String,
    private val sortBy: String,
    private val onError: (String) -> Unit
) : PagingSource<Int, StudentData>() {



    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StudentData> {
        val page = params.key ?: 1
        return try {
            val response = apiService.getAllStudents(token, page, params.loadSize,  sortBy)
            val students = response.data ?: emptyList()

            val nextKey = if (response.meta?.currentPage == response.meta?.lastPage) {
                null
            } else {
                page + (params.loadSize / PAGE_SIZE)
            }
            LoadResult.Page(
                data = students.filterNotNull(),
                prevKey = if (page == 1) null else page - 1,
                nextKey = nextKey
            )
        }  catch (exception: Exception) {
            val errorMessage = exception.localizedMessage
            onError(errorMessage ?: "null")
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, StudentData>): Int? {
        val anchorPosition = state.anchorPosition
        val anchorPage = anchorPosition?.let { state.closestPageToPosition(it) }

        return anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
    }


}
