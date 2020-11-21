package com.example.weatherapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.MainActivity
import com.example.weatherapp.adapters.ForecastWeatherRecyclerViewAdapter
import com.example.weatherapp.databinding.FragmentForcastBinding
import com.example.weatherapp.other.Operations
import com.example.weatherapp.other.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ForecastFragment : Fragment() {

    private var _binding: FragmentForcastBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var locationManager: LocationManager

    private lateinit var _activity: MainActivity

    private lateinit var _adapter: ForecastWeatherRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _activity = activity as MainActivity

        _activity.supportActionBar?.title = "Forecast"
        _binding = FragmentForcastBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpAdapter()
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
        }else {
            if(MainActivity.viewModel.forecastWeather.value == null)
            getForecastWeather()
        }

        MainActivity.viewModel.forecastWeather.observe(viewLifecycleOwner,{

            if(it.status == Status.LOADING){
                binding.progressCircular.visibility = View.VISIBLE
            }else{
                binding.progressCircular.visibility = View.INVISIBLE
                _adapter.submitList(it.data!!.weathers)
            }
        })


        binding.calendarView.setCalendarListener(object : CollapsibleCalendar.CalendarListener {
            override fun onClickListener() {

            }

            override fun onDataUpdate() {

            }

            override fun onDayChanged() {

            }

            override fun onDaySelect() {
                val day = binding.calendarView.selectedDay
                Toast.makeText(
                    requireContext(),
                    "${day?.year}-${day?.month?.plus(1)}-${day?.day}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onItemClick(v: View) {

            }

            override fun onMonthChange() {

            }

            override fun onWeekChange(position: Int) {

            }

        })
    }

    @SuppressLint("MissingPermission")
    private fun getForecastWeather(){
        if(Operations.checkForInternetConnection(requireContext())){
            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Operations.showDialog(requireContext(),_activity)
                return
            }
            viewLifecycleOwner.lifecycleScope.launch {
                val location = withContext(Dispatchers.IO){
                    fusedLocationProviderClient.lastLocation.await()
                }

                if(location !=null){
                    MainActivity.viewModel.forecastWeather(location.latitude,location.longitude)
                }else {
                    requestLocation()
                }
            }

            binding.internetText.visibility = View.INVISIBLE
        }else{
            binding.internetText.visibility = View.VISIBLE
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        val locationRequest =
            LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(1000)
                .setNumUpdates(1)

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                val location1 = locationResult?.lastLocation
                MainActivity.viewModel.forecastWeather(location1!!.latitude, location1.longitude)
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback,
            Looper.myLooper()
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getForecastWeather()
            }
        }
    }

    private fun setUpAdapter(){
        _adapter = ForecastWeatherRecyclerViewAdapter()
        binding.forecastWeatherList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = _adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}