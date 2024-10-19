package com.yogadimas.simastekom.common.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yogadimas.simastekom.api.ApiService
import com.yogadimas.simastekom.model.responses.PaginationResponse
import com.yogadimas.simastekom.repository.AdminStudentRepository.Companion.PAGE_SIZE

class GenericPagingSource<T : Any>(
    private val token: String,
    private val keyword: String? = null,
    private val sortBy: String? = "asc",
    private val sortDir: String? = null,
    private val fetchData: suspend (String, Int, Int, String?, String?, String?) -> PaginationResponse<T>,
    private val onError: (String) -> Unit
) : PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: 1
        return try {
            // Panggil API dengan parameter dinamis berdasarkan fungsi fetchData
            val response = fetchData(
                token,
                page,
                params.loadSize,
                keyword,
                sortBy,
                sortDir
            )

            val data = response.data ?: emptyList()

            val nextKey = if (response.meta?.currentPage == response.meta?.lastPage) {
                null
            } else {
                page + (params.loadSize / PAGE_SIZE)
            }

            LoadResult.Page(
                data = data.filterNotNull(),
                prevKey = if (page == 1) null else page - 1,
                nextKey = nextKey
            )
        } catch (exception: Exception) {
            val errorMessage = exception.localizedMessage ?: "null"
            onError(errorMessage)
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        val anchorPosition = state.anchorPosition
        val anchorPage = anchorPosition?.let { state.closestPageToPosition(it) }

        return anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
    }
}

