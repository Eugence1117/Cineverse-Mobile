package com.example.cineverseprototype

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.LruCache
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso

class Singleton constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: Singleton? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Singleton(context).also {
                    INSTANCE = it
                }
            }
    }

    val imageLoader: ImageLoader by lazy {
        ImageLoader(requestQueue,
            object : ImageLoader.ImageCache {
                private val cache = LruCache<String, Bitmap>(20)
                override fun getBitmap(url: String): Bitmap {
                    return cache.get(url)
                }
                override fun putBitmap(url: String, bitmap: Bitmap) {
                    cache.put(url, bitmap)
                }
            })
    }

    val requestQueue: RequestQueue by lazy {
        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Volley.newRequestQueue(context.applicationContext)
    }
    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }

    val picasso:Picasso by lazy{
        Picasso.get()
    }

    val toast: Toast by lazy{
        Toast(context)
    }

    fun showToast(msg:String,length:Int){
        toast.cancel()

        toast.setText(msg)
        toast.duration = length
        toast.show()
    }

    val preference: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun checkSessionCookie(headers: MutableMap<String, String>) {
        if (headers.containsKey(Constant.SET_COOKIE_KEY)
            && headers[Constant.SET_COOKIE_KEY]!!.startsWith(Constant.SESSION_COOKIE)
        ) {
            var cookie = headers[Constant.SET_COOKIE_KEY]
            if (cookie!!.isNotEmpty()) {
                val splitCookie = cookie.split(";".toRegex()).toTypedArray()
                val splitSessionId = splitCookie[0].split("=".toRegex()).toTypedArray()
                cookie = splitSessionId[1]
                val prefEditor: SharedPreferences.Editor = preference.edit()
                prefEditor.putString(Constant.SESSION_COOKIE, cookie)
                prefEditor.commit()
            }
        }
    }

    fun addSessionCookie(headers: MutableMap<String, String>) {
        val sessionId = preference.getString(Constant.SESSION_COOKIE, "")
        if (sessionId!!.isNotEmpty()) {
            val builder = StringBuilder()
            builder.append(Constant.SESSION_COOKIE)
            builder.append("=")
            builder.append(sessionId)
            if (headers.containsKey(Constant.COOKIE_KEY)) {
                builder.append("; ")
                builder.append(headers[Constant.COOKIE_KEY])
            }
            headers[Constant.COOKIE_KEY] = builder.toString()
        }
    }
}