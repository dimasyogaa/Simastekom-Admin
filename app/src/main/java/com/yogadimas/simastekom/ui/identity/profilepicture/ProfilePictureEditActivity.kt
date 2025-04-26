package com.yogadimas.simastekom.ui.identity.profilepicture

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.ImageLoader
import coil.load
import coil.request.ImageRequest
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yogadimas.simastekom.BuildConfig
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.enums.ContentType
import com.yogadimas.simastekom.common.enums.FieldType
import com.yogadimas.simastekom.common.enums.Str
import com.yogadimas.simastekom.common.helper.SnackBarHelper
import com.yogadimas.simastekom.common.helper.convertImageViewToFile
import com.yogadimas.simastekom.common.helper.getParcelableExtra
import com.yogadimas.simastekom.common.helper.goToLogin
import com.yogadimas.simastekom.common.helper.reduceFileImage
import com.yogadimas.simastekom.common.helper.showLoadingFade
import com.yogadimas.simastekom.common.helper.uriToFile
import com.yogadimas.simastekom.common.state.State
import com.yogadimas.simastekom.databinding.ActivityProfilePictureEditBinding
import com.yogadimas.simastekom.model.responses.Errors
import com.yogadimas.simastekom.model.responses.ProfilePictureData
import com.yogadimas.simastekom.viewmodel.admin.AdminAllUserTypeRoleViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import kotlin.coroutines.resume



class ProfilePictureEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfilePictureEditBinding

    private val contextActivity = this@ProfilePictureEditActivity

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }
    private val viewmodel: AdminAllUserTypeRoleViewModel by viewModel()

    private var filePhoto: File? = null
    private var isDeletedPhoto: Boolean = false

    private var isAlertDialogShow = false
    private var dialog: AlertDialog? = null

    private var profilePictureData: ProfilePictureData? = ProfilePictureData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilePictureEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showDefaultView(false)

        auth()
        toolbar()
        mainContent()
        button()
    }

    private fun auth() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            authViewModel.getUser().asFlow().collect { user ->
                if (user.first == AuthPreferences.DEFAULT_VALUE) goToLogin(contextActivity)
            }
        }
    }

    private fun toolbar() = binding.toolbar.apply {
        setNavigationIcon(R.drawable.z_ic_close)
        setNavigationOnClickListener { finish() }
    }

    private fun mainContent() {
        profilePictureData = getParcelableExtra(intent, KEY_ADMIN)
        setupRefresh { getData() }
        initialGetAndObserveData()
    }

    private fun getData() = executeMode { token, userType, userId ->
        viewmodel.getProfilePicture(token, userType, userId)
    }

    private fun initialGetAndObserveData() = executeMode { token, _, _ ->
        getData()
        collectProfilePictureState(token)
    }


    private fun executeMode(action: suspend (String, String, String) -> Unit) =
        lifecycleScope.launch {
            val token = getToken()
            profilePictureData?.apply {
                action(token, userType.orEmpty(), userId.orEmpty())
            }
        }


    private suspend fun collectProfilePictureState(token: String) {
        viewmodel.profilePictureState.collect { state ->
            when (state) {
                is State.Loading -> showLoadingView(true)
                is State.Success -> showDataView(token, state.data)
                is State.ErrorClient -> showErrorClientView(state.error)
                is State.ErrorServer -> showErrorServerView(state.error)
            }
        }
    }

    private fun showDataView(token: String, responseData: ProfilePictureData) =
        lifecycleScope.launch {
            val deferred = async {
                var userId: String? = null
                var userType: String? = null

                // Fungsi untuk memuat gambar dan menunggu hingga selesai
                suspend fun loadImageAndShowDefaultView(pictureUrl: String): Boolean =
                    suspendCancellableCoroutine { continuation ->
                        binding.ivProfile.load(pictureUrl) {
                            crossfade(true)
                            error(R.drawable.z_ic_placeholder_profile)
                            listener(
                                onSuccess = { _, _ ->
                                    defaultViewIsShown()
                                    saveImageAsFile(pictureUrl)
                                    continuation.resume(true)
                                },
                                onError = { _, _ ->
                                    defaultViewIsShown()
                                    continuation.resume(true)
                                }
                            )
                        }
                    }

                binding.apply {
                    profilePictureData?.let { data ->
                        data.apply {
                            userId = responseData.userId
                            userType = responseData.userType
                            profilePicture = responseData.profilePicture

                            // Panggil fungsi untuk memuat gambar dan tunggu hingga selesai
                            val pictureUrl = BuildConfig.BASE_URL + profilePicture
                            loadImageAndShowDefaultView(pictureUrl)
                        }
                    }
                }

                // Mengembalikan nilai userId, userType, dan status tampilan default
                Triple(userId, userType, true)
            }

            // Tunggu hasil dari async, dapatkan userId, userType, dan isDefaultViewShown
            val (userId, userType, isDefaultViewShown) = deferred.await()

            binding.btnSave.setOnClickListener {
                updateSave(token, userType.orEmpty(), userId.orEmpty())
            }


            if (isDefaultViewShown && (responseData.isAdded || responseData.isUpdated || responseData.isDeleted)) {
                val message = getSuccessMessage(responseData)
                alertSuccess(message)
            }
        }


    private fun saveImageAsFile(picture: String) = lifecycleScope.launch(Dispatchers.IO) {
            val loader = ImageLoader(contextActivity)
            val req = ImageRequest.Builder(contextActivity)
                .data(picture)
                .allowHardware(false)  // Untuk mendukung bitmap jika dibutuhkan konversi
                .build()
            val result = (loader.execute(req).drawable as? BitmapDrawable)?.bitmap
            result?.let { bitmap ->
                filePhoto = convertImageViewToFile(contextActivity, bitmap)
            }
        }


    private fun getSuccessMessage(responseData: ProfilePictureData): String {
        val success = getString(R.string.text_success)
        val label = getString(R.string.text_profile_photo)
        val msgRes = when {
            responseData.isAdded -> R.string.text_alert_add_format
            responseData.isUpdated -> R.string.text_alert_update_format
            else -> R.string.text_alert_delete_format
        }
        return getString(msgRes, success, label)
    }


    private fun showErrorClientView(error: Errors) {
        defaultViewIsShown()
        val message = error.errors?.message?.firstOrNull() ?: Str.EMPTY.value
        alertError(message)
    }


    private fun showErrorServerView(error: String) {
        showLoadingView(false)
        showFailedConnectView(true)
        showSnackBar(error)
    }

    private fun button() = binding.apply {
        btnChangePhoto.setOnClickListener { startGallery() }
        btnDeletePhoto.setOnClickListener { deleteImageView() }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, contextActivity)
            filePhoto = myFile
            isDeletedPhoto = false
            binding.ivProfile.setImageURI(selectedImg)
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = ContentType.IMAGE_ALL.value
        val chooser = Intent.createChooser(intent, getString(R.string.select_photo))
        launcherIntentGallery.launch(chooser)
    }

    private fun deleteImageView() {
        isDeletedPhoto = true
        binding.ivProfile.setImageDrawable(
            ContextCompat.getDrawable(
                contextActivity,
                R.drawable.z_ic_placeholder_profile
            )
        )
    }


    private suspend fun getToken(): String = authViewModel.getUser().asFlow().first().first


    private fun updateSave(token: String, userType: String, userId: String) {
        var file: File? = null
        lifecycleScope.launch(Dispatchers.Main) {
            showDefaultView(false)
            showFailedConnectView(false)
            showLoadingView(true)

            withContext(Dispatchers.Default) {
                if (filePhoto != null) {
                    file = reduceFileImage(filePhoto as File)
                }
                withContext(Dispatchers.Main) {

                    val requestImageFile =
                        file?.asRequestBody(ContentType.IMAGE_JPEG.value.toMediaType())

                    val imageMultipart: MultipartBody.Part? = requestImageFile?.let {
                        MultipartBody.Part.createFormData(
                            FieldType.PROFILE_PICTURE.value, file?.name, it
                        )
                    }


                    viewmodel.setManipulationProfilePicture(
                        token,
                        userType,
                        userId,
                        profilePicture = imageMultipart,
                        isDeletedPhoto
                    )

                }
            }
        }
    }


    private fun alertSuccess(message: String) {
        showAlertDialog(msg = message, status = STATUS_SUCCESS)
    }

    private fun alertError(message: String) {
        showAlertDialog(msg = message, status = STATUS_ERROR)
    }

    private fun showAlertDialog(msg: String = "", status: String) {

        val unauthorized = msg == getString(R.string.text_const_unauthorized)
        var title = ""
        var message = ""
        var icon: Drawable? = null
        when (status) {
            STATUS_SUCCESS -> {
                icon = ContextCompat.getDrawable(contextActivity, R.drawable.z_ic_check)
                val wrappedDrawable = DrawableCompat.wrap(icon!!).mutate()
                val color = ContextCompat.getColor(contextActivity, R.color.colorFixedGreen)
                DrawableCompat.setTint(wrappedDrawable, color)
                title = getString(R.string.text_success)
                message = msg
            }

            STATUS_ERROR -> {
                if (unauthorized) {
                    icon = ContextCompat.getDrawable(contextActivity, R.drawable.z_ic_warning)
                    title = getString(R.string.text_login_again)
                    message = getString(R.string.text_please_login_again)
                } else {
                    icon = ContextCompat.getDrawable(contextActivity, R.drawable.z_ic_warning)
                    title = getString(R.string.text_error_format, "")
                    message = msg
                }

            }
        }

        if (dialog == null) {
            if (status == STATUS_SUCCESS) {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1500)
                    isAlertDialogShow = false
                    dialog?.dismiss()
                    dialog = null
                }
            }
            dialog = MaterialAlertDialogBuilder(contextActivity).apply {
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

    private fun showLoadingView(isVisible: Boolean) {
        showLoadingFade(binding.mainProgressBar, isVisible)
        if (isVisible) {
            showDefaultView(false)
            showFailedConnectView(false)
        }
    }

    private fun showDefaultView(isVisible: Boolean) {
        binding.apply {
            if (isVisible) {
                toolbar.visibility = View.VISIBLE
                ivProfile.visibility = View.VISIBLE
                layoutBtnPhoto.visibility = View.VISIBLE
                btnSave.visibility = View.VISIBLE
            } else {
                toolbar.visibility = View.INVISIBLE
                ivProfile.visibility = View.GONE
                layoutBtnPhoto.visibility = View.GONE
                btnSave.visibility = View.GONE
            }
        }
    }

    private fun showFailedConnectView(isVisible: Boolean) {
        binding.viewHandle.viewFailedConnect.root.isVisible = isVisible
    }

    private fun showSnackBar(message: String) {
        SnackBarHelper.display(binding.root as ViewGroup, message, contextActivity)
    }

    private fun setupRefresh(block: suspend () -> Unit) =
        binding.viewHandle.viewFailedConnect.btnRefresh.setOnClickListener {
            lifecycleScope.launch { block() }
        }

    private fun defaultViewIsShown() {
        showLoadingView(false)
        showDefaultView(true)
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

        const val KEY_ADMIN = "key_admin"
    }
}