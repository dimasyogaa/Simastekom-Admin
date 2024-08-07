package com.yogadimas.simastekom.model.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class CampusListResponse(

    @field:SerializedName("data")
    val campusData: List<CampusData>
)

data class CampusObjectResponse(

    @field:SerializedName("data")
    val campusData: CampusData,
)

@Parcelize
data class CampusData(

    @field:SerializedName("nama")
    val name: String? = null,

    @field:SerializedName("kode")
    val code: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("alamat")
    val address: String? = null,

    @field:SerializedName("is_added")
    val isAdded: Boolean = false,

    @field:SerializedName("is_updated")
    val isUpdated: Boolean = false,

    @field:SerializedName("is_deleted")
    val isDeleted: Boolean = false,
) : Parcelable
