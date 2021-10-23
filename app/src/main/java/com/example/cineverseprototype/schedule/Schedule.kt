package com.example.cineverseprototype.schedule

import android.util.Log
import com.example.cineverseprototype.R
import org.json.JSONException
import org.json.JSONObject

class Schedule(val scheduleId:String, val startTime:Long, val endTime:Long, val type:String, val branchName:String, val capacity: Capacity) {

    fun getTheatreColor():String{
        return if(type == "Beanie"){
            "#de5c9d"
        }
        else{
           "#0d6efd"
        }
    }

    fun getTheatreIcon():Int{
        return if(type == "Beanie"){
            R.drawable.baseline_chair_beanie_24dp
        }
        else{
            R.drawable.baseline_chair_single_24dp
        }
    }

    companion object{
        private val TAG = javaClass.name

        fun toObject(obj: JSONObject): Schedule?{
            try{
                val scheduleId = obj.getString("scheduleId")
                val startTime = obj.getLong("startTime")
                val endTime = obj.getLong("endTime")
                val type = obj.getString("theatreType")
                val branchName = obj.getString("branchName")
                val capacity = Capacity.toObject(obj.getJSONObject("capacity"))

                return if(capacity == null){
                    null
                } else{
                    Schedule(scheduleId,startTime,endTime,type,branchName,capacity)
                }
            }
            catch (ex: JSONException){
                Log.e(TAG,ex.stackTraceToString())
                return null
            }
        }
    }
}