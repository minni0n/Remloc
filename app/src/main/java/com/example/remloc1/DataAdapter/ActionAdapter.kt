package com.example.remloc1.DataAdapter

import android.annotation.SuppressLint
import android.app.Activity
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.remloc1.Data.ActionsData
import com.example.remloc1.Data.PlacesData
import com.example.remloc1.R


class ActionAdapter(private val context: Activity, private val arrayList: ArrayList<ActionsData>,) : ArrayAdapter<ActionsData>(context, R.layout.action_item, arrayList){

    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.action_item, null)

        val placeName: TextView = view.findViewById(R.id.placeName2)
        val actionType: TextView = view.findViewById(R.id.actionName)


        placeName.text = arrayList[position].placeName
        actionType.text = arrayList[position].actionType


        return view
    }
}