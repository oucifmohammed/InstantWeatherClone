package com.example.weatherapp.data.remote


import com.google.gson.annotations.SerializedName

data class ForecastWeatherResponse(
    @SerializedName("list")
    val weathers: List<WeatherForecast>,
) {

    data class WeatherForecast(
        @SerializedName("dt")
        val dt: Int,

        @SerializedName("dt_txt")
        val dt_txt: String,

        @SerializedName("wind")
        val wind: Wind,

        @SerializedName("main")
        val main: Main,

        @SerializedName("weather")
        val weatherDescriptions: List<WeatherDescription>
    ){
        data class Wind(
            @SerializedName("speed")
            val speed: Double,

            @SerializedName("deg")
            val deg: Int
        )

        data class Main(
            @SerializedName("temp_max")
            val temp_max: Double,

            @SerializedName("humidity")
            val humidity: Int
        )

        data class WeatherDescription(
            @SerializedName("main")
            val main: String,

            @SerializedName("description")
            val description: String
        )
    }
}