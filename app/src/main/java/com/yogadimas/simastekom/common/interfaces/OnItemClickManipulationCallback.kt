package com.yogadimas.simastekom.common.interfaces



interface OnItemClickManipulationCallback<T> {
    fun onItemClicked(data: T)
    fun onDeleteClicked(data: T)
}