package com.example.cineverseprototype.volley

import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

open class VolleyJsonRequest(method:Int, url:String, params:Map<String,String>, val responseListener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener) :
    Request<JSONObject>(method,url,errorListener) {

    override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
        return try {
            val jsonString = String(response!!.data, Charset.forName(HttpHeaderParser.parseCharset(response.headers)))
            Response.success(JSONObject(jsonString),
                HttpHeaderParser.parseCacheHeaders(response));
        } catch (e: UnsupportedEncodingException) {
            Response.error(ParseError(e));
        } catch (je: JSONException) {
            Response.error(ParseError(je));
        }
    }

    override fun getParams():Map<String,String>{
        return params
    }

    override fun deliverResponse(response: JSONObject?) {
        responseListener.onResponse(response!!)
    }
}