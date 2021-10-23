package com.example.cineverseprototype.schedule

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.color
import androidx.recyclerview.widget.RecyclerView
import com.example.cineverseprototype.R
import com.example.cineverseprototype.movie.MovieRecycleAdapter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ScheduleRecycleAdapter(val scheduleList:ArrayList<Schedule>): RecyclerView.Adapter<ScheduleRecycleAdapter.ScheduleViewHolder>() {

    class ScheduleViewHolder(view: View):RecyclerView.ViewHolder(view){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.schedule_item,parent,false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val format = SimpleDateFormat("hh:mm a")
        holder.itemView.findViewById<TextView>(R.id.scheduleTime).text = format.format(Date(scheduleList[position].startTime))
        holder.itemView.findViewById<TextView>(R.id.theatreType).text = "${scheduleList[position].type} Theatre"
        holder.itemView.findViewById<ImageView>(R.id.theatreSeat).setImageResource(scheduleList[position].getTheatreIcon())
        val seatInfo = SpannableStringBuilder().color(
            Color.parseColor(scheduleList[position].capacity.getColor()))
            { append(scheduleList[position].capacity.seatOccupied.toString()) }
            .append("/${scheduleList[position].capacity.totalSeat}")

        holder.itemView.findViewById<TextView>(R.id.capacity).text = seatInfo
    }

    override fun getItemCount(): Int {
        return scheduleList.size
    }
}