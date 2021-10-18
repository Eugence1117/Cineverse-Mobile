package com.example.cineverseprototype

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.example.cineverseprototype.databinding.ActivityResetPasswordBinding
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.nio.charset.Charset

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private val TAG = javaClass.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setTitle("Reset Password")
        binding.progress.hide()

        binding.emailInput.doAfterTextChanged {
            val email = it.toString()
            if(email.isNullOrEmpty()){
                binding.emailLayout.error = "Please fill-in this field."
            }
            else if(!email.matches("^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+\$".toRegex())){
                binding.emailLayout.error = "Invalid email format."
            }
            else{
                binding.emailLayout.error = null
            }
        }

        binding.btnReset.setOnClickListener {
            val preference = Singleton.getInstance(this).preference
            val domain = preference.getString(Constant.WEB_SERVICE_DOMAIN_NAME,null)
            if(domain == null){
                Toast.makeText(this,"No connection established. Please specify the connection in Setting.",Toast.LENGTH_LONG).show()
            }
            else{
                if(validateField()){
                    showLoading()
                    val api = "$domain/guest/forgetPassword"
                    val queue = Singleton.getInstance(this).requestQueue

                    val retryPolicy = DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                    val email = binding.emailInput.text.toString().trim()
                    val req: JsonObjectRequest = object : JsonObjectRequest(
                        Method.POST, api,
                        null,
                        Response.Listener { response ->
                            hideLoading()
                            try{
                                if(!response.getBoolean("result")){
                                    Toast.makeText(this,response.getString("msg"),Toast.LENGTH_SHORT).show()
                                }
                                else{
                                    Toast.makeText(this,response.getString("msg"),Toast.LENGTH_LONG).show()
                                }
                            }
                            catch(ex: JSONException){
                                Log.e(TAG,ex.stackTraceToString())
                                Toast.makeText(this,"Received invalid response from server. Please try again later.",Toast.LENGTH_LONG).show()
                            }
                        },
                        Response.ErrorListener { error ->
                            hideLoading()
                            Log.e(TAG, error.stackTraceToString())
                            if (error is TimeoutError || error is NoConnectionError) {
                                Toast.makeText(this, "Request timed out. Please try again later.",Toast.LENGTH_LONG).show()
                            } else if (error is AuthFailureError) {
                                Toast.makeText(this,"Unexpected error occurred. Please try again later.",Toast.LENGTH_LONG).show()
                            } else if (error is ServerError) {
                                Toast.makeText(this,"Unexpected error occurred from server. Please try again later.",Toast.LENGTH_LONG).show()
                            } else if (error is NetworkError) {
                                Toast.makeText(this,"Connection error. Please try again later.",Toast.LENGTH_LONG).show()
                            } else if (error is ParseError) {
                                Toast.makeText(this,"Received unexpected response from server. Please try again later.",Toast.LENGTH_LONG).show()
                            }
                            else{
                                try{
                                    when (error.networkResponse.statusCode) {
                                        HttpURLConnection.HTTP_BAD_REQUEST -> {
                                            Toast.makeText(this,"Unable to process your request. Please try again later.",Toast.LENGTH_SHORT).show()
                                        }
                                        HttpURLConnection.HTTP_NOT_FOUND -> {
                                            Toast.makeText(this,"Unable to locate the service you request. Please try again later.",Toast.LENGTH_SHORT).show()
                                        }
                                        HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                                            Toast.makeText(this,"Unknown error occurred. Please try again later.",Toast.LENGTH_SHORT).show()
                                        }
                                        HttpURLConnection.HTTP_UNAUTHORIZED -> {
                                            Toast.makeText(this,"Account not found. Please try again.",Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                catch(ex:Exception){
                                    Log.e(TAG,ex.stackTraceToString())
                                    Toast.makeText(this,"Unexpected error occurred. Please try again later.",Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                        override fun getBody(): ByteArray {
                            val requestBody = JSONObject()
                            requestBody.put("email",email)
                            return email.toByteArray(Charset.forName("utf-8"))
                        }

                        @Throws(AuthFailureError::class)
                        override fun getHeaders(): Map<String, String>? {
                            val params: MutableMap<String, String> = HashMap()
                            params["Content-Type"] = "application/raw"
                            return params
                        }
                    }
                    req.retryPolicy = retryPolicy
                    queue.add(req)
                }
            }
        }
    }
    private fun showLoading(){
        binding.progress.show()
        binding.btnReset.isEnabled = false
    }

    private fun hideLoading(){
        binding.progress.hide()
        binding.btnReset.isEnabled = true
    }
    private fun validateField():Boolean{
        var isValid = true
        val email = binding.emailInput.text.toString().trim()
        if(email.isNullOrEmpty()){
            isValid = false
            binding.emailLayout.error = "Please fill-in this field."
            binding.emailLayout.requestFocus()
        }
        else if(!email.matches("^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+\$".toRegex())){
            binding.emailLayout.error = "Invalid email format."
            isValid = false
            binding.emailLayout.requestFocus()
        }
        else{
            binding.emailLayout.error = null
        }

        return isValid
    }
}