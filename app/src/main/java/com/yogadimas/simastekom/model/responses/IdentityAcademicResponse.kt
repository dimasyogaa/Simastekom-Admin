package com.yogadimas.simastekom.model.responses

import com.google.gson.annotations.SerializedName

data class IdentityAcademicListResponse(

    @field:SerializedName("data")
    val identityAcademicData: List<IdentityAcademicData>,
)

data class IdentityAcademicObjectResponse(

    @field:SerializedName("data")
    val identityAcademicData: IdentityAcademicData,
)

data class IdentityAcademicData(

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("kode")
    val code: String? = null,

    @field:SerializedName("nama")
    val name: String? = null,

    @field:SerializedName("is_added")
    val isAdded: Boolean = false,

    @field:SerializedName("is_updated")
    val isUpdated: Boolean = false,

    @field:SerializedName("is_deleted")
    val isDeleted: Boolean = false,

    )
