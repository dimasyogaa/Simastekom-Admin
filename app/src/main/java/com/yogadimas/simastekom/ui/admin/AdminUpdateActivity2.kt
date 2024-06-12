package com.yogadimas.simastekom.ui.admin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import coil.ImageLoader
import coil.load
import coil.request.ImageRequest
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.yogadimas.simastekom.BuildConfig
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.databinding.ActivityAdminUpdate2Binding
import com.yogadimas.simastekom.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.datastore.preferences.SettingPreferences
import com.yogadimas.simastekom.helper.hideKeyboard
import com.yogadimas.simastekom.helper.onTextChange
import com.yogadimas.simastekom.helper.showLoading
import com.yogadimas.simastekom.helper.uriToFile
import com.yogadimas.simastekom.interfaces.OnOptionDialogListenerInterface
import com.yogadimas.simastekom.model.responses.AdminData
import com.yogadimas.simastekom.ui.dialog.GenderDialogFragment
import com.yogadimas.simastekom.ui.login.LoginActivity
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import com.yogadimas.simastekom.viewmodel.factory.SettingViewModelFactory
import com.yogadimas.simastekom.viewmodel.setting.SettingViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.UUID

class AdminUpdateActivity2 : AppCompatActivity(), OnOptionDialogListenerInterface {


    private lateinit var binding: ActivityAdminUpdate2Binding

    private val adminViewModel: AdminViewModel by viewModels()

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private val settingViewModel: SettingViewModel by viewModels {
        SettingViewModelFactory.getInstance(SettingPreferences.getInstance(dataStore))
    }

    private var isLoading = false
    private var dialogHasBeenShow = false


    private var photoFile: File? = null

    private var deletePhoto: Boolean = false

    private var isSuccessDialogShowingOrientation = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUpdate2Binding.inflate(layoutInflater)
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
                        this@AdminUpdateActivity2,
                        R.drawable.z_ic_placeholder_profile
                    )
                )
            }

            fabSave.setOnClickListener {
                updateSave()
            }

            identityPersonal(this@apply)


            viewHandle.viewFailedConnect.btnRefresh.setOnClickListener {
                getAdmin()
            }

            onTextChange(edtId, inputLayoutId)
            onTextChange(edtName, inputLayoutName)


        }


    }


    private fun getAdmin() {
        authViewModel.getUser().observe(this) {
            val (token, userId, userType) = it
            if (token == AuthPreferences.DEFAULT_VALUE) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            } else {
                adminViewModel.token = token
                adminViewModel.getIdentityPersonal(userType, userId)
            }
        }

        getAdminData()


    }


    @SuppressLint("SetTextI18n")
    private fun getAdminData() {
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

                if (it.isUpdated) {
                    isVisibleAllView(false)
                    isSuccessDialogShowingOrientation = true
                    showAlertDialog(status = STATUS_SUCCESS)
                } else {
                    displayView(it)
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

                        errors.password != null -> {
                            listMessage = errors.password
                        }

                        errors.name != null -> {
                            listMessage = errors.name
                        }

                        errors.gender != null -> {
                            listMessage = errors.gender
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

    private fun displayView(it: AdminData?) {
        if (it != null) {
            binding.edtId.setText(it.username)
            binding.edtName.setText(it.name)

            val picture = BuildConfig.BASE_URL + it.profilePicture

            binding.ivProfile.load(picture) {
                crossfade(true)
                placeholder(R.drawable.z_ic_placeholder_profile)
                error(R.drawable.z_ic_placeholder_profile)
            }
            val loader = ImageLoader(this@AdminUpdateActivity2)
            val req = ImageRequest.Builder(this@AdminUpdateActivity2)
                .data(picture)
                .target { result ->
                    val bitmap = (result as BitmapDrawable).bitmap
                    photoFile = convertImageViewToFile(bitmap)
                }
                .build()

            val disposable = loader.enqueue(req)

            setDrawableGender(it.gender)
            binding.edtGender.setText(it.gender)
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

    private fun identityPersonal(binding: ActivityAdminUpdate2Binding) {
        binding.apply {

            settingViewModel.getSetting().observe(this@AdminUpdateActivity2) {
                setVisibilityIdentityPersonal(it.first)
                setVisibilityPlaceDateBirth(it.second)
                setVisibilityAddressHome(it.third)
            }

            dropdownLayout(layoutIdentityPersonal, tbIdentityPersonal)
            tbIdentityPersonal.setOnCheckedChangeListener { _, isChecked ->
                hideKeyboard()
                setVisibilityIdentityPersonal(isChecked)
            }

            edtIdCardNumber.transformationMethod = null

            edtGender.setOnClickListener {
                hideKeyboard()
                val optionDialogFragment = GenderDialogFragment()

                val fragmentManager = supportFragmentManager

                optionDialogFragment.show(
                    fragmentManager,
                    GenderDialogFragment::class.java.simpleName
                )
            }

            dropdownLayout(layoutPlaceDateBirth, tbPlaceDateBirth)
            tbPlaceDateBirth.setOnCheckedChangeListener { _, isChecked ->
                hideKeyboard()
                setVisibilityPlaceDateBirth(isChecked)


            }
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText(getString(R.string.text_label_date_birth))
                    .build()
            edtDateBirth.setOnClickListener {
                hideKeyboard()
                datePicker.show(supportFragmentManager, getString(R.string.text_label_date_birth))
            }

            dropdownLayout(layoutAddressHome, tbAddressHome)
            tbAddressHome.setOnCheckedChangeListener { _, isChecked ->
                hideKeyboard()
                setVisibilityAddressHome(isChecked)
            }
        }
    }

    private fun dropdownLayout(linearLayout: LinearLayout, toggleButton: ToggleButton) {
        linearLayout.setOnClickListener {
            toggleButton.isChecked = !toggleButton.isChecked
        }
    }

    private fun setVisibilityIdentityPersonal(isChecked: Boolean) {
        binding.apply {
            if (isChecked) {
                formIdentityPersonal.visibility = View.VISIBLE
                tbIdentityPersonal.isChecked = true
            } else {
                formIdentityPersonal.visibility = View.GONE
                tbIdentityPersonal.isChecked = false
            }
        }
    }

    private fun setVisibilityPlaceDateBirth(isChecked: Boolean) {
        binding.apply {
            if (isChecked) {
                inputLayoutPlaceBirth.visibility = View.VISIBLE
                edtPlaceBirth.visibility = View.VISIBLE
                inputLayoutDateBirth.visibility = View.VISIBLE
                edtDateBirth.visibility = View.VISIBLE
                tbPlaceDateBirth.isChecked = true
            } else {
                inputLayoutPlaceBirth.visibility = View.GONE
                edtPlaceBirth.visibility = View.GONE
                inputLayoutDateBirth.visibility = View.GONE
                edtDateBirth.visibility = View.GONE
                tbPlaceDateBirth.isChecked = false
            }
        }
    }

    private fun setVisibilityAddressHome(isChecked: Boolean) {
        binding.apply {
            if (isChecked) {
                inputLayoutProvince.visibility = View.VISIBLE
                edtProvince.visibility = View.VISIBLE
                inputLayoutCityRegency.visibility = View.VISIBLE
                edtCityRegency.visibility = View.VISIBLE
                inputLayoutDistrict.visibility = View.VISIBLE
                edtDistrict.visibility = View.VISIBLE
                inputLayoutVillage.visibility = View.VISIBLE
                edtVillage.visibility = View.VISIBLE
                inputLayoutRw.visibility = View.VISIBLE
                edtRw.visibility = View.VISIBLE
                inputLayoutRt.visibility = View.VISIBLE
                edtRt.visibility = View.VISIBLE
                inputLayoutStreet.visibility = View.VISIBLE
                edtStreet.visibility = View.VISIBLE
                inputLayoutAddressOtherDetail.visibility = View.VISIBLE
                edtAddressOtherDetail.visibility = View.VISIBLE
                tbAddressHome.isChecked = true
            } else {
                inputLayoutProvince.visibility = View.GONE
                edtProvince.visibility = View.GONE
                inputLayoutCityRegency.visibility = View.GONE
                edtCityRegency.visibility = View.GONE
                inputLayoutDistrict.visibility = View.GONE
                edtDistrict.visibility = View.GONE
                inputLayoutVillage.visibility = View.GONE
                edtVillage.visibility = View.GONE
                inputLayoutRw.visibility = View.GONE
                edtRw.visibility = View.GONE
                inputLayoutRt.visibility = View.GONE
                edtRt.visibility = View.GONE
                inputLayoutStreet.visibility = View.GONE
                edtStreet.visibility = View.GONE
                inputLayoutAddressOtherDetail.visibility = View.GONE
                edtAddressOtherDetail.visibility = View.GONE
                tbAddressHome.isChecked = false
            }
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

    override fun onOptionChosen(text: String, category: String) {
        setDrawableGender(text)
        binding.edtGender.setText(text)
    }

    private fun setDrawableGender(gender: String?) {
        binding.apply {
            if (gender != null) {
                when (gender.lowercase()) {
                    getString(R.string.text_man).lowercase() -> inputLayoutGender.startIconDrawable =
                        ContextCompat.getDrawable(this@AdminUpdateActivity2, R.drawable.z_ic_man)

                    getString(R.string.text_woman).lowercase() -> inputLayoutGender.startIconDrawable =
                        ContextCompat.getDrawable(this@AdminUpdateActivity2, R.drawable.z_ic_woman)
                }
            }

        }


    }

    private fun updateSave() {
        hideKeyboard()
        if (checkValidated(
                binding.edtId.text.toString().trim(),
                binding.edtName.text.toString().trim()
            )
        ) {
            // var file: File? = null
            // lifecycleScope.launch(Dispatchers.Main) {
            //     isVisibleAllView(false)
            //     failedToConnect(false)
            //     showLoadingMain(true)
            //
            //     withContext(Dispatchers.Default) {
            //         if (photoFile != null) {
            //             file = reduceFileImage(photoFile as File)
            //         }
            //         withContext(Dispatchers.Main) {
            //             val username =
            //                 binding.edtId.text.toString()
            //                     .toRequestBody("text/plain".toMediaType())
            //
            //             val name = binding.edtName.text.toString()
            //                 .toRequestBody("text/plain".toMediaType())
            //
            //
            //             val requestImageFile = file?.asRequestBody("image/jpeg".toMediaType())
            //
            //             val imageMultipart: MultipartBody.Part? = requestImageFile?.let {
            //                 MultipartBody.Part.createFormData(
            //                     "foto_profil", file?.name, it
            //                 )
            //             }
            //
            //
            //             val deletePhoto = deletePhoto.toString()
            //                 .toRequestBody("text/plain".toMediaType())
            //
            //
            //             val gender = binding.edtGender.text.toString().lowercase()
            //                 .toRequestBody("text/plain".toMediaType())
            //
            //             adminViewModel.updateAdminCurrent(
            //                 imageMultipart,
            //                 username,
            //                 name,
            //                 deletePhoto,
            //                 gender
            //             )
            //
            //         }
            //     }
            // }
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
                message = getString(R.string.text_alert_update_data, title)
            }

            STATUS_ERROR -> {
                if (unauthorized) {
                    icon = ContextCompat.getDrawable(this, R.drawable.z_ic_warning)
                    title = getString(R.string.title_dialog_login_again)
                    message = getString(R.string.text_please_login_again)
                } else {
                    icon = ContextCompat.getDrawable(this, R.drawable.z_ic_warning)
                    title = getString(R.string.text_error, "")
                    message = msg
                }

            }
        }


        if (!dialogHasBeenShow) {
            dialogHasBeenShow = true
            MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setIcon(icon)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.text_ok)) { _, _ ->
                    dialogHasBeenShow = false
                    when (status) {
                        STATUS_SUCCESS -> {
                            isSuccessDialogShowingOrientation = false
                            finish()
                            return@setPositiveButton
                        }

                        STATUS_ERROR -> {
                            if (unauthorized) authViewModel.saveUser(
                                null,
                                null,
                                null
                            ) else return@setPositiveButton
                        }
                    }
                }
                .show()
        }


    }

    private fun showLoadingMain(boolean: Boolean) {
        showLoading(binding.mainProgressBar, boolean)
        if (boolean) {
            isVisibleAllView(false)
            failedToConnect(false)
        }
    }

    private fun failedToConnect(boolean: Boolean) {
        if (boolean) {
            binding.viewHandle.viewFailedConnect.root.visibility = View.VISIBLE
        } else {
            binding.viewHandle.viewFailedConnect.root.visibility = View.GONE
        }

    }

    private fun isVisibleAllView(boolean: Boolean) {
        if (boolean) {
            binding.toolbar.visibility = View.VISIBLE
            binding.fabSave.visibility = View.VISIBLE
            binding.ivProfile.visibility = View.VISIBLE
            binding.layoutBtnPhoto.visibility = View.VISIBLE
            binding.inputLayoutId.visibility = View.VISIBLE
            binding.inputLayoutName.visibility = View.VISIBLE
            binding.div1.visibility = View.VISIBLE
            binding.layoutIdentityPersonal.visibility = View.VISIBLE
            binding.div2.visibility = View.VISIBLE
        } else {
            binding.toolbar.visibility = View.GONE
            binding.fabSave.visibility = View.GONE
            binding.ivProfile.visibility = View.GONE
            binding.layoutBtnPhoto.visibility = View.GONE
            binding.inputLayoutId.visibility = View.GONE
            binding.inputLayoutName.visibility = View.GONE
            binding.div1.visibility = View.GONE
            binding.layoutIdentityPersonal.visibility = View.GONE
            binding.div2.visibility = View.GONE
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
            inputLayoutIdCardNumber.editText?.clearFocus()
            inputLayoutDateBirth.editText?.clearFocus()
            inputLayoutProvince.editText?.clearFocus()
            inputLayoutCityRegency.editText?.clearFocus()
        }
    }

    override fun onPause() {
        super.onPause()
        binding.apply {
            settingViewModel.saveSetting(
                tbIdentityPersonal.isChecked,
                tbPlaceDateBirth.isChecked,
                tbAddressHome.isChecked
            )
        }
    }

    private fun checkValidated(id: String, name: String): Boolean {

        fun stringFormat(string: String): String {
            return String.format(getString(R.string.mandatory_field), string)
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


    companion object {
        private const val STATUS_SUCCESS = "status_success"
        private const val STATUS_ERROR = "status_error"
        private const val KEY_SUCCESS_DIALOG_SHOWING = "key_success_dialog_showing"
    }


}