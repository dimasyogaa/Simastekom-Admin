package com.yogadimas.simastekom.simastekom_mahasiswa.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yogadimas.simastekom.simastekom_mahasiswa.di.smViewModelModule
import org.koin.core.context.loadKoinModules

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadKoinModules(
            listOf(smViewModelModule)
        )
    }
}



