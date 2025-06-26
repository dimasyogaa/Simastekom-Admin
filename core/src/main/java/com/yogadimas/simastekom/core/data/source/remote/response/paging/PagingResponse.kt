package com.yogadimas.simastekom.core.data.source.remote.response.paging

import com.google.gson.annotations.SerializedName

data class PagingResponse<ObjectData>(

    @field:SerializedName("code")
    val code: Int? = null,

    @field:SerializedName("data")
    val data: List<ObjectData>? = null,

    @field:SerializedName("meta")
    val meta: Meta? = null,

    @field:SerializedName("messageCode")
    val messageCode: String? = null,

    @field:SerializedName("links")
    val links: Links? = null
)




data class Meta(

    @field:SerializedName("path")
    val path: String? = null,

    @field:SerializedName("per_page")
    val perPage: Int? = null,

    @field:SerializedName("total")
    val total: Int? = null,

    @field:SerializedName("last_page")
    val lastPage: Int? = null,

    @field:SerializedName("from")
    val from: Int? = null,

    @field:SerializedName("links")
    val links: List<LinksItem?>? = null,

    @field:SerializedName("to")
    val to: Int? = null,

    @field:SerializedName("current_page")
    val currentPage: Int? = null
)



data class Links(

    @field:SerializedName("next")
    val next: String? = null,

    @field:SerializedName("last")
    val last: String? = null,

    @field:SerializedName("prev")
    val prev: Any? = null,

    @field:SerializedName("first")
    val first: String? = null
)



data class LinksItem(

    @field:SerializedName("active")
    val active: Boolean? = null,

    @field:SerializedName("label")
    val label: String? = null,

    @field:SerializedName("url")
    val url: Any? = null
)