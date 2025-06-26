package com.yogadimas.simastekom.simastekom_mahasiswa.ui.important_contact

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Window
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.yogadimas.simastekom.simastekom_mahasiswa.databinding.ActivitySmimportantContactMenuBinding
import com.yogadimas.simastekom.simastekom_mahasiswa.ui.important_category.SMImportantContactCategoryActivity

class SMImportantContactMenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySmimportantContactMenuBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        setupToolbar()
        setupButtonMenu()
    }

    private fun setupButtonMenu() {
        fun navigateTo(activityClass: Class<*>) {
            val intent = Intent(this@SMImportantContactMenuActivity, activityClass)
            startActivity(intent)
        }

        binding.apply {

            btnImportantContact.setOnClickListener {
                navigateTo(SMImportantContactActivity::class.java)
            }
            btnRelationImportantContactClassGroup.setOnClickListener { }
            btnClassGroup.setOnClickListener { }
            btnImportantContactCategory.setOnClickListener {
                navigateTo(SMImportantContactCategoryActivity::class.java)
            }


        }
    }


    private fun setupView() {
        binding = ActivitySmimportantContactMenuBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setStatusBarColor(window)
    }

    private fun setStatusBarColor(window: Window) {
        val typedValue = TypedValue()
        val theme = window.context.theme
        theme.resolveAttribute(com.yogadimas.simastekom.R.color.md_theme_surface, typedValue, true)
        val color = typedValue.data
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.statusBarColor = color
            val isLight = resources.configuration.uiMode and
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                    android.content.res.Configuration.UI_MODE_NIGHT_NO

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
}