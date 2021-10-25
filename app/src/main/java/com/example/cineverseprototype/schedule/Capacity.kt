package com.example.cineverseprototype.schedule

import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

class Capacity(val seatOccupied:Int, val totalSeat:Int) : Serializable{

    fun getColor():String{
        val percentage = ((seatOccupied / totalSeat ) * 100).toInt()
        return if(percentage < 50){
            "#198754"
        }
        else if (percentage in 50..75){
            "#ffc107"
        }
        else if (percentage in 76..100){
            "#fd7e14"
        }
        else{
            "#adb5bd"
        }
    }

    override fun toString(): String {
        return "$seatOccupied/$totalSeat"
    }

    companion object{
        private val TAG = javaClass.name

        fun toObject(obj:JSONObject):Capacity?{
            try{
                val seatOccupied = obj.getInt("seatOccupied")
                val totalSeat = obj.getInt("totalSeat")

                return Capacity(seatOccupied,totalSeat)
            }
            catch (ex: JSONException){
                Log.e(TAG,ex.stackTraceToString())
                return null
            }
        }
    }
}