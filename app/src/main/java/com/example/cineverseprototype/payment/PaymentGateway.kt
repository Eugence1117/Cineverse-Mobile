package com.example.cineverseprototype.payment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.example.cineverseprototype.Constant
import com.example.cineverseprototype.MainActivity
import com.example.cineverseprototype.R
import com.example.cineverseprototype.Singleton
import com.example.cineverseprototype.Util
import com.example.cineverseprototype.databinding.ActivityPaymentGatewayBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection

class PaymentGateway : AppCompatActivity() {

    private lateinit var binding:ActivityPaymentGatewayBinding
    private val TAG = javaClass.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentGatewayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progress.hide()
        binding.toolbar.setNavigationOnClickListener {
            returnToHome()
        }

        val invalidDialog = AlertDialog.Builder(this)
        invalidDialog.setTitle("Error")
        invalidDialog.setMessage("Unable to identified the transaction you select.")
        invalidDialog.setPositiveButton("Return to Home"){ dialog, _ ->
            dialog.dismiss()
        }
        invalidDialog.setOnDismissListener {
            returnToHome()
        }

        val transactionId:String? = intent.getStringExtra("transactionId")

        if(transactionId.isNullOrEmpty()){
            val dialog = invalidDialog.create()
            dialog.show()
        }
        else{
            val clickListener = View.OnClickListener {
                try{
                    val status = (it.tag as String).toInt()
                    updatePayment(transactionId,status)
                }
                catch(ex:Exception){
                    Log.e(TAG,ex.stackTraceToString())
                    val dialog = invalidDialog.create()
                    dialog.show()
                }
            }

            binding.btnCancel.setOnClickListener(clickListener)
            binding.btnPay.setOnClickListener(clickListener)
        }
    }

    private fun showLoading(){
        binding.progress.show()
        binding.btnPay.isEnabled = false
        binding.btnCancel.isEnabled = false
    }

    private fun hideLoading(){
        binding.progress.hide()
        binding.btnPay.isEnabled = true
        binding.btnCancel.isEnabled = true
    }

    private fun returnToHome(){
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun updatePayment(paymentId:String, statusCode:Int){
        val preference = Singleton.getInstance(this).preference
        val expiredDialog = Util.createSessionExpiredDialog(this)
        val domain = preference.getString(Constant.WEB_SERVICE_DOMAIN_NAME,null)
        if(domain == null){
            Singleton.getInstance(applicationContext).showToast("No connection established. Please specify the connection in Setting.",
                Toast.LENGTH_LONG)
        }
        else{
            val cookie = preference.getString(Constant.SESSION_COOKIE,null)
            if(cookie.isNullOrEmpty()){
                expiredDialog.show()
            }
            else{
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setTitle("Error occurred")
                dialogBuilder.setPositiveButton("Exit"){ dialog, _ ->
                    dialog.dismiss()
                }
                dialogBuilder.setOnDismissListener {
                    returnToHome()
                }

                showLoading()
                val queue = Singleton.getInstance(this).requestQueue

                val api = "$domain/stimulate/updatePayment"

                val request = object: StringRequest(Request.Method.POST,api,
                    {
                        hideLoading()
                        val response = JSONObject(it)
                        try{
                            if(response.isNull("result")){
                                val errorMsg = response.getString("errorMsg")
                                dialogBuilder.setMessage(errorMsg)
                                dialogBuilder.show()
                            }
                            else{
                                val result = response.getString("result")

                                val successDialog = AlertDialog.Builder(this)
                                successDialog.setTitle("Payment Success")
                                successDialog.setMessage("The payment has been completed. You may view your ticket on home page.")
                                successDialog.setPositiveButton("Exit"){ dialog, _ ->
                                    dialog.dismiss()
                                }
                                successDialog.setOnDismissListener {
                                    returnToHome()
                                }

                                successDialog.create().show()
                            }
                        }
                        catch (ex: JSONException){
                            Log.e(TAG,ex.stackTraceToString())
                            dialogBuilder.setMessage("Unable to process the data from server. Please try again later.")
                            dialogBuilder.show()
                        }
                    },
                    {
                        hideLoading()
                        if (it is TimeoutError || it is NoConnectionError) {
                            dialogBuilder.setMessage("Request timed out. Please try again later.")
                            dialogBuilder.show()
                        } else if (it is AuthFailureError) {
                            expiredDialog.show()
                        } else if (it is ServerError) {
                            dialogBuilder.setMessage("Unexpected error occurred. Please try again later.")
                            dialogBuilder.show()
                        } else if (it is NetworkError) {
                            dialogBuilder.setMessage("Unexpected error occurred. Please try again later.")
                            dialogBuilder.show()
                        } else if (it is ParseError) {
                            dialogBuilder.setMessage("Received unexpected response from server. Please try again later.")
                            dialogBuilder.show()
                        }
                        else{
                            try{
                                when (it.networkResponse.statusCode) {
                                    HttpURLConnection.HTTP_BAD_REQUEST -> {
                                        dialogBuilder.setMessage("Unable to process your request. Please try again later.")
                                        dialogBuilder.show()
                                    }
                                    HttpURLConnection.HTTP_NOT_FOUND -> {
                                        dialogBuilder.setMessage("Unable to locate the service you request. Please try again later.")
                                        dialogBuilder.show()
                                    }
                                    HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                                        dialogBuilder.setMessage("Unknown error occurred. Please try again later.")
                                        dialogBuilder.show()
                                    }
                                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                                        dialogBuilder.setMessage("Account not found. Please try again.")
                                        dialogBuilder.show()
                                    }
                                }
                            }
                            catch(ex:Exception){
                                Log.e(TAG,ex.stackTraceToString())
                                dialogBuilder.setMessage("Unexpected error occurred. Please try again later.")
                                dialogBuilder.show()
                            }
                        }
                    }){
                    override fun getParams(): MutableMap<String, String> {
                        val map = LinkedHashMap<String,String>()
                        map["transactionId"] = paymentId
                        map["statusCode"] = statusCode.toString()
                        return map
                    }
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        val headers = HashMap<String, String>()
                        Singleton.getInstance(this@PaymentGateway).addSessionCookie(headers)
                        return headers
                    }
                }
                queue.add(request)
            }
        }
    }

    override fun onBackPressed() {
        returnToHome()
    }
}