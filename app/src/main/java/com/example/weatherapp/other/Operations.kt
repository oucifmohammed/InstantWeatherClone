package com.example.weatherapp.other

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.provider.Settings
import androidx.core.app.ActivityCompat.startActivityForResult
import com.example.weatherapp.MainActivity

object Operations {

    fun checkForInternetConnection(context: Context): Boolean{
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isAvailable && networkInfo.isConnected
    }

    fun showDialog(context: Context,activity: MainActivity){
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setTitle("GPS Disabled")
            setMessage("Gps is disabled, in order to use the application properly you need to enable GPS of your device")
            setPositiveButton(
                "Enable GPS"
            ) { dialog, which ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity.startActivityForResult(intent, 1)
            }
        }
        builder.create().show()
    }
}