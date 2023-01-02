package com.example.weatherapp.utils

/**
 * The URL of weather API and cities API consists of 2 parts (start & end)
 * Between them set city name from editText or weatherAdapter item
 * Api key changes frequently, because Api is FREE and get their services for a certain period
 */

object Constants {
    private const val API_KEY_WEATHER = "de164ce5dd0049b6858183613222112" //"70d256c0dded414d83f194305220412"
    private const val API_KEY_CITIES = "9f7253386e95c6b475b673806aa566e0" //"e0d61f7198962472d8928f7c4cd36e9e"

    const val WEATHER_START_URL = "https://api.weatherapi.com/v1/forecast.json?key=$API_KEY_WEATHER&q="
    const val WEATHER_END_URL = "&days=10&aqi=no&alerts=no"

    const val CITIES_START_URL = "https://htmlweb.ru/geo/api.php?city_name="
    const val CITIES_END_URL = "&json&api_key=$API_KEY_CITIES"
}