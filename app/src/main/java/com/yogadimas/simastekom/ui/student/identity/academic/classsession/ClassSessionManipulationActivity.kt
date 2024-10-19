package com.yogadimas.simastekom.ui.student.identity.academic.classsession

import android.app.Activity
import android.content.Intent
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.databinding.ActivityClassSessionManipulationBinding
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.helper.hideKeyboard
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.model.responses.NameData
import com.yogadimas.simastekom.ui.login.LoginActivity
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import org.koin.androidx.viewmodel.ext.android.viewModel

class ClassSessionManipulationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClassSessionManipulationBinding

    private val adminViewModel: AdminViewModel by viewModel()

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private var isLoading = false
    private var isAlertDialogShow = false

    private var dialog: AlertDialog? = null

    private var isEditDeleteView = false
    private var id: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassSessionManipulationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainCall()
    }

    private fun mainCall() {
        id = intent.getIntExtra(KEY_EXTRA_ID, 0)

        isEditDeleteView = id != 0

        if (isEditDeleteView) {
            editDeleteMode()
        } else {
            addMode()
        }

        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }
            edtName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    buttonIsEnabled()
                }

                override fun afterTextChanged(p0: Editable?) {}

            })

            btnSave.setOnClickListener { save() }

            viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { getAdminAndData() }
        }

        getAdminAndData()
    }


    private fun addMode() {
        binding.apply {
            toolbar.apply {
                title = getString(R.string.text_add)
                menu.clear()
            }
        }
    }

    private fun editDeleteMode() {
        binding.apply {
            toolbar.apply {
                title = getString(R.string.text_change_or_delete)
                menu.clear()
                menuInflater.inflate(R.menu.top_appbar_delete_menu, menu)
                val icon = menu.findItem(R.id.deleteMenu)?.icon?.mutate()
                val color = ContextCompat.getColor(context, R.color.md_theme_error)
                if (icon != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        icon.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
                    } else {
                        @Suppress("DEPRECATION")
                        icon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
                    }
                }


            }
        }
    }


    private fun getAdminAndData() {
        buttonIsEnabled()

        authViewModel.getUser().observe(this) {
            val token = it.first
            if (token == AuthPreferences.DEFAULT_VALUE) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                adminViewModel.token = token
                if (isEditDeleteView) {
                    adminViewModel.getClassSessionById(id)
                }
            }
        }

        adminViewModel.isLoading.observe(this) {
            isLoading = it
            showLoadingMain(it)
        }

        adminViewModel.name.observe(this) { eventData ->
            eventData.getContentIfNotHandled()?.let {
                if (isLoading) {
                    isVisibleAllView(false)
                } else {
                    isVisibleAllView(true)
                }
                failedToConnect(false)

                binding.apply {
                    edtName.setText(it.name)
                    toolbar.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.deleteMenu -> {
                                delete(it.id,it.name ?: "")
                                true
                            }

                            else -> false
                        }
                    }
                }




                if (it.isAdded || it.isUpdated || it.isDeleted) {
                    val success = getString(R.string.text_success)
                    val label = getString(R.string.text_label_class_session)
                    val resultIntent = Intent()

                    val msg = when {
                        it.isAdded -> R.string.text_alert_add
                        it.isUpdated -> R.string.text_alert_change
                        else -> R.string.text_alert_delete
                    }

                    resultIntent.putExtra(
                        KEY_EXTRA_SUCCESS,
                        getString(msg, success, label)
                    )

                    setResult(KEY_RESULT_CODE, resultIntent)
                    finish()
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

    private fun save() {
        binding.apply {
            hideKeyboard()
            if (isEditDeleteView) {
                adminViewModel.updateClassSession(
                    id,
                    NameData(
                        name = edtName.text.toString().trim(),
                    )
                )
            } else {
                adminViewModel.addClassSession(
                    NameData(
                        id = id,
                        name = edtName.text.toString().trim(),
                    )
                )
            }
        }
    }

    private fun delete(id: Int?, name: String) {
        binding.apply {
            hideKeyboard()
            showAlertDialog(
                name,
                STATUS_DELETED,
                id ?: 0
            )
        }
    }


    private fun Activity.hideKeyboard() {
        fun clearFocus() {
            binding.apply {
                inputLayoutName.editText?.clearFocus()
            }
        }
        hideKeyboard(currentFocus ?: View(this))
        clearFocus()
    }

    private fun buttonIsEnabled() {
        binding.apply {
            btnSave.isEnabled = edtName.text.toString().isNotEmpty()
        }
    }

    private fun showAlertDialog(msg: String = "", status: String, id: Int = 0) {

        val unauthorized = msg == getString(R.string.text_const_unauthorized)
        var title = ""
        var message = ""
        var icon: Drawable? = null
        when (status) {
            STATUS_DELETED -> {
                icon = ContextCompat.getDrawable(this, R.drawable.z_ic_delete)
                val wrappedDrawable = DrawableCompat.wrap(icon!!).mutate()
                val color = ContextCompat.getColor(this, R.color.md_theme_error)
                DrawableCompat.setTint(wrappedDrawable, color)
                title = getString(R.string.text_delete)
                message = getString(R.string.text_question_do_you_want_to_delete, msg)
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

            dialog = MaterialAlertDialogBuilder(this).apply {
                setCancelable(false)
                setIcon(icon)
                setTitle(title)
                setMessage(message)
                if (status == STATUS_DELETED || status == STATUS_ERROR) {
                    setPositiveButton(getString(R.string.text_ok)) { _, _ ->
                        isAlertDialogShow = false
                        dialog = null
                        when (status) {
                            STATUS_DELETED -> adminViewModel.deleteClassSession(id)
                            STATUS_ERROR -> {
                                if (unauthorized) authViewModel.saveUser(
                                    null,
                                    null,
                                    null
                                ) else return@setPositiveButton
                            }

                        }
                    }

                    if (status == STATUS_DELETED) {
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
                inputLayoutName.visibility = View.VISIBLE
                edtName.visibility = View.VISIBLE
                btnSave.visibility = View.VISIBLE

            } else {
                toolbar.visibility = View.INVISIBLE
                inputLayoutName.visibility = View.GONE
                edtName.visibility = View.GONE
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

    companion object {
        private const val STATUS_DELETED = "status_deleted"
        private const val STATUS_ERROR = "status_error"

        const val KEY_EXTRA_ID = "key_extra_id"

        const val KEY_EXTRA_SUCCESS = "key_extra_success"
        const val KEY_RESULT_CODE = 200
    }
}