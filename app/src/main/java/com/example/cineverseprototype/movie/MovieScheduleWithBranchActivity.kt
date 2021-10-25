package com.example.cineverseprototype.movie

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.text.bold
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.example.cineverseprototype.*
import com.example.cineverseprototype.R
import com.example.cineverseprototype.databinding.ActivityMovieScheduleWithBranchBinding
import com.example.cineverseprototype.schedule.DateRecycleAdapter
import com.example.cineverseprototype.schedule.Schedule
import com.example.cineverseprototype.schedule.ScheduleRecycleAdapter
import com.example.cineverseprototype.schedule.ScheduleWithBranchRecycleAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MovieScheduleWithBranchActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMovieScheduleWithBranchBinding
    private val TAG = javaClass.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieScheduleWithBranchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val movie = intent.getSerializableExtra("movie") as Movie

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = movie.movieName

        binding.progress.hide()
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.scheduleList.addItemDecoration(DividerItemDecoration(this))

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Error")
        dialogBuilder.setMessage("Unable to identify the movie that you selected. Please try again later.")
        dialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        dialogBuilder.setOnDismissListener {
            finish()
        }

        val errorDialog = dialogBuilder.create()
        if(movie.movieId.isEmpty()){
            errorDialog.show()
        }
        else{
            Singleton.getInstance(this).picasso.load(movie.picURL).placeholder(R.drawable.baseline_image_grey_400_48dp).into(binding.movieImage)

            val format = SimpleDateFormat("dd-MMM-yyyy")

            binding.movieDuration.text = SpannableStringBuilder().bold {append("Duration : ")}.append(movie.getTotalTime())
            binding.moviePlot.text = movie.synopsis
            binding.movieCast.text = SpannableStringBuilder().bold {append("Cast By : ")}.append(movie.cast)
            binding.movieCensorship.text =  SpannableStringBuilder().bold {append("Censorship : ")}.append(movie.censorship)
            binding.movieDirector.text =  SpannableStringBuilder().bold {append("Director : ")}.append(movie.director)
            binding.movieDistributor.text =  SpannableStringBuilder().bold {append("Distributor : ")}.append(movie.distributor)
            binding.movieReleaseDate.text =  SpannableStringBuilder().bold {append("Release Date : ")}.append(format.format(
                Date(movie.releaseDate)))
            binding.movieType.text =  SpannableStringBuilder().bold {append("Type : ")}.append(movie.movieType)

            getData(movie)
        }

        val sheetBehavior = BottomSheetBehavior.from(binding.contentLayout)
        sheetBehavior.isFitToContents = true
        sheetBehavior.isHideable = false//prevents the boottom sheet from completely hiding off the screen
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED//initially state to fully expanded

        binding.bookButton.setOnClickListener {
            if(sheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        binding.movieInfoBtn.setOnClickListener {
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
    }

    private fun showLoading(){
        binding.progress.show()
    }

    private fun hideLoading(){
        binding.progress.hide()
    }

    private fun getData(movie:Movie){
        val preference = Singleton.getInstance(this).preference
        val expiredDialog = Util.createSessionExpiredDialog(this)
        val domain = preference.getString(Constant.WEB_SERVICE_DOMAIN_NAME,null)
        if(domain == null){
            Toast.makeText(this,"No connection established. Please specify the connection in Setting.",
                Toast.LENGTH_LONG).show()
        }
        else{
            val cookie = preference.getString(Constant.SESSION_COOKIE,null)
            if(cookie.isNullOrEmpty()){
                expiredDialog.show()
            }
            else{
                showLoading()
                val queue = Singleton.getInstance(this).requestQueue

                val retryPolicy = DefaultRetryPolicy(20000, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                val api = "$domain/api/retrieveScheduleByMovie?movieId=${movie.movieId}"

                val request = object: JsonObjectRequest(Method.GET,api,null,
                    {
                        hideLoading()
                        try{
                            if(it.isNull("result")){
                                val errorMsg = it.getString("errorMsg")
                                Toast.makeText(this,errorMsg, Toast.LENGTH_SHORT).show()
                            }
                            else{
                                val result = it.get("result") as JSONObject

                                val scheduleList:MutableMap<Date,MutableMap<String,ArrayList<Schedule>>> = HashMap()
                                val dateList:ArrayList<Date> = ArrayList()

                                if(result.length() == 0){
                                    Toast.makeText(this,"No Schedule Available", Toast.LENGTH_SHORT).show()
                                }

                                result.keys().forEach { key ->
                                    val time = key.toLongOrNull()
                                    if(time != null){
                                        val scheduleDate = Date(time)
                                        dateList.add(scheduleDate)

                                        val dailySchedule:MutableMap<String,ArrayList<Schedule>> = HashMap()

                                        val branchesSchedule = result.get(key) as JSONObject
                                        branchesSchedule.keys().forEach { branchName ->
                                            //Each Branch
                                            val schedules = branchesSchedule.get(branchName) as JSONArray

                                            for(i in 0 until schedules.length()){
                                                val schedule = Schedule.toObject(schedules[i] as JSONObject)
                                                if(schedule != null){
                                                    if(dailySchedule.containsKey(branchName)){
                                                        val list = dailySchedule[branchName]
                                                        list!!.add(schedule)
                                                        list.sortBy { item -> item.startTime }
                                                    }
                                                    else{
                                                        val list:ArrayList<Schedule> = ArrayList()
                                                        list.add(schedule)
                                                        dailySchedule[branchName] = list
                                                    }
                                                }
                                            }
                                        }
                                        //Done For one Day
                                        scheduleList[scheduleDate] = dailySchedule
                                    }
                                    else{
                                        Log.e(TAG,"$key is unparseable date")
                                    }
                                }
                                dateList.sort()
                                val adapter = DateRecycleAdapter(dateList,object:
                                    DateRecycleAdapter.ClickListener{
                                    override fun onItemClick(position: Int, v: View?) {
                                        val dateSelected = dateList[position]

                                        val branchList:MutableList<String> = ArrayList()
                                        scheduleList[dateSelected]!!.keys.forEach {
                                            branchList.add(it)
                                        }
                                        val scheduleRecycleAdapter = ScheduleWithBranchRecycleAdapter(branchList,scheduleList[dateSelected]!!,movie)
                                        binding.scheduleList.swapAdapter(AlphaInAnimationAdapter(scheduleRecycleAdapter),false)
                                    }

                                })
                                binding.dateView.adapter = adapter
                                //Set Default View For Schedule
                                if(dateList.size > 0){
                                    val dateSelected = dateList[0]

                                    val branchList:MutableList<String> = ArrayList()
                                    scheduleList[dateSelected]!!.keys.forEach {
                                        branchList.add(it)
                                    }
                                    val scheduleRecycleAdapter = ScheduleWithBranchRecycleAdapter(branchList,scheduleList[dateSelected]!!,movie)

                                    binding.scheduleList.adapter = scheduleRecycleAdapter
                                }
                            }
                        }
                        catch (ex: JSONException){
                            Log.e(TAG,ex.stackTraceToString())
                            Toast.makeText(this,"Unable to process the data from server. Please try again later.",
                                Toast.LENGTH_SHORT).show()
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
                        Singleton.getInstance(this@MovieScheduleWithBranchActivity).addSessionCookie(headers)
                        return headers
                    }
                }
                request.retryPolicy = retryPolicy
                queue.add(request)
            }
        }
    }
}