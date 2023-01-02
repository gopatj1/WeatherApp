package com.example.weatherapp.adapters

/**
 * Main data class - model. Every day is WeatherModel, which consist
 * of a fields, uses for display weather data in main card and in adapters item
 * hours - is JSONArrayObject, which inside contain weather data of every hour of a day
 */

data class WeatherModel(
    val city: String,
    val time: String,
    val condition: String,
    val currentTemp: String,
    val maxTemp: String,
    val minTemp: String,
    val imageUrl: String,
    val hours: String
)