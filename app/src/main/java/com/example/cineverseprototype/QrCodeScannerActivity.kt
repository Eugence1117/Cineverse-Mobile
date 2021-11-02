package com.example.cineverseprototype

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.example.cineverseprototype.databinding.ActivityQrCodeScannerBinding
import com.example.cineverseprototype.payment.Payment
import com.example.cineverseprototype.theatre.SeatLayout
import com.example.cineverseprototype.theatre.SeatRecycleAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection

class QrCodeScannerActivity : AppCompatActivity() {

    private lateinit var binding:ActivityQrCodeScannerBinding
    private lateinit var codeScanner: CodeScanner
    private lateinit var sheetBehavior:BottomSheetBehavior<LinearLayout>
    private val PERMISSIONS_REQUEST_CAMERA = 150
    private var isPermit = false

    private val TAG = javaClass.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideLoading()

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        getCameraPermission()

        codeScanner = CodeScanner(this, binding.scannerView)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        sheetBehavior = BottomSheetBehavior.from(binding.contentLayout)
        sheetBehavior.isFitToContents = true
        sheetBehavior.isHideable = false//prevents the boottom sheet from completely hiding off the screen
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED//initially state to fully expanded

        binding.infoBtn.setOnClickListener {
            if(sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
                sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            else{
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        binding.contentLayout.setOnClickListener {
            if(sheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }


        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                retrievePayment(it.text)
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }

        binding.scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun showLoading(){
        binding.progress.show()
        binding.progress.visibility = View.VISIBLE
    }

    private fun hideLoading(){
        binding.progress.visibility = View.GONE
        binding.progress.hide()
    }

    private fun retrievePayment(transactionId:String){
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
                showLoading()
                val queue = Singleton.getInstance(this).requestQueue

                val api = "$domain/stimulate/getPaymentById?paymentId=$transactionId"

                val request = object: JsonObjectRequest(Request.Method.GET,api,null,
                    {
                        hideLoading()
                        try{
                            if(it.isNull("result")){
                                val errorMsg = it.getString("errorMsg")
                                Toast.makeText(this,errorMsg, Toast.LENGTH_SHORT).show()
                            }
                            else {
                                val result = it.getJSONObject("result")
                                val payment = Payment.toObject(result)
                                if(payment != null){
                                    if(sheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                                        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                                    }

                                    binding.movieName.text = payment.movieName
                                    binding.branchName.text = payment.getBranchInfo()
                                    binding.theatreType.text = payment.getSeatInfo()
                                    binding.scheduleTime.text = payment.getScheduleTime()
                                    binding.scheduleDate.text = payment.getScheduleDate()

                                    binding.statusValue.text = payment.paymentStatus
                                    binding.statusValue.setTextColor(Color.parseColor(payment.getStatusColor()))

                                    binding.dateLabel.text = payment.getDateLabel()
                                    binding.dateValue.text = payment.getLatestDate()
                                    binding.ticketPrice.text = String.format("RM %.2f",payment.totalPrice)

                                    if(payment.showMethod()){
                                        binding.payMethodContainer.visibility = View.VISIBLE
                                        binding.paidMethod.text = payment.paymentType
                                    }
                                    else{
                                        binding.payMethodContainer.visibility = View.GONE
                                    }

                                    binding.copyIdBtn.setOnClickListener {
                                        val clipboard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("ReferenceId", payment.paymentId)
                                        clipboard.setPrimaryClip(clip)

                                        Singleton.getInstance(applicationContext).showToast("Reference ID has been copied to your clipboard.",
                                            Toast.LENGTH_SHORT)
                                    }
                                }
                                else{
                                    Singleton.getInstance(applicationContext).showToast("Unable to process the data retrieved from server. Please try again later.",
                                        Toast.LENGTH_SHORT)
                                }
                            }
                        }
                        catch (ex: JSONException){
                            Log.e(TAG,ex.stackTraceToString())
                            Singleton.getInstance(applicationContext).showToast("Unable to process the data retrieved from server. Please try again later.",
                                Toast.LENGTH_SHORT)
                        }
                    },
                    {
                        hideLoading()
                        if (it is TimeoutError || it is NoConnectionError) {
                            Toast.makeText(this, "Request timed out. Please try again later.",
                                Toast.LENGTH_LONG).show()
                        } else if (it is AuthFailureError) {
                            expiredDialog.show()
                        } else if (it is ServerError) {
                            Toast.makeText(this,"Unexpected error occurred. Please try again later.",
                                Toast.LENGTH_LONG).show()
                        } else if (it is NetworkError) {
                            Toast.makeText(this,"Unexpected error occurred. Please try again later.",
                                Toast.LENGTH_LONG).show()
                        } else if (it is ParseError) {
                            Toast.makeText(this,"Received unexpected response from server. Please try again later.",
                                Toast.LENGTH_LONG).show()
                        }
                        else{
                            try{
                                when (it.networkResponse.statusCode) {
                                    HttpURLConnection.HTTP_BAD_REQUEST -> {
                                        Toast.makeText(this,"Unable to process your request. Please try again later.",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                    HttpURLConnection.HTTP_NOT_FOUND -> {
                                        Toast.makeText(this,"Unable to locate the service you request. Please try again later.",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                    HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                                        Toast.makeText(this,"Unknown error occurred. Please try again later.",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                                        Toast.makeText(this,"Account not found. Please try again.",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            catch(ex:Exception){
                                Log.e(TAG,ex.stackTraceToString())
                                Toast.makeText(this,"Unexpected error occurred. Please try again later.",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }){
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        val headers = HashMap<String, String>()
                        Singleton.getInstance(this@QrCodeScannerActivity).addSessionCookie(headers)
                        return headers
                    }
                }
                queue.add(request)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isPermit = true
                } else {
                    finish()
                }
            }
        }
    }

    private fun getCameraPermission() {
        val sharedPreferences = applicationContext.getSharedPreferences(Constant.APPLICATION_PREFERENCES, MODE_PRIVATE)
        if (ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            isPermit = true
            Log.i("Google Map", "Location permission granted")
        }
        else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSIONS_REQUEST_CAMERA)
            } else {
                val isFirstTime = sharedPreferences.getBoolean(Constant.CAMERA_PERMISSION, true)
                if (isFirstTime) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSIONS_REQUEST_CAMERA)
                    val editor = sharedPreferences.edit()
                    editor.putBoolean(Constant.CAMERA_PERMISSION, false)
                    editor.apply()
                } else {
                    AlertDialog.Builder(this)
                        .setTitle("Permission required")
                        .setMessage("You need to enable the camera permission.")
                        .setCancelable(false)
                        .setPositiveButton("Open Setting") { _, _ ->
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package", this.packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }
                        .setNegativeButton("Cancel") { _, _ ->
                            finish()
                        }.show()
                }
            }


        }
    }
}