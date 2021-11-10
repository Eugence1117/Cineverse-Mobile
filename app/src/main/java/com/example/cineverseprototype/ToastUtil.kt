package com.example.cineverseprototype

import android.content.Context
import android.widget.Toast

class ToastUtil {

    companion object{
        var toast:Toast? = null

        fun initializeToast(context: Context, text:String, length:Int): Toast {
            cancelToast()
            toast = Toast.makeText(context,text,length)
            return toast!!
        }

        private fun cancelToast(){
            if(toast != null){
                toast!!.cancel()
            }
        }
    }
}