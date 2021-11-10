package com.example.cineverseprototype.movie

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.ethanhua.skeleton.Skeleton
import com.ethanhua.skeleton.SkeletonScreen
import com.example.cineverseprototype.*
import com.example.cineverseprototype.R
import com.example.cineverseprototype.databinding.ActivityMoviesFromBranchBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MoviesFromBranchActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMoviesFromBranchBinding
    private val TAG = javaClass.name
    private var skeletonScreen:SkeletonScreen? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMoviesFromBranchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val branchName = intent.getStringExtra("branchName")
        val operatingHour = intent.getStringExtra("operatingHour")
        val branchId = intent.getStringExtra("branchId")

        supportActionBar?.setTitle(branchName)

        binding.movieList.addItemDecoration(DividerItemDecoration(this))

        val dialogBuilder =AlertDialog.Builder(this)
        dialogBuilder.setTitle("Error")
        dialogBuilder.setMessage("Unable to identify the branch that you selected. Please try again later.")
        dialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        dialogBuilder.setOnDismissListener {
            finish()
        }

        val errorDialog = dialogBuilder.create()
        if(branchId.isNullOrEmpty()){
            errorDialog.show()
        }
        else{
            binding.refreshBtn.setOnRefreshListener {
                getData(branchId)
            }
            getData(branchId)
        }
    }

    private fun showMovieView(){
        binding.emptyContainer.visibility = View.GONE
        binding.movieList.visibility = View.VISIBLE
    }

    private fun hideMovieView(){
        binding.emptyContainer.visibility = View.VISIBLE
        binding.movieList.visibility = View.GONE
    }

    private fun showLoading(){
        val adapter = RecycleViewSkeleton()
        skeletonScreen = Skeleton.bind(binding.movieList).adapter(adapter).load(R.layout.movie_skeleton).count(10).show()
        binding.progress.show()
    }

    private fun hideLoading(){
        if(binding.refreshBtn.isRefreshing){
            binding.refreshBtn.isRefreshing = false
        }
        binding.progress.hide()
        if(skeletonScreen != null){
            skeletonScreen!!.hide()
        }

    }

    private fun getData(branchId:String){
        val preference = Singleton.getInstance(this).preference
        val expiredDialog = Util.createSessionExpiredDialog(this)
        val domain = preference.getString(Constant.WEB_SERVICE_DOMAIN_NAME,null)
        if(domain == null){
            ToastUtil.initializeToast(applicationContext,"No connection established. Please specify the connection in Setting.", Toast.LENGTH_LONG).show()
        }
        else{
            val cookie = preference.getString(Constant.SESSION_COOKIE,null)
            if(cookie.isNullOrEmpty()){
                expiredDialog.show()
            }
            else{
                showLoading()
                val queue = Singleton.getInstance(this).requestQueue

                val api = "$domain/api/retrieveMovieByBranch?branchId=$branchId"

                val request = object: JsonObjectRequest(Request.Method.GET,api,null,
                    {
                        hideLoading()
                        try{
                            if(it.isNull("result")){
                                hideMovieView()
                                val errorMsg = it.getString("errorMsg")
                                ToastUtil.initializeToast(applicationContext,errorMsg,
                                    Toast.LENGTH_LONG).show()
                            }
                            else{
                                val result = it.get("result") as JSONArray
                                val movieList:ArrayList<Movie> = ArrayList<Movie>()
                                if(result.length() == 0){
                                    hideMovieView()
                                }
                                else{
                                    showMovieView()
                                    for(i in 0 until result.length()){
                                        val movie = Movie.toObject(result.get(i) as JSONObject)
                                        if(movie != null){
                                            movieList.add(movie)
                                        }
                                        else{
                                            ToastUtil.initializeToast(applicationContext,"Unable to retrieve complete data from server.", Toast.LENGTH_LONG).show()
                                        }
                                    }

                                    val adapter = MovieRecycleAdapter(movieList,branchId)
                                    binding.movieList.adapter = adapter

                                }
                            }
                        }
                        catch (ex: JSONException){
                            Log.e(TAG,ex.stackTraceToString())
                            hideMovieView()
                            ToastUtil.initializeToast(applicationContext,"Unable to process the data from server. Please try again later.", Toast.LENGTH_LONG).show()
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
                        Singleton.getInstance(this@MoviesFromBranchActivity).addSessionCookie(headers)
                        return headers
                    }
                }
                queue.add(request)
            }
        }
    }

    class RecycleViewSkeleton():RecyclerView.Adapter<MovieRecycleAdapter.MovieViewHolder>(){
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): MovieRecycleAdapter.MovieViewHolder {
            return MovieRecycleAdapter.MovieViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.movie_skeleton,parent,false))
        }

        override fun onBindViewHolder(holder: MovieRecycleAdapter.MovieViewHolder, position: Int) {
        }

        override fun getItemCount(): Int {
            return 10
        }

    }
}