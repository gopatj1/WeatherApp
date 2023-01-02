package com.example.weatherapp

/** Interface for updating weather in card of MainFragment after vertical swipe
 * from DaysFragment or HoursFragment via MainActivity (like connector) */

interface SwipeInterface{
    fun onSwipe()
}