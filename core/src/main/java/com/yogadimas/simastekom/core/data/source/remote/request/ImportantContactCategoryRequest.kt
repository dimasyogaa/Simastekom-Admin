package com.yogadimas.simastekom.core.data.source.remote.request

import com.google.gson.annotations.SerializedName
import com.yogadimas.simastekom.core.data.source.remote.request.base.BaseRequest
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImportantContactCategoryRequest(

    @field:SerializedName("nama")
    var name: String? = null,

    ) : BaseRequest()