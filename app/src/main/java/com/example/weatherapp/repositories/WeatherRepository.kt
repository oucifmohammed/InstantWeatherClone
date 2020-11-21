package com.example.weatherapp.repositories

import com.example.weatherapp.data.remote.Api
import com.example.weatherapp.data.remote.ForecastWeatherResponse
import com.example.weatherapp.data.remote.WeatherResponse
import com.example.weatherapp.other.Resource
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val weatherApi: Api,
) {

    suspend fun getCurrentWeather(lat: Double, lon: Double): Resource<WeatherResponse> {

        return try {
            val response = weatherApi.getCurrentWeather(lat, lon)
            if (response.isSuccessful) {
                response.body()?.let {
                    return@let Resource.success(it)
                } ?: Resource.error("Unknown error occurred", null)
            } else {
                Resource.error("Unknown error occurred", null)
            }
        } catch (e: Exception) {
            Resource.error("Could not reach the server,check your internet connection", null)
        }
    }

    suspend fun searchForWeather(location: String): Resource<WeatherResponse> {
        return try {
            val response = weatherApi.searchForWeather(location)
            if (response.isSuccessful) {
                response.body()?.let {
                    return@let Resource.success(it)
                } ?: Resource.error("Unknown error occurred", null)
            } else {
                Resource.error("Unknown error occurred", null)
            }
        } catch (e: Exception) {
            Resource.error("Could not reach the server,check your internet connection", null)
        }
    }

    suspend fun forecastWeather(lat: Double, lon: Double): Resource<ForecastWeatherResponse> {
        return try {
            val response = weatherApi.getWeatherForecast(lat, lon)
            if (response.isSuccessful) {
                response.body()?.let {
                    return@let Resource.success(it)
                } ?: Resource.error("Unknown error occurred", null)
            } else {
                Resource.error("Unknown error occurred", null)
            }
        } catch (e: Exception) {
            Resource.error("Could not reach the server,check your internet connection", null)
        }
    }
}