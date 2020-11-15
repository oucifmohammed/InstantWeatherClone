package com.example.weatherapp.other

import android.content.Context
import android.net.ConnectivityManager

object Operations {

    fun checkForInternetConnection(context: Context): Boolean{
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isAvailable && networkInfo.isConnected
    }
}