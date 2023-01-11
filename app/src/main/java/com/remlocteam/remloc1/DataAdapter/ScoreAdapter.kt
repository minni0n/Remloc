package com.remlocteam.remloc1.DataAdapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.remlocteam.remloc1.Data.ActionsData
import com.remlocteam.remloc1.Data.ScoreData
import com.remlocteam.remloc1.R

class ScoreAdapter(private val context: Activity, private val arrayList: ArrayList<ScoreData>,) : ArrayAdapter<ScoreData>(context, R.layout.place_score, arrayList) {

    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.place_score, null)

        val placeName: TextView = view.findViewById(R.id.scorePlaceName)
        val placeScore: TextView = view.findViewById(R.id.scorePlaceScore)


        placeName.text = arrayList[position].placeName
        placeScore.text = arrayList[position].placeScore.toString()


        return view
    }
}