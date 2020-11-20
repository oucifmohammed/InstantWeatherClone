package com.example.weatherapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.data.remote.WeatherResponse
import com.example.weatherapp.databinding.FragmentHomeBinding
import com.example.weatherapp.other.Constants.DATE_KEY
import com.example.weatherapp.other.Constants.DESCRIPTION_KEY
import com.example.weatherapp.other.Constants.HUMIDITY_KEY
import com.example.weatherapp.other.Constants.MAIN_WEATHER_KEY
import com.example.weatherapp.other.Constants.PLACE_KEY
import com.example.weatherapp.other.Constants.TEMP_KEY
import com.example.weatherapp.other.Constants.WIND_SPEED_KEY
import com.example.weatherapp.other.Operations
import com.example.weatherapp.other.Resource
import com.example.weatherapp.other.Status
import com.example.weatherapp.other.WeatherIconGenerator
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var locationManager: LocationManager

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val activity = activity as MainActivity
        activity.supportActionBar?.title = "Home"

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 1
            )
        } else {
            getCurrentWeather()
        }

        MainActivity.viewModel.currentWeather.observe(viewLifecycleOwner, {

            if (it.status == Status.LOADING) {
                binding.progress.visibility = View.VISIBLE
                binding.bottomPart.visibility = View.INVISIBLE
                binding.location.visibility = View.INVISIBLE
                binding.date.visibility = View.INVISIBLE
                binding.weatherTemperature.visibility = View.INVISIBLE
                binding.mainWeather.visibility = View.INVISIBLE
                binding.weatherIconView.visibility = View.INVISIBLE
            } else if (it.status == Status.SUCCESS) {
                provideCurrentWeatherDataToScreen(it)
            }
        })


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.appbar_menu, menu)

        val updateIcon = menu.findItem(R.id.updateIcon)
        val searchIcon = menu.findItem(R.id.searchIcon)
        searchIcon.isVisible = false

        updateIcon.setOnMenuItemClickListener {
            getCurrentWeather()
            true
        }
    }
    @SuppressLint("SetTextI18n")
    private fun provideCurrentWeatherDataToScreen(currentWeather: Resource<WeatherResponse>) {
        binding.progress.visibility = View.INVISIBLE
        binding.bottomPart.visibility = View.VISIBLE
        binding.location.visibility = View.VISIBLE
        binding.date.visibility = View.VISIBLE
        binding.weatherTemperature.visibility = View.VISIBLE
        binding.mainWeather.visibility = View.VISIBLE
        binding.weatherIconView.visibility = View.VISIBLE

        binding.location.text = currentWeather.data?.name
        binding.date.text = getCurrentTime()
        binding.mainWeather.text = "${currentWeather.data?.weather?.first()?.main}"
        WeatherIconGenerator.getIconResources(
            requireContext(), binding.weatherIconView,
            currentWeather.data?.weather?.first()?.description
        )
        binding.windSpeedValue.text = "${currentWeather.data?.wind?.speed} m/s"
        binding.humidityValue.text = "${currentWeather.data?.main?.humidity}%"
        binding.weatherTemperature.text =
            "${currentWeather.data?.main?.tempMax?.minus(273.15)?.roundToInt()}\u2103"

        writeDataToDataStore(
            currentWeather.data?.name!!,
            System.currentTimeMillis(),
            currentWeather.data.weather.first().description!!,
            currentWeather.data.main?.tempMax!!.toFloat(),
            currentWeather.data.weather.first().main!!,
            currentWeather.data.main.humidity!!,
            currentWeather.data.wind?.speed!!.toFloat()
        )
    }


    @SuppressLint("MissingPermission")
    private fun getCurrentWeather() {

        if (Operations.checkForInternetConnection(requireContext())) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showDialog()
                return
            }
            lifecycleScope.launch {
                val location = withContext(Dispatchers.IO) {
                    fusedLocationProviderClient.lastLocation.await()
                }
                if (location != null) {
                    MainActivity.viewModel.getCurrentWeather(location.latitude, location.longitude)
                } else {
                    requestLocation()
                }
            }

            binding.internetText.visibility = View.INVISIBLE
        } else {
            provideDataStoreValues()
            Toast.makeText(
                requireContext(),
                "You need internet connection to see the current weather",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    fun provideDataStoreValues() {
        val placeFlow = dataStore.data.map {
            it[PLACE_KEY] ?: ""
        }

        val dateFlow = dataStore.data.map {
            it[DATE_KEY] ?: System.currentTimeMillis()
        }

        val descriptionFlow = dataStore.data.map {
            it[DESCRIPTION_KEY] ?: ""
        }

        val tempFlow = dataStore.data.map {
            it[TEMP_KEY] ?: 0f
        }

        val mainWeatherFlow = dataStore.data.map {
            it[MAIN_WEATHER_KEY] ?: ""
        }

        val humidityFlow = dataStore.data.map {
            it[HUMIDITY_KEY] ?: 0
        }

        val windSpeedFlow = dataStore.data.map {
            it[WIND_SPEED_KEY] ?: 0f
        }

        lifecycleScope.launch{
            val place = placeFlow.first()

            if(place == ""){
                binding.internetText.visibility = View.VISIBLE
                return@launch
            }else {
                binding.progress.visibility = View.INVISIBLE
                binding.bottomPart.visibility = View.VISIBLE

                binding.location.text = place

                val time = dateFlow.first()
                val dateFormat = SimpleDateFormat("EEEE MMM d, hh:mm aaa")
                binding.date.text = dateFormat.format(Date(time))

                val main = mainWeatherFlow.first()
                binding.mainWeather.text = main

                val description = descriptionFlow.first()
                WeatherIconGenerator.getIconResources(
                    requireContext(), binding.weatherIconView,
                    description
                )

                val windSpeed = windSpeedFlow.first()
                binding.windSpeedValue.text = "$windSpeed m/s"

                val humidity = humidityFlow.first()
                binding.humidityValue.text = "$humidity%"

                val temp = tempFlow.first()
                binding.weatherTemperature.text =
                    "${temp.minus(273.15).roundToInt()}\u2103"
            }
        }
    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setTitle("GPS Disabled")
            setMessage("Gps is disabled, in order to use the application properly you need to enable GPS of your device")
            setPositiveButton(
                "Enable GPS"
            ) { dialog, which ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(intent, 1)
            }
        }
        builder.create().show()
    }

    private fun writeDataToDataStore(
        place: String,
        date: Long,
        description: String,
        temp: Float,
        main: String,
        humidity: Int,
        windSpeed: Float
    ) {
        lifecycleScope.launch {
            dataStore.edit {
                it[PLACE_KEY] = place
                it[DATE_KEY] = date
                it[DESCRIPTION_KEY] = description
                it[TEMP_KEY] = temp
                it[MAIN_WEATHER_KEY] = main
                it[HUMIDITY_KEY] = humidity
                it[WIND_SPEED_KEY] = windSpeed
            }
        }

    }

    @SuppressLint("MissingPermission")
    fun requestLocation() {
        val locationRequest =
            LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(1000)
                .setNumUpdates(1)
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                val location1 = locationResult?.lastLocation
                MainActivity.viewModel.getCurrentWeather(location1!!.latitude, location1.longitude)
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback,
            Looper.myLooper()
        )
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentTime(): String {
        val currentTime = System.currentTimeMillis()
        val date = Date(currentTime)
        val dateFormat = SimpleDateFormat("EEEE MMM d, hh:mm aaa")
        return dateFormat.format(date)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentWeather()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) {
            getCurrentWeather()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}