package com.yogadimas.simastekom.interfaces

// untuk komunikasi
fun interface OnOptionDialogListenerInterface {
    fun onOptionChosen(text: String, category: String)
}