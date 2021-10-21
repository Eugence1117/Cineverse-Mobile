package com.example.cineverseprototype.member

import com.android.tools.build.jetifier.core.utils.Log
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class Member(
    val name:String,
    val username:String,
    val dateOfBirth:Date,
    val status:Int,
    val email:String,
    val createDate: Date,
    val picUrl:String?) {

    companion object{
        fun toObject(obj:JSONObject):Member?{
            return try{
                val name = obj.getString("name")
                val username = obj.getString("username")
                val dateOfBirth = obj.getLong("dateOfBirth")
                val status = obj.getInt("status")
                val email = obj.getString("email")
                val createDate = obj.getLong("createdDate")
                val picUrl = if(obj.isNull("picUrl")) null else obj.getString("picUrl")

                Member(name,username,Date(dateOfBirth),status,email,Date(createDate),picUrl)
            } catch(ex:JSONException){
                Log.e("Member",ex.stackTraceToString())
                null
            }
        }
    }

}