package com.example.weatherapp.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.MainActivity
import com.example.weatherapp.MainViewModel
import com.example.weatherapp.SwipeInterface
import com.example.weatherapp.utils.Utils
import com.example.weatherapp.adapters.WeatherAdapter
import com.example.weatherapp.adapters.WeatherModel
import com.example.weatherapp.databinding.FragmentHoursBinding
import org.json.JSONArray
import org.json.JSONObject

/**
 * HoursFragment placed in 1 tab of tabLay in MainFragment and show items in
 * recyclerViewAdapter with weather data of every hour of a day.
 * Model automatically handle new data from server, parse hoursJSONArray and
 * put it weather to adapter. Items not clickable. Use swipeInterface for
 * update weather data after vertical swipe
 */

class HoursFragment : Fragment() {
    private lateinit var adapter: WeatherAdapter
    private lateinit var binding: FragmentHoursBinding
    private val model: MainViewModel by activityViewModels() // for automatically updating data
    private lateinit var swipeInterface: SwipeInterface // instance of interface of vertical swipe

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // connect MainActivity and Fragment fro create instance or swipeInterface
        activity?.let {
            instantiateSwipeInterface(it)
        }
        binding = FragmentHoursBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
    }

    private fun initRcView() = with(binding){
        recViewHours.layoutManager = LinearLayoutManager(activity) // default horizontal layout
        adapter = WeatherAdapter(null) // null - don't listen click on hour item
        recViewHours.adapter = adapter
        //ViewCompat.animate(imTwistBar).rotation(10000000f)
        /* model automatically update weather data and adapter, when new data
         * coming from server after click of vertical swipe */
        model.lifeDataCurrent.observe(viewLifecycleOwner) {
            tvEmptyHours.visibility = View.GONE
            progressBar.visibility = View.GONE
            adapter.submitList(getHoursList(it)) // items - every hour of a day data
        }
        /* swipe interface listener. Call in MainActivity fun getLocation() from MainFragment
        * update weather data. Stop twisting. And vibrate vertical swipe*/
        swipeRefreshLayout.setOnRefreshListener {
            swipeInterface.onSwipe()
            swipeRefreshLayout.isRefreshing = false // stop twist bar
            vibrate(requireContext())
        }
    }

    /* parse hoursJSONArray and get from them data about every hour of a day
     * put to ArrayList<WeatherModel> and set it data to adapter */
    private fun getHoursList(wItem: WeatherModel): List<WeatherModel>{
        val hoursArray = JSONArray(wItem.hours)
        val list = ArrayList<WeatherModel>()
        for (i in 0 until hoursArray.length()){
            val item = WeatherModel(
                wItem.city,
                Utils.parseData("yyyy-MM-dd HH:mm", "HH:mm a",
                    (hoursArray[i] as JSONObject).getString("time")),
                (hoursArray[i] as JSONObject)
                    .getJSONObject("condition").getString("text"),
                (hoursArray[i] as JSONObject).getString("temp_c"),
                "",
                "",
                (hoursArray[i] as JSONObject)
                    .getJSONObject("condition").getString("icon"),
                ""
            )
            list.add(item)
        }
        return list
    }

    companion object {
        @JvmStatic
        fun newInstance() = HoursFragment()
    }

    /* Create instance of interface (set context) for update weather after vertical swipe */
    private fun instantiateSwipeInterface(context: Context) {
        swipeInterface = context as MainActivity
    }
}