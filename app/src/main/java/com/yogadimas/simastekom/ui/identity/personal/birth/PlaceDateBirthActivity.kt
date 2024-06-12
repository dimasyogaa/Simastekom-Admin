package com.yogadimas.simastekom.ui.identity.personal.birth

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.yogadimas.simastekom.MainActivity
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.databinding.ActivityPlaceDateBirthBinding
import com.yogadimas.simastekom.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.helper.capitalizeWords
import com.yogadimas.simastekom.helper.hideKeyboard
import com.yogadimas.simastekom.helper.showLoading
import com.yogadimas.simastekom.helper.simpleDateFormatHelper
import com.yogadimas.simastekom.model.PlaceBirth
import com.yogadimas.simastekom.model.responses.IdentityPersonalData
import com.yogadimas.simastekom.ui.login.LoginActivity
import com.yogadimas.simastekom.ui.profile.ProfileFragment
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class PlaceDateBirthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaceDateBirthBinding

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private val adminViewModel: AdminViewModel by viewModels()


    private var isLoading = false
    private var isAlertDialogShow = false

    private var isSuccessDialogShowingOrientation = false

    private var placeValue = ""
    private var dateValue: Calendar? = null

    private var dialog: AlertDialog? = null

    private lateinit var datePicker: MaterialDatePicker<Long>

    private lateinit var constraintsBuilder: CalendarConstraints.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceDateBirthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())

        dateValue = Calendar.getInstance(Locale.getDefault())


        binding.apply {

            if (savedInstanceState != null) {

                isSuccessDialogShowingOrientation =
                    savedInstanceState.getBoolean(KEY_SUCCESS_DIALOG_SHOWING)
                if (isSuccessDialogShowingOrientation) {
                    showAlertDialog(status = STATUS_SUCCESS)
                }

                placeValue = savedInstanceState.getString(KEY_PLACE).orEmpty()
                dateValue?.timeInMillis = savedInstanceState.getLong(KEY_DATE, 0L)
                edtPlaceBirth.setText(placeValue)

                if (dateValue?.timeInMillis != 0L) {
                    edtDateBirth.setText(dateValue?.time?.let { simpleDateFormatHelper().format(it) })
                } else {
                    edtDateBirth.text = null
                }
            }


            toolbar.setNavigationOnClickListener {
                finish()
            }
            toolbar.menu.findItem(R.id.profileMenu).setOnMenuItemClickListener {
                val intent = Intent(this@PlaceDateBirthActivity, MainActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.putExtra(MainActivity.KEY_PAGE, ProfileFragment.NAME_FRAGMENT)
                startActivity(intent)
                true
            }


            buttonIsEnabled()

            viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { getUser() }

        }


        getUser()


    }

    private fun getUser() {


        authViewModel.getUser().observe(this) {
            val (token, userId, userType) = it
            if (token == AuthPreferences.DEFAULT_VALUE) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                adminViewModel.token = token
                adminViewModel.getIdentityPersonal(userType, userId)
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


                    if (it.placeDateBirth != null) {

                        val placeDateBirth  = PlaceBirth.parse(it.placeDateBirth)

                        placeValue = placeDateBirth?.place.orEmpty()
                        edtPlaceBirth.setText(placeValue)

                        dateValue?.timeInMillis =
                            placeDateBirth?.birthDate?.timeInMillis ?: 0L

                        if (dateValue?.timeInMillis != 0L) {
                            edtDateBirth.setText(dateValue?.time?.let { date ->
                                simpleDateFormatHelper().format(date)
                            })
                        } else {
                            edtDateBirth.text = null
                        }

                        buttonIsEnabled()
                    } else {
                        dateValue?.timeInMillis = 0L
                    }

                    edtPlaceBirth.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            val placeValue = s.toString()
                            val hasLeadingSpace = placeValue.startsWith(" ")
                            val isEmpty = placeValue.isEmpty()


                            if (isEmpty) {
                                inputLayoutPlaceBirth.isErrorEnabled = false
                            } else {
                                inputLayoutPlaceBirth.isErrorEnabled = hasLeadingSpace
                            }
                            inputLayoutPlaceBirth.error = when {
                                hasLeadingSpace -> getString(R.string.text_cannot_contain_spaces_early, getString(R.string.text_label_place_birth))
                                isEmpty -> null
                                else -> null
                            }

                            buttonIsEnabled()
                        }

                        override fun afterTextChanged(s: Editable?) {}
                    })


                    calendar()


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

    private fun calendar() {
        binding.apply {
            datePicker = MaterialDatePicker.Builder.datePicker().apply {
                setTitleText(getString(R.string.text_label_date_birth))
                setCalendarConstraints(constraintsBuilder.build())
                if (dateValue?.timeInMillis != 0L) {
                    setSelection(dateValue?.timeInMillis)
                } else {
                    setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                }
            }.build()

            edtDateBirth.setOnClickListener {
                if (!datePicker.isAdded) {
                    hideKeyboard()
                    datePicker.show(
                        supportFragmentManager,
                        getString(R.string.text_label_date_birth)
                    )
                }

            }

            datePicker.addOnPositiveButtonClickListener { selection ->

                dateValue?.timeInMillis = selection

                edtDateBirth.setText(dateValue?.time?.let { date ->
                    simpleDateFormatHelper().format(
                        date
                    )
                })

                buttonIsEnabled()
            }

        }
    }

    private fun updateSave(userType: String, userId: String) {
        hideKeyboard()

        adminViewModel.updateIdentityPersonal(
            userType,
            userId,
            IdentityPersonalData(
                placeDateBirth = PlaceBirth(
                    placeValue.capitalizeWords().trim(),
                    dateValue
                ).toString()
            )
        )

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

    private fun Activity.hideKeyboard() {
        fun clearFocus() {
            binding.apply {
                inputLayoutPlaceBirth.editText?.clearFocus()
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
                inputLayoutPlaceBirth.visibility = View.VISIBLE
                edtPlaceBirth.visibility = View.VISIBLE
                inputLayoutDateBirth.visibility = View.VISIBLE
                edtDateBirth.visibility = View.VISIBLE
                btnSave.visibility = View.VISIBLE

            } else {
                toolbar.visibility = View.INVISIBLE
                inputLayoutPlaceBirth.visibility = View.GONE
                edtPlaceBirth.visibility = View.GONE
                inputLayoutDateBirth.visibility = View.GONE
                edtDateBirth.visibility = View.GONE
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

    private fun buttonIsEnabled() {
        binding.apply {
            btnSave.isEnabled = edtPlaceBirth.text.toString().isNotEmpty() && edtDateBirth.text.toString().isNotEmpty() && !inputLayoutPlaceBirth.isErrorEnabled
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
        outState.putString(KEY_PLACE, placeValue)
        outState.putLong(KEY_DATE, dateValue?.timeInMillis ?: 0L)
        outState.putBoolean(KEY_SUCCESS_DIALOG_SHOWING, isSuccessDialogShowingOrientation)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val KEY_PLACE = "key_place"
        private const val KEY_DATE = "key_date"

        private const val KEY_SUCCESS_DIALOG_SHOWING = "key_success_dialog_showing"

        private const val STATUS_SUCCESS = "status_success"
        private const val STATUS_ERROR = "status_error"
    }
}