package com.yogadimas.simastekom.common.interfaces

import com.yogadimas.simastekom.common.enums.FieldType
import com.yogadimas.simastekom.model.responses.IdentityPersonalData


fun interface OnItemClickIdentityPersonalCallback {
    fun onItemClicked(data: IdentityPersonalData, fieldType: FieldType)
}