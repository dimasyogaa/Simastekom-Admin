package com.yogadimas.simastekom.ui.admin

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.load
import coil.request.ImageRequest
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.yogadimas.simastekom.BuildConfig
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.databinding.ActivityAdminEditBinding
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.helper.hideKeyboard
import com.yogadimas.simastekom.common.helper.isContainsSpace
import com.yogadimas.simastekom.common.helper.reduceFileImage
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.common.helper.uriToFile
import com.yogadimas.simastekom.ui.identity.personal.IdentityPersonalEditActivity
import com.yogadimas.simastekom.ui.login.LoginActivity
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.UUID

class AdminEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminEditBinding

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private val adminViewModel: AdminViewModel by viewModel()

    private var isLoading = false
    private var isAlertDialogShow = false

    private var photoFile: File? = null

    private var deletePhoto: Boolean = false

    private var isSuccessDialogShowingOrientation = false

    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            isSuccessDialogShowingOrientation =
                savedInstanceState.getBoolean(KEY_SUCCESS_DIALOG_SHOWING)
            if (isSuccessDialogShowingOrientation) {
                showAlertDialog(status = STATUS_SUCCESS)
            }
        }

        getAdmin()

        binding.apply {


            toolbar.setNavigationIcon(R.drawable.z_ic_close)
            toolbar.setNavigationOnClickListener {
                finish()
            }

            btnChangePhoto.setOnClickListener {
                startGallery()
            }

            btnDeletePhoto.setOnClickListener {
                deletePhoto = true
                binding.ivProfile.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@AdminEditActivity,
                        R.drawable.z_ic_placeholder_profile
                    )
                )
            }

            btnSave.isEnabled =
                edtId.text.toString().trim().isNotEmpty() || edtName.text.toString().trim()
                    .isNotEmpty()
            edtId.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0 != null) {
                        if (isContainsSpace(p0)) {
                            inputLayoutId.error = getString(
                                R.string.text_cannot_contain_spaces_format,
                                getString(R.string.text_label_id_username)
                            )
                            inputLayoutId.isErrorEnabled = true
                            btnSave.isEnabled = false
                        } else {
                            inputLayoutId.error = null
                            inputLayoutId.isErrorEnabled = false
                            btnSave.isEnabled = p0.isNotEmpty()
                        }
                    }
                }

                override fun afterTextChanged(p0: Editable?) {}

            })

            edtName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0.toString().isNotEmpty()) {
                        inputLayoutName.error = null
                        inputLayoutName.isErrorEnabled = false
                        btnSave.isEnabled = true
                    }
                    if (p0.toString().isEmpty()) {
                        btnSave.isEnabled = false
                    }
                }

                override fun afterTextChanged(p0: Editable?) {}

            })

            btnSave.setOnClickListener { updateSave() }

            tvIdentityPersonal.setOnClickListener {
                startActivity(
                    Intent(
                        this@AdminEditActivity,
                        IdentityPersonalEditActivity::class.java
                    )
                )
            }

            viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { getAdmin() }

        }
    }

    private fun getAdmin() {
        authViewModel.getUser().observe(this) {
            val token = it.first
            if (token == AuthPreferences.DEFAULT_VALUE) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                adminViewModel.token = token
                adminViewModel.getAdminCurrent()
            }
        }

        adminViewModel.isLoading.observe(this) {
            isLoading = it
            showLoadingMain(it)
        }

        adminViewModel.adminData.observe(this) { eventData ->
            eventData.getContentIfNotHandled()?.let {
                if (isLoading) {
                    isVisibleAllView(false)
                } else {
                    isVisibleAllView(true)
                }
                failedToConnect(false)



                if (it.logout) {
                    authViewModel.saveUser(null, null, null)
                }

                binding.apply {
                    edtId.setText(it.username)
                    edtName.setText(it.name)

                    val picture = BuildConfig.BASE_URL + it.profilePicture

                    ivProfile.load(picture) {
                        crossfade(true)
                        placeholder(R.drawable.z_ic_placeholder_profile)
                        error(R.drawable.z_ic_placeholder_profile)
                    }
                    val loader = ImageLoader(this@AdminEditActivity)
                    val req = ImageRequest.Builder(this@AdminEditActivity)
                        .data(picture)
                        .target { result ->
                            val bitmap = (result as BitmapDrawable).bitmap
                            photoFile = convertImageViewToFile(bitmap)
                        }
                        .build()

                    loader.enqueue(req)
                }

                if (it.isUpdated) {
                    isSuccessDialogShowingOrientation = true
                    showAlertDialog(status = STATUS_SUCCESS)
                }
            }


        }

        adminViewModel.errors.observe(this) { eventError ->
            eventError.getContentIfNotHandled()?.let { data ->
                if (data.errors != null) {
                    val errors = data.errors
                    var listMessage: List<String> = listOf()
                    when {
                        errors.message != null -> {
                            listMessage = errors.message
                        }

                        errors.username != null -> {
                            listMessage = errors.username
                        }

                        errors.name != null -> {
                            listMessage = errors.name
                        }
                    }
                    isVisibleAllView(true)
                    failedToConnect(false)
                    showAlertDialog(listMessage[0], STATUS_ERROR)
                }
            }
        }

        adminViewModel.errorsSnackbarText.observe(this) { eventString ->
            eventString.getContentIfNotHandled()?.let { snackBarText ->
                hideKeyboard()
                isVisibleAllView(false)
                failedToConnect(true)
                Snackbar.make(
                    binding.root as ViewGroup,
                    snackBarText,
                    Snackbar.LENGTH_SHORT
                ).show()

            }
        }

    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Pilih foto")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this)
            photoFile = myFile
            deletePhoto = false
            binding.ivProfile.setImageURI(selectedImg)
        }
    }

    private fun convertImageViewToFile(bitmap: Bitmap): File {
        val wrapper = ContextWrapper(this)
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")
        val stream: OutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, stream)
        stream.flush()
        stream.close()
        return file
    }

    private fun checkValidated(id: String, name: String): Boolean {

        fun stringFormat(string: String): String {
            return String.format(getString(R.string.text_mandatory_field_format), string)
        }

        fun checkInputIsEmpty(
            inputView: TextInputLayout,
            message: String? = null,
        ) {
            if (message != null) {
                inputView.isErrorEnabled = true
                inputView.error = stringFormat(message)
            } else {
                inputView.error = null
                inputView.isErrorEnabled = false
            }

        }

        var allInputAreFilled = false

        binding.apply {
            if (id.isEmpty()) {
                checkInputIsEmpty(inputLayoutId, getString(R.string.text_label_id_username))
                if (name.isNotEmpty()) checkInputIsEmpty(inputLayoutName)
            } else {
                checkInputIsEmpty(inputLayoutId)
                if (name.isEmpty()) {
                    checkInputIsEmpty(inputLayoutName, getString(R.string.text_label_name))
                } else {
                    checkInputIsEmpty(inputLayoutName)
                    allInputAreFilled = true
                }
            }
        }


        return allInputAreFilled
    }

    private fun updateSave() {
        hideKeyboard()
        if (checkValidated(
                binding.edtId.text.toString().trim(),
                binding.edtName.text.toString().trim()
            )
        ) {
            var file: File? = null
            lifecycleScope.launch(Dispatchers.Main) {
                isVisibleAllView(false)
                failedToConnect(false)
                showLoadingMain(true)

                withContext(Dispatchers.Default) {
                    if (photoFile != null) {
                        file = reduceFileImage(photoFile as File)
                    }
                    withContext(Dispatchers.Main) {
                        val username =
                            binding.edtId.text.toString().trim()
                                .toRequestBody("text/plain".toMediaType())

                        val name = binding.edtName.text.toString().trim()
                            .toRequestBody("text/plain".toMediaType())


                        val requestImageFile = file?.asRequestBody("image/jpeg".toMediaType())

                        val imageMultipart: MultipartBody.Part? = requestImageFile?.let {
                            MultipartBody.Part.createFormData(
                                "foto_profil", file?.name, it
                            )
                        }


                        val deletePhoto = deletePhoto.toString()
                            .toRequestBody("text/plain".toMediaType())


                        adminViewModel.updateAdminCurrent(
                            imageMultipart,
                            username,
                            name,
                            deletePhoto
                        )

                    }
                }
            }
        }
    }

    private fun showAlertDialog(msg: String = "", status: String) {

        val unauthorized = msg == getString(R.string.text_const_unauthorized)
        var title = ""
        var message = ""
        var icon: Drawable? = null
        when (status) {
            STATUS_SUCCESS -> {
                icon = ContextCompat.getDrawable(this, R.drawable.z_ic_check)
                val wrappedDrawable = DrawableCompat.wrap(icon!!).mutate()
                val color = ContextCompat.getColor(this, R.color.colorFixedGreen)
                DrawableCompat.setTint(wrappedDrawable, color)
                title = getString(R.string.text_success)
                message = getString(R.string.text_alert_update_data_format, title)
            }

            STATUS_ERROR -> {
                if (unauthorized) {
                    icon = ContextCompat.getDrawable(this, R.drawable.z_ic_warning)
                    title = getString(R.string.text_login_again)
                    message = getString(R.string.text_please_login_again)
                } else {
                    icon = ContextCompat.getDrawable(this, R.drawable.z_ic_warning)
                    title = getString(R.string.text_error, "")
                    message = msg
                }

            }
        }

        if (dialog == null) {
            if (status == STATUS_SUCCESS) {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1500)
                    isAlertDialogShow = false
                    isSuccessDialogShowingOrientation = false
                    dialog?.dismiss()
                    dialog = null
                }
            }
            dialog = MaterialAlertDialogBuilder(this).apply {
                setCancelable(false)
                setIcon(icon)
                setTitle(title)
                setMessage(message)
                if (status == STATUS_ERROR) {
                    setPositiveButton(getString(R.string.text_ok)) { _, _ ->
                        isAlertDialogShow = false
                        dialog = null
                        if (unauthorized) authViewModel.saveUser(
                            null,
                            null,
                            null
                        ) else return@setPositiveButton
                    }
                }
            }.create()
        }



        if (!isAlertDialogShow) {
            isAlertDialogShow = true
            dialog?.show()
        }


    }

    private fun showLoadingMain(boolean: Boolean) {
        showLoading(binding.mainProgressBar, boolean)
        if (boolean) {
            isVisibleAllView(false)
            failedToConnect(false)
        }
    }

    private fun isVisibleAllView(boolean: Boolean) {
        binding.apply {
            if (boolean) {
                toolbar.visibility = View.VISIBLE
                ivProfile.visibility = View.VISIBLE
                layoutBtnPhoto.visibility = View.VISIBLE
                inputLayoutId.visibility = View.VISIBLE
                inputLayoutName.visibility = View.VISIBLE
                btnSave.visibility = View.VISIBLE
                div1.visibility = View.VISIBLE
                tvIdentityPersonal.visibility = View.VISIBLE
                div2.visibility = View.VISIBLE
            } else {
                toolbar.visibility = View.INVISIBLE
                ivProfile.visibility = View.GONE
                layoutBtnPhoto.visibility = View.GONE
                inputLayoutId.visibility = View.GONE
                inputLayoutName.visibility = View.GONE
                btnSave.visibility = View.GONE
                div1.visibility = View.GONE
                tvIdentityPersonal.visibility = View.GONE
                div2.visibility = View.GONE
            }
        }
    }

    private fun failedToConnect(boolean: Boolean) {
        if (boolean) {
            binding.viewHandle.viewFailedConnect.root.visibility = View.VISIBLE
        } else {
            binding.viewHandle.viewFailedConnect.root.visibility = View.GONE
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_SUCCESS_DIALOG_SHOWING, isSuccessDialogShowingOrientation)
        super.onSaveInstanceState(outState)
    }

    private fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
        clearFocus()
    }

    private fun clearFocus() {
        binding.apply {
            inputLayoutId.editText?.clearFocus()
            inputLayoutName.editText?.clearFocus()
        }
    }

    override fun onStop() {
        super.onStop()
        if (dialog != null) {
            isAlertDialogShow = false
            dialog?.dismiss()
            dialog = null
        }
    }

    companion object {
        private const val STATUS_SUCCESS = "status_success"
        private const val STATUS_ERROR = "status_error"
        private const val KEY_SUCCESS_DIALOG_SHOWING = "key_success_dialog_showing"
    }
}