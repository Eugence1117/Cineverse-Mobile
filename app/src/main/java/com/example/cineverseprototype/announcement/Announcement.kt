package com.example.cineverseprototype.announcement

import com.android.tools.build.jetifier.core.utils.Log
import com.example.cineverseprototype.branch.Branch
import org.json.JSONException
import org.json.JSONObject

class Announcement(val announcementId:String, val pictureURL:String) {

    companion object {
        fun toObject(obj: JSONObject): Announcement? {
            try {
                val announcementId = obj.getString("announcementId")
                val pictureURL = obj.getString("picURL")

                return Announcement(announcementId,pictureURL)
            } catch (ex: JSONException) {
                Log.e("Announcement", ex.stackTraceToString())
                return null
            }
        }
    }
}