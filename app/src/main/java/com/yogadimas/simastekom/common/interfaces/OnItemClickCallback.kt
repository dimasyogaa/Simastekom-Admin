package com.yogadimas.simastekom.common.interfaces


fun interface OnItemClickCallback<T> {
    fun onItemClicked(data: T)
}