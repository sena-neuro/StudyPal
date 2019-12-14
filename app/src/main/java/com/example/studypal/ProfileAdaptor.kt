package com.example.studypal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ProfileAdaptor(private val sessionHistory: ArrayList<SessionData>):
    RecyclerView.Adapter<ProfileAdaptor.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
        val date = Date(sessionHistory[position].sessionDate)
        holder.sessionDate.text = format.format(date)
        holder.sessionLength.text = "Total Minutes in Session: %d".format(sessionHistory[position].totalSessionDurationMins)
        holder.sessionScore.text = "Session score: %f".format(sessionHistory[position].sessionScore)
    }

    override fun getItemCount(): Int {
        return sessionHistory.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var sessionDate: TextView = itemView.findViewById<View>(R.id.sessionDate) as TextView
        internal var sessionLength:TextView = itemView.findViewById<View>(R.id.sessionLengthText) as TextView
        internal var sessionScore:TextView = itemView.findViewById<View>(R.id.sessionScoreText) as TextView

    }
}