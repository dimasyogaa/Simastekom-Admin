package com.yogadimas.simastekom.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ErrorData(
    var message: List<String>? = null,
): Parcelable