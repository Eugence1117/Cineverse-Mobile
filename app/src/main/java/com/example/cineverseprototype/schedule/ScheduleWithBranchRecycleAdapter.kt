package com.example.cineverseprototype.schedule

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.cineverseprototype.ExpandCollapseAnimation
import com.example.cineverseprototype.R
import com.example.cineverseprototype.movie.Movie
import kotlin.collections.ArrayList

class ScheduleWithBranchRecycleAdapter(val branchList:MutableList<String>, val scheduleList:Map<String,ArrayList<Schedule>>, val movie: Movie): RecyclerView.Adapter<ScheduleWithBranchRecycleAdapter.ScheduleViewHolder>() {

    class ScheduleViewHolder(view: View):RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cinema_schedule_group_item,parent,false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.branchName).text = branchList[position]

        val schedules = scheduleList[branchList[position]]
        if(schedules!=null){
            val adapter = ScheduleRecycleAdapter(schedules,movie)
            val recyclerView = holder.itemView.findViewById<RecyclerView>(R.id.scheduleList)
            recyclerView.adapter = adapter


            holder.itemView.findViewById<ConstraintLayout>(R.id.headerLayout).setOnClickListener {
                if(recyclerView.visibility == View.VISIBLE){
                    holder.itemView.findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.baseline_keyboard_arrow_down_grey_400_24dp)
                    recyclerView.visibility = View.GONE
                }
                else{
                    recyclerView.visibility = View.VISIBLE
                    holder.itemView.findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.baseline_keyboard_arrow_up_grey_400_24dp)
                }

            }
        }
    }

    override fun getItemCount(): Int {
        return branchList.size
    }
}