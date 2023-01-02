package com.example.weatherapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weatherapp.adapters.WeatherModel

/**
 * automatically updates data from the server using Observer (observer)
 * without the need to monitor the LC of the fragment.
 * When the views are available, the data is updated automatically
 */

class MainViewModel : ViewModel() {
    val lifeDataCurrent = MutableLiveData<WeatherModel>() // weather of 1 day
    val lifeDataList = MutableLiveData<List<WeatherModel>>()  // weather list (hours/week)
}