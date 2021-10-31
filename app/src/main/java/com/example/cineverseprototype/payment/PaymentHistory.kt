package com.example.cineverseprototype.payment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.example.cineverseprototype.ClickListener
import com.example.cineverseprototype.Constant
import com.example.cineverseprototype.R
import com.example.cineverseprototype.Singleton
import com.example.cineverseprototype.Util
import com.example.cineverseprototype.databinding.ActivityPaymentHistoryBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection

class PaymentHistory : AppCompatActivity() {

    private lateinit var binding:ActivityPaymentHistoryBinding
    private val TAG = javaClass.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Payment History"
        binding.progress.hide()
        hideList()

        binding.refreshBtn.setOnRefreshListener {
            retrieveData()
        }

        retrieveData()
    }

    private fun hideLoading(){
        binding.progress.hide()
        if(binding.refreshBtn.isRefreshing){
            binding.refreshBtn.isRefreshing = false
        }
    }

    private fun showLoading(){
        binding.progress.show()
    }

    private fun hideList(){
        binding.emptyContainer.visibility = View.VISIBLE
        binding.cardList.visibility = View.GONE
    }

    private fun showList(){
        binding.emptyContainer.visibility = View.GONE
        binding.cardList.visibility = View.VISIBLE
    }

    private fun retrieveData(){
        val expiredDialog = Util.createSessionExpiredDialog(this)
        val preference = Singleton.getInstance(applicationContext).preference
        val domainName = preference.getString(Constant.WEB_SERVICE_DOMAIN_NAME,null)
        if(domainName == null){
            Toast.makeText(applicationContext,"No connection established. Please specify the connection in Setting.",
                Toast.LENGTH_LONG).show()
        }
        else{
            val cookie = preference.getString(Constant.SESSION_COOKIE,null)
            if(cookie.isNullOrEmpty()){
                expiredDialog.show()
            }
            else{
                showLoading()
                val queue = Singleton.getInstance(applicationContext).requestQueue
                val api = "$domainName/api/getAllPayment"
                val request = object: JsonObjectRequest(Request.Method.GET,api,null,
                    {
                        hideLoading()
                        try{
                            if(!it.isNull("errorMsg")){
                                val errorMsg = it.getString("errorMsg")
                                hideList()
                                Singleton.getInstance(applicationContext).showToast(errorMsg, Toast.LENGTH_LONG)
                            }
                            else {
                                if(it.isNull("result")){
                                    hideList()
                                }
                                else{
                                    val result = it.getJSONArray("result")

                                    val arrayList = ArrayList<Payment>()
                                    for(i in 0 until result.length()){
                                        val paymentObject = result[i] as JSONObject

                                        val paymentInfo = Payment.toObject(paymentObject)
                                        if(paymentInfo != null){
                                            arrayList.add(paymentInfo)
                                        }
                                    }

                                    if(arrayList.size > 0){
                                        showList()

                                        val adapter = PaymentCardRecyclerAdapter(arrayList,object :
                                            ClickListener {
                                            override fun onItemClick(position: Int, v: View?) {
                                                val payment = arrayList[position]

                                                if(payment.paymentStatus == "Pending"){
                                                    val bottomSheetDialog = BottomSheetDialog(this@PaymentHistory);
                                                    bottomSheetDialog.setContentView(R.layout.bottom_sheet_layout)
                                                    bottomSheetDialog.findViewById<LinearLayout>(R.id.viewTransaction)?.setOnClickListener {
                                                        val intent = Intent(this@PaymentHistory,ViewPaymentActivity::class.java)
                                                        intent.putExtra("payment",payment)
                                                        startActivity(intent)
                                                    }
                                                    bottomSheetDialog.findViewById<LinearLayout>(R.id.proceedPayment)?.setOnClickListener {
                                                        val intent = Intent(this@PaymentHistory,PaymentGateway::class.java)
                                                        //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                        intent.putExtra("transactionId",payment.paymentId)
                                                        startActivity(intent)
                                                    }
                                                    bottomSheetDialog.show()
                                                }
                                                else{
                                                    val intent = Intent(this@PaymentHistory,ViewPaymentActivity::class.java)
                                                    intent.putExtra("payment",payment)
                                                    startActivity(intent)
                                                }
                                            }

                                        })
                                        binding.cardList.adapter = adapter
                                    }
                                    else{
                                        hideList()
                                    }
                                }
                            }
                        }
                        catch(ex: JSONException){
                            Log.e(TAG,ex.stackTraceToString())
                            hideList()
                            Singleton.getInstance(applicationContext).showToast("Unable to process the data from server. Please try again later.", Toast.LENGTH_LONG)
                        }
                    },
                    {
                        hideLoading()
                        if (it is TimeoutError || it is NoConnectionError) {
                            Singleton.getInstance(applicationContext).showToast("Request timed out. Please try again later.",
                                Toast.LENGTH_LONG)
                        } else if (it is AuthFailureError) {
                            expiredDialog.show()
                        } else if (it is ServerError) {
                            Singleton.getInstance(applicationContext).showToast("Unexpected error occurred. Please try again later.",
                                Toast.LENGTH_LONG)
                        } else if (it is NetworkError) {
                            Singleton.getInstance(applicationContext).showToast("Unexpected error occurred. Please try again later.",
                                Toast.LENGTH_LONG)
                        } else if (it is ParseError) {
                            Singleton.getInstance(applicationContext).showToast("Received unexpected response from server. Please try again later.",
                                Toast.LENGTH_LONG)
                        }
                        else{
                            try{
                                when (it.networkResponse.statusCode) {
                                    HttpURLConnection.HTTP_BAD_REQUEST -> {
                                        Singleton.getInstance(applicationContext).showToast("Unable to process your request. Please try again later.",
                                            Toast.LENGTH_LONG)
                                        Toast.makeText(applicationContext,"", Toast.LENGTH_SHORT).show()
                                    }
                                    HttpURLConnection.HTTP_NOT_FOUND -> {
                                        Singleton.getInstance(applicationContext).showToast("Unable to locate the service you request. Please try again later.",
                                            Toast.LENGTH_LONG)
                                    }
                                    HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                                        Singleton.getInstance(applicationContext).showToast("Unknown error occurred. Please try again later.",
                                            Toast.LENGTH_LONG)
                                    }
                                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                                        Singleton.getInstance(applicationContext).showToast("Account not found. Please try again.",
                                            Toast.LENGTH_LONG)
                                    }
                                }
                            }
                            catch(ex:Exception){
                                Log.e(TAG,ex.stackTraceToString())
                                Singleton.getInstance(applicationContext).showToast("Unexpected error occurred. Please try again later.",
                                    Toast.LENGTH_LONG)
                            }
                        }
                    }){
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        val headers = HashMap<String, String>()
                        Singleton.getInstance(applicationContext).addSessionCookie(headers)
                        return headers
                    }
                }
                queue.add(request)
            }
        }
    }
}