package com.yogadimas.simastekom.simastekom_mahasiswa.di

import com.yogadimas.simastekom.simastekom_mahasiswa.ui.BaseViewModel
import com.yogadimas.simastekom.simastekom_mahasiswa.ui.important_category.SMImportantContactCategoryViewModel
import com.yogadimas.simastekom.simastekom_mahasiswa.ui.important_contact.SMImportantContactViewModel
import com.yogadimas.simastekom.simastekom_mahasiswa.ui.main.SMMainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val smViewModelModule = module {
    viewModel { BaseViewModel(get()) }
    viewModel { SMMainViewModel(get()) }
    viewModel { SMImportantContactViewModel(get(), get()) }
    viewModel { SMImportantContactCategoryViewModel(get(), get()) }
}