package com.yogadimas.simastekom.ui.identity.personal.address

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.yogadimas.simastekom.MainActivity
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.databinding.ActivityAddressHomeBinding
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.helper.getParcelableExtra
import com.yogadimas.simastekom.common.helper.hideKeyboard
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.model.Address
import com.yogadimas.simastekom.model.responses.IdentityPersonalData
import com.yogadimas.simastekom.ui.identity.personal.IdentityPersonalEditActivity.Companion.KEY_ADMIN_STUDENT
import com.yogadimas.simastekom.ui.login.LoginActivity
import com.yogadimas.simastekom.ui.profile.ProfileFragment
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddressHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddressHomeBinding

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private val adminViewModel: AdminViewModel by viewModel()

    private var isLoading = false
    private var isAlertDialogShow = false

    private var isSuccessDialogShowingOrientation = false

    private var dialog: AlertDialog? = null


    private var addressData: Address? = Address()

    private var identityPersonalData: IdentityPersonalData? = IdentityPersonalData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddressHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        identityPersonalData = getParcelableExtra(intent, KEY_ADMIN_STUDENT)

        if (savedInstanceState != null) {

            isSuccessDialogShowingOrientation =
                savedInstanceState.getBoolean(KEY_SUCCESS_DIALOG_SHOWING)
            if (isSuccessDialogShowingOrientation) {
                showAlertDialog(status = STATUS_SUCCESS)
            }

            addressData = if (Build.VERSION.SDK_INT >= 33) {
                savedInstanceState.getParcelable(KEY_ADDRESS, Address::class.java)
            } else {
                @Suppress("DEPRECATION")
                savedInstanceState.getParcelable(KEY_ADDRESS)
            }


        }

        binding.apply {

            toolbar.setNavigationOnClickListener {
                finish()
            }

            toolbar.menu.findItem(R.id.profileMenu).setOnMenuItemClickListener {
                hideKeyboard()
                if (addressData?.province.orEmpty() != edtProvince.text.toString() ||
                    addressData?.cityRegency.orEmpty() != edtCityRegency.text.toString() ||
                    addressData?.district.orEmpty() != edtDistrict.text.toString() ||
                    addressData?.village.orEmpty() != edtVillage.text.toString() ||
                    addressData?.rw.orEmpty() != edtRw.text.toString() ||
                    addressData?.rt.orEmpty() != edtRt.text.toString() ||
                    addressData?.street.orEmpty() != edtStreet.text.toString() ||
                    addressData?.otherDetailAddress.orEmpty() != edtAddressOtherDetail.text.toString()
                ) {
                    showAlertDialog(status = STATUS_PROFILE_FRAGMENT)
                } else {
                    val intent =
                        Intent(
                            this@AddressHomeActivity,
                            MainActivity::class.java
                        ).apply {
                            flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra(
                                MainActivity.KEY_PAGE,
                                ProfileFragment.NAME_FRAGMENT
                            )
                        }
                    startActivity(intent)
                }

                true
            }

            val layouts: Array<TextInputLayout> by lazy {
                arrayOf(
                    inputLayoutProvince,
                    inputLayoutCityRegency,
                    inputLayoutDistrict,
                    inputLayoutVillage,
                    inputLayoutRw,
                    inputLayoutRt,
                    inputLayoutStreet,
                    inputLayoutAddressOtherDetail
                )
            }

            val editTexts: Array<TextInputEditText> by lazy {
                arrayOf(
                    edtProvince, edtCityRegency, edtDistrict, edtVillage, edtRw,
                    edtRt, edtStreet, edtAddressOtherDetail
                )
            }

            val textWatchers: Array<TextWatcher> by lazy {
                Array(editTexts.size) { i ->
                    object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int,
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int,
                        ) {
                            val placeValue = s.toString()
                            val hasLeadingSpace = placeValue.startsWith(" ")
                            val isEmpty = placeValue.isEmpty()

                            if (isEmpty) {
                                layouts[i].isErrorEnabled = false
                            } else {
                                layouts[i].isErrorEnabled = hasLeadingSpace
                            }
                            layouts[i].error = when {
                                hasLeadingSpace -> getString(
                                    R.string.text_cannot_contain_spaces_early,
                                    getString(R.string.text_label_place_birth)
                                )

                                isEmpty -> null
                                else -> null
                            }
                            buttonIsEnabled(layouts, editTexts)
                        }

                        override fun afterTextChanged(s: Editable?) {}
                    }
                }
            }

            editTexts.forEachIndexed { index, editText ->
                editText.addTextChangedListener(textWatchers[index])
            }


            buttonIsEnabled(layouts, editTexts)



            viewHandle.viewFailedConnect.btnRefresh.setOnClickListener {
                getUser(
                    layouts,
                    editTexts
                )
            }

            getUser(layouts, editTexts)

        }


        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (hasChanges()) {
                    showAlertDialog(status = STATUS_HAS_CHANGED)
                } else {
                    finish()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)

    }

    private fun getUser(layouts: Array<TextInputLayout>, editTexts: Array<TextInputEditText>) {
        authViewModel.getUser().observe(this) {
            var (token, userId, userType) = it
            if (token == AuthPreferences.DEFAULT_VALUE) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                adminViewModel.token = token
                if (identityPersonalData?.isFromAdminStudent == true) {
                    identityPersonalData?.let { data ->
                        userType = data.userType.orEmpty()
                        userId = data.userId.orEmpty()
                        adminViewModel.getIdentityPersonal(userType, userId)
                    }
                } else {
                    adminViewModel.getIdentityPersonal(userType, userId)
                }
                binding.btnSave.setOnClickListener { updateSave(userType, userId) }
            }
        }

        adminViewModel.isLoading.observe(this) {
            isLoading = it
            showLoadingMain(it)
        }

        adminViewModel.identityPersonal.observe(this) { eventData ->
            eventData.getContentIfNotHandled()?.let {
                if (isLoading) {
                    isVisibleAllView(false)
                } else {
                    isVisibleAllView(true)
                }
                failedToConnect(false)


                binding.apply {
                    addressData = Address.parse(it.address).apply {
                        setEditText(province, edtProvince)
                        setEditText(cityRegency, edtCityRegency)
                        setEditText(district, edtDistrict)
                        setEditText(village, edtVillage)
                        setEditText(rw, edtRw)
                        setEditText(rt, edtRt)
                        setEditText(street, edtStreet)
                        setEditText(otherDetailAddress, edtAddressOtherDetail)
                    }






                    buttonIsEnabled(layouts, editTexts)

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

    private fun updateSave(userType: String, userId: String) {
        hideKeyboard()


        binding.apply {

            adminViewModel.updateIdentityPersonal(
                userType,
                userId,
                IdentityPersonalData(
                    address = Address(
                        province = edtProvince.text.toString(),
                        cityRegency = edtCityRegency.text.toString(),
                        district = edtDistrict.text.toString(),
                        village = edtVillage.text.toString(),
                        rw = edtRw.text.toString(),
                        rt = edtRt.text.toString(),
                        street = edtStreet.text.toString(),
                        otherDetailAddress = edtAddressOtherDetail.text.toString(),
                    ).toDatabase()
                )
            )
        }


    }

    private fun showAlertDialog(msg: String = "", status: String) {

        val unauthorized = msg == getString(R.string.text_const_unauthorized)
        var title = ""
        var message = ""
        var icon: Drawable? = null
        when (status) {
            STATUS_HAS_CHANGED -> {
                icon = ContextCompat.getDrawable(this, R.drawable.z_ic_warning)
                title = getString(R.string.text_changes_not_saved)
                message = getString(R.string.text_exit_not_saved)
            }

            STATUS_PROFILE_FRAGMENT -> {
                icon = ContextCompat.getDrawable(this, R.drawable.z_ic_person)
                title = getString(R.string.title_profile)
                message = getString(R.string.text_question_do_you_want_to_back_profile_page)
            }

            STATUS_SUCCESS -> {
                icon = ContextCompat.getDrawable(this, R.drawable.z_ic_check)
                val wrappedDrawable = DrawableCompat.wrap(icon!!).mutate()
                val color = ContextCompat.getColor(this, R.color.colorFixedGreen)
                DrawableCompat.setTint(wrappedDrawable, color)
                title = getString(R.string.text_success)
                message = getString(
                    R.string.text_alert_change,
                    title,
                    getString(R.string.text_place_date_birth)
                )
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
                if (status != STATUS_SUCCESS) {
                    setPositiveButton(getString(R.string.text_ok)) { _, _ ->
                        isAlertDialogShow = false
                        dialog = null
                        when (status) {
                            STATUS_HAS_CHANGED -> {
                                finish()
                            }

                            STATUS_PROFILE_FRAGMENT -> {
                                val intent =
                                    Intent(
                                        this@AddressHomeActivity,
                                        MainActivity::class.java
                                    ).apply {
                                        flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        putExtra(
                                            MainActivity.KEY_PAGE,
                                            ProfileFragment.NAME_FRAGMENT
                                        )
                                    }
                                startActivity(intent)
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
                    if (status == STATUS_HAS_CHANGED || status == STATUS_PROFILE_FRAGMENT) {
                        setNegativeButton(getString(R.string.text_cancel)) { _, _ ->
                            isAlertDialogShow = false
                            dialog = null
                            return@setNegativeButton
                        }
                    }
                }

            }.create()
        }



        if (!isAlertDialogShow) {
            isAlertDialogShow = true
            dialog?.show()
        }


    }

    private fun setEditText(value: String, editText: TextInputEditText) {
        if (value.contains("-") || value.isEmpty()) {
            editText.text = null
        } else {
            editText.setText(value)
        }
    }

    private fun setIsEmpty(value: String): String {
        return if (value.contains("-") || value.isEmpty()) {
            ""
        } else {
            value
        }
    }

    private fun hasChanges(): Boolean {
        binding.apply {
            return setIsEmpty(addressData?.province.orEmpty()) != edtProvince.text.toString() ||
                    setIsEmpty(addressData?.cityRegency.orEmpty()) != edtCityRegency.text.toString() ||
                    setIsEmpty(addressData?.district.orEmpty()) != edtDistrict.text.toString() ||
                    setIsEmpty(addressData?.village.orEmpty()) != edtVillage.text.toString() ||
                    setIsEmpty(addressData?.rw.orEmpty()) != edtRw.text.toString() ||
                    setIsEmpty(addressData?.rt.orEmpty()) != edtRt.text.toString() ||
                    setIsEmpty(addressData?.street.orEmpty()) != edtStreet.text.toString() ||
                    setIsEmpty(addressData?.otherDetailAddress.orEmpty()) != edtAddressOtherDetail.text.toString()
        }

    }

    private fun buttonIsEnabled(
        layouts: Array<TextInputLayout>,
        editTexts: Array<TextInputEditText>,
    ) {

        binding.btnSave.isEnabled =
            editTexts.any { it.text.toString().isNotEmpty() } && layouts.all { !it.isErrorEnabled }

    }

    private fun Activity.hideKeyboard() {
        fun clearFocus() {
            binding.apply {
                inputLayoutProvince.editText?.clearFocus()
                inputLayoutCityRegency.editText?.clearFocus()
                inputLayoutDistrict.editText?.clearFocus()
                inputLayoutVillage.editText?.clearFocus()
                inputLayoutRw.editText?.clearFocus()
                inputLayoutRt.editText?.clearFocus()
                inputLayoutStreet.editText?.clearFocus()
                inputLayoutAddressOtherDetail.editText?.clearFocus()
            }
        }
        hideKeyboard(currentFocus ?: View(this))
        clearFocus()
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
                btnSave.visibility = View.VISIBLE

            } else {
                toolbar.visibility = View.INVISIBLE
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
                btnSave.visibility = View.GONE

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

    override fun onStop() {
        super.onStop()
        if (dialog != null) {
            isAlertDialogShow = false
            dialog?.dismiss()
            dialog = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // outState.putString(KEY_PLACE, placeValue)
        // outState.putLong(KEY_DATE, dateValue?.timeInMillis ?: 0L)
        outState.putBoolean(KEY_SUCCESS_DIALOG_SHOWING, isSuccessDialogShowingOrientation)
        outState.putParcelable(KEY_ADDRESS, addressData)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val KEY_ADDRESS = "key_address"

        private const val KEY_SUCCESS_DIALOG_SHOWING = "key_success_dialog_showing"

        private const val STATUS_HAS_CHANGED = "status_has_changed"
        private const val STATUS_PROFILE_FRAGMENT = "status_profile_fragment"
        private const val STATUS_SUCCESS = "status_success"
        private const val STATUS_ERROR = "status_error"
    }
}