package com.yogadimas.simastekom.ui.student

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yogadimas.simastekom.R
import com.yogadimas.simastekom.databinding.ActivityLoginBinding
import com.yogadimas.simastekom.databinding.ActivityStudentBinding
import com.yogadimas.simastekom.datastore.ObjectDataStore.dataStore
import com.yogadimas.simastekom.datastore.preferences.AuthPreferences
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.auth.AuthViewModel
import com.yogadimas.simastekom.viewmodel.factory.AuthViewModelFactory

class StudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentBinding

    private val adminViewModel: AdminViewModel by viewModels()

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory.getInstance(AuthPreferences.getInstance(dataStore))
    }

    private var isLoading = false

    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            toolbar.setNavigationOnClickListener {
                finish()
            }

                searchView.setupWithSearchBar(searchBar)

                searchView
                    .editText
                    .setOnEditorActionListener { textView, actionId, event ->

                        searchBar.setText(searchView.text)

                        searchView.hide()

                        Toast.makeText(this@StudentActivity, searchView.text, Toast.LENGTH_SHORT).show()

                        false
                    }





        }
    }


}