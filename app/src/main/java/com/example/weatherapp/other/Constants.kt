package com.example.weatherapp.other

import androidx.datastore.preferences.preferencesKey

object Constants {

    const val API_URL = "https://api.openweathermap.org/data/2.5/"
    val PLACE_KEY = preferencesKey<String>("PLACE_KEY")
     val DATE_KEY = preferencesKey<Long>("DATE_KEY")
     val DESCRIPTION_KEY = preferencesKey<String>("DESCRIPTION_KEY")
     val TEMP_KEY = preferencesKey<Float>("TEMP_KEY")
     val MAIN_WEATHER_KEY = preferencesKey<String>("MAIN_WEATHER_KEY")
     val HUMIDITY_KEY = preferencesKey<Int>("HUMIDITY_KEY")
     val WIND_SPEED_KEY = preferencesKey<Float>("WIND_SPEED_KEY")
     const val DATA_STORE_NAME = "WEATHER_DATA_STORE"
}