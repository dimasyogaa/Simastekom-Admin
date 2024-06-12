package com.yogadimas.simastekom.interfaces



interface OnItemClickCallback<T> {
    fun onItemClicked(data: T)
    fun onDeleteClicked(data: T)
}