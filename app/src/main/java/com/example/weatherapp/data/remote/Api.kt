package com.example.weatherapp.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {

    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appid: String = "607db7665e377b375e0cd656ba5f417f"
    ): Response<WeatherResponse>
}