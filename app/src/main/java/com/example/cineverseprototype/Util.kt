package com.example.cineverseprototype

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.android.volley.VolleyError
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset


class Util {
    companion object{
        private val TAG = javaClass.name

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

        fun getFileExtentsion(uri: Uri, contentResolver:ContentResolver):String?{
            val mime = MimeTypeMap.getSingleton()
            return mime.getExtensionFromMimeType(contentResolver.getType(uri))
        }

        fun createImageData(uri:Uri,contentResolver:ContentResolver):ByteArray?{
            return try {
                val inputStream = contentResolver.openInputStream(uri)
                inputStream?.buffered()?.readBytes()
            } catch(io: IOException){
                Log.e(TAG,io.stackTraceToString())
                null
            }
        }

        fun createSessionExpiredDialog(activity: Activity):AlertDialog{
            val dialog = AlertDialog.Builder(activity)
            dialog.setTitle("Session Expired")
            dialog.setMessage("Your session has expired. Please log-in again.")
            dialog.setPositiveButton("OK",null)
            dialog.setOnDismissListener {
                val intent = Intent(activity,LoginActivity::class.java)
                activity.startActivity(intent)
                activity.finish()
            }

            return dialog.create()
        }

    }
}