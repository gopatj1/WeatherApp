package com.example.weatherapp.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.MainActivity
import com.example.weatherapp.MainViewModel
import com.example.weatherapp.R
import com.example.weatherapp.adapters.SearchAdapter
import com.example.weatherapp.adapters.VpAdapter
import com.example.weatherapp.adapters.WeatherModel
import com.example.weatherapp.databinding.FragmentMainBinding
import com.example.weatherapp.utils.Constants.CITIES_END_URL
import com.example.weatherapp.utils.Constants.CITIES_START_URL
import com.example.weatherapp.utils.Constants.WEATHER_END_URL
import com.example.weatherapp.utils.Constants.WEATHER_START_URL
import com.example.weatherapp.utils.Utils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import org.json.JSONObject

/**
 * MainFragment is main window of app. It placed in placeHolder of MainActivity and do
 * main work/options of app. Here a main card with weather data of a current day
 * and logic of work of their buttons.
 * TabLayout with 2 tabs: HoursFragment and DaysFragment. This fragments show data
 * in recyclerViewAdapter items of every hour of a day and every day of a week appropriately.
 * Here we check app permission of use location. Check that device location is enabled.
 * Do GET to weather server or city server and parse response.
 */

class MainFragment : Fragment(), SearchAdapter.ListenerCitySearch {
    private lateinit var activityMain: MainActivity // global field for using context
    private lateinit var fLocationClient: FusedLocationProviderClient // get geolocation
    private val fList = listOf( // list of fragments using in tabLay
        HoursFragment.newInstance(),
        DaysFragment.newInstance()
    )
    private val tList = listOf( // names of tabs
        "Hours",
        "Days"
    )
    private lateinit var pLauncher: ActivityResultLauncher<String> // start geo permission activity and return result (allowed/denied)
    private lateinit var binding: FragmentMainBinding
    private val model: MainViewModel by activityViewModels() // for automatically updating data
    private lateinit var imBackground: ImageView // different background image depending on the weather
    private lateinit var imGPSGlob: ImageView // shown when selecting the weather GPS and hidden in other
    private lateinit var imSoundGlob: ImageView // sound off & on
    private lateinit var searchAdapter: SearchAdapter // search city adapter
    lateinit var searchFrameLayGlobField: FrameLayout // global field FrameLayout of search cities window
    private lateinit var etSearchCity: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        activityMain = activity as MainActivity
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        init()
        updateCurrentCard()
    }

    override fun onResume() {
        super.onResume()
        checkLocationEnabled()
    }

    /* Init view pager adapter and tabLay, location service, setup logic of search city button,
     * sound button and update location weather button */
    private fun init() = with(binding) {
        imBackground = imBg
        fLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        /* Init VpAdapter and get them fragment list. Then connect Vp and tabLay
        * and setup callback, which open appropriate fragment after tab click of horizontal swipe */
        val adapter = VpAdapter(activity as FragmentActivity, fList)
        viewPager2.adapter = adapter
        TabLayoutMediator(
            tabLayout,
            viewPager2
        ) {
                tab, pos -> tab.text = tList[pos]
        }.attach()

        /* Init search city window. It open after search click and close after close click,
        * or backPressed, or click to transparent part of FrameLay. If edit text is empty,
        * then in listView adapter shows standart city list. If not empty, then in adapter
        * shows cities which find after GET query to city server with name from ed text */
        imSearchCity.setOnClickListener{
            clickSearchFrameLay(show = true, vibrate = true, clearText = true, standartAdapter = true, hideKey = true)
        }
        // adapter search city with standart cities list
        searchAdapter = SearchAdapter(
            requireContext(),
            this@MainFragment,
            ArrayList(listOf(*resources.getStringArray(R.array.city_names))))
        lvSearch.adapter = searchAdapter
        // find cities after GET query to city server with name from ed text
        etSearchCity = etSearch
        etSearchCity.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if(etSearchCity.text.isNotEmpty())
                    requestWeatherOrCitiesData(etSearchCity.text.toString(), 'C')
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })
        // close button. Firstly clear edit text, secondly close search window
        imCloseSearch.setOnClickListener{
            if (etSearchCity.text.isNotEmpty())
                clickSearchFrameLay(show = true, vibrate = true, clearText = true, standartAdapter = true, hideKey = false)
            else clickSearchFrameLay(show = false, vibrate = true, clearText = true, standartAdapter = true, hideKey = true)
        }
        // click to transparent part of FrameLay. Search city window lost focus and we close it
        searchFrameLayGlobField = searchFrameLay
        searchFrameLayGlobField.setOnClickListener{
            clickSearchFrameLay(show = false, vibrate = false, clearText = true, standartAdapter = true, hideKey = true)
        }
        // click to enter of keyboard, then hide keyboard, don't clear edit text and don't hide search window
        etSearchCity.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                clickSearchFrameLay(show = true, vibrate = true, clearText = false, standartAdapter = false, hideKey = true)
            }
            false
        }

        /* Image shown when selecting the weather GPS and hidden in other */
        imGPSGlob = imGPS

        /* sound button change image to on/off after click or change ringer
         * mode if device and change boolean field of play sound */
        imSoundGlob = imSound
        imSoundGlob.setOnClickListener{
            activityMain.playSound = !activityMain.playSound
            checkSound(activityMain)
            vibrate(requireContext())
        }

        /* Update location weather button */
        imUpdateWeather.setOnClickListener{
            getLocation() // get geo and update weather data
            vibrate(requireContext())
        }
    }

    /* check do device geolocation is enabled (not geo permission) */
    private fun isLocationEnabled(): Boolean{
        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /* if device location enabled, then get geolocation and update weather data.
    * else show dialog and ask user for enable device geolocation, then open geo setting
    * activity for result if positive action */
    private fun checkLocationEnabled() {
        if (isLocationEnabled()){
            getLocation()
        } else {
            showLocationDisabledDialog(requireContext())
        }
    }

    /* check location permission allow. check device location enabled. Then get location latitude
    * and longitude with high accuracy and do GET weather query with them. Also show GPS icon */
    fun getLocation(){
        val ct = CancellationTokenSource() // token for processing cancellation when requesting
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.token)
            .addOnCompleteListener{
                try {
                    requestWeatherOrCitiesData("${it.result.latitude},${it.result.longitude}", 'W')
                    imGPSGlob.visibility = View.VISIBLE
                } catch (e: Exception) {
                    showLocationDisabledDialog(requireContext())
                }
            }
    }

    /* When new weather data coming from server (after update btn click, or days item click)
    * view model automatically update main card, change background image and play sound */
    private fun updateCurrentCard() = with(binding){
        model.lifeDataCurrent.observe(viewLifecycleOwner){
            val maxMinText = "${it.maxTemp}°C/${it.maxTemp}°C"
            tvData.text = it.time
            tvCity.text = it.city
            tvCurrentTemp.text = if (it.currentTemp.isNotEmpty()) it.currentTemp + "°C" else maxMinText
            tvCondition.text = it.condition
            tvMaxMin.text = if (it.currentTemp.isEmpty()) "" else maxMinText
            Picasso.get().load("https:" + it.imageUrl).into(imWeather) // download picture from URL
            tabLayout.selectTab(tabLayout.getTabAt(0)) // silent click to hours tab
            /* change the background picture according to condition and play sound */
            with(tvCondition.text.toString().lowercase()) {
                when {
                    contains("snow") || contains("sleet") || contains("blizzard")|| contains("ice")
                    -> {
                        imBackground.setImageResource(R.drawable.bg_snow)
                        activityMain.playSound('S')
                    }
                    contains("rain") || contains("drizzle") || contains("outbreaks")
                    -> {
                        imBackground.setImageResource(R.drawable.bg_rain)
                        activityMain.playSound('R')
                    }
                    contains("sunny") || contains("clear")
                    -> {
                        imBackground.setImageResource(R.drawable.bg_sun)
                        activityMain.playSound('D') // default 'sun' sound
                    }
                    contains("cloudy") || contains("overcast") || contains("mist")|| contains("mist") || contains("fog")
                    -> {
                        imBackground.setImageResource(R.drawable.bg_cloud)
                        activityMain.playSound('C')
                    }
                }
            }
        }

    }

    /* check has app geolocation permission, and start dialog for get allow geo permission */
    private fun checkPermission() {
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    /* start geolocation permission activity and allow this permission or show alert toast */
    private fun permissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            Toast.makeText(activity, "Permission is $it", Toast.LENGTH_SHORT).show()
        }
    }

    /* Do GET request to weather ('W') server or city ('C') server,
    * using the name of city and char symbol. Then parse result in special method,
    * or show error */
    private fun requestWeatherOrCitiesData(city: String, weatherOrCity: Char) {
        var url = ""
        when(weatherOrCity) { // либо погоду либо города
            'W' -> url = WEATHER_START_URL + city + WEATHER_END_URL
            'C' -> url = CITIES_START_URL + city + CITIES_END_URL
        }
        val queue = Volley.newRequestQueue(context) // очередь гет запросов
        val request = StringRequest(
            Request.Method.GET,
            url,
            {
                    result ->
                parseWeatherOrCitiesData(result, weatherOrCity)
            },
            {
                    error ->
                Log.d("MyTag", "Error: $error")
                Toast.makeText(requireContext(), "Can\'t load data! Try again! $error", Toast.LENGTH_LONG).show()
            }
        )
        queue.add(request)
    }

    /* Parse result from server. If weather data ('W'), then parse several days and add them to list.
    * Then use weather of fist day [0] of a list in main card of MainFragment.
    * If city data ('C'), then fill arrayList of find cities and update search adapter of
    * search city window */
    private fun parseWeatherOrCitiesData(result: String, weatherOrCity: Char) {
        val mainObject = JSONObject(result)
        when(weatherOrCity) {
            'W' -> {
                val list = parseDays(mainObject)
                parseCurrentData(mainObject, list[0])
            }
            'C' -> {
                val searchCitiesList = ArrayList<String>()
                for(i in 0..9)
                    try {
                        searchCitiesList.add(mainObject.getJSONObject(i.toString()).getString("english"))
                    } catch (e : Exception) {
                        Log.d("MyTag", "Error add to cities list: $e")
                    }
                    searchAdapter.updateResults(searchCitiesList)
            }
        }
    }

    /* Weather data of several days. Parse JSON data from server. And fill in all fields of
     * WeatherModelItem, which then add to ArrayList<WeatherModel>.
     * Then set lifeDataList value. And automatically observer update
     * weather data of several days in DaysFragment adapter */
    private fun parseDays(mainObject: JSONObject): List<WeatherModel>{
        val list = ArrayList<WeatherModel>()
        val daysArray = mainObject.getJSONObject("forecast").getJSONArray("forecastday")
        val name = mainObject.getJSONObject("location").getString("name")
        for (i in 0 until daysArray.length()){
            val day = daysArray[i] as JSONObject
            val item = WeatherModel(
                name,
                Utils.parseData("yyyy-MM-dd", "dd/MM/yy EEEE", day.getString("date")),
                day.getJSONObject("day").getJSONObject("condition").getString("text"),
                "",
                day.getJSONObject("day").getString("maxtemp_c").toFloat().toInt().toString(), // delete decimal
                day.getJSONObject("day").getString("mintemp_c").toFloat().toInt().toString(),
                day.getJSONObject("day").getJSONObject("condition").getString("icon"),
                day.getJSONArray("hour").toString()
            )
            list.add(item)
        }
        model.lifeDataList.value = list // trigger for observer about new weather data of 3-7 days
        return list
    }

    /* Weather data today. Parse JSON data from server. And fill in all fields of
     * WeatherModelItem. Then set lifeData value. And automatically observer update
     * weather data in main card */
    private fun parseCurrentData(mainObject: JSONObject, weatherItem: WeatherModel) {
        val item = WeatherModel(
            mainObject.getJSONObject("location").getString("name"),
            Utils.parseData("yyyy-MM-dd HH:mm", "dd/MM/yy EEEE",
                mainObject.getJSONObject("current").getString("last_updated")),
            mainObject.getJSONObject("current")
                .getJSONObject("condition").getString("text"),
            mainObject.getJSONObject("current").getString("temp_c"),
            weatherItem.maxTemp,
            weatherItem.minTemp,
            mainObject.getJSONObject("current")
                .getJSONObject("condition").getString("icon"),
            weatherItem.hours
        )
        model.lifeDataCurrent.value = item // trigger for observer about new weather data today
    }

    /* Search city handler. Show/hide search FrameLay with animation, do/not vibrate,
     * clear/not edit text, set/not standart city in search city adapter,
     * hide/show keyboard. */
    fun clickSearchFrameLay(show: Boolean, vibrate: Boolean, clearText: Boolean, standartAdapter: Boolean, hideKey: Boolean) {
        ViewCompat.animate(searchFrameLayGlobField)
            .alpha(if(show) 1f else 0f)
            .translationY(if(show) -50f else 50f)
            .setDuration(100)
            .setListener(object : ViewPropertyAnimatorListener {
                override fun onAnimationCancel(view: View) {}

                override fun onAnimationStart(view: View) {
                    if(show) searchFrameLayGlobField.visibility = View.VISIBLE
                }
                override fun onAnimationEnd(view: View) {
                    if(!show) searchFrameLayGlobField.visibility = View.GONE
                }
            })
        if(vibrate) vibrate(requireContext())
        if(clearText) etSearchCity.text.clear()
        if(standartAdapter)
            searchAdapter.updateResults(ArrayList(listOf(*resources.getStringArray(R.array.city_names))))
        if(hideKey) hideKeyboard()
    }

    /* change image to soundOn or soundOff */
    fun checkSound(activity: MainActivity) {
        if(activity.playSound) imSoundGlob.setImageResource(R.drawable.ic_sound_on)
        else imSoundGlob.setImageResource(R.drawable.ic_sound_off)
    }

    /* click to city item in search FrameLay via interface. Do GET weather
    * for this city and update data. Also hide GPS icon */
    override fun onClick(item: String) {
        requestWeatherOrCitiesData(item, 'W')
        clickSearchFrameLay(show = false, vibrate = true, clearText = true, standartAdapter = true, hideKey = true)
        imGPSGlob.visibility = View.GONE
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}