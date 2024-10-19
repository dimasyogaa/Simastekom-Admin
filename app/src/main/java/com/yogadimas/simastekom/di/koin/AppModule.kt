package com.yogadimas.simastekom.di.koin

import com.yogadimas.simastekom.api.ApiConfig
import com.yogadimas.simastekom.repository.AdminStudentRepository
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val appModule = module {

    // Provide ApiService
    single { ApiConfig.getApiService() } // tidak bisa menggunakan singleOf karena bukan constructor dan

//    // Provide AdminStudentRepository
//    single { AdminStudentRepository(get()) }
//
//    // Provide AdminViewModel
//    viewModel { AdminViewModel(get()) }


    // Provide AdminStudentRepository
    singleOf(::AdminStudentRepository)

    // Provide AdminViewModel
    viewModelOf(::AdminViewModel)

}