package com.yogadimas.simastekom.model

import com.yogadimas.simastekom.common.enums.ManipulationType

data class HitApi<T>(
    val token: String,
    val id: String,
    val manipulationType: ManipulationType,
    val data: T? = null,
)

