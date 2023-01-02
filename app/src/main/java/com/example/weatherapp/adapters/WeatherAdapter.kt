package com.example.weatherapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ListItemBinding
import com.squareup.picasso.Picasso

/**
 * The RecyclerView adapter accepts elements of the WeatherModel and the WeatherAdapter.Holder class.
 * The Holder class prints this elements in adapter like items (connect layout and data).
 * Comparator class compares the old item with the new item when updating data from the server.
 * If items if different redraws it onBindViewHolder(). Else does not change. Thus saving memory
 * because don't redraw every items.
 * In constructor set listener: Listener? which handle click on item and update weather in main card
 * if listener not null (DaysFragment) and to do nothing if null (HoursFragment)
 */

class WeatherAdapter(private val listener: Listener?) : ListAdapter<WeatherModel, WeatherAdapter.Holder>(Comparator()) {

    class Holder(view: View, private val listener: Listener?) : RecyclerView.ViewHolder(view){
        private val binding = ListItemBinding.bind(view)
        private var itemTemp: WeatherModel? = null // use for define clicked item
        init {
            // handle item click if not null (DaysFragment) and to do nothing if null (HoursFragment)
            itemView.setOnClickListener{
                itemTemp?.let { it1 -> listener?.onClick(it1) }
            }
        }

        fun bind(itemWeather: WeatherModel) = with(binding){
            itemTemp = itemWeather // itemView with all data and layout if clicked
            tvDataItem.text = itemWeather.time
            tvConditionItem.text = itemWeather.condition
            // the weather of today or the min/max of another day selected in the DaysFragment adapter
            tvTempItem.text = if (itemWeather.currentTemp.isNotEmpty()) {
                itemWeather.currentTemp + "°C"
            } else {
                "${itemWeather.maxTemp}°C/${itemWeather.maxTemp}°C"
            }
            Picasso.get().load("https:" + itemWeather.imageUrl).into(imWeatherDayHour) // download icon
        }
    }

    class Comparator : DiffUtil.ItemCallback<WeatherModel>(){
        override fun areItemsTheSame(oldItem: WeatherModel, newItem: WeatherModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: WeatherModel, newItem: WeatherModel): Boolean {
            return oldItem == newItem
        }

    }

    /* create in memory new item. Call the same amount items count */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return Holder(view, listener) // возвращаем инициализированный холдер с связанными данными и указанным листенером
    }

    /* draw every item of adapter. Call the same amount items count */
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    /* click in item if DaysFragment. If HoursFragment not clickable - null */
    interface Listener{
        fun onClick(item: WeatherModel)
    }
}