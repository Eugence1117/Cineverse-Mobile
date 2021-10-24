package com.example.cineverseprototype.schedule

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cineverseprototype.R
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ScheduleWithBranchRecycleAdapter(val branchList:MutableList<String>, val scheduleList:Map<String,ArrayList<Schedule>>): RecyclerView.Adapter<ScheduleWithBranchRecycleAdapter.ScheduleViewHolder>() {

    class ScheduleViewHolder(view: View):RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cinema_schedule_group_item,parent,false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.branchName).text = branchList[position]

        val schedules = scheduleList[branchList[position]]
        if(schedules!=null){
            val adapter = ScheduleRecycleAdapter(schedules)
            val recyclerView = holder.itemView.findViewById<RecyclerView>(R.id.scheduleList)
            recyclerView.adapter = adapter

            holder.itemView.setOnClickListener {
                if(recyclerView.visibility == View.VISIBLE){
                    recyclerView.visibility = View.GONE
                }
                else{
                    recyclerView.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return branchList.size
    }
}