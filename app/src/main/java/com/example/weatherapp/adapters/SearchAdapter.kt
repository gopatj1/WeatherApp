package com.example.weatherapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.weatherapp.R

/**
 * Adapter of searchCity listView. Show items of searched city with city name
 * from editText after GET to city server. Override standart methods. Init class Holder.
 * Handle click to item and update weather data for this city through interface
 */

class SearchAdapter(
    context: Context?,
    private val listener: ListenerCitySearch?,
    allCityArrayList: ArrayList<String>
) : BaseAdapter() {

    private var allCity: ArrayList<String>?
    private var inflater: LayoutInflater

    init {
        allCity = allCityArrayList
        inflater = LayoutInflater.from(context)
    }

    fun updateResults(results: ArrayList<String>) {
        allCity = results
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return allCity!!.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private class ListRowHolder(row: View?) {
        val llContainer: LinearLayout
        val tvCityName: TextView

        init {
            this.llContainer = row?.findViewById(R.id.llContainer) as LinearLayout
            this.tvCityName = row.findViewById(R.id.tvCityName) as TextView
        }
    }

    /* inflate every item. Set simple itemHolder, city name and set listener
     * of item click which updated weather data for this city through interface */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val view: View?
        val vh: ListRowHolder
        if (convertView == null) {
            view = this.inflater.inflate(R.layout.city_list_item, parent, false)
            vh = ListRowHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as ListRowHolder
        }
        vh.tvCityName.text = allCity!![position]
        vh.llContainer.setOnClickListener {
            listener?.onClick(allCity!![position])
        }
        return view
    }

    interface ListenerCitySearch{
        fun onClick(item: String)
    }
}