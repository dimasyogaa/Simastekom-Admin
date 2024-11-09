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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yogadimas.simastekom.BuildConfig
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.databinding.ContentMainProfileBinding
import com.yogadimas.simastekom.databinding.FragmentProfileBinding
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.helper.showLoading
import com.yogadimas.simastekom.common.interfaces.OnCallbackFromFragmentInterface
import com.yogadimas.simastekom.model.responses.AdminData
import com.yogadimas.simastekom.ui.admin.AdminEditActivity
import com.yogadimas.simastekom.ui.identity.personal.IdentityPersonalDetailActivity
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private var _contentMainProfile: ContentMainProfileBinding? = null

    private val binding get() = _binding!!
    private val contentMainProfile get() = _contentMainProfile!!

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
        _contentMainProfile = binding.contentMainProfile

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mCallback.getData(NAME_FRAGMENT)



        getAdmin()

        contentMainProfile.apply {
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



        binding.viewHandle.viewFailedConnect.btnRefresh.setOnClickListener { getAdmin() }

        binding.swipeRefresh.setOnRefreshListener {
            getAdmin()
            binding.swipeRefresh.isRefreshing = false
        }


    }


    private fun getAdmin() {
        authViewModel.getUser().observe(viewLifecycleOwner) {
            val token = it.first
            if (token == AuthPreferences.DEFAULT_VALUE) {
                Navigation.createNavigateOnClickListener(R.id.action_profileFragment_to_loginActivity)
            } else {
                adminViewModel.token = token
                adminViewModel.getAdminCurrent()
            }
        }

        getAdminData()
    }

    @SuppressLint("SetTextI18n")
    private fun getAdminData() {
        adminViewModel.isLoading.observe(viewLifecycleOwner) {
            isLoading = it
            showLoadingMain(it)
        }

        adminViewModel.adminData.observe(viewLifecycleOwner) {eventData ->
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

                displayAdminData(it)
            }

        }
        // adminViewModel.identityPersonal.observe(viewLifecycleOwner) {
        //     displayIdentityPersonal(it)
        // }


        adminViewModel.errors.observe(viewLifecycleOwner) { eventError ->
            eventError.getContentIfNotHandled()?.let { data ->
                if (data.errors != null) {
                    val listMessage = data.errors.message.orEmpty()
                    isVisibleAllView(true)
                    failedToConnect(false)
                    mCallback.getError(listMessage[0], 400)
                }
            }
        }


        adminViewModel.errorsSnackbarText.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { snackBarText ->
                isVisibleAllView(false)
                failedToConnect(true)
                mCallback.getError(snackBarText, 500)
            }
        }
    }


    private fun identityPersonal() {
        contentMainProfile.apply {

            // layoutIdentityPersonal.setOnClickListener {
            //     tbIdentityPersonal.isChecked = !tbIdentityPersonal.isChecked
            // }
            //
            // tbIdentityPersonal.setOnCheckedChangeListener { _, isChecked ->
            //     if (isChecked) {
            //         layoutContentIdentityPersonal.visibility = View.VISIBLE
            //     } else {
            //         layoutContentIdentityPersonal.visibility = View.GONE
            //     }
            // }
        }
    }

    private fun showLogoutDialog() {
        isAlertDialogShow = true
        if (dialogLogout == null) {
            dialogLogout = MaterialAlertDialogBuilder(
                requireContext(),
                R.style.ThemeOverlay_App_MaterialAlertDialog_Logout
            )
                .setCancelable(false)
                .setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.z_ic_logout))
                .setTitle(getString(R.string.text_logout))
                .setMessage(getString(R.string.text_question_do_you_want_to_leave_this_account))
                .setNegativeButton(getString(R.string.text_cancel)) { _, _ ->
                    isAlertDialogShow = false
                    dialogLogout = null
                    return@setNegativeButton
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


    private fun displayAdminData(it: AdminData) {
        binding.contentMainProfile.tvName.text = it.name
        binding.contentMainProfile.tvUsername.text = it.username
        binding.contentMainProfile.ivProfile.load(BuildConfig.BASE_URL + it.profilePicture) {
            crossfade(true)
            placeholder(R.drawable.z_ic_placeholder_profile)
            error(R.drawable.z_ic_placeholder_profile)
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
            binding.viewHandle.viewFailedConnect.root.visibility =
                View.VISIBLE
        } else {
            binding.viewHandle.viewFailedConnect.root.visibility = View.GONE
        }

    }

    private fun isVisibleAllView(boolean: Boolean) {
        if (boolean) {
            binding.contentMainProfile.root.visibility = View.VISIBLE
        } else {
            binding.contentMainProfile.root.visibility = View.INVISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _contentMainProfile = null
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        if (dialogLogout != null) {
            dialogLogout?.dismiss();
            dialogLogout = null;
        }
    }

    companion object {
        const val NAME_FRAGMENT = "ProfileFragment"
    }


}