package com.yogadimas.simastekom.simastekom_mahasiswa.ui.main

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Window
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.core.common.customs.ToastHelper
import com.yogadimas.simastekom.core.common.extensions.getParcelableData
import com.yogadimas.simastekom.core.ui.model.auth.AuthUserUiModel
import com.yogadimas.simastekom.simastekom_mahasiswa.databinding.ActivitySmmainBinding
import com.yogadimas.simastekom.simastekom_mahasiswa.ui.BaseActivity
import com.yogadimas.simastekom.simastekom_mahasiswa.ui.classes.SMClassesMenuActivity
import com.yogadimas.simastekom.simastekom_mahasiswa.ui.important_contact.SMImportantContactMenuActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class SMMainActivity : BaseActivity() {
    private lateinit var binding: ActivitySmmainBinding
    private val activityContext = this@SMMainActivity

    private val viewModel: SMMainViewModel by viewModel()

    private lateinit var user: AuthUserUiModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        setupIntent()
        setupListener()
        saveAuth()
        getAuth()
    }

    private fun saveAuth() {
        viewModel.setSaveUser(user)
    }

    private fun getAuth() = lifecycleScope.launch {
        viewModel.getUser().collectLatest { showToast(it.userType.toString()) }
    }

    private fun showToast(message: String) {
        ToastHelper.show(activityContext, message)
    }


    private fun setupView() {
        binding = ActivitySmmainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setStatusBarColor(window)
        setupToolbar()
    }

    private fun setStatusBarColor(window: Window) {
        val typedValue = TypedValue()
        val theme = window.context.theme
        theme.resolveAttribute(R.color.md_theme_surface, typedValue, true)
        val color = typedValue.data
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.statusBarColor = color
            val isLight = resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK ==
                    Configuration.UI_MODE_NIGHT_NO

            WindowInsetsControllerCompat(window, window.decorView).apply {
                isAppearanceLightStatusBars = isLight
                isAppearanceLightNavigationBars = isLight
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener { finish() }
        }
    }

    private fun setupIntent() {
        user = intent.getParcelableData<AuthUserUiModel>("auth_user") ?: AuthUserUiModel()
    }

    private fun setupListener() {
        fun navigateTo(activityClass: Class<*>) {
            val intent = Intent(activityContext, activityClass)
            startActivity(intent)
        }

        binding.apply {

            btnClass.setOnClickListener {
                navigateTo(SMClassesMenuActivity::class.java)
            }
            btnImportantContact.setOnClickListener {
                navigateTo(SMImportantContactMenuActivity::class.java)
            }

        }
    }


}