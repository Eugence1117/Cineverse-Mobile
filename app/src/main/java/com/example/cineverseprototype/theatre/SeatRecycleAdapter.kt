package com.example.cineverseprototype.theatre

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cineverseprototype.R

class SeatRecycleAdapter(private val seatList:ArrayList<Any>) : RecyclerView.Adapter<SeatRecycleAdapter.SeatViewHolder>() {

    private val SEAT_VIEW_TYPE = 1
    private val LABEL_VIEW_TYPE = 2
    private val BLANK_VIEW = 3
    private val SEAT_BEANIE_TYPE = 4

    class SeatViewHolder(view: View) : RecyclerView.ViewHolder(view){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewHolder {
        return when(viewType){
            SEAT_BEANIE_TYPE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.beanie_seat_item,parent,false)
                SeatViewHolder(view)
            }
            SEAT_VIEW_TYPE-> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.seat_item,parent,false)
                SeatViewHolder(view)
            }
            LABEL_VIEW_TYPE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.label_item,parent,false)
                SeatViewHolder(view)
            }
            BLANK_VIEW -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.empty_item,parent,false)
                SeatViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.empty_item,parent,false)
                SeatViewHolder(view)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = seatList[position]
        if(item is SeatLayout.SeatCol){
            if(item.reference.isNullOrEmpty()){
                return SEAT_VIEW_TYPE
            }
            else{
                return SEAT_BEANIE_TYPE
            }
        }
        else{
            if((seatList[position] as SeatLabel).isEmpty){
                return BLANK_VIEW
            }
            else{
                return LABEL_VIEW_TYPE
            }
        }
    }
    override fun onBindViewHolder(holder: SeatViewHolder, position: Int) {
        if(holder.itemViewType == SEAT_VIEW_TYPE){
            val seat = seatList[position] as SeatLayout.SeatCol
            if(seat.isSelected){
                holder.itemView.isEnabled = false
                (holder.itemView as CheckBox).isChecked = true
            }
            else{
                holder.itemView.tag = seatList[position]
            }
        }
        else if (holder.itemViewType == LABEL_VIEW_TYPE){
            val label = seatList[position] as SeatLabel
            (holder.itemView as TextView).text = label.label
        }
    }

    override fun getItemCount(): Int {
        return seatList.size
    }
}