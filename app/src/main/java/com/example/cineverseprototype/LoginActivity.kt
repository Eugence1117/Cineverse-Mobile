package com.example.cineverseprototype

import android.R.attr.start
import android.R.attr.tag
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.preference.PreferenceManager
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.cineverseprototype.databinding.ActivityLoginBinding
import com.example.cineverseprototype.member.RegistrationActivity
import org.json.JSONException
import java.lang.NullPointerException
import java.net.HttpURLConnection


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val TAG = javaClass.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progress.hide()

        val preference = Singleton.getInstance(this).preference
        val username = preference.getString(Constant.USERNAME_KEY,null)
        val password = preference.getString(Constant.PASSWORD_KEY,null)
        val isRemember = preference.getBoolean(Constant.REMEMBER_OPTION,false)
        if(!username.isNullOrEmpty()){
            binding.usernameInput.setText(username)
        }
        if(!password.isNullOrEmpty()){
            binding.passwordInput.setText(password)
        }
        binding.rememberChkbox.isChecked = isRemember

        binding.loginBtn.setOnClickListener {
            if(validateInput()){
                val preference = PreferenceManager.getDefaultSharedPreferences(this)
                val domain = preference.getString(Constant.WEB_SERVICE_DOMAIN_NAME,null)
                if(domain == null){
                    Toast.makeText(this,"No connection established to the server.",Toast.LENGTH_SHORT).show()
                }
                else{
                    validateMember(domain)
                }
            }
        }

        binding.btnSetting.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        binding.forgetBtn.setOnClickListener {
            val intent = Intent(this,ResetPasswordActivity::class.java)
            startActivity(intent)
        }

        binding.usernameInput.doAfterTextChanged {
            if(it.toString().isNullOrEmpty()){
                binding.usernameLayout.error = "Please fill-in this field."
            }
            else{
                binding.usernameLayout.error = null
            }
        }

        binding.passwordInput.doAfterTextChanged {
            if(it.toString().isNullOrEmpty()){
                binding.passwordLayout.error = "Please fill-in this field."
            }
            else{
                binding.passwordLayout.error = null
            }
        }

        binding.passwordInput.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    //showLoading()
                    val preference = PreferenceManager.getDefaultSharedPreferences(this)
                    val domain = preference.getString(Constant.WEB_SERVICE_DOMAIN_NAME,null)
                    if(domain == null){
                        Toast.makeText(this,"No connection established to the server.",Toast.LENGTH_SHORT).show()
                    }
                    else{
                        validateMember(domain)
                    }
                }
            }
            false
        }

        binding.signUpButton.setOnClickListener {
            val intent = Intent(this@LoginActivity,RegistrationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun startLoading(){
        binding.progress.show()
        binding.loginBtn.isEnabled = false
        binding.signUpButton.isEnabled = false
    }

    private fun finishLoading(){
        binding.progress.hide()
        binding.loginBtn.isEnabled = true
        binding.signUpButton.isEnabled = true
    }

    private fun validateMember(domain:String){
        startLoading()
        var username = binding.usernameInput.text.toString()
        var password = binding.passwordInput.text.toString()

        //http://192.168.43.20:8080/CineverseWS
        val loginAPI = "$domain/login"

        val queue = Volley.newRequestQueue(this)

        val request: StringRequest = object : StringRequest(
            Request.Method.POST, loginAPI,
            Response.Listener { response ->
                finishLoading()
                val preference = Singleton.getInstance(this).preference
                val editor = preference.edit()
                editor.putBoolean(Constant.REMEMBER_OPTION,binding.rememberChkbox.isChecked)
                if(binding.rememberChkbox.isChecked){
                    editor.putString(Constant.USERNAME_KEY,username)
                    editor.putString(Constant.PASSWORD_KEY,password)
                }
                else{
                    editor.putString(Constant.USERNAME_KEY,null)
                    editor.putString(Constant.PASSWORD_KEY,null)
                }
                editor.commit()
                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
                finish()

            },
            Response.ErrorListener { error ->
                finishLoading()
                Log.e(TAG,error.stackTraceToString())
                if (error is TimeoutError || error is NoConnectionError) {
                    Toast.makeText(this, "Request timed out. Please try again later.",Toast.LENGTH_LONG).show()
                } else if (error is AuthFailureError) {
                    Toast.makeText(this,"Incorrect username or password. Please try again later.",Toast.LENGTH_LONG).show()
                } else if (error is ServerError) {
                    Toast.makeText(this,"Unexpected error occurred. Please try again later.",Toast.LENGTH_LONG).show()
                } else if (error is NetworkError) {
                    Toast.makeText(this,"Unexpected error occurred. Please try again later.",Toast.LENGTH_LONG).show()
                } else if (error is ParseError) {
                    Toast.makeText(this,"Received unexpected response from server. Please try again later.",Toast.LENGTH_LONG).show()
                } else if (error is ClientError) {
                    Toast.makeText(this,"Unable to find the service you requested. Please make sure the connection is corret.",Toast.LENGTH_LONG).show()
                }
                else{
                    try{
                        val responseCode = error.networkResponse.statusCode
                        when (responseCode) {
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
                        binding.usernameInput.requestFocus()
                    }
                    catch(ex:Exception){
                        Log.e(TAG,ex.stackTraceToString())
                        Toast.makeText(this,"Unexpected error occurred. Please try again later.",Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
            override fun getParams(): Map<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                params["username"] = username
                params["password"] = password
                return params
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Type"] = "application/x-www-form-urlencoded"
                return params
            }

            override fun parseNetworkResponse(response: NetworkResponse): Response<String?>? {
//                            Log.i("response", response.headers.toString())
//                            val responseHeaders = response.headers
//                            val rawCookies = responseHeaders!!["Set-Cookie"]
//
//                            Log.i("cookies", rawCookies!!)
                if(headers != null){
                    Singleton.getInstance(this@LoginActivity).checkSessionCookie(response.headers!!)
                }
                return super.parseNetworkResponse(response)
            }
        }
        queue.add(request)
        //One Time Read no need this
        //MySingleton.getInstance(this).addToRequestQueue(stringRequest)
    }
    private fun validateInput():Boolean{
        var username = binding.usernameInput.text.toString()
        var password = binding.passwordInput.text.toString()
        var isValid = true

        if(username.isNullOrEmpty()){
            binding.usernameLayout.error = "Please fill-in this field."
            binding.usernameInput.requestFocus()
            isValid = false
        }

        if(password.isNullOrEmpty()){
            binding.passwordLayout.error = "Please fill-in this field."
            if(isValid) {
                binding.usernameInput.requestFocus()
            }
            isValid = false
        }

        return isValid
    }
}