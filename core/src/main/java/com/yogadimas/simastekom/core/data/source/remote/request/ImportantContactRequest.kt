package com.yogadimas.simastekom.core.data.source.remote.request

import com.google.gson.annotations.SerializedName
import com.yogadimas.simastekom.core.data.source.remote.request.base.BaseRequest
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImportantContactRequest(

    @field:SerializedName("nama")
    var name: String? = null,

    @field:SerializedName("telepon")
    var phone: String? = null,

    @field:SerializedName("id_kontak_penting_kategori")
    var categoryId: Int? = null,

    var categoryName: String? = null,

    @field:SerializedName("keterangan")
    var information: String? = null,
) : BaseRequest()