@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.example.weatherapp.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

/* Parsing Date from server. Set output format and english language */
object Utils {
    @SuppressLint("SimpleDateFormat")
    fun parseData(inputFormat: String, outputFormat: String, data: String): String {
        val input = SimpleDateFormat(inputFormat, Locale.ENGLISH)
        val output = SimpleDateFormat(outputFormat, Locale.ENGLISH)
        return output.format(input.parse(data)).toString()
    }
}