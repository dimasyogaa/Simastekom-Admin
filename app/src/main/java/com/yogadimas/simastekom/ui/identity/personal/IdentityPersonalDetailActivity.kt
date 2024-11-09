package com.yogadimas.simastekom.ui.identity.personal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.helper.setStripIfNull
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.databinding.ActivityIdentityPersonalDetailBinding
import com.yogadimas.simastekom.model.responses.AddressData
import com.yogadimas.simastekom.ui.login.LoginActivity
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import org.koin.androidx.viewmodel.ext.android.viewModel

class IdentityPersonalDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIdentityPersonalDetailBinding

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private val adminViewModel: AdminViewModel by viewModel()

    private var isLoading = false
    private var isAlertDialogShow = false

    private var dialogAlert: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityIdentityPersonalDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getUser()

        binding.apply {
            toolbar.setOnClickListener { clearFocus() }
            layoutRoot.setOnClickListener { clearFocus() }
            toolbar.setNavigationOnClickListener { finish() }
            viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { getUser() }
        }

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

                if (it.isUpdated) {
                    isVisibleAllView(false)
                } else {
                    binding.apply {
                        tvPhone.text = it.phone.setStripIfNull()
                        tvEmail.text = it.email.setStripIfNull()
                        tvIdCardNumber.text = it.idCardNumber.setStripIfNull()
                        tvPlaceDateBirth.text = it.placeDateBirth.setStripIfNull()
                        tvAddressHome.text = AddressData.getAddressData(it.address).setStripIfNull()
                        setGender(it.gender.orEmpty())
                        tvReligion.text = it.religion.setStripIfNull()
                    }

                }

            }
        }

        adminViewModel.errors.observe(this) { eventError ->
            eventError.getContentIfNotHandled()?.let { data ->
                if (data.errors != null) {
                    val listMessage = data.errors.message.orEmpty()
                    isVisibleAllView(true)
                    failedToConnect(false)
                    showAlertDialog(listMessage[0])
                }
            }
        }

        adminViewModel.errorsSnackbarText.observe(this) { eventString ->
            eventString.getContentIfNotHandled()?.let { snackBarText ->
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


    private fun setGender(gender: String?) {
        binding.apply {
            if (gender != null) {
                when (gender.lowercase()) {
                    getString(R.string.text_man).lowercase() -> {
                        ivGender.setImageDrawable(
                            ContextCompat.getDrawable(
                                this@IdentityPersonalDetailActivity,
                                R.drawable.z_ic_man
                            )
                        )

                        tvGender.text = getString(R.string.text_man)
                    }

                    getString(R.string.text_woman).lowercase() -> {
                        ivGender.setImageDrawable(
                            ContextCompat.getDrawable(
                                this@IdentityPersonalDetailActivity,
                                R.drawable.z_ic_woman
                            )
                        )

                        tvGender.text = getString(R.string.text_woman)
                    }
                }
            }
        }
    }

    private fun showAlertDialog(error: String) {
        val errorMessage =
            if (error == getString(R.string.text_const_unauthorized)) getString(R.string.text_please_login_again) else error

        if (dialogAlert == null) {
            dialogAlert = MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setIcon(ContextCompat.getDrawable(this, R.drawable.z_ic_warning))
                .setTitle(getString(R.string.text_login_again))
                .setMessage(errorMessage)
                .setPositiveButton(getString(R.string.text_ok)) { _, _ ->
                    isAlertDialogShow = false
                    authViewModel.saveUser(null, null, null)
                }.create()
        }

        if (!isAlertDialogShow) {
            isAlertDialogShow = true
            dialogAlert?.show()
        }

    }

    private fun clearFocus() {
        binding.apply {
            tvPhone.clearFocus()
            tvEmail.clearFocus()
            ivIdCardNumber.clearFocus()
            tvPlaceDateBirth.clearFocus()
            tvAddressHome.clearFocus()
            tvGender.clearFocus()
            tvReligion.clearFocus()
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
                ivPhone.visibility = View.VISIBLE
                tvRowPhone.visibility = View.VISIBLE
                tvPhone.visibility = View.VISIBLE
                ivEmail.visibility = View.VISIBLE
                tvRowEmail.visibility = View.VISIBLE
                tvEmail.visibility = View.VISIBLE
                ivIdCardNumber.visibility = View.VISIBLE
                tvRowIdCardNumber.visibility = View.VISIBLE
                tvIdCardNumber.visibility = View.VISIBLE
                ivPlaceDateBirth.visibility = View.VISIBLE
                tvRowPlaceDateBirth.visibility = View.VISIBLE
                tvPlaceDateBirth.visibility = View.VISIBLE
                ivAddressHome.visibility = View.VISIBLE
                tvRowAddressHome.visibility = View.VISIBLE
                tvAddressHome.visibility = View.VISIBLE
                ivGender.visibility = View.VISIBLE
                tvRowGender.visibility = View.VISIBLE
                tvGender.visibility = View.VISIBLE
                ivReligion.visibility = View.VISIBLE
                tvRowReligion.visibility = View.VISIBLE
                tvReligion.visibility = View.VISIBLE

            } else {
                toolbar.visibility = View.INVISIBLE
                ivPhone.visibility = View.GONE
                tvRowPhone.visibility = View.GONE
                tvPhone.visibility = View.GONE
                ivEmail.visibility = View.GONE
                tvRowEmail.visibility = View.GONE
                tvEmail.visibility = View.GONE
                ivIdCardNumber.visibility = View.GONE
                tvRowIdCardNumber.visibility = View.GONE
                tvIdCardNumber.visibility = View.GONE
                ivPlaceDateBirth.visibility = View.GONE
                tvRowPlaceDateBirth.visibility = View.GONE
                tvPlaceDateBirth.visibility = View.GONE
                ivAddressHome.visibility = View.GONE
                tvRowAddressHome.visibility = View.GONE
                tvAddressHome.visibility = View.GONE
                ivGender.visibility = View.GONE
                tvRowGender.visibility = View.GONE
                tvGender.visibility = View.GONE
                ivReligion.visibility = View.GONE
                tvRowReligion.visibility = View.GONE
                tvReligion.visibility = View.GONE
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
        if (dialogAlert != null) {
            isAlertDialogShow = false
            dialogAlert?.dismiss()
            dialogAlert = null
        }
    }
}