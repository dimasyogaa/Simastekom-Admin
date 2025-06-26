package com.yogadimas.simastekom.model.responses

import com.google.gson.annotations.SerializedName


data class NameListResponse(

    @field:SerializedName("data")
    val nameData: List<NameData>
)

data class NameObjectResponse(

    @field:SerializedName("data")
    val nameData: NameData,
)

data class NameData(

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("nama")
    val name: String? = null,

    @field:SerializedName("keterangan")
    val information: String? = null,

    @field:SerializedName("is_added")
    val isAdded: Boolean = false,

    @field:SerializedName("is_updated")
    val isUpdated: Boolean = false,

    @field:SerializedName("is_deleted")
    val isDeleted: Boolean = false,
)
