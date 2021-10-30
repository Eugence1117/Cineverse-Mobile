package com.example.cineverseprototype.payment

import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class Payment(
    val paymentId:String,
    val paymentStatus:String,
    val paymentType:String,
    val totalPrice:Double,
    val voucherId:String,
    val createDate: Date,
    val lastUpdate:Date,
    val paidOn:Date,
    val movieName:String,
    val scheduleStartTime:Date,
    val branchName:String,
    val theatreName:String,
    val theatreType:String,
    val seatList:String

) {

    fun getBranchInfo():String{
        return "$branchName - Hall $theatreName"
    }

    fun getSeatInfo():String{
        return "$theatreType Seat - $seatList"
    }

    fun getScheduleDate():String{
        val format = SimpleDateFormat("dd/MM/yyyy")
        return format.format(scheduleStartTime)
    }

    fun getScheduleTime():String{
        val format = SimpleDateFormat("hh:mm a")
        return format.format(scheduleStartTime).toUpperCase()
    }

    fun getStatusColor():String{
        return when(paymentStatus){
            "Pending" -> {"#ffc107"}
            "Paid" -> {"#198754"}
            "Completed" -> {"#198754"}
            "Pending Refund" -> {"#ffc107"}
            "Refunded" -> {"#198754"}
            "Cancelled" -> {"#fd7e14"}
            else -> { "#adb5bd"}
        }
    }

    companion object{
        fun toObject(obj: JSONObject): Payment? {
            try{
                val paymentId:String = obj.getString("paymentId")
                val paymentStatus:String = obj.getString("paymentStatus")
                val paymentType:String = obj.getString("paymentType")
                val totalPrice:Double = obj.getDouble("totalPrice")
                val voucherId:String = obj.getString("voucherId")
                val createDate:Long = obj.getLong("createDate")
                val lastUpdate:Long = obj.getLong("lastUpdate")
                val paidOn:Long = obj.getLong("paidOn")
                val movieName:String = obj.getString("movieName")
                val scheduleStartTime:Long = obj.getLong("scheduleStartTime")
                val branchName:String = obj.getString("branchName")
                val theatreName:String = obj.getString("theatreName")
                val theatreType:String = obj.getString("theatreType")
                val seatList:String = obj.getString("seatList")

                return Payment(paymentId, paymentStatus, paymentType, totalPrice, voucherId, Date(createDate), Date(lastUpdate), Date(paidOn), movieName, Date(scheduleStartTime), branchName, theatreName, theatreType, seatList)
            }
            catch(ex: JSONException){
                Log.e("Payment",ex.stackTraceToString())
                return null
            }
        }
    }



}