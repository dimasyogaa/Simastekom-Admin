package com.yogadimas.simastekom.ui.identity.personal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.helper.SnackBarHelper
import com.yogadimas.simastekom.common.helper.setStripIfNull
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.databinding.ActivityIdentityPersonalDetailBinding
import com.yogadimas.simastekom.model.responses.AddressData
import com.yogadimas.simastekom.model.responses.IdentityPersonalData
import com.yogadimas.simastekom.ui.login.LoginActivity
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import org.koin.androidx.viewmodel.ext.android.viewModel

class IdentityPersonalDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIdentityPersonalDetailBinding

    private val contextActivity = this@IdentityPersonalDetailActivity

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }
    private val adminViewModel: AdminViewModel by viewModel()

    private var dialogAlert: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIdentityPersonalDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        showDefaultView(false)
        setupListeners()
        fetchIdentityPersonalDetail()
        observeIdentityPersonalDetail()
    }


    private fun fetchIdentityPersonalDetail() {
        authViewModel.getUser().observe(contextActivity) { user ->
            val (token, userId, userType) = user
            if (token == AuthPreferences.DEFAULT_VALUE) {
                startActivity(Intent(contextActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            } else {
                adminViewModel.token = token
                adminViewModel.getIdentityPersonal(userType, userId)
            }
        }
    }

    private fun observeIdentityPersonalDetail() {
        adminViewModel.apply {
            isLoading.observe(contextActivity, ::showLoadingView)
            identityPersonal.observe(contextActivity) { eventData ->
                eventData.getContentIfNotHandled()?.let {
                    showDefaultView(!isLoading.value!!)
                    showFailedConnectView(false)
                    handleIdentityPersonalDetailData(it)
                }
            }
            errors.observe(contextActivity) { eventError ->
                eventError.getContentIfNotHandled()?.let {
                    showDefaultView(true)
                    showAlertDialog(it.errors?.message?.firstOrNull().orEmpty())
                }
            }
            errorsSnackbarText.observe(contextActivity) { eventString ->
                eventString.getContentIfNotHandled()?.let {
                    showSnackbar(it)
                    showFailedConnectView(true)
                }
            }
        }
    }

    private fun handleIdentityPersonalDetailData(data: IdentityPersonalData) {
        binding.apply {
            tvPhone.text = data.phone.setStripIfNull()
            tvEmail.text = data.email.setStripIfNull()
            tvIdCardNumber.text = data.idCardNumber.setStripIfNull()
            tvPlaceDateBirth.text = data.placeDateBirth.setStripIfNull()
            val address = AddressData.getAddressData(data.address)
            tvAddressHome.text = address.setStripIfNull()
            setGender(data.gender.orEmpty())
            tvReligion.text = data.religion.setStripIfNull()
        }
    }

    private fun setGender(gender: String) {
        val drawable = when (gender.lowercase()) {
            getString(R.string.text_man).lowercase() -> R.drawable.z_ic_man
            getString(R.string.text_woman).lowercase() -> R.drawable.z_ic_woman
            else -> null
        }
        binding.ivGender.setImageDrawable(drawable?.let { ContextCompat.getDrawable(this, it) })
        binding.tvGender.text = gender
    }


    private fun setupListeners() {
        binding.apply {
            toolbar.setOnClickListener { clearFocus() }
            layoutRoot.setOnClickListener { clearFocus() }
            toolbar.setNavigationOnClickListener { finish() }
            viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { observeIdentityPersonalDetail() }
        }
    }

    private fun clearFocus() {
        binding.root.clearFocus()
    }


    private fun showLoadingView(isVisible: Boolean) {
        showLoading(binding.mainProgressBar, isVisible)
        if (isVisible) showDefaultView(false) else showFailedConnectView(false)
    }

    private fun showDefaultView(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.apply {
            appBarLayout.visibility = visibility
            toolbar.visibility = visibility
            ivPhone.visibility = visibility
            tvRowPhone.visibility = visibility
            tvPhone.visibility = visibility
            ivEmail.visibility = visibility
            tvRowEmail.visibility = visibility
            tvEmail.visibility = visibility
            ivIdCardNumber.visibility = visibility
            tvRowIdCardNumber.visibility = visibility
            tvIdCardNumber.visibility = visibility
            ivPlaceDateBirth.visibility = visibility
            tvRowPlaceDateBirth.visibility = visibility
            tvPlaceDateBirth.visibility = visibility
            ivAddressHome.visibility = visibility
            tvRowAddressHome.visibility = visibility
            tvAddressHome.visibility = visibility
            ivGender.visibility = visibility
            tvRowGender.visibility = visibility
            tvGender.visibility = visibility
            ivReligion.visibility = visibility
            tvRowReligion.visibility = visibility
            tvReligion.visibility = visibility
        }
    }

    private fun showFailedConnectView(isVisible: Boolean) {
        binding.viewHandle.viewFailedConnect.root.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun showSnackbar(message: String) {
        SnackBarHelper.display(binding.root as ViewGroup, message, contextActivity)
    }

    private fun showAlertDialog(error: String) {
        val message = if (error == getString(R.string.text_const_unauthorized)) {
            getString(R.string.text_please_login_again)
        } else error

        dialogAlert = dialogAlert ?: MaterialAlertDialogBuilder(this)
            .setCancelable(false)
            .setIcon(ContextCompat.getDrawable(this, R.drawable.z_ic_warning))
            .setTitle(getString(R.string.text_login_again))
            .setMessage(message)
            .setPositiveButton(getString(R.string.text_ok)) { _, _ ->
                authViewModel.saveUser(null, null, null)
            }
            .create()
        dialogAlert?.show()
    }

    override fun onStop() {
        super.onStop()
        dialogAlert?.dismiss()
        dialogAlert = null
    }
}
