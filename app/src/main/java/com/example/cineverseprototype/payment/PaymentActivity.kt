package com.example.cineverseprototype.payment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.ethanhua.skeleton.Skeleton
import com.ethanhua.skeleton.SkeletonScreen
import com.example.cineverseprototype.Constant
import com.example.cineverseprototype.R
import com.example.cineverseprototype.Singleton
import com.example.cineverseprototype.Util
import com.example.cineverseprototype.databinding.ActivityPaymentBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection


class PaymentActivity : AppCompatActivity() {

    private lateinit var binding:ActivityPaymentBinding
    private lateinit var leaveDialog:AlertDialog
    private val TAG = javaClass.name
    private val voucherList:MutableList<Voucher> = ArrayList<Voucher>()
    private var priceDetails:PriceDetails? = null

    private lateinit var applyListener:View.OnClickListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progress.hide()

        val scheduleId = intent.getStringExtra("scheduleId")
        val seatSelected = intent.getStringArrayListExtra("seatSelected")

        val confirmDialog = AlertDialog.Builder(this)
        confirmDialog.setTitle("Cancel Payment")
        confirmDialog.setMessage("Are you sure to cancel the current payment?")
        confirmDialog.setPositiveButton("YES"){ dialog, _ ->
            finish()
        }
        confirmDialog.setNegativeButton("Cancel"){ dialog,_ ->
            dialog.dismiss()
        }

        leaveDialog = confirmDialog.create()
        binding.toolbar.setNavigationOnClickListener {
            confirmDialog.show()
        }

        if(scheduleId != null && !seatSelected.isNullOrEmpty()){
            loadSkeleton()
            retrieveStartupData(scheduleId,seatSelected)
            retrieveVoucherAvailable()

            applyListener = View.OnClickListener {
                val voucherId = binding.voucherCode.text.toString()
                if(voucherId.isNullOrEmpty()){
                    binding.voucherCode.error = "Voucher Code that applied can not be empty."
                    binding.voucherCode.requestFocus()
                } else{
                    validateVoucher(voucherId,scheduleId,seatSelected)
                }
            }
            binding.applyBtn.setOnClickListener(applyListener)

            binding.confirmButton.setOnClickListener {
                val paymentMethod = when(binding.paymentMethod.checkedRadioButtonId){
                    -1 ->{
                        Singleton.getInstance(applicationContext).showToast("Payment Method not selected.",Toast.LENGTH_LONG)
                        binding.paymentMethod.requestFocus()
                        null
                    }
                    R.id.internetBanking -> {
                        Constant.INTERNET_BANKING_CODE
                    }
                    R.id.cardPayment -> {
                        Constant.CARD_PAYMENT_CODE
                    }
                    else ->{
                        -1
                    }
                }

                if(paymentMethod != null){
                    val voucherId = binding.voucherCode.text.toString()
                    proceedPayment(scheduleId,seatSelected,paymentMethod,voucherId)
                }
            }
        }
        else{
            val errorDialog = AlertDialog.Builder(this)
            errorDialog.setTitle("Invalid Request")
            errorDialog.setMessage("Unable to identify your request. Please try again later")
            errorDialog.setPositiveButton("Exit"){ dialog, _ ->
                dialog.dismiss()
            }
            errorDialog.setOnDismissListener {
                finish()
            }

            errorDialog.create().show()
        }
    }

    private var skeleton:SkeletonScreen? = null

    private fun loadSkeleton(){
       skeleton = Skeleton.bind(binding.contentLayout).load(R.layout.payment_skeleton).show()
    }

    private fun hideSkeleton(){
        if(skeleton != null){
            skeleton!!.hide()
        }
    }
    private fun showLoading(){
        binding.progress.show()
    }

    private fun hideLoading(){
        binding.progress.hide()
    }

    private fun retrieveVoucherAvailable(){
        val preference = Singleton.getInstance(this).preference
        val expiredDialog = Util.createSessionExpiredDialog(this)
        val domain = preference.getString(Constant.WEB_SERVICE_DOMAIN_NAME,null)
        if(domain == null){
            Singleton.getInstance(applicationContext).showToast("No connection established. Please specify the connection in Setting.",Toast.LENGTH_LONG)
        }
        else{
            val cookie = preference.getString(Constant.SESSION_COOKIE,null)
            if(cookie.isNullOrEmpty()){
                expiredDialog.show()
            }
            else{
                showLoading()
                val queue = Singleton.getInstance(this).requestQueue

                val api = "$domain/api/getAllAvailableVoucher"

                val request = object: JsonObjectRequest(Request.Method.GET,api,null,
                    {
                        hideLoading()
                        try{
                            if(it.isNull("result")){
                                val errorMsg = it.getString("errorMsg")
                                Toast.makeText(this,errorMsg, Toast.LENGTH_SHORT).show()
                            }
                            else{
                                val result = it.get("result") as JSONArray
                                voucherList.clear()
                                for(i in 0 until result.length()){
                                    val voucher = Voucher.toObject(result[i] as JSONObject)
                                    if(voucher != null){
                                        voucherList.add(voucher)
                                    }
                                }
                                val adapter: ArrayAdapter<Voucher> = ArrayAdapter<Voucher>(this, android.R.layout.simple_dropdown_item_1line, voucherList.toTypedArray())
                                binding.voucherCode.setAdapter(adapter)
                            }
                        }
                        catch (ex: JSONException){
                            Log.e(TAG,ex.stackTraceToString())
                            Singleton.getInstance(applicationContext).showToast("Unable to process the data from server. Please try again later.",
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
                        Singleton.getInstance(this@PaymentActivity).addSessionCookie(headers)
                        return headers
                    }
                }
                queue.add(request)
            }
        }
    }

    private fun retrieveStartupData(scheduleId:String,seatSelected:ArrayList<String>){
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
                    finish()
                }

                showLoading()
                val queue = Singleton.getInstance(this).requestQueue

                val api = "$domain/api/computePayment"

                val request = object: StringRequest(Request.Method.POST,api,
                    {
                        val response = JSONObject(it)
                        try{
                            if(response.isNull("result")){
                                val errorMsg = response.getString("errorMsg")
                                dialogBuilder.setMessage(errorMsg)
                                dialogBuilder.show()
                            }
                            else{
                                val result = response.get("result") as JSONObject
                                val resultData = PaymentDetails.toObject(result)
                                if(resultData != null){
                                    priceDetails = resultData.getPriceDetails()

                                    binding.movieName.text = resultData.movieName
                                    binding.theatreType.text = resultData.getTheatreInfo()
                                    binding.branchName.text = resultData.branchName
                                    binding.scheduleDate.text = resultData.getScheduleDate()
                                    binding.scheduleTime.text = resultData.getScheduleTime()

                                    if(resultData.theatreType == "Beanie"){
                                        binding.ticketLabel.text = "Beanie Ticket x${seatSelected.size} (${String.format("RM %.2f",resultData.ticketPrice)} @each)"
                                    }
                                    else{
                                        binding.ticketLabel.text = "Standard Ticket x${seatSelected.size} (RM ${String.format("RM %.2f",resultData.ticketPrice)} @each)"
                                    }
                                    binding.taxLabel.text = "Entertainment Tax (${Constant.TAX_PERCENTAGE * 100}%)"
                                    binding.voucherLabel.text = "Voucher Code - none"

                                    binding.ticketPrice.text = String.format("RM %.2f",resultData.totalPrice)
                                    binding.taxPrice.text = String.format("RM %.2f",resultData.tax)
                                    binding.subTotal.text = String.format("RM %.2f",(resultData.totalPrice+resultData.tax))
                                    binding.discountAmount.text = String.format("RM %.2f",resultData.amountDiscount)
                                    binding.grandTotal.text = String.format("RM %.2f",resultData.discountedTotalPrice)
                                    binding.voucherMsg.text = getString(R.string.savingMsg,resultData.amountDiscount)

                                    hideSkeleton()
                                }
                                else{
                                    dialogBuilder.setMessage("Unable to process the data from server. Please try again later.")
                                    dialogBuilder.show()
                                }
                            }
                            hideLoading()
                        }
                        catch (ex: JSONException){
                            Log.e(TAG,ex.stackTraceToString())
                            dialogBuilder.setMessage("Unable to process the data from server. Please try again later.")
                            dialogBuilder.show()
                        }
                    },
                    {
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
                        map["scheduleId"] = scheduleId
                        map["paymentMethod"] = (-1).toString()
                        map["seatNum"] = JSONArray(seatSelected).toString().replace("[","").replace("]","").replace("\"","")
                        return map
                    }
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        val headers = HashMap<String, String>()
                        Singleton.getInstance(this@PaymentActivity).addSessionCookie(headers)
                        return headers
                    }
                }
                queue.add(request)
            }
        }
    }

    private fun proceedPayment(scheduleId:String,seatSelected:ArrayList<String>,paymentType:Int,voucherId:String){
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
                    finish()
                }

                showLoading()
                val queue = Singleton.getInstance(this).requestQueue

                val api = "$domain/api/processPayment"

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
                                val transactionId = response.get("result") as String
                                val intent = Intent(this,PaymentGateway::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra("transactionId",transactionId)

                                startActivity(intent)
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
                        map["scheduleId"] = scheduleId
                        if(!voucherId.isNullOrEmpty()){
                            map["voucherId"] = voucherId
                        }
                        map["paymentMethod"] = paymentType.toString()
                        map["seatNum"] = JSONArray(seatSelected).toString().replace("[","").replace("]","").replace("\"","")
                        return map
                    }
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        val headers = HashMap<String, String>()
                        Singleton.getInstance(this@PaymentActivity).addSessionCookie(headers)
                        return headers
                    }
                }
                queue.add(request)
            }
        }
    }

    private fun validateVoucher(voucherId:String,scheduleId:String,seatSelected:ArrayList<String>){
        if(priceDetails != null){
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

                    val api = "$domain/api/verifyVoucherAvailability"

                    val request = object: StringRequest(Request.Method.POST,api,
                        {
                            hideLoading()
                            val response = JSONObject(it)
                            try{
                                if(response.isNull("result")){
                                    val errorMsg = response.getString("errorMsg")
                                    Toast.makeText(this,errorMsg, Toast.LENGTH_SHORT).show()
                                }
                                else{
                                    val result = response.get("result")
                                    if(result is JSONObject){
                                        binding.voucherCode.error = null
                                        priceDetails = PriceDetails.toObject(result)

                                        if(priceDetails != null){
                                            binding.voucherLabel.text = "Voucher Code - ${binding.voucherCode.text}"

                                            binding.ticketPrice.text = String.format("RM %.2f",priceDetails!!.totalPrice)
                                            binding.taxPrice.text = String.format("RM %.2f",priceDetails!!.tax)
                                            binding.subTotal.text = String.format("RM %.2f",(priceDetails!!.totalPrice+priceDetails!!.tax))

                                            fadeInData(binding.discountAmount)
                                            binding.discountAmount.text = String.format("RM %.2f",priceDetails!!.amountDiscount)
                                            fadeInData(binding.grandTotal)
                                            binding.grandTotal.text = String.format("RM %.2f",priceDetails!!.discountedTotalPrice)
                                            binding.voucherMsg.text = getString(R.string.savingMsg,priceDetails!!.amountDiscount)

                                            Toast.makeText(this,"Voucher applied.",Toast.LENGTH_SHORT).show()

                                            binding.voucherCode.isEnabled = false
                                            binding.applyBtn.text = "Cancel"
                                            binding.applyBtn.setOnClickListener { view ->
                                                binding.applyBtn.text = "Apply"
                                                binding.voucherCode.isEnabled = true
                                                binding.voucherCode.text.clear()

                                                retrieveStartupData(scheduleId,seatSelected)
                                                view.setOnClickListener(applyListener)
                                            }
                                        }
                                        else{
                                            Toast.makeText(this,"Unable to process the data from server. Please try again later.",
                                                Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    else if(result is String){
                                        binding.voucherCode.error = result.toString()
                                        binding.voucherCode.requestFocus()
                                    }
                                }
                            }
                            catch (ex: JSONException){
                                Log.e(TAG,ex.stackTraceToString())
                                Toast.makeText(this,"Unable to process the data from server. Please try again later.",
                                    Toast.LENGTH_LONG).show()
                            }
                        },
                        {
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
                        override fun getParams(): MutableMap<String, String> {
                            val map = LinkedHashMap<String,String>()
                            map["scheduleId"] = scheduleId
                            map["totalPrice"] = priceDetails!!.totalPrice.toString()
                            map["totalTicket"] = seatSelected.size.toString()
                            map["voucherCode"] = voucherId
                            return map
                        }
                        @Throws(AuthFailureError::class)
                        override fun getHeaders(): Map<String, String> {
                            val headers = HashMap<String, String>()
                            Singleton.getInstance(this@PaymentActivity).addSessionCookie(headers)
                            return headers
                        }
                    }
                    queue.add(request)
                }
            }
        }
        else{
            Singleton.getInstance(applicationContext).showToast("Unable to retrieve the data right now. Please try again later",
                Toast.LENGTH_LONG)
        }
    }

    private fun fadeInData(view:View){
        view.startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.fade_in))
    }
    override fun onBackPressed() {
        leaveDialog.show()
    }
}