package com.example.cineverseprototype.branch

import android.util.Log
import org.json.JSONException
import org.json.JSONObject

class Branch(val branchId:String, val branchName:String, val address:String, val postcode:String, val district:String, val state:String, val startTime:String,val endTime:String) {

    fun toBusinessHour():String{
        return "${startTime.toUpperCase()} to ${endTime.toUpperCase()}"
    }
    fun toFullAddress():String{
        return "$address\n$postcode $district"
    }

    companion object{
        fun toObject(obj: JSONObject): Branch? {
            try{
                val branchId = obj.getString("branchId")
                val branchName = obj.getString("branchName")
                val address = obj.getString("address")
                val postcode = obj.getString("postCode")
                val district = obj.getString("districtName")
                val state = obj.getString("stateName")
                val startTime = obj.getString("startTime")
                val endTime = obj.getString("endTime")

                return Branch(branchId,branchName,address,postcode,district,state,startTime,endTime)
            }
            catch(ex: JSONException){
                Log.e("Member",ex.stackTraceToString())
                return null
            }
        }

        fun toObject(obj:Map<String,String>):Branch?{
            try{
                val branchId = obj["branchId"].toString()
                val branchName = obj["branchName"].toString()
                val address = obj["address"].toString()
                val postcode = obj["postCode"].toString()
                val district = obj["districtName"].toString()
                val state = obj["stateName"].toString()
                val startTime = obj["startTime"].toString()
                val endTime = obj["endTime"].toString()

                return Branch(branchId,branchName,address,postcode,district,state,startTime,endTime)
            }
            catch(ex: JSONException){
                Log.e("Member",ex.stackTraceToString())
                return null
            }
        }
    }
}