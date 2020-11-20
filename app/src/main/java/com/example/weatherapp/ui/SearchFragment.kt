package com.example.weatherapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.data.remote.WeatherResponse
import com.example.weatherapp.databinding.FragmentSearchBinding
import com.example.weatherapp.other.Operations
import com.example.weatherapp.other.Resource
import com.example.weatherapp.other.Status
import com.example.weatherapp.other.WeatherIconGenerator
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var activityMain: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater,container,false)

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityMain = activity as MainActivity
        activityMain.supportActionBar?.title = "Search"

        MainActivity.viewModel.searchWeather.observe(viewLifecycleOwner,{
            if(it.status == Status.LOADING){
                binding.progress.visibility = View.VISIBLE
                binding.bottomPart.visibility = View.INVISIBLE
                binding.location.visibility = View.INVISIBLE
                binding.weatherTemperature.visibility = View.INVISIBLE
                binding.mainWeather.visibility = View.INVISIBLE
                binding.weatherIconView.visibility = View.INVISIBLE
            }else {
                provideWeatherDataToScreen(it)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.appbar_menu,menu)

        val updateIcon = menu.findItem(R.id.updateIcon)
        updateIcon.isVisible = false

        val searchIcon = menu.findItem(R.id.searchIcon)
        val searchView = searchIcon.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(Operations.checkForInternetConnection(requireContext())){
                    MainActivity.viewModel.searchForWeather(query!!)
                    binding.internetText.visibility = View.INVISIBLE
                }else{
                    binding.internetText.visibility = View.VISIBLE
                }

                searchView.setQuery("", false)
                searchView.clearFocus()
                searchIcon.collapseActionView()
                activityMain.supportActionBar?.title = query
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
    }

    @SuppressLint("SetTextI18n")
    private fun provideWeatherDataToScreen(searchedWeather: Resource<WeatherResponse>){

        binding.progress.visibility = View.INVISIBLE
        binding.bottomPart.visibility = View.VISIBLE
        binding.location.visibility = View.VISIBLE
        binding.weatherTemperature.visibility = View.VISIBLE
        binding.mainWeather.visibility = View.VISIBLE
        binding.weatherIconView.visibility = View.VISIBLE

        binding.location.text = searchedWeather.data?.name
        binding.mainWeather.text = "${searchedWeather.data?.weather?.first()?.main}"
        WeatherIconGenerator.getIconResources(
            requireContext(), binding.weatherIconView,
            searchedWeather.data?.weather?.first()?.description
        )
        binding.windSpeedValue.text = "${searchedWeather.data?.wind?.speed} m/s"
        binding.humidityValue.text = "${searchedWeather.data?.main?.humidity}%"
        binding.weatherTemperature.text =
            "${searchedWeather.data?.main?.tempMax?.minus(273.15)?.roundToInt()}\u2103"
    }
}