package com.example.weatherapp.di

import android.content.Context
import android.location.LocationManager
import androidx.datastore.preferences.createDataStore
import com.example.weatherapp.data.remote.Api
import com.example.weatherapp.other.Constants.API_URL
import com.example.weatherapp.other.Constants.DATA_STORE_NAME
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApi(): Api =
        Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Api::class.java)

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @Provides
    @Singleton
    fun provideLocationManager(@ApplicationContext context: Context) =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context) = context.createDataStore(DATA_STORE_NAME)
}