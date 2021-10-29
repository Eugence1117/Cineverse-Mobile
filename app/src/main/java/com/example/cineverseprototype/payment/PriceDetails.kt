package com.example.cineverseprototype.payment

import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.util.*

class PriceDetails(
    val totalPrice:Double,
    val tax:Double,
    val amountDiscount:Double,
    val discountedTotalPrice:Double) {

    companion object{
        fun toObject(obj: JSONObject):PriceDetails?{
            try{
                val totalPrice:Double = obj.getDouble("totalPrice")
                val tax:Double = obj.getDouble("tax")
                val amountDiscount:Double = obj.getDouble("amountDiscounted")
                val discountedTotalPrice:Double = obj.getDouble("discountedTotalPrice")

                return PriceDetails(totalPrice, tax, amountDiscount, discountedTotalPrice)
            }
            catch (ex: ParseException){
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