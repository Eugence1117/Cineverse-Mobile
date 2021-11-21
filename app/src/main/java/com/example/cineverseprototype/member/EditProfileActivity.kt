package com.example.cineverseprototype.member

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.ethanhua.skeleton.Skeleton
import com.ethanhua.skeleton.SkeletonScreen
import com.example.cineverseprototype.*
import com.example.cineverseprototype.R
import com.example.cineverseprototype.databinding.ActivityEditProfileBinding
import com.example.cineverseprototype.databinding.FragmentDeleteAccountBinding
import com.example.cineverseprototype.picasso.CircleTransform
import com.example.cineverseprototype.volley.FileDataPart
import com.example.cineverseprototype.volley.VolleyMultipartRequest
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Callback
import org.json.JSONObject
import java.net.HttpURLConnection
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding:ActivityEditProfileBinding
    private var imageUri : Intent? = null
    private val TAG = javaClass.name
    private var skeleton:SkeletonScreen? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        supportActionBar?.title = "Edit Profile"
        binding.progress.hide()

        binding.toolbar.setNavigationOnClickListener {
            if(checkChanges()){
                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setTitle("Unsaved Changes")
                alertDialog.setMessage("Exit without apply changes will not save the changes that you has been made. Are you sure to exit?")
                alertDialog.setPositiveButton("Exit") { _, _ ->
                    finish()
                }
                alertDialog.setNegativeButton("Cancel"){
                        dialog,_ ->
                    dialog.dismiss()
                }
                alertDialog.show()
            }
            else{
                finish()
            }
        }

        val expiredDialog = Util.createSessionExpiredDialog(this)

        val fullName = intent.getStringExtra("full_name")
        val username = intent.getStringExtra("username")
        val picture = intent.getStringExtra("picture")
        val birthDate = intent.getLongExtra("birthDate",0L)
        val email = intent.getStringExtra("email")

        binding.usernameInput.setText(username)
        binding.fullNameInput.setText(fullName)
        binding.emailInput.setText(email)

        showPictureLoading()
        if(picture == null){
            Singleton.getInstance(this).picasso.load(R.drawable.baseline_account_circle_grey_300_48dp).placeholder(R.drawable.baseline_account_circle_grey_300_48dp).transform(CircleTransform()).into(binding.profilePic,object:Callback{
                override fun onSuccess() {
                    hidePictureLoading()
                }

                override fun onError(e: java.lang.Exception?) {
                    hidePictureLoading()
                }

            })
        }
        else{
            Singleton.getInstance(this).picasso.load(picture).transform(CircleTransform()).into(binding.profilePic,object:
                Callback {
                override fun onSuccess() {
                    hidePictureLoading()
                }

                override fun onError(e: java.lang.Exception?) {
                    Singleton.getInstance(this@EditProfileActivity).picasso.load(R.drawable.baseline_account_circle_grey_300_48dp).placeholder(R.drawable.baseline_account_circle_grey_300_48dp).transform(CircleTransform()).into(binding.profilePic,object:Callback {
                        override fun onSuccess() {
                            hidePictureLoading()
                        }

                        override fun onError(e: java.lang.Exception?) {
                            hidePictureLoading()
                        }
                    })
                }
            })
        }
        if(birthDate != 0L){
            var format = SimpleDateFormat("dd MMM yyyy")
            binding.birthDateInput.setText(format.format(Date(birthDate)))
            binding.birthDateInput.tag = birthDate
        }

        var dialog = AlertDialog.Builder(this)
        var view = layoutInflater.inflate(R.layout.fragment_change_password,null)

        val oldPasswordLayout = view.findViewById<TextInputLayout>(R.id.oldPasswordLayout)
        val oldPassword = view.findViewById<TextInputEditText>(R.id.oldPasswordEditText)
        val newPasswordLayout = view.findViewById<TextInputLayout>(R.id.newPasswordLayout)
        val newPassword = view.findViewById<TextInputEditText>(R.id.newPasswordEditText)
        oldPassword.doAfterTextChanged {

            if(it.isNullOrEmpty()){
                oldPasswordLayout.error = "This field cannot be empty."
            }
            else{
                oldPasswordLayout.error = null
            }
        }

        newPassword.doAfterTextChanged {
            if(it.isNullOrEmpty()){
                newPasswordLayout.error = "This field cannot be empty."
            }
            else if(it.length < Constant.PASSWORD_MIN_LENGTH || it.length > Constant.PASSWORD_MAX_LENGTH){
                newPasswordLayout.error = "Your new password must consist of ${Constant.PASSWORD_MIN_LENGTH}-${Constant.PASSWORD_MAX_LENGTH} Characters."
            }
            else{
                newPasswordLayout.error = null
            }
        }

        dialog.setView(view)
        dialog.setTitle("Change Password")
        dialog.setPositiveButton("Apply Changes",null)
        dialog.setNegativeButton("Cancel",null)
        val passwordDialog = dialog.create()

        val deleteBinding = FragmentDeleteAccountBinding.inflate(layoutInflater)
        deleteBinding.oldPasswordEditText.doAfterTextChanged {
            if(it.isNullOrEmpty()){
                deleteBinding.oldPasswordLayout.error = "This field cannot be empty."
            }
            else{
                deleteBinding.oldPasswordLayout.error = null
            }
        }
        dialog = AlertDialog.Builder(this)
        dialog.setTitle("Delete Account")
        dialog.setView(deleteBinding.root)
        dialog.setMessage("Are you sure to delete your account?\nIf you confirm, please enter your password to proceed.")
        dialog.setIcon(R.drawable.round_warning_red_500_24dp)
        dialog.setPositiveButton("Confirm",null)
        dialog.setNegativeButton("Cancel",null)
        val confirmDialog = dialog.create()

        binding.toolbar.setOnMenuItemClickListener { it ->
            when(it.itemId){
                R.id.changePassword -> {
                    passwordDialog.show()
                    passwordDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if(validatePasswordField(oldPassword,newPassword,oldPasswordLayout,newPasswordLayout)){
                            val preference = Singleton.getInstance(this).preference
                            val domain = preference.getString(Constant.WEB_SERVICE_DOMAIN_NAME,null)
                            if(domain == null){
                                ToastUtil.initializeToast(applicationContext,"No connection established. Please specify the connection in Setting.", Toast.LENGTH_SHORT).show()
                            }
                            else {
                                val cookie = preference.getString(Constant.SESSION_COOKIE, null)
                                if (cookie.isNullOrEmpty()) {
                                    expiredDialog.show()
                                } else {
                                    showLoading()

                                    val oldPass = oldPassword.text.toString().trim()
                                    val newPass = newPassword.text.toString().trim()

                                    val api = "$domain/api/changePassword"
                                    val requestQueue = Volley.newRequestQueue(this)

                                    val request = object: StringRequest(
                                            Request.Method.POST,api,
                                        { jsonString ->
                                            if(!isFinishing){
                                                val jsonResult = JSONObject(jsonString)
                                                hideLoading()
                                                val result = jsonResult.getBoolean("result")
                                                if(result){
                                                    ToastUtil.initializeToast(applicationContext,"Password updated", Toast.LENGTH_SHORT).show()
                                                    passwordDialog.dismiss()
                                                }
                                                else{
                                                    val msg = jsonResult.getString("msg")
                                                    ToastUtil.initializeToast(applicationContext,msg, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        {
                                            hideLoading()
                                            if (it is TimeoutError || it is NoConnectionError) {
                                                ToastUtil.initializeToast(applicationContext,"Request timed out. Please try again later.",
                                                    Toast.LENGTH_LONG).show()
                                            } else if (it is AuthFailureError) {
                                                expiredDialog.show()
                                            } else if (it is ServerError) {
                                                ToastUtil.initializeToast(applicationContext,"Unexpected error occurred. Please try again later.",
                                                    Toast.LENGTH_LONG).show()
                                            } else if (it is NetworkError) {
                                                ToastUtil.initializeToast(applicationContext,"Unexpected error occurred. Please try again later.",
                                                    Toast.LENGTH_LONG).show()
                                            } else if (it is ParseError) {
                                                ToastUtil.initializeToast(applicationContext,"Received unexpected response from server. Please try again later.",
                                                    Toast.LENGTH_LONG).show()
                                            }
                                            else{
                                                try{
                                                    when (it.networkResponse.statusCode) {
                                                        HttpURLConnection.HTTP_BAD_REQUEST -> {
                                                            ToastUtil.initializeToast(applicationContext,"Unable to process your request. Please try again later.",
                                                                Toast.LENGTH_LONG).show()
                                                        }
                                                        HttpURLConnection.HTTP_NOT_FOUND -> {
                                                            ToastUtil.initializeToast(applicationContext,"Unable to locate the service you request. Please try again later.",
                                                                Toast.LENGTH_LONG).show()
                                                        }
                                                        HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                                                            ToastUtil.initializeToast(applicationContext,"Unknown error occurred. Please try again later.",
                                                                Toast.LENGTH_LONG).show()
                                                        }
                                                        HttpURLConnection.HTTP_UNAUTHORIZED -> {
                                                            expiredDialog.show()
                                                        }
                                                    }
                                                }
                                                catch(ex:Exception){
                                                    Log.e(TAG,ex.stackTraceToString())
                                                    ToastUtil.initializeToast(applicationContext,"Unexpected error occurred. Please try again later.",
                                                        Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }){
                                        override fun getParams(): MutableMap<String, String> {
                                            val params = mutableMapOf<String,String>()
                                            params["currentPassword"] = oldPass
                                            params["newPassword"] = newPass
                                            return params
                                        }
                                        @Throws(AuthFailureError::class)
                                        override fun getHeaders(): Map<String, String> {
                                            val headers = HashMap<String, String>()
                                            Singleton.getInstance(this@EditProfileActivity).addSessionCookie(headers)
                                            return headers
                                        }
                                    }
                                    requestQueue.add(request)
                                }
                            }
                        }

                    }
                }
                R.id.deleteAccount ->{
                    confirmDialog.show()
                    confirmDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            _ ->
                        val password = deleteBinding.oldPasswordEditText.text.toString()
                        if(password.isNullOrEmpty()){
                            deleteBinding.oldPasswordLayout.error = "This field cannot be empty."
                        }
                        else{
                            deleteBinding.oldPasswordLayout.error = null

                            val preference = Singleton.getInstance(this).preference
                            val domain = preference.getString(Constant.WEB_SERVICE_DOMAIN_NAME,null)
                            if(domain == null){
                                ToastUtil.initializeToast(applicationContext,"No connection established. Please specify the connection in Setting.", Toast.LENGTH_SHORT).show()
                            }
                            else {
                                val cookie = preference.getString(Constant.SESSION_COOKIE, null)
                                if (cookie.isNullOrEmpty()) {
                                    expiredDialog.show()
                                }
                                else{
                                    showLoading()

                                    val api = "$domain/api/deleteAccount"
                                    val requestQueue = Volley.newRequestQueue(this)

                                    val request = object: StringRequest(
                                        Request.Method.POST,api,
                                        { jsonString ->
                                            if(!isFinishing){
                                                val jsonResult = JSONObject(jsonString)
                                                hideLoading()
                                                val result = jsonResult.getBoolean("result")
                                                if(result){
                                                    ToastUtil.initializeToast(applicationContext,"Account Deleted", Toast.LENGTH_SHORT).show()
                                                    confirmDialog.dismiss()
                                                    logout(expiredDialog)
                                                }
                                                else{
                                                    val msg = jsonResult.getString("msg")
                                                    ToastUtil.initializeToast(applicationContext,msg, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        {error ->
                                            hideLoading()
                                            if (error is TimeoutError || error is NoConnectionError) {
                                                ToastUtil.initializeToast(applicationContext,"Request timed out. Please try again later.",
                                                    Toast.LENGTH_LONG).show()
                                            } else if (error is AuthFailureError) {
                                                expiredDialog.show()
                                            } else if (error is ServerError) {
                                                ToastUtil.initializeToast(applicationContext,"Unexpected error occurred. Please try again later.",
                                                    Toast.LENGTH_LONG).show()
                                            } else if (error is NetworkError) {
                                                ToastUtil.initializeToast(applicationContext,"Unexpected error occurred. Please try again later.",
                                                    Toast.LENGTH_LONG).show()
                                            } else if (error is ParseError) {
                                                ToastUtil.initializeToast(applicationContext,"Received unexpected response from server. Please try again later.",
                                                    Toast.LENGTH_LONG).show()
                                            }
                                            else{
                                                try{
                                                    when (error.networkResponse.statusCode) {
                                                        HttpURLConnection.HTTP_BAD_REQUEST -> {
                                                            ToastUtil.initializeToast(applicationContext,"Unable to process your request. Please try again later.",
                                                                Toast.LENGTH_LONG).show()
                                                        }
                                                        HttpURLConnection.HTTP_NOT_FOUND -> {
                                                            ToastUtil.initializeToast(applicationContext,"Unable to locate the service you request. Please try again later.",
                                                                Toast.LENGTH_LONG).show()
                                                        }
                                                        HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                                                            ToastUtil.initializeToast(applicationContext,"Unknown error occurred. Please try again later.",
                                                                Toast.LENGTH_LONG).show()
                                                        }
                                                        HttpURLConnection.HTTP_UNAUTHORIZED -> {
                                                            expiredDialog.show()
                                                        }
                                                    }
                                                }
                                                catch(ex:Exception){
                                                    Log.e(TAG,ex.stackTraceToString())
                                                    ToastUtil.initializeToast(applicationContext,"Unexpected error occurred. Please try again later.",
                                                        Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }){
                                        override fun getBody(): ByteArray {
                                            val requestBody = JSONObject()
                                            requestBody.put("currentPassword",password)
                                            return password.toByteArray(Charset.forName("utf-8"))
                                        }
                                        @Throws(AuthFailureError::class)
                                        override fun getHeaders(): Map<String, String> {
                                            val headers = HashMap<String, String>()
                                            Singleton.getInstance(this@EditProfileActivity).addSessionCookie(headers)
                                            headers["Content-Type"] = "application/raw"
                                            return headers
                                        }
                                    }
                                    requestQueue.add(request)
                                }
                            }
                            //Proceed
                        }
                    }
                }
            }
            true
        }

        binding.fullNameInput.doAfterTextChanged {
            val fullName = it.toString()
            Log.i(TAG,"Full Name $it, Matches${fullName.matches("/^[A-Za-z\\s]*\$/".toRegex())}")
            if(fullName.isNullOrEmpty()){
                binding.fullNameLayout.error = "Please fill-in this field."
            }
            else if(!fullName.matches("^[A-Za-z ]*\$".toRegex())){
                binding.fullNameLayout.error = "Full Name only allow upper case and lower characters with spacing."
            }
            else{
                binding.fullNameLayout.error = null
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

        binding.btnApplyChanges.setOnClickListener {
            val domain = Singleton.getInstance(this).preference.getString(Constant.WEB_SERVICE_DOMAIN_NAME,null)
            if(domain == null){
                ToastUtil.initializeToast(applicationContext,"No connection established to the server.",
                    Toast.LENGTH_SHORT).show()
            }
            else{
                if(validateField()){
                    if(checkChanges()){
                        val preference = Singleton.getInstance(this).preference
                        val domain = preference.getString(Constant.WEB_SERVICE_DOMAIN_NAME,null)
                        if(domain == null){
                            ToastUtil.initializeToast(applicationContext,"No connection established. Please specify the connection in Setting.",
                                Toast.LENGTH_SHORT).show()
                        }
                        else{
                            val cookie = preference.getString(Constant.SESSION_COOKIE,null)
                            if(cookie.isNullOrEmpty()){
                                expiredDialog.show()
                            }
                            else{
                                showLoading()
                                val api = "$domain/api/editAccount"
                                val queueRequest = Volley.newRequestQueue(this)
                                val retryPolicy = DefaultRetryPolicy(10000, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

                                val name = binding.fullNameInput.text.toString().trim()
                                val birthDate = binding.birthDateInput.tag as Long

                                var file:FileDataPart? = null
                                val form = EditForm(name,birthDate)
                                if(imageUri != null){
                                    val picture = Util.createImageData(imageUri!!.data!!,contentResolver)
                                    if(picture != null){
                                        val extension = Util.getFileExtentsion(imageUri!!.data!!,contentResolver)
                                        file = FileDataPart("ProfileImage.${extension}",picture,extension!!)
                                    }
                                }

                                val req = object : VolleyMultipartRequest(
                                    Method.POST,
                                    api,
                                    Response.Listener { response ->
                                        hideLoading()

                                        if(!isFinishing){
                                            val builder = android.app.AlertDialog.Builder(this)
                                            builder.setTitle("Account updated")
                                            builder.setMessage("Your account details has been updated.")
                                            builder.setPositiveButton("OK",DialogInterface.OnClickListener{ dialog,_ ->
                                                dialog.dismiss()
                                            })
                                            builder.setOnDismissListener {
                                                val intent = Intent()
                                                intent.putExtra("response", true)
                                                setResult(RESULT_OK, intent)
                                                finish()
                                            }
                                            builder.show()
                                        }
                                    },
                                    Response.ErrorListener { error ->
                                        hideLoading()
                                        if (error is TimeoutError || error is NoConnectionError) {
                                            ToastUtil.initializeToast(applicationContext,"Request timed out. Please try again later.",
                                                Toast.LENGTH_LONG).show()
                                        } else if (error is AuthFailureError) {
                                            expiredDialog.show()
                                        } else if (error is ServerError) {
                                            ToastUtil.initializeToast(applicationContext,"Unexpected error occurred. Please try again later.",
                                                Toast.LENGTH_LONG).show()
                                        } else if (error is NetworkError) {
                                            ToastUtil.initializeToast(applicationContext,"Unexpected error occurred. Please try again later.",
                                                Toast.LENGTH_LONG).show()
                                        } else if (error is ParseError) {
                                            ToastUtil.initializeToast(applicationContext,"Received unexpected response from server. Please try again later.",
                                                Toast.LENGTH_LONG).show()
                                        }
                                        else{
                                            try{
                                                when (error.networkResponse.statusCode) {
                                                    HttpURLConnection.HTTP_BAD_REQUEST -> {
                                                        ToastUtil.initializeToast(applicationContext,"Unable to process your request. Please try again later.",
                                                            Toast.LENGTH_LONG).show()
                                                    }
                                                    HttpURLConnection.HTTP_NOT_FOUND -> {
                                                        ToastUtil.initializeToast(applicationContext,"Unable to locate the service you request. Please try again later.",
                                                            Toast.LENGTH_LONG).show()
                                                    }
                                                    HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                                                        ToastUtil.initializeToast(applicationContext,"Unknown error occurred. Please try again later.",
                                                            Toast.LENGTH_LONG).show()
                                                    }
                                                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                                                        expiredDialog.show()
                                                    }
                                                }
                                            }
                                            catch(ex:Exception){
                                                Log.e(TAG,ex.stackTraceToString())
                                                ToastUtil.initializeToast(applicationContext,"Unexpected error occurred. Please try again later.",
                                                    Toast.LENGTH_LONG).show()
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
                                        if(file != null){
                                            params["picture"] = file!!
                                        }
                                        return params
                                    }
                                    @Throws(AuthFailureError::class)
                                    override fun getHeaders(): MutableMap<String, String> {
                                        val headers = HashMap<String, String>()
                                        Singleton.getInstance(this@EditProfileActivity).addSessionCookie(headers)
                                        return headers
                                    }
                                }
                                req.retryPolicy = retryPolicy
                                queueRequest!!.add(req)

                            }
                        }
                    }
                    else{
                        ToastUtil.initializeToast(applicationContext,"No changes has been made.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if(checkChanges()){
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Unsaved Changes")
            alertDialog.setMessage("Exit without apply changes will not save the changes that you has been made. Are you sure to exit?")
            alertDialog.setPositiveButton("Exit") { _, _ ->
                super.onBackPressed()
            }
            alertDialog.setNegativeButton("Cancel"){
                dialog,_ ->
                dialog.dismiss()
            }
            alertDialog.show()
        }
        else{
            super.onBackPressed()
        }
    }

    private fun showPictureLoading(){
        skeleton = Skeleton.bind(binding.profilePic).load(R.layout.profile_picture_skeleton).show()
    }

    private fun hidePictureLoading(){
        if(skeleton != null){
            skeleton!!.hide()
        }
    }

    private fun checkChanges():Boolean{
        val fullName = intent.getStringExtra("full_name")
        val birthDate = intent.getLongExtra("birthDate",0L)

        var isEdited:Boolean = false
        if(fullName != binding.fullNameInput.text.toString()){
            isEdited = true
        }

        if(birthDate == 0L && binding.birthDateInput.tag != null){
            isEdited = true
        }
        else if(birthDate != 0L && binding.birthDateInput.tag != null && birthDate != binding.birthDateInput.tag){
            isEdited = true
        }

        if(imageUri != null){
            isEdited = true
        }

        return isEdited
    }

    private fun validatePasswordField(old:EditText, new:EditText, oldLayout:TextInputLayout,newLayout:TextInputLayout):Boolean{
        var isValid = true

        val oldPassword = old.text.trim()
        if(oldPassword.isNullOrEmpty()){
            oldLayout.error = "This field cannot be empty."
            isValid = false
            oldLayout.requestFocus()
        }
        else{
            oldLayout.error = null
        }

        val newPassword = new.text.trim()
        if(newPassword.isNullOrEmpty()){
            newLayout.error = "This field cannot be empty."
            if(isValid){
                newLayout.requestFocus()
            }
            isValid = false
        }
        else if(newPassword.length < Constant.PASSWORD_MIN_LENGTH || newPassword.length > Constant.PASSWORD_MAX_LENGTH){
            newLayout.error = "Your new password must consist of ${Constant.PASSWORD_MIN_LENGTH}-${Constant.PASSWORD_MAX_LENGTH} Characters."
            if(isValid){
                newLayout.requestFocus()
            }
            isValid = false
        }
        else{
            newLayout.error = null
        }

        return isValid
    }

    private fun validateField():Boolean{
        var isValid = true
        var firstFocusElement: View? = null

        val fullName = binding.fullNameInput.text.toString()
        if(fullName.isNullOrEmpty()){
            binding.fullNameLayout.error = "Please fill-in this field."
            if(firstFocusElement == null){
                firstFocusElement = binding.fullNameLayout
            }
            isValid = false
        }
        else if(!fullName.matches(Regex("^[A-Za-z ]*\$"))){
            binding.fullNameLayout.error = "Full Name only allow upper case and lower characters with spacing."
            if(firstFocusElement == null){
                firstFocusElement = binding.fullNameLayout
            }
            isValid = false
        }
        else{
            binding.fullNameLayout.error = null
        }

        val timestamp = binding.birthDateInput.tag
        if(timestamp == null){
            binding.birthDateLayout.error = "Please select your birth date."
            if(firstFocusElement == null){
                firstFocusElement = binding.birthDateLayout
            }
            isValid = false
        }
        else if(timestamp !is Long){
            binding.birthDateLayout.error = "Invalid Date."
            if(firstFocusElement == null){
                firstFocusElement = binding.birthDateLayout
            }
            isValid = false
        }
        else{
            val birthDate = Date(timestamp as Long)
            val birthDateCal = Calendar.getInstance()
            birthDateCal.time = birthDate

            val currentDateCal = Calendar.getInstance()
            currentDateCal.time = Date()

            val years = currentDateCal.get(Calendar.YEAR) - birthDateCal.get(Calendar.YEAR)
            if(years < 18){
                binding.birthDateLayout.error = "You must be at least 18 years old to register an account."
                if(firstFocusElement == null){
                    firstFocusElement = binding.birthDateLayout
                }
                isValid = false
            }
            else{
                binding.birthDateLayout.error = null
            }
        }

        if(!isValid && firstFocusElement != null){
            firstFocusElement.requestFocus()
        }
        return isValid

    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            Log.i("PHOTO", data.toString())
            //binding.profilePic.setImageURI(data?.data)
            Singleton.getInstance(this).picasso.load(data?.data).transform(CircleTransform()).into(binding.profilePic)
            imageUri = data
        } else {
            Log.i("PHOTO", "CANCELLED")
        }
    }

    private fun showDatePicker(){
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())
            .build()

        val dateRangePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Your Birth Date")
            .setCalendarConstraints(constraints)
            .build()

        dateRangePicker.addOnPositiveButtonClickListener {
            var timestamp = dateRangePicker.selection
            Log.i(TAG,"Parameter$it & Selection$timestamp")
            if (timestamp != null) {
                val birthDate = Date(timestamp as Long)
                val birthDateCal = Calendar.getInstance()
                birthDateCal.time = birthDate

                val currentDateCal = Calendar.getInstance()
                currentDateCal.time = Date()

                val years = currentDateCal.get(Calendar.YEAR) - birthDateCal.get(Calendar.YEAR)
                if(years < 18){
                    binding.birthDateLayout.error = "You must be at least 18 years old to register an account."
                }
                else{
                    binding.birthDateLayout.error = null
                }

                var format = SimpleDateFormat("dd MMM yyyy")
                binding.birthDateInput.setText(format.format(Date(timestamp!!)))
                binding.birthDateInput.tag = timestamp
            }
        }
        dateRangePicker.show(supportFragmentManager, "DatePicker")
    }

    private fun showLoading(){
        binding.progress.show()
        binding.btnApplyChanges.isEnabled = false
    }

    private fun hideLoading(){
        binding.progress.hide()
        binding.btnApplyChanges.isEnabled = true
    }

    private fun logout(expiredDialog:android.app.AlertDialog){
        val preference = Singleton.getInstance(this).preference
        val domain = preference.getString(Constant.WEB_SERVICE_DOMAIN_NAME,null)
        if(domain == null){
            ToastUtil.initializeToast(applicationContext,"No connection established. Please specify the connection in Setting.",
                Toast.LENGTH_SHORT).show()
        }
        else{
            val cookie = preference.getString(Constant.SESSION_COOKIE,null)
            if(cookie.isNullOrEmpty()){
                expiredDialog.show()
            }
            else{
                val api = "$domain/logout"
                val requestQueue = Volley.newRequestQueue(this)

                val request = object: StringRequest(
                    Request.Method.GET,api,
                    { jsonString ->
                        Log.i(TAG,"Device log out.")
                        val intent = Intent(this, LoginActivity::class.java)
                        val editor = preference.edit()
                        editor.remove(Constant.SESSION_COOKIE)
                        editor.commit()

                        startActivity(intent)
                        finish()
                    },
                    { error ->
                        if (error is TimeoutError || error is NoConnectionError) {
                            ToastUtil.initializeToast(applicationContext,"Request timed out. Please try again later.",
                                Toast.LENGTH_LONG).show()
                        } else if (error is AuthFailureError) {
                            expiredDialog.show()
                        } else if (error is ServerError) {
                            ToastUtil.initializeToast(applicationContext,"Unexpected error occurred. Please try again later.",
                                Toast.LENGTH_LONG).show()
                        } else if (error is NetworkError) {
                            ToastUtil.initializeToast(applicationContext,"Unexpected error occurred. Please try again later.",
                                Toast.LENGTH_LONG).show()
                        } else if (error is ParseError) {
                            ToastUtil.initializeToast(applicationContext,"Received unexpected response from server. Please try again later.",
                                Toast.LENGTH_LONG).show()
                        }
                        else{
                            try{
                                when (error.networkResponse.statusCode) {
                                    HttpURLConnection.HTTP_BAD_REQUEST -> {
                                        ToastUtil.initializeToast(applicationContext,"Unable to process your request. Please try again later.",
                                            Toast.LENGTH_LONG).show()
                                    }
                                    HttpURLConnection.HTTP_NOT_FOUND -> {
                                        ToastUtil.initializeToast(applicationContext,"Unable to locate the service you request. Please try again later.",
                                            Toast.LENGTH_LONG).show()
                                    }
                                    HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                                        ToastUtil.initializeToast(applicationContext,"Unknown error occurred. Please try again later.",
                                            Toast.LENGTH_LONG).show()
                                    }
                                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                                        expiredDialog.show()
                                    }
                                }
                            }
                            catch(ex:Exception){
                                Log.e(TAG,ex.stackTraceToString())
                                ToastUtil.initializeToast(applicationContext,"Unexpected error occurred. Please try again later.",
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                    }){
                }
                requestQueue.add(request)
            }

        }
    }
}