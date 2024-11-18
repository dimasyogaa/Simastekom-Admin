package com.yogadimas.simastekom.ui.mainpage.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yogadimas.simastekom.BuildConfig
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.enums.ErrorCode
import com.yogadimas.simastekom.common.interfaces.OnCallbackFromFragmentInterface
import com.yogadimas.simastekom.common.state.State
import com.yogadimas.simastekom.databinding.FragmentProfileBinding
import com.yogadimas.simastekom.model.responses.AdminData
import com.yogadimas.simastekom.ui.admin.AdminEditActivity
import com.yogadimas.simastekom.ui.identity.personal.IdentityPersonalDetailActivity
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val contextFragment = this@ProfileFragment

    private val authViewModel: AuthViewModel by activityViewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(requireContext().dataStore))
    }
    private val adminViewModel: AdminViewModel by viewModel()

    private var isLoading = false
    private var isAlertDialogShow = false

    private lateinit var mCallback: OnCallbackFromFragmentInterface
    private var dialogLogout: AlertDialog? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallback = activity as OnCallbackFromFragmentInterface
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mCallback.getData(NAME_FRAGMENT)


        fetchAdminData()
        observeAdminData()
        setupClickListeners()
    }


    private fun fetchAdminData() {
        authViewModel.getUser().observe(viewLifecycleOwner) { (token) ->
            if (token == AuthPreferences.DEFAULT_VALUE) {
                Navigation.createNavigateOnClickListener(R.id.action_profileFragment_to_loginActivity)
            } else {
                adminViewModel.token = token
                adminViewModel.getAdminCurrent()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeAdminData() {
        adminViewModel.run {
            isLoading.observe(viewLifecycleOwner) { loading ->
                contextFragment.isLoading = loading
                showLoadingView(loading)
            }
            adminData.observe(viewLifecycleOwner) { eventData ->
                eventData.getContentIfNotHandled()?.let { data ->
                    showDefaultView(!contextFragment.isLoading)
                    showFailedConnectView(false)
                    if (data.logout) authViewModel.saveUser(null, null, null)
                    showAdminDataView(data)
                }
            }
            errors.observe(viewLifecycleOwner) { eventError ->
                eventError.getContentIfNotHandled()?.errors?.message?.firstOrNull()
                    ?.let { errorMsg ->
                        showDefaultView(true)
                        showFailedConnectView(false)
                        mCallback.getError(errorMsg, ErrorCode.CLIENT)
                    }
            }
            errorsSnackbarText.observe(viewLifecycleOwner) { eventSnackbarText ->
                eventSnackbarText.getContentIfNotHandled()?.let { snackBarText ->
                    showDefaultView(false)
                    showFailedConnectView(true)
                    mCallback.getError(snackBarText,  ErrorCode.SERVER)
                }
            }
        }
    }

    private fun showAdminDataView(data: AdminData) = binding.contentMainProfile.apply {
        tvName.text = data.name
        tvUsername.text = data.username
        ivProfile.load(BuildConfig.BASE_URL + data.profilePicture) {
            crossfade(true)
            placeholder(R.drawable.z_ic_placeholder_profile)
            error(R.drawable.z_ic_placeholder_profile)
        }
    }

    private fun showLoadingView(visible: Boolean) {
        binding.mainProgressBar.isVisible = visible
        if (visible) {
            showDefaultView(false)
            showFailedConnectView(false)
        }
    }

    private fun showFailedConnectView(visible: Boolean) {
        binding.viewHandle.viewFailedConnect.root.isVisible = visible
    }

    private fun showDefaultView(visible: Boolean) {
        binding.contentMainProfile.root.isVisible = visible
    }

    private fun setupClickListeners() = binding.apply {
        contentMainProfile.run {
            btnEdit.setOnClickListener {
                startActivity(Intent(requireActivity(), AdminEditActivity::class.java))
            }
            btnIdentityPersonal.setOnClickListener {
                startActivity(Intent(requireActivity(), IdentityPersonalDetailActivity::class.java))
            }
            btnLogout.setOnClickListener {
                if (!isAlertDialogShow) showLogoutDialog()
            }
        }
        viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { fetchAdminData() }
        swipeRefresh.setOnRefreshListener {
            fetchAdminData()
            swipeRefresh.isRefreshing = false
        }
    }


    private fun showLogoutDialog() {
        if (dialogLogout == null) {
            isAlertDialogShow = true
            dialogLogout = MaterialAlertDialogBuilder(
                requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog_Logout
            ).setCancelable(false)
                .setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.z_ic_logout))
                .setTitle(getString(R.string.text_logout))
                .setMessage(getString(R.string.text_question_do_you_want_to_leave_this_account))
                .setNegativeButton(getString(R.string.text_cancel)) { _, _ ->
                    isAlertDialogShow = false
                    dialogLogout = null
                }
                .setPositiveButton(getString(R.string.text_ok)) { _, _ ->
                    isAlertDialogShow = false
                    dialogLogout = null
                    if (!isLoading) logout()
                }.create()
        }
        dialogLogout?.show()
    }

    private fun logout() {
        adminViewModel.logout()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        dialogLogout?.dismiss()
        dialogLogout = null
    }

    companion object {
        const val NAME_FRAGMENT = "ProfileFragment"
    }
}
