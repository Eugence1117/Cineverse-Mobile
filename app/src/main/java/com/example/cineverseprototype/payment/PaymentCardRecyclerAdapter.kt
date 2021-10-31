package com.example.cineverseprototype.payment

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cineverseprototype.ClickListener
import com.example.cineverseprototype.R

class PaymentCardRecyclerAdapter(val paymentList:ArrayList<Payment>,val listener:ClickListener):RecyclerView.Adapter<PaymentCardRecyclerAdapter.PaymentViewHolder>() {


    class PaymentViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.payment_card,parent,false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val payment = paymentList[position]

        holder.itemView.findViewById<TextView>(R.id.movieName).text = payment.movieName
        holder.itemView.findViewById<TextView>(R.id.branchName).text = payment.getBranchInfo()
        holder.itemView.findViewById<TextView>(R.id.paymentStatus).setTextColor(Color.parseColor(payment.getStatusColor()))
        holder.itemView.findViewById<TextView>(R.id.paymentStatus).text = payment.paymentStatus
        holder.itemView.findViewById<TextView>(R.id.scheduleDate).text = payment.getScheduleDate()
        holder.itemView.findViewById<TextView>(R.id.scheduleTime).text = payment.getScheduleTime()

        holder.itemView.setOnClickListener {
            listener.onItemClick(position,holder.itemView)
        }
    }

    override fun getItemCount(): Int {
        return paymentList.size
    }
}