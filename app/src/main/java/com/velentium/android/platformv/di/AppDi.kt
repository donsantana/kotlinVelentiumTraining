package com.velentium.android.platformv.di

import android.app.Application
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.velentium.android.platformv.ui.viewmodels.ConnectionViewModel
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

//val apiModule = module {
//    // TODO: add app API modules here
//}

val viewModelModule = module {
    viewModel {
        ConnectionViewModel(application = androidApplication())
    }
}

val networkModule = module {
    fun cacheFactory(application: Application): Cache {
        val size: Long = 10L * 1024L * 1024L
        return Cache(application.cacheDir, size)
    }

    fun httpClientFactory(cache: Cache): OkHttpClient =
        OkHttpClient.Builder()
            .cache(cache)
            .build()

    fun gsonFactory(): Gson =
        GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .create()

    fun retrofitFactory(gson: Gson, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(client)
            .build()

    single { cacheFactory(androidApplication()) }
    single { httpClientFactory(get()) }
    single { gsonFactory() }
    single { retrofitFactory(get(), get()) }
}

// TODO: add Realm
//val realmModule = module {
//
//}