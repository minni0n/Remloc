package com.remlocteam.remloc1.DataAdapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.remlocteam.remloc1.Data.PlacesData
import com.remlocteam.remloc1.R


class PlaceAdapter(private val context: Activity, private val arrayList: ArrayList<PlacesData>) : ArrayAdapter<PlacesData>(context, R.layout.place_item, arrayList){

    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.place_item, null)

        val placeName: TextView = view.findViewById(R.id.placeNameItem)
        val address: TextView = view.findViewById(R.id.addressItem)
//        val settingsBtn: ImageButton = view.findViewById(R.id.settingsPlaceBtn)

        placeName.text = arrayList[position].placeName
        address.text = arrayList[position].addressLine

        return view
    }
}