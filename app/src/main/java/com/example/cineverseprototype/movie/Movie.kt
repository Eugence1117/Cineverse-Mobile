package com.example.cineverseprototype.movie

import android.util.Log
import org.json.JSONException
import org.json.JSONObject

class Movie(
    val movieId: String,
    val movieName: String,
    val picURL: String,
    val totalTime:Int,
    val language: String,
    val distributor: String,
    val cast: String,
    val director: String,
    val synopsis: String,
    val movieType: String,
    val censorship: String,
    val releaseDate: Long
)
{
    fun getTotalTime():String{
        val hour = (totalTime / 60).toInt()
        val minutes = (totalTime % 60).toInt()
        return "$hour hour(s) $minutes minute(s)"
    }

    companion object{

        private val TAG = javaClass.name
        fun toObject(obj: JSONObject):Movie?{
            try{
                val movieId = obj.getString("movieId")
                val movieName = obj.getString("movieName")
                val picURL = obj.getString("picURL")
                val totalTime = obj.getInt("totalTime")
                val language = obj.getString("language")
                val distributor = obj.getString("distributor")
                val cast = obj.getString("cast")
                val director = obj.getString("director")
                val synopsis = obj.getString("synopsis")
                val movieType = obj.getString("movieType")
                val censorship = obj.getString("censorship")
                val releaseDate = obj.getLong("releaseDate")

                return Movie(movieId,movieName,picURL,totalTime,language,distributor,cast,director,synopsis,movieType,censorship,releaseDate)
            }
            catch (ex:JSONException){
                Log.e(TAG,ex.stackTraceToString())
                return null
            }
        }
    }
}