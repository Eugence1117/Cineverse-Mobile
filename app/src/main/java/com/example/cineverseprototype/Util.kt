package com.example.cineverseprototype

import android.content.Context
import android.util.Log
import com.android.volley.VolleyError
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset


class Util {
    companion object{
        fun parseVolleyError(error: VolleyError) {
            try {
                val responseBody = String(error.networkResponse.data, Charset.forName("utf-8"))
                val data = JSONObject(responseBody)
                val errors = data.getJSONArray("errors")
                val jsonMessage = errors.getJSONObject(0)
                val message = jsonMessage.getString("message")
                Log.e("Volley Error",message)
            } catch (e: JSONException) {
            } catch (error: UnsupportedEncodingException) {
            }
        }
    }
}