package com.yogadimas.simastekom.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.LifecycleOwner
import com.yogadimas.simastekom.core.BuildConfig.*
import com.yogadimas.simastekom.core.common.customs.DialogAlert
import com.yogadimas.simastekom.core.data.repository.AuthUserRepository
import com.yogadimas.simastekom.core.data.repository.ImportantContactRepository
import com.yogadimas.simastekom.core.data.source.local.AuthLocalDataSource
import com.yogadimas.simastekom.core.data.source.local.auth.AuthInterceptor
import com.yogadimas.simastekom.core.data.source.local.auth.AuthPreferences
import com.yogadimas.simastekom.core.data.source.local.auth.authDataStore
import com.yogadimas.simastekom.core.data.source.remote.datasource.ImportantContactRemoteDataSource
import com.yogadimas.simastekom.core.data.source.remote.network.SimastekomMahasiswaApiService
import com.yogadimas.simastekom.core.domain.repository.IImportantContactRepository
import com.yogadimas.simastekom.core.domain.repository.auth.IAuthUserRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java


val coreNetworkModule = module {
    single {
        val loggingInterceptor = if (DEBUG) {
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        } else {
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.NONE)
        }
        OkHttpClient.Builder()
            .addInterceptor(get<Interceptor>())
            .addInterceptor(loggingInterceptor)
            .build()
    }

    single {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL_API)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(SimastekomMahasiswaApiService::class.java)
    }
}

val coreAuthModule = module {
    single<DataStore<Preferences>> { get<Context>().authDataStore }
    single { AuthPreferences(get()) }
    single<Interceptor> { AuthInterceptor(get()) }
}

val coreRepositoryModule = module {
    single { AuthLocalDataSource(get()) }
    single { ImportantContactRemoteDataSource(get()) }

    single<IAuthUserRepository> { AuthUserRepository(get()) }
    single<IImportantContactRepository> { ImportantContactRepository(get()) }
}


val coreUiHelperModule = module {
    factory { (context: Context, lifecycleOwner: LifecycleOwner) ->
        DialogAlert(context, lifecycleOwner)
    }
}



