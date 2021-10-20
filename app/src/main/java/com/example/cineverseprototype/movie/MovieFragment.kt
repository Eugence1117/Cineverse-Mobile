package com.example.cineverseprototype.movie

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.ethanhua.skeleton.Skeleton
import com.ethanhua.skeleton.SkeletonScreen
import com.example.cineverseprototype.Constant
import com.example.cineverseprototype.R
import com.example.cineverseprototype.Singleton
import com.example.cineverseprototype.Util
import com.example.cineverseprototype.announcement.Announcement
import com.example.cineverseprototype.databinding.FragmentMovieBinding
import com.synnapps.carouselview.ImageClickListener
import com.synnapps.carouselview.ImageListener
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection


class MovieFragment : Fragment() {


    private val TAG = javaClass.name
    private lateinit var binding:FragmentMovieBinding
    private var skeletonScreen:SkeletonScreen? = null
    private var carouselSkeleton:SkeletonScreen? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMovieBinding.inflate(inflater,container,false)

        // Inflate the layout for requireContext() fragment
        binding.movieList.setHasFixedSize(true)
        getMovieData()
        getAnnouncementData()
        return binding.root
    }

    private fun showCarouselLoading(){
        carouselSkeleton = Skeleton.bind(binding.posterCarousel).load(R.layout.profile_picture_skeleton).show()
    }

    private fun hideCarouselLoading(){
        if(carouselSkeleton != null){
            carouselSkeleton!!.hide()
        }
    }

    private fun showLoading(){
        val adapter = RecycleViewSkeleton()
        skeletonScreen = Skeleton.bind(binding.movieList).adapter(adapter).load(R.layout.movie_poster_skeleton).count(18).show()
        binding.progress.show()
    }

    private fun hideLoading(){
        binding.progress.hide()
        if(skeletonScreen != null){
            skeletonScreen!!.hide()
        }
    }

    private fun getAnnouncementData(){
        val preference = Singleton.getInstance(requireContext()).preference
        val expiredDialog = Util.createSessionExpiredDialog(requireActivity())
        val domain = preference.getString(Constant.WEB_SERVICE_DOMAIN_NAME,null)
        if(domain == null){
            Toast.makeText(requireContext(),"No connection established. Please specify the connection in Setting.",
                Toast.LENGTH_LONG).show()
        }
        else{
            val cookie = preference.getString(Constant.SESSION_COOKIE,null)
            if(cookie.isNullOrEmpty()){
                if(isAdded){
                    expiredDialog.show()
                }
            }
            else{
                showCarouselLoading()
                val queue = Singleton.getInstance(requireContext()).requestQueue

                val api = "$domain/api/getAnnouncement"

                val request = object: JsonObjectRequest(Request.Method.GET,api,null,
                    {
                        hideCarouselLoading()
                        try{
                            if(isAdded){
                                if(it.isNull("result")){
                                    val errorMsg = it.getString("errorMsg")
                                    Toast.makeText(requireContext(),errorMsg, Toast.LENGTH_SHORT).show()
                                }
                                else{
                                    val result = it.get("result") as JSONArray

                                    val announcements:ArrayList<Announcement> = ArrayList<Announcement>()
                                    if(result.length() == 0){
                                        Toast.makeText(requireContext(),"No announcement Available", Toast.LENGTH_SHORT).show()
                                    }
                                    for(i in 0 until result.length()){
                                        val announcement = Announcement.toObject(result.get(i) as JSONObject)
                                        if(announcement != null){
                                            announcements.add(announcement)
                                        }
                                        else{
                                            Toast.makeText(requireContext(),"Unable to retrieve complete data from server.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    binding.posterCarousel.setImageListener { position, imageView ->
                                        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                                        Singleton.getInstance(requireActivity()).picasso.load(
                                            announcements[position].pictureURL)
                                            .placeholder(R.drawable.baseline_image_grey_400_48dp)
                                            .into(imageView)
                                    }
                                    binding.posterCarousel.pageCount = announcements.size
                                }
                            }
                        }
                        catch (ex: JSONException){
                            Log.e(TAG,ex.stackTraceToString())
                            Toast.makeText(requireContext(),"Unable to process the data from server. Please try again later.",
                                Toast.LENGTH_SHORT).show()
                        }
                    },
                    {
                        hideCarouselLoading()
                        if (it is TimeoutError || it is NoConnectionError) {
                            Toast.makeText(requireContext(), "Request timed out. Please try again later.",
                                Toast.LENGTH_LONG).show()
                        } else if (it is AuthFailureError) {
                            if(isAdded){
                                expiredDialog.show()
                            }
                        } else if (it is ServerError) {
                            Toast.makeText(requireContext(),"Unexpected error occurred. Please try again later.",
                                Toast.LENGTH_LONG).show()
                        } else if (it is NetworkError) {
                            Toast.makeText(requireContext(),"Unexpected error occurred. Please try again later.",
                                Toast.LENGTH_LONG).show()
                        } else if (it is ParseError) {
                            Toast.makeText(requireContext(),"Received unexpected response from server. Please try again later.",
                                Toast.LENGTH_LONG).show()
                        }
                        else{
                            try{
                                when (it.networkResponse.statusCode) {
                                    HttpURLConnection.HTTP_BAD_REQUEST -> {
                                        Toast.makeText(requireContext(),"Unable to process your request. Please try again later.",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                    HttpURLConnection.HTTP_NOT_FOUND -> {
                                        Toast.makeText(requireContext(),"Unable to locate the service you request. Please try again later.",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                    HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                                        Toast.makeText(requireContext(),"Unknown error occurred. Please try again later.",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                                        Toast.makeText(requireContext(),"Account not found. Please try again.",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            catch(ex:Exception){
                                Log.e(TAG,ex.stackTraceToString())
                                Toast.makeText(requireContext(),"Unexpected error occurred. Please try again later.",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }){
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        val headers = HashMap<String, String>()
                        Singleton.getInstance(requireContext()).addSessionCookie(headers)
                        return headers
                    }
                }
                queue.add(request)
            }
        }
    }

    private fun getMovieData(){
        val preference = Singleton.getInstance(requireContext()).preference
        val expiredDialog = Util.createSessionExpiredDialog(requireActivity())
        val domain = preference.getString(Constant.WEB_SERVICE_DOMAIN_NAME,null)
        if(domain == null){
            Toast.makeText(requireContext(),"No connection established. Please specify the connection in Setting.",
                Toast.LENGTH_LONG).show()
        }
        else{
            val cookie = preference.getString(Constant.SESSION_COOKIE,null)
            if(cookie.isNullOrEmpty()){
                expiredDialog.show()
            }
            else{
                showLoading()
                val queue = Singleton.getInstance(requireContext()).requestQueue

                val api = "$domain/api/retrieveMovieAvailable"

                val request = object: JsonObjectRequest(Request.Method.GET,api,null,
                    {
                        hideLoading()
                        try{
                            if(it.isNull("result")){
                                val errorMsg = it.getString("errorMsg")
                                Toast.makeText(requireContext(),errorMsg, Toast.LENGTH_SHORT).show()
                            }
                            else{
                                val result = it.get("result") as JSONArray
                                val movieList:ArrayList<Movie> = ArrayList<Movie>()
                                if(result.length() == 0){
                                    Toast.makeText(requireContext(),"No movie Available", Toast.LENGTH_SHORT).show()
                                }
                                for(i in 0 until result.length()){
                                    val movie = Movie.toObject(result.get(i) as JSONObject)
                                    if(movie != null){
                                        movieList.add(movie)
                                    }
                                    else{
                                        Toast.makeText(requireContext(),"Unable to retrieve complete data from server.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                val adapter = MoviePosterRecycleAdapter(movieList)
                                binding.movieList.adapter = adapter
                            }
                        }
                        catch (ex: JSONException){
                            Log.e(TAG,ex.stackTraceToString())
                            Toast.makeText(requireContext(),"Unable to process the data from server. Please try again later.",
                                Toast.LENGTH_SHORT).show()
                        }
                    },
                    {
                        hideLoading()
                        if (it is TimeoutError || it is NoConnectionError) {
                            Toast.makeText(requireContext(), "Request timed out. Please try again later.",
                                Toast.LENGTH_LONG).show()
                        } else if (it is AuthFailureError) {
                            expiredDialog.show()
                        } else if (it is ServerError) {
                            Toast.makeText(requireContext(),"Unexpected error occurred. Please try again later.",
                                Toast.LENGTH_LONG).show()
                        } else if (it is NetworkError) {
                            Toast.makeText(requireContext(),"Unexpected error occurred. Please try again later.",
                                Toast.LENGTH_LONG).show()
                        } else if (it is ParseError) {
                            Toast.makeText(requireContext(),"Received unexpected response from server. Please try again later.",
                                Toast.LENGTH_LONG).show()
                        }
                        else{
                            try{
                                when (it.networkResponse.statusCode) {
                                    HttpURLConnection.HTTP_BAD_REQUEST -> {
                                        Toast.makeText(requireContext(),"Unable to process your request. Please try again later.",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                    HttpURLConnection.HTTP_NOT_FOUND -> {
                                        Toast.makeText(requireContext(),"Unable to locate the service you request. Please try again later.",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                    HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                                        Toast.makeText(requireContext(),"Unknown error occurred. Please try again later.",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                                        Toast.makeText(requireContext(),"Account not found. Please try again.",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            catch(ex:Exception){
                                Log.e(TAG,ex.stackTraceToString())
                                Toast.makeText(requireContext(),"Unexpected error occurred. Please try again later.",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }){
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        val headers = HashMap<String, String>()
                        Singleton.getInstance(requireContext()).addSessionCookie(headers)
                        return headers
                    }
                }
                queue.add(request)
            }
        }
    }

    class RecycleViewSkeleton(): RecyclerView.Adapter<MovieRecycleAdapter.MovieViewHolder>(){
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): MovieRecycleAdapter.MovieViewHolder {
            return MovieRecycleAdapter.MovieViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.movie_poster_skeleton,parent,false))
        }

        override fun onBindViewHolder(holder: MovieRecycleAdapter.MovieViewHolder, position: Int) {
        }

        override fun getItemCount(): Int {
            return 10
        }

    }
}