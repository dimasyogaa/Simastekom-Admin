package com.yogadimas.simastekom

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.yogadimas.simastekom.databinding.ActivitySplashBinding
import com.yogadimas.simastekom.common.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.common.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.ui.login.LoginActivity
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {


    private lateinit var binding: ActivitySplashBinding

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private var tokenReceived = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            tokenReceived

        }
        route()

    }


    private fun route() {
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel.getUser().observe(this@SplashActivity) {
            this.tokenReceived = false
            if (it.first == AuthPreferences.DEFAULT_VALUE) {
                navigateToLogin()
            } else {
                navigateToMain()
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this@SplashActivity, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun navigateToMain() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

}