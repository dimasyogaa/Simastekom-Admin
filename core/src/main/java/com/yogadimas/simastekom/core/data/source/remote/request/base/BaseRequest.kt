package com.yogadimas.simastekom.core.data.source.remote.request.base

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
open class BaseRequest(open var id: Int = 0): Parcelable