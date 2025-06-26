package com.yogadimas.simastekom.core.common.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class IdNameResult(
    val id: Int? = null,
    val name: String? = null,
): Parcelable