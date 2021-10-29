package com.example.cineverseprototype.payment

import android.databinding.tool.ext.capitalizeUS
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class PaymentDetails(
    val branchName:String,
    val theatreName:String,
    val theatreType:String,
    val startTime:Date,
    val movieName:String,
    val totalPrice:Double,
    val tax:Double,
    val amountDiscount:Double,
    val discountedTotalPrice:Double

) {
    fun getTheatreInfo():String{
        return "Hall $theatreName - $theatreType"
    }

    fun getScheduleTime():String{
        val format = SimpleDateFormat("hh:mm aa")
        return format.format(startTime)
    }

    fun getScheduleDate():String{
        val format = SimpleDateFormat("dd MMMM yyyy")
        return format.format(startTime)
    }

    fun getPriceDetails():PriceDetails{
        return PriceDetails(totalPrice,tax,amountDiscount,discountedTotalPrice)
    }

    companion object{
        fun toObject(obj:JSONObject):PaymentDetails?{
            try{
                val branchName:String = obj.getString("branchName")
                val theatreName:String = obj.getString("theatreName")
                val theatreType:String = obj.getString("theatreType")
                val startTime:Long = obj.getLong("startTime")
                val movieName:String = obj.getString("movieName")
                val totalPrice:Double = obj.getDouble("totalPrice")
                val tax:Double = obj.getDouble("tax")
                val amountDiscount:Double = obj.getDouble("amountDiscounted")
                val discountedTotalPrice:Double = obj.getDouble("discountedTotalPrice")

                return PaymentDetails(branchName, theatreName, theatreType, Date(startTime), movieName, totalPrice, tax, amountDiscount, discountedTotalPrice)
            }
            catch (ex:ParseException){
                Log.i(javaClass.name,ex.stackTraceToString())
                return null
            }
            catch (ex: JSONException){
                Log.i(javaClass.name,ex.stackTraceToString())
                return null
            }
        }
    }
}