package com.example.cineverseprototype.schedule

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.cineverseprototype.R
import java.text.SimpleDateFormat
import java.util.*

class DateRecycleAdapter(private val dateList:ArrayList<Date>, private val listener: ClickListener): RecyclerView.Adapter<DateRecycleAdapter.ScheduleViewHolder>()  {

    private var checkedView:TextView? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int,
    ): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.date_item,parent,false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val format = SimpleDateFormat("dd-MMM")
        val date = dateList[position]!!
        holder.itemView.findViewById<TextView>(R.id.dateItem).text = format.format(date)

        if(position == 0 && checkedView == null){
            checkedView = holder.itemView.findViewById<TextView>(R.id.dateItem)
            styleCheckItem()
        }

        holder.itemView.findViewById<TextView>(R.id.dateItem).setOnClickListener {
            styleUncheckItem()
            checkedView = holder.itemView.findViewById<TextView>(R.id.dateItem)
            styleCheckItem()
            listener.onItemClick(position,holder.itemView)
        }
    }

    fun styleCheckItem(){
        if(checkedView != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                checkedView!!.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(checkedView!!.context,R.color.secondaryColor))
            }
            checkedView!!.setTextColor(ContextCompat.getColor(checkedView!!.context,R.color.secondaryTextColor))
        }
    }

    fun styleUncheckItem(){
        if(checkedView != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                checkedView!!.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(checkedView!!.context,R.color.primaryDarkColor))
            }
            checkedView!!.setTextColor(ContextCompat.getColor(checkedView!!.context,R.color.primaryTextColor))
        }
    }

    override fun getItemCount(): Int {
        return dateList.size
    }

    class ScheduleViewHolder(view: View):RecyclerView.ViewHolder(view){
    }

    interface ClickListener {
        fun onItemClick(position: Int, v: View?)
    }
}