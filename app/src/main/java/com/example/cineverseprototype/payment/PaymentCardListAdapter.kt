package com.example.cineverseprototype.payment

import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.cineverseprototype.R

class PaymentCardListAdapter(context: Context, private val paymentList:ArrayList<Payment>) : ArrayAdapter<Payment>(context, R.layout.payment_card,paymentList){

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if(view == null){
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = layoutInflater.inflate(R.layout.payment_card,null)
        }

        val payment = paymentList[position]!!

        view!!.findViewById<TextView>(R.id.movieName).text = payment.movieName
        view!!.findViewById<TextView>(R.id.branchName).text = payment.getBranchInfo()
        view!!.findViewById<TextView>(R.id.paymentStatus).setTextColor(Color.parseColor(payment.getStatusColor()))
        view!!.findViewById<TextView>(R.id.paymentStatus).text = payment.paymentStatus
        view!!.findViewById<TextView>(R.id.scheduleDate).text = payment.getScheduleDate()
        view!!.findViewById<TextView>(R.id.scheduleTime).text = payment.getScheduleTime()

        return view!!

    }
}