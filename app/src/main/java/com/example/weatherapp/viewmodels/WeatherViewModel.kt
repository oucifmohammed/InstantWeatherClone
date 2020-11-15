package com.example.weatherapp.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.remote.WeatherResponse
import com.example.weatherapp.other.Resource
import com.example.weatherapp.repositories.WeatherRepository
import kotlinx.coroutines.launch

class WeatherViewModel @ViewModelInject constructor(
    private val repository: WeatherRepository
): ViewModel(){


    private val _currentWeather = MutableLiveData<Resource<WeatherResponse>>()
    val currentWeather: LiveData<Resource<WeatherResponse>> = _currentWeather

    fun getCurrentWeather(lat: Double,lon: Double) = viewModelScope.launch {
        _currentWeather.value = Resource.loading(null)
        val response = repository.getCurrentWeather(lat,lon)
        _currentWeather.value = response
    }
}