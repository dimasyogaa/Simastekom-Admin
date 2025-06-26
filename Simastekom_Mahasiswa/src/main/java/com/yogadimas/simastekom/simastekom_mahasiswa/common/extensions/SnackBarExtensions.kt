package com.yogadimas.simastekom.simastekom_mahasiswa.common.extensions

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.yogadimas.simastekom.core.R
import com.yogadimas.simastekom.core.common.customs.SnackBar

fun AppCompatActivity.showSnackBarErrorServer(
    viewBinding: ViewBinding,
    message: String?,
    anchor: View? = null
) {
    SnackBar.display(
        viewGroup = viewBinding.root as ViewGroup,
        message = message ?: getString(R.string.text_there_is_an_error_on_server),
        lifecycleOwner = this,
        anchorView = anchor
    )
}
