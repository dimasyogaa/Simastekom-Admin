package com.yogadimas.simastekom.core.data.source.remote.datasource.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yogadimas.simastekom.core.common.constants.PAGE_SIZE
import com.yogadimas.simastekom.core.data.source.remote.response.paging.PagingResponse
import com.yogadimas.simastekom.core.utils.CustomPagingSourceException
import com.yogadimas.simastekom.core.utils.getErrorCode

class GenericPagingSource<T : Any>(
    private val loadFunction: suspend (page: Int, size: Int) -> PagingResponse<T>
) : PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: 1
        val size = params.loadSize

        return try {
            val response = loadFunction(page, size)
            val dataList = response.data ?: emptyList()
            val currentPage = response.meta?.currentPage ?: page
            val lastPage = response.meta?.lastPage ?: page

            val nextKey =
                if (currentPage == lastPage || dataList.isEmpty()) null else page + (size / PAGE_SIZE)

            LoadResult.Page(
                data = dataList,
                prevKey = if (page > 1) page - 1 else null,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(CustomPagingSourceException(code = getErrorCode(e)))
        }
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return state.anchorPosition?.let { anchorPos ->
            state.closestPageToPosition(anchorPos)?.let { page ->
                page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
            }
        }
    }
}
