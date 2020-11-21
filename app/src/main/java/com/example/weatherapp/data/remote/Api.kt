package com.example.weatherapp.data.remote

import com.example.weatherapp.BuildConfig
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {

    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appid: String = BuildConfig.API_KEY
    ): Response<WeatherResponse>

    @GET("weather")
    suspend fun searchForWeather(
        @Query("q") location: String,
        @Query("appid") appid: String = BuildConfig.API_KEY
    ): Response<WeatherResponse>

    @GET("forecast")
    suspend fun getWeatherForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appid: String = BuildConfig.API_KEY
    ): Response<ForecastWeatherResponse>
}