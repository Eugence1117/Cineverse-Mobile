package com.example.cineverseprototype.payment

import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.util.*

class Voucher(
    private val voucherCode:String,
    private val minSpend:Double,
    private val reward:Double,
    private val quantity:Int,
    private val calculateUnit:Int
) {
    override fun toString(): String {
        return voucherCode
    }

    companion object{
        fun toObject(obj: JSONObject):Voucher?{
            try{
                val voucherCode = obj.getString("voucherCode")
                val minSpend = obj.getDouble("minSpend")
                val reward = obj.getDouble("reward")
                val quantity = obj.getInt("quantity")
                val calculateUnit = obj.getInt("calculateUnit")

                return Voucher(voucherCode, minSpend, reward, quantity, calculateUnit)
            }
            catch (ex: JSONException){
                Log.i(javaClass.name,ex.stackTraceToString())
                return null
            }
        }
    }
}
