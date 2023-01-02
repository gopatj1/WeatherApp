package com.example.weatherapp.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.MainActivity
import com.example.weatherapp.MainViewModel
import com.example.weatherapp.SwipeInterface
import com.example.weatherapp.adapters.WeatherAdapter
import com.example.weatherapp.adapters.WeatherModel
import com.example.weatherapp.databinding.FragmentDaysBinding

/**
 * DaysFragment placed in 2 tab of tabLay in MainFragment and show items in
 * recyclerViewAdapter with weather data of every day of a week.
 * Model automatically handle new parsed data from server, and
 * put it weather to adapter. Items click is update weather in main card of
 * MainActivity and items in HoursFragment via interface. Use swipeInterface for
 * update weather data after vertical swipe
 */

class DaysFragment : Fragment(), WeatherAdapter.Listener {
    private lateinit var adapter: WeatherAdapter
    private lateinit var binding: FragmentDaysBinding
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
        binding = FragmentDaysBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
    }

    private fun initRcView() = with(binding){
        recViewDays.layoutManager = LinearLayoutManager(activity) // default horizontal layout
        /* init adapter. Set listener this@DaysFragment, when click to
         * day item, and then update current day weather in main card
         and update adapter of Hours fragment */
        adapter = WeatherAdapter(this@DaysFragment)
        recViewDays.adapter = adapter
        /* model automatically update weather data and adapter, when new data
         * coming from server after click of vertical swipe */
        model.lifeDataList.observe(viewLifecycleOwner) {
            tvEmptyDays.visibility = View.GONE
            progressBar.visibility = View.GONE
            adapter.submitList(it) // items - every day of a week
        }
        /* swipe interface listener. Call in MainActivity fun getLocation() from MainFragment
        * update weather data. Stop twisting. And vibrate vertical swipe*/
        swipeRefreshLayout.setOnRefreshListener {
            swipeInterface.onSwipe()
            swipeRefreshLayout.isRefreshing = false // stop twist bar
            vibrate(requireContext())
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = DaysFragment()
    }

    // клик на айтем для из списка через интерфейс
    override fun onClick(item: WeatherModel) {
        model.lifeDataCurrent.value = item // передаем нажатый айтем
        vibrate(requireContext())
    }

    /* Create instance of interface (set context) for update weather after vertical swipe */
    private fun instantiateSwipeInterface(context: Context) {
        swipeInterface = context as MainActivity
    }
}