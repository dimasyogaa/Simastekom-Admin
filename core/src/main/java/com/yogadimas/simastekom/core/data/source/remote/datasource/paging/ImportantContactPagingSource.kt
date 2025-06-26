package com.yogadimas.simastekom.core.data.source.remote.datasource.paging


import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yogadimas.simastekom.core.common.constants.PAGE_SIZE
import com.yogadimas.simastekom.core.data.source.remote.network.SimastekomMahasiswaApiService
import com.yogadimas.simastekom.core.data.source.remote.request.paging.PagingRequest
import com.yogadimas.simastekom.core.data.source.remote.response.ImportantContactData
import com.yogadimas.simastekom.core.utils.CustomPagingSourceException
import com.yogadimas.simastekom.core.utils.getErrorCode
import com.yogadimas.simastekom.core.utils.toQueryMap

class ImportantContactPagingSource(
    private val apiService: SimastekomMahasiswaApiService,
    private val searchKeyword: String?
) : PagingSource<Int, ImportantContactData>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ImportantContactData> {
        val page = params.key ?: 1
        val size = params.loadSize

        return try {

            val request = PagingRequest(page = page, size = size, searchKeyword = searchKeyword)
            val response = apiService.getImportantContacts(request.toQueryMap())
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
        } catch (exception: Exception) {
            LoadResult.Error(CustomPagingSourceException(code = getErrorCode(exception)))
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ImportantContactData>): Int? {
        return state.anchorPosition?.let { anchorPos ->
            state.closestPageToPosition(anchorPos)?.let { page ->
                page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
            }
        }
    }
}
