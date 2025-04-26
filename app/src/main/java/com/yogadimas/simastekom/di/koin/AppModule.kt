package com.yogadimas.simastekom.di.koin

import com.yogadimas.simastekom.api.ApiConfig
import com.yogadimas.simastekom.repository.AdminStudentRepository
import com.yogadimas.simastekom.repository.AdminLecturerRepository
import com.yogadimas.simastekom.repository.AdminAllUserTypeRoleRepository
import com.yogadimas.simastekom.test.SimpleIdlingResource
import com.yogadimas.simastekom.ui.dialog.ImageViewerDialogFragment
import com.yogadimas.simastekom.viewmodel.admin.AdminViewModel
import com.yogadimas.simastekom.viewmodel.admin.AdminLecturerViewModel
import com.yogadimas.simastekom.viewmodel.admin.AdminAllUserTypeRoleViewModel
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

    factory { (imageUrl: String) -> ImageViewerDialogFragment.newInstance(imageUrl) }

    // Provide AdminStudentRepository
    singleOf(::AdminStudentRepository)
    singleOf(::AdminLecturerRepository)
    singleOf(::AdminAllUserTypeRoleRepository)

    // Provide AdminViewModel
    viewModelOf(::AdminViewModel)
    viewModelOf(::AdminLecturerViewModel)
    viewModelOf(::AdminAllUserTypeRoleViewModel)

}
