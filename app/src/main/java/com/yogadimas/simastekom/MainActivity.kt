package com.yogadimas.simastekom

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import coil.load
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.yogadimas.simastekom.BuildConfig.BASE_URL
import com.yogadimas.simastekom.databinding.ActivityMainBinding
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.common.enums.ErrorCode
import com.yogadimas.simastekom.common.interfaces.OnCallbackFromFragmentInterface
import com.yogadimas.simastekom.test.SimpleIdlingResource
import com.yogadimas.simastekom.ui.mainpage.action.ActionFragment
import com.yogadimas.simastekom.ui.login.LoginActivity
import com.yogadimas.simastekom.ui.password.PasswordEditActivity
import com.yogadimas.simastekom.ui.mainpage.profile.ProfileFragment
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity(), OnCallbackFromFragmentInterface {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var binding: ActivityMainBinding

    private val adminViewModel: AdminViewModel by viewModel()

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private lateinit var idlingResource: SimpleIdlingResource
    fun getIdlingResourceMain(): SimpleIdlingResource = idlingResource

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navViewDrawer: NavigationView
    private lateinit var navViewBottom: BottomNavigationView

    private var isLoading = false
    private var dialogHasBeenShow = false
    private var hasBeenClicked = false

    private var dialogLoading: AlertDialog? = null
    private var dialogAlert: AlertDialog? = null
    private var dialogLogout: AlertDialog? = null

    private var currentPage by Delegates.notNull<Int>()

    private lateinit var sharedPreferencesAppearedOnce: SharedPreferences
    private lateinit var editorAppearedOnce: SharedPreferences.Editor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        sharedPreferencesAppearedOnce = getSharedPreferences(SERVER_ERROR_NAME, Context.MODE_PRIVATE)
        editorAppearedOnce = sharedPreferencesAppearedOnce.edit()



        drawerLayout = binding.drawerLayout
        navViewDrawer = binding.navViewDrawer
        navViewBottom = binding.appBarMain.contentMain.navViewBottom


        if (intent != null) {
            if (intent.getStringExtra(KEY_PAGE) == ProfileFragment.NAME_FRAGMENT) {
                currentPage = R.id.profileFragment
            }
        }




        uINavigation()
        onClickMenuNavigation()

        idlingResource = SimpleIdlingResource()

    }





    override fun onStart() {
        super.onStart()
        navViewDrawer.menu.findItem(R.id.passwordEditActivity).setVisible(false)
        navViewDrawer.menu.findItem(R.id.logoutMenu).setVisible(false)
        navViewDrawer.menu.findItem(R.id.refreshMenu).setVisible(false)
        authAdmin()
    }


    private fun authAdmin(isRefresh: Boolean = false) {
        setServerErrorSharedPreferences(true)



        authViewModel.getUser().observe(this) {
            if (it.first == AuthPreferences.DEFAULT_VALUE) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                adminViewModel.token = it.first
                adminViewModel.getAdminCurrent()
            }
        }

        getAdminData(isRefresh)


    }

    private fun getAdminData(isRefresh: Boolean = false) {


        val header = navViewDrawer.getHeaderView(0)
        val profileCircleImageView: ImageView =
            header.findViewById(R.id.iv_profile)
        val tvName: TextView = header.findViewById(R.id.tv_name)
        val tvUserName: TextView = header.findViewById(R.id.tv_username)

        adminViewModel.isLoading.observe(this) {
            isLoading = it
            showLoadingDialog(it)
            if (!isLoading && isRefresh) drawerLayout.openDrawer(GravityCompat.START)
        }

        adminViewModel.adminData.observe(this) { eventData ->
            eventData.getContentIfNotHandled()?.let {
                if (isLoading) {
                    // isVisibleAllView(false)
                    navViewDrawer.menu.findItem(R.id.passwordEditActivity).setVisible(false)
                    navViewDrawer.menu.findItem(R.id.logoutMenu).setVisible(false)
                    navViewDrawer.menu.findItem(R.id.refreshMenu).setVisible(false)
                } else {
                    navViewDrawer.menu.findItem(R.id.passwordEditActivity).setVisible(true)
                    navViewDrawer.menu.findItem(R.id.logoutMenu).setVisible(true)
                    navViewDrawer.menu.findItem(R.id.refreshMenu).setVisible(false)
                }

                if (it.isLogout) {
                    authViewModel.saveUser(null, null, null)
                }


                authViewModel.saveUser(it.token, it.userId, it.userType)

                // showSnackbarError = false
                profileCircleImageView.load(BASE_URL + it.profilePicture) {
                    crossfade(true)
                    placeholder(R.drawable.z_ic_placeholder_profile)
                    error(R.drawable.z_ic_placeholder_profile)
                }

                tvName.text = it.name
                tvUserName.text = it.username

            }
        }


        adminViewModel.errors.observe(this) { eventError ->
            navViewDrawer.menu.findItem(R.id.passwordEditActivity).setVisible(true)
            navViewDrawer.menu.findItem(R.id.logoutMenu).setVisible(true)
            navViewDrawer.menu.findItem(R.id.refreshMenu).setVisible(false)
            eventError.getContentIfNotHandled()?.let { data ->
                if (data.errors != null) {
                    val listMessage = data.errors.message.orEmpty()
                    showAlertDialog(listMessage[0])
                    // isVisibleAllView(true)
                    // failedToConnect(false)
                }
            }
        }

        adminViewModel.errorsSnackbarText.observe(this) { eventString ->

            navViewDrawer.menu.findItem(R.id.refreshMenu).setVisible(true)
            navViewDrawer.menu.findItem(R.id.passwordEditActivity).setVisible(false)
            navViewDrawer.menu.findItem(R.id.logoutMenu).setVisible(false)


            eventString.getContentIfNotHandled()?.let { snackBarText ->
                if (getServerErrorSharedPreferences()) {
                    setServerErrorSharedPreferences(false)
                    Snackbar.make(
                        binding.root as ViewGroup,
                        snackBarText,
                        Snackbar.LENGTH_SHORT
                    ).setAnchorView(navViewBottom).show()

                    // isVisibleAllView(false)
                    // failedToConnect(true)
                }




            }


        }
    }

    private fun uINavigation() {
        setSupportActionBar(binding.appBarMain.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.actionFragment, R.id.profileFragment
            ), drawerLayout
        )


        setupActionBarWithNavController(navController, appBarConfiguration)
        navViewDrawer.setupWithNavController(navController)
        navViewBottom.setupWithNavController(navController)


        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    when (currentPage) {
                        R.id.actionFragment -> finish()
                        R.id.profileFragment -> findNavController(R.id.nav_host_fragment_content_main).navigate(
                            R.id.actionFragment
                        )
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)


    }

    private fun onClickMenuNavigation() {
        navViewDrawer.menu.findItem(R.id.passwordEditActivity).setOnMenuItemClickListener {
            if (!hasBeenClicked) {
                hasBeenClicked = true
                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {
                        drawerLayout.closeDrawer(GravityCompat.START)
                    }
                    delay(300)
                    withContext(Dispatchers.Main) {
                        val intent = Intent(this@MainActivity, PasswordEditActivity::class.java)
                        startActivity(intent)
                    }
                    hasBeenClicked = false
                }


            }

            true
        }

        navViewDrawer.menu.findItem(R.id.logoutMenu).setOnMenuItemClickListener {

            showLogoutDialog()

            true
        }

        navViewDrawer.menu.findItem(R.id.refreshMenu).setOnMenuItemClickListener {

            authAdmin(true)
            drawerLayout.closeDrawer(GravityCompat.START)

            true
        }

        navViewBottom.menu.findItem(R.id.actionFragment).setOnMenuItemClickListener {
            when (currentPage) {
                R.id.profileFragment -> findNavController(R.id.nav_host_fragment_content_main).navigate(
                    R.id.actionFragment
                )
            }
            true
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
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
                    dialogHasBeenShow = false
                    dialogAlert = null
                    authViewModel.saveUser(null, null, null)
                }.create()
        }

        if (!dialogHasBeenShow) {
            dialogHasBeenShow = true
            dialogAlert?.show()
        }

    }

    private fun showLogoutDialog() {

        if (dialogLogout == null) {
            dialogLogout = MaterialAlertDialogBuilder(
                this@MainActivity,
                R.style.ThemeOverlay_App_MaterialAlertDialog_Logout
            )
                .setCancelable(false)
                .setIcon(ContextCompat.getDrawable(this@MainActivity, R.drawable.z_ic_logout))
                .setTitle(getString(R.string.text_logout))
                .setMessage("Anda ingin keluar dari akun ini?")
                .setNegativeButton(getString(R.string.text_cancel)) { _, _ ->
                    dialogHasBeenShow = false
                    dialogLogout = null
                    return@setNegativeButton
                }
                .setPositiveButton(getString(R.string.text_ok)) { _, _ ->
                    dialogHasBeenShow = false
                    dialogLogout = null
                    if (!isLoading) logout()
                }.create()
        }

        if (!dialogHasBeenShow) {
            dialogHasBeenShow = true
            dialogLogout?.show()
        }


    }

    private fun showLoadingDialog(boolean: Boolean) {


        if (dialogLoading == null) {
            dialogLoading = MaterialAlertDialogBuilder(this@MainActivity)
                .setCancelable(false)
                .setView(R.layout.layout_progress_bar)
                .create()
        }

        if (boolean) {
            if (!dialogHasBeenShow) {
                    dialogHasBeenShow = true
                    dialogLoading?.show()
                    val screenWidth = resources.displayMetrics.widthPixels
                    val desiredWidthPercentage = 0.7
                    val width = (screenWidth * desiredWidthPercentage).toInt()
                    val height = ViewGroup.LayoutParams.WRAP_CONTENT
                    dialogLoading?.window?.setLayout(width, height)
            }
        } else {
                dialogHasBeenShow = false
                dialogLoading?.dismiss()
                dialogLoading = null

        }


    }

    private fun logout() {
        adminViewModel.logout()
    }


    private fun showSnackBar(message: String) {

        Snackbar.make(
            binding.root as ViewGroup,
            message,
            Snackbar.LENGTH_SHORT
        ).setAnchorView(navViewBottom).show()

    }

    override fun getError(message: String, code: ErrorCode) {
        when (code) {
            ErrorCode.CLIENT -> showAlertDialog(message)
            ErrorCode.SERVER -> showSnackBar(message)
            ErrorCode.UNAUTHORIZED -> {}
        }
    }

    override fun getData(message: String) {
        when (message) {
            ActionFragment.NAME_FRAGMENT ->  currentPage = R.id.actionFragment
            ProfileFragment.NAME_FRAGMENT -> currentPage = R.id.profileFragment
        }
    }


    override fun onResume() {
        super.onResume()
        findNavController(R.id.nav_host_fragment_content_main).navigate(currentPage)
    }

    override fun onStop() {
        super.onStop()
        setServerErrorSharedPreferences(false)
        adminViewModel.isLoading.removeObservers(this)
        adminViewModel.adminData.removeObservers(this)
        adminViewModel.errors.removeObservers(this)
        adminViewModel.errorsSnackbarText.removeObservers(this)
        if (dialogLoading != null) {
            dialogHasBeenShow = false
            dialogLoading?.dismiss()
            dialogLoading = null
        }
        if (dialogAlert != null) {
            dialogHasBeenShow = false
            dialogAlert?.dismiss()
            dialogAlert = null
        }
        if (dialogLogout != null) {
            dialogHasBeenShow = false
            dialogLogout?.dismiss()
            dialogLogout = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_IS_LOADING, isLoading)
        super.onSaveInstanceState(outState)
    }

    private fun setServerErrorSharedPreferences(boolean: Boolean) {
        editorAppearedOnce.putBoolean(SERVER_ERROR_KEY, boolean)
        editorAppearedOnce.apply()
    }

    private fun getServerErrorSharedPreferences(): Boolean {
        return sharedPreferencesAppearedOnce.getBoolean(SERVER_ERROR_KEY, false)
    }

    companion object {
        const val KEY_PAGE = "key_page"
        private const val KEY_IS_LOADING = "key_is_loading"

        private const val SERVER_ERROR_NAME = "server_error_name"
        private const val SERVER_ERROR_KEY = "server_error_key"

    }


}