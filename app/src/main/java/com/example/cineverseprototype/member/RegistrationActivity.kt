package com.example.cineverseprototype.member

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.doAfterTextChanged
import androidx.preference.PreferenceManager
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.cineverseprototype.*
import com.example.cineverseprototype.R
import com.example.cineverseprototype.databinding.ActivityRegistrationBinding
import com.example.cineverseprototype.picasso.CircleTransform
import com.example.cineverseprototype.volley.FileDataPart
import com.example.cineverseprototype.volley.VolleyMultipartRequest
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class RegistrationActivity : AppCompatActivity() {

    private val TAG = javaClass.name
    private lateinit var binding:ActivityRegistrationBinding
    private var imageUri : Intent? = null
    private var queueRequest: RequestQueue? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Register Member"
        binding.progress.hide()

        queueRequest = Volley.newRequestQueue(this)
        //loadDefaultProfilePic()

        binding.signUpButton.setOnClickListener {
            if(validateField()){
                startLoading()

                val domain = getPreference().getString(Constant.WEB_SERVICE_DOMAIN_NAME,null)
                if(domain == null){
                    Toast.makeText(this,"No connection established to the server.", Toast.LENGTH_SHORT).show()
                }
                else{
                    val date = Date(binding.birthDateInput.tag as Long)
                    val username = binding.usernameInput.text.toString()
                    val fullName = binding.fullNameInput.text.toString()
                    val email = binding.emailInput.text.toString()
                    val password = binding.passwordInput.text.toString()

                    lateinit var form:RegistrationForm
                    if(imageUri == null){
                        form =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                val defaultIcon = AppCompatResources.getDrawable(this,R.drawable.baseline_account_circle_grey_300_48dp)
                                val bitmap = getBitmap(defaultIcon as VectorDrawable)
                                val stream = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.PNG,100,stream)
                                var file = FileDataPart("ProfileImage.png",stream.toByteArray(),"png")
                                RegistrationForm(fullName,username,date,password,email,file)
                        } else {
                            //ContextCompat.getDrawable(this,R.drawable.baseline_account_circle_grey_300_48dp)
                            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.baseline_account_circle_grey_300_48dp)
                            val stream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.PNG,100,stream)

                            var file = FileDataPart("ProfileImage.png",stream.toByteArray(),"png")
                            RegistrationForm(fullName,username,date,password,email,file)
                        }
                    }
                    else{
                        val picture = Util.createImageData(imageUri!!.data!!,contentResolver)
                        form = if(picture != null){
                            val extension = Util.getFileExtentsion(imageUri!!.data!!,contentResolver)
                            var file = FileDataPart("ProfileImage.${extension}",picture,extension!!)
                            RegistrationForm(fullName,username,date,password,email,file)
                        } else{
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                val defaultIcon = AppCompatResources.getDrawable(this,R.drawable.baseline_account_circle_grey_300_48dp)
                                val bitmap = getBitmap(defaultIcon as VectorDrawable)
                                val stream = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.PNG,100,stream)
                                var file = FileDataPart("ProfileImage.png",stream.toByteArray(),"png")
                                RegistrationForm(fullName,username,date,password,email,file)
                            } else {
                                //ContextCompat.getDrawable(this,R.drawable.baseline_account_circle_grey_300_48dp)
                                val bitmap = BitmapFactory.decodeResource(resources, R.drawable.baseline_account_circle_grey_300_48dp)
                                val stream = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.PNG,100,stream)

                                var file = FileDataPart("ProfileImage.png",stream.toByteArray(),"png")
                                RegistrationForm(fullName,username,date,password,email,file)
                            }
                        }
                    }
                    val retryPolicy = DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

                    val api = "$domain/guest/registerAccount"
                    val req = object : VolleyMultipartRequest(
                        Method.POST,
                        api,
                        Response.Listener { response ->
                            if(!isFinishing){
                                finishLoading()

                                val builder = AlertDialog.Builder(this)
                                builder.setTitle("Account Created")
                                builder.setMessage("Please go to your mailbox to verify your account.")
                                builder.setPositiveButton("OK",DialogInterface.OnClickListener{ dialog,_ ->
                                    dialog.dismiss()
                                })
                                builder.setOnDismissListener {
                                    finish()
                                }
                                builder.show()
                            }
                        },
                        Response.ErrorListener { error ->
                            finishLoading()
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
                        override fun getParams(): MutableMap<String, String> {
                            var params = mutableMapOf<String,String>()
                            params.putAll(form.toMap())
                            return params
                        }
                        override fun getByteData(): MutableMap<String, FileDataPart> {
                            var params = HashMap<String, FileDataPart>()
                            if(form.picture != null){
                                params["picture"] = form.picture!!
                            }
                            return params
                        }
                    }
                    req.retryPolicy = retryPolicy
                    queueRequest!!.add(req)
                }
            }
        }

        binding.usernameInput.doAfterTextChanged {
            val username = it.toString().toLowerCase()
            if(username.isNullOrEmpty()){
                binding.usernameLayout.error = "Please fill-in this field."
            }
            else if(username.length < 8 || username.length > 20){
                binding.usernameLayout.error = "Username must within 8-20 characters."
            }
            else if(!username.matches(Regex("^[a-z0-9]*\$"))){
                binding.usernameLayout.error = "Username only allow lower case and number without spacing."
            }
            else{
                val domain = getPreference().getString(Constant.WEB_SERVICE_DOMAIN_NAME,null)
                if(domain == null){
                    Toast.makeText(this,"No connection established to the server.", Toast.LENGTH_SHORT).show()
                }
                else{
                    validateUsername(domain,username)
                }
            }
        }

        binding.emailInput.doAfterTextChanged {
            val email = it.toString()
            if(email.isNullOrEmpty()){
                binding.emailLayout.error = "Please fill-in this field."
            }
            else if(!email.matches("^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+\$".toRegex())){
                binding.emailLayout.error = "Invalid email format."
            }
            else{
                val domain = getPreference().getString(Constant.WEB_SERVICE_DOMAIN_NAME,null)
                if(domain == null){
                    Toast.makeText(this,"No connection established to the server.", Toast.LENGTH_SHORT).show()
                }
                else{
                    validateEmail(domain,email)
                }
            }
        }

        binding.passwordInput.doAfterTextChanged {
            val password = it.toString()
            if(password.isNullOrEmpty()){
                binding.passwordInput.error = "Please fill-in this field."
            }
            else if(password.length < Constant.PASSWORD_MIN_LENGTH || password.length > Constant.PASSWORD_MAX_LENGTH){
                binding.passwordInput.error = "Password must consist of ${Constant.PASSWORD_MIN_LENGTH}-${Constant.PASSWORD_MAX_LENGTH} Characters."
            }
            else{
                binding.passwordInput.error = null
            }
        }

        binding.fullNameInput.doAfterTextChanged {
            val fullName = it.toString()
            Log.i(TAG,"Full Name $it, Matches${fullName.matches("/^[A-Za-z\\s]*\$/".toRegex())}")
            if(fullName.isNullOrEmpty()){
                binding.fullNameInput.error = "Please fill-in this field."
            }
            else if(!fullName.matches("^[A-Za-z ]*\$".toRegex())){
                binding.fullNameInput.error = "Full Name only allow upper case and lower characters with spacing."
            }
            else{
                binding.fullNameInput.error = null
            }
        }

        binding.changePictureBtn.setOnClickListener {
            var intent = Intent()
            intent.type = ("image/*")
            intent.action = Intent.ACTION_GET_CONTENT
            resultLauncher.launch(intent)
        }

        binding.birthDateLayout.setStartIconOnClickListener {
            showDatePicker()
        }

        binding.birthDateInput.setOnClickListener {
            showDatePicker()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        queueRequest?.stop()
    }
    private fun showDatePicker(){
        val dateRangePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Your Birth Date")
            .build()
        dateRangePicker.addOnPositiveButtonClickListener {
            var timestamp = dateRangePicker.selection
            Log.i(TAG,"Parameter$it & Selection$timestamp")
            if (timestamp != null) {
                var format = SimpleDateFormat("dd MMM yyyy")
                binding.birthDateInput.setText(format.format(Date(timestamp!!)))
                binding.birthDateInput.tag = timestamp
            }
        }
        dateRangePicker.show(supportFragmentManager, "DatePicker")
    }

    private fun getBitmap(vectorDrawable: VectorDrawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return bitmap
    }

    private fun validateField():Boolean{
        var isValid = true
        var firstFocusElement: View? = null

        val username = binding.usernameInput.text.toString().toLowerCase()
        if(username.isNullOrEmpty()){
            binding.usernameLayout.error = "Please fill-in this field."
            if(firstFocusElement == null){
                firstFocusElement = binding.usernameLayout
            }
            isValid = false
        }
        else if(username.length < 8 || username.length > 20){
            binding.usernameLayout.error = "Username must within 8-20 characters."
            if(firstFocusElement == null){
                firstFocusElement = binding.usernameLayout
            }
            isValid = false
        }
        else if(!username.matches(Regex("^[a-z0-9]*\$"))){
            binding.usernameLayout.error = "Username only allow lower case and number without spacing."
            if(firstFocusElement == null){
                firstFocusElement = binding.usernameLayout
            }
            isValid = false
        }
        else{
            binding.usernameLayout.error = null
        }

        val email = binding.emailInput.text.toString()
        if(email.isNullOrEmpty()){
            binding.emailLayout.error = "Please fill-in this field."
            if(firstFocusElement == null){
                firstFocusElement = binding.emailLayout
            }
            isValid = false
        }
        else if(!email.matches("^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+\$".toRegex())){
            binding.emailLayout.error = "Invalid email format."
            if(firstFocusElement == null){
                firstFocusElement = binding.emailLayout
            }
            isValid = false
        }
        else{
            binding.emailLayout.error = null
        }

        val fullName = binding.fullNameInput.text.toString()
        if(fullName.isNullOrEmpty()){
            binding.fullNameInput.error = "Please fill-in this field."
            if(firstFocusElement == null){
                firstFocusElement = binding.fullNameLayout
            }
            isValid = false
        }
        else if(!fullName.matches(Regex("^[A-Za-z ]*\$"))){
            binding.fullNameInput.error = "Full Name only allow upper case and lower characters with spacing."
            if(firstFocusElement == null){
                firstFocusElement = binding.fullNameLayout
            }
            isValid = false
        }
        else{
            binding.fullNameLayout.error = null
        }

        val password = binding.passwordInput.text.toString()
        if(password.isNullOrEmpty()){
            binding.passwordInput.error = "Please fill-in this field."
            if(firstFocusElement == null){
                firstFocusElement = binding.passwordLayout
            }
            isValid = false
        }
        else if(password.length < Constant.PASSWORD_MIN_LENGTH || password.length > Constant.PASSWORD_MAX_LENGTH){
            binding.passwordInput.error = "Password must consist of ${Constant.PASSWORD_MIN_LENGTH}-${Constant.PASSWORD_MAX_LENGTH} Characters."
            if(firstFocusElement == null){
                firstFocusElement = binding.passwordLayout
            }
            isValid = false
        }else{
            binding.passwordLayout.error = null
        }

        val timestamp = binding.birthDateInput.tag
        if(timestamp == null){
            binding.birthDateInput.error = "Please select your birth date."
            if(firstFocusElement == null){
                firstFocusElement = binding.birthDateLayout
            }
            isValid = false
        }
        else if(timestamp !is Long){
            binding.birthDateInput.error = "Invalid Date."
            if(firstFocusElement == null){
                firstFocusElement = binding.birthDateLayout
            }
            isValid = false
        }
        else{
            binding.birthDateLayout.error = null
        }

        if(!isValid && firstFocusElement != null){
            firstFocusElement.requestFocus()
        }
        return isValid

    }

    private fun validateEmail(domain:String, email:String){
        val api = "$domain/guest/checkEmail"

        val queue = Singleton.getInstance(this).requestQueue

        val req: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, api,
            null,
            Response.Listener { response ->
                Log.i("HttpClient", response.toString())
                if(!response.getBoolean("result")){
                    binding.emailLayout.error = response.getString("msg")
                }
                else{
                    binding.emailLayout.error = null
                }
            },
            Response.ErrorListener { error ->
                Log.e(TAG, error.stackTraceToString())
                Toast.makeText(this,"Unexpected error occurred. Please try again later.",Toast.LENGTH_SHORT).show()
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
        queue.add(req)
    }

    private fun validateUsername(domain:String, username:String){
        val api = "$domain/guest/checkUsername"

        val queue = Singleton.getInstance(this).requestQueue

        val req: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, api,
            null,
            Response.Listener { response ->
                Log.i("HttpClient", response.toString())
                if(!response.getBoolean("result")){
                    binding.usernameLayout.error = response.getString("msg")
                }
                else{
                    binding.usernameLayout.error = null
                }
            },
            Response.ErrorListener { error ->
                Log.e(TAG, error.stackTraceToString())
                Toast.makeText(this,"Unexpected error occurred. Please try again later.",Toast.LENGTH_SHORT).show()
            }) {
            override fun getBody(): ByteArray {
                val requestBody = JSONObject()
                requestBody.put("username",username)
                return username.toByteArray(Charset.forName("utf-8"))
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Type"] = "application/raw"
                return params
            }
        }

        queue.add(req)
    }

    private fun getPreference():SharedPreferences{
        return PreferenceManager.getDefaultSharedPreferences(this)
    }

    private fun startLoading(){
        binding.progress.show()
        binding.signUpButton.isEnabled = false
    }

    private fun finishLoading(){
        binding.progress.hide()
        binding.signUpButton.isEnabled = true
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                Log.i("PHOTO", data.toString())
                //binding.profilePic.setImageURI(data?.data)
                Singleton.getInstance(this).picasso.load(data?.data).transform(CircleTransform()).into(binding.profilePic)
                imageUri = data
                Log.i(TAG,"Image Type ${Util.getFileExtentsion(data?.data!!,contentResolver)}")
            } else {
                Log.i("PHOTO", "CANCELLED")
            }
        }
}