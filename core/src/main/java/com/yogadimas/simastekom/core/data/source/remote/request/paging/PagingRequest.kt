package com.yogadimas.simastekom.core.data.source.remote.request.paging

import com.google.gson.annotations.SerializedName


data class PagingRequest(
    @field:SerializedName("page")
    val page: Int? =null,

    @field:SerializedName("size")
    val size: Int? =null,

    @field:SerializedName("search")
    val searchKeyword: String? = null
)
