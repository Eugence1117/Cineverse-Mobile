package com.example.cineverseprototype.theatre

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.example.cineverseprototype.Constant
import com.example.cineverseprototype.Singleton
import com.example.cineverseprototype.ToastUtil
import com.example.cineverseprototype.Util
import com.example.cineverseprototype.databinding.ActivitySeatBookingBinding
import com.example.cineverseprototype.movie.Movie
import com.example.cineverseprototype.payment.PaymentActivity
import com.example.cineverseprototype.schedule.Schedule
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.text.SimpleDateFormat

class SeatBookingActivity : AppCompatActivity() {

    private lateinit var binding:ActivitySeatBookingBinding
    private val TAG = javaClass.name

    private var totalPrice:Double = 0.0
    private val seatSelected:MutableSet<String> = HashSet<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeatBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Select Seats"
        val schedule = intent.getSerializableExtra("schedule") as? Schedule
        val movie = intent.getSerializableExtra("movie") as? Movie

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Error")
        dialogBuilder.setMessage("Unable to identify the schedule that you selected. Please try again later.")
        dialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        dialogBuilder.setOnDismissListener {
            finish()
        }

        val errorDialog = dialogBuilder.create()

        if(schedule == null || movie == null){
            errorDialog.show()
        }
        else{
            val dateFormat = SimpleDateFormat("dd MMM yyyy")
            val timeFormat = SimpleDateFormat("hh:mm a")
            binding.movieName.text = movie.movieName
            binding.branchName.text = schedule.branchName
            binding.scheduleDate.text = dateFormat.format(schedule.startTime)
            binding.scheduleTime.text = timeFormat.format(schedule.startTime)
            binding.seatSelected.text = "${seatSelected.size} Seat Selected "
            binding.price.text = String.format("%.2f",totalPrice)

            retrieveData(schedule.scheduleId)
            binding.paymentBtn.setOnClickListener {
                if(seatSelected.size > 0){

                   val intent  = Intent(this, PaymentActivity::class.java)

                    val arrayList = ArrayList<String>()
                    arrayList.addAll(seatSelected)
                    intent.putExtra("scheduleId",schedule.scheduleId)
                    intent.putExtra("seatSelected",arrayList)
                    startActivity(intent)
                }
                else{
                    ToastUtil.initializeToast(applicationContext,"No Seat Selected. Minimum 1 seat is required.",Toast.LENGTH_SHORT).show()
                }
            }

            binding.refreshBtn.setOnRefreshListener {
                updatePrice(totalPrice,0.0)
                totalPrice = 0.0
                seatSelected.clear()
                updateSeatUI()
                retrieveData(schedule.scheduleId)
            }
        }
    }

    private fun showLoading(){
        binding.progress.show()
    }

    private fun hideLoading(){
        binding.progress.hide()
        if(binding.refreshBtn.isRefreshing){
            binding.refreshBtn.isRefreshing = false
        }
    }

    private fun updateSeatUI(){
        if(seatSelected.size > 1){
            binding.seatSelected.text = "${seatSelected.size} Seats Selected"
        }
        else{
            binding.seatSelected.text = "${seatSelected.size} Seat Selected"
        }
    }

    private fun updatePrice(before:Double,after:Double){
        val animator = ValueAnimator.ofFloat(before.toFloat(),after.toFloat())
        animator.duration = 300
        animator.addUpdateListener {
            binding.price.text = String.format("%.2f",it.animatedValue as Float)
        }
        animator.start()
    }

    private fun retrieveData(scheduleId:String){
        val preference = Singleton.getInstance(this).preference
        val expiredDialog = Util.createSessionExpiredDialog(this)
        val domain = preference.getString(Constant.WEB_SERVICE_DOMAIN_NAME,null)
        if(domain == null){
            ToastUtil.initializeToast(applicationContext,"No connection established. Please specify the connection in Setting.",Toast.LENGTH_LONG).show()
        }
        else{
            val cookie = preference.getString(Constant.SESSION_COOKIE,null)
            if(cookie.isNullOrEmpty()){
                expiredDialog.show()
            }
            else{
                showLoading()
                val queue = Singleton.getInstance(this).requestQueue

                val api = "$domain/api/retrieveSeatLayout?scheduleId=$scheduleId"

                val request = object: JsonObjectRequest(Request.Method.GET,api,null,
                    {
                        hideLoading()
                        try{
                            if(it.isNull("result")){
                                val errorMsg = it.getString("errorMsg")
                                ToastUtil.initializeToast(this,errorMsg,
                                    Toast.LENGTH_LONG).show()
                            }
                            else{
                                val result = it.get("result") as JSONObject
                                val seatLayout = SeatLayout.toObject(result)
                                if(seatLayout != null){
                                    Log.i(TAG,seatLayout.toString())
                                    val layoutManager = GridLayoutManager(this,seatLayout.seatCol+1)
                                    val adapter = SeatRecycleAdapter(seatLayout.convertToColumn(),object:SeatRecycleAdapter.CheckListener{

                                        override fun onCheckChanged(
                                            position: Int,
                                            button: CompoundButton,
                                            isChecked: Boolean,
                                        ) {
                                            if(button is CheckBox){
                                                val seat = button.tag as SeatLayout.SeatCol
                                                if(button.isChecked){
                                                    if(seatSelected.size >= 6){
                                                        button.isChecked = false

                                                        ToastUtil.initializeToast(applicationContext,"Maximum Seat Reached.",Toast.LENGTH_SHORT).show()
                                                    }
                                                    else{
                                                        val previousPrice = totalPrice
                                                        totalPrice += seat.seatPrice
                                                        seatSelected.add(seat.seatNum)
                                                        if(!seat.reference.isNullOrEmpty()){
                                                            seatSelected.add(seat.reference)
                                                            totalPrice += seat.seatPrice
                                                        }
                                                        updatePrice(previousPrice,totalPrice)
                                                    }
                                                }
                                                else{
                                                    val previousPrice = totalPrice
                                                    totalPrice -= seat.seatPrice
                                                    seatSelected.remove(seat.seatNum)
                                                    if(!seat.reference.isNullOrEmpty()){
                                                        seatSelected.remove(seat.reference)
                                                        totalPrice -= seat.seatPrice

                                                    }
                                                    updatePrice(previousPrice,totalPrice)
                                                }
                                                updateSeatUI()
                                            }
                                        }

                                    })
                                    binding.seatLayout.adapter = adapter
                                    layoutManager.spanSizeLookup = object:GridLayoutManager.SpanSizeLookup(){
                                        override fun getSpanSize(position: Int): Int {
                                            if(adapter.getItemViewType(position) == 4){
                                                return 2
                                            }
                                            return 1
                                        }

                                    }
                                    binding.seatLayout.layoutManager = layoutManager
                                }
                                else{
                                    Log.e(TAG,"Error in parsing data")
                                }

                            }
                        }
                        catch (ex: JSONException){
                            Log.e(TAG,ex.stackTraceToString())
                            ToastUtil.initializeToast(applicationContext,"Unable to process the data from server. Please try again later.", Toast.LENGTH_SHORT).show()
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
                                        ToastUtil.initializeToast(applicationContext,"Account not found. Please try again.",
                                            Toast.LENGTH_LONG).show()
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
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        val headers = HashMap<String, String>()
                        Singleton.getInstance(this@SeatBookingActivity).addSessionCookie(headers)
                        return headers
                    }
                }
                queue.add(request)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            onBackPressed()
            return true
        }
        return false
    }

}