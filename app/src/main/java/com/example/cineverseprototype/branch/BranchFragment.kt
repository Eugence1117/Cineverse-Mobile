package com.example.cineverseprototype.branch

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.ethanhua.skeleton.Skeleton
import com.ethanhua.skeleton.SkeletonScreen
import com.example.cineverseprototype.Constant
import com.example.cineverseprototype.R
import com.example.cineverseprototype.Singleton
import com.example.cineverseprototype.Util
import com.example.cineverseprototype.databinding.FragmentBranchBinding
import com.example.cineverseprototype.movie.MoviesFromBranchActivity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.util.*
import kotlin.collections.HashMap


class BranchFragment : Fragment() {

    private lateinit var binding:FragmentBranchBinding
    private val TAG = javaClass.name
    private var skeletonScreen: SkeletonScreen? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBranchBinding.inflate(inflater,container,false)
        binding.progress.hide()

        binding.refreshBtn.setOnRefreshListener {
            getData()
        }

        if(isAdded){
            getData()
        }
        return binding.root
    }

    private fun showLoading(){
        binding.progress.show()
        skeletonScreen = Skeleton.bind(binding.expandList).load(R.layout.branch_skeleton).show()
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

    private fun getData(){
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
                showLoading()
                val queue = Singleton.getInstance(requireContext()).requestQueue

                val api = "$domain/api/retrieveBranches"
                val request = object: JsonObjectRequest(Request.Method.GET,api,null,
                    {
                        if(isAdded){
                            hideLoading()
                            try{
                                if(it.isNull("result")){
                                    val errorMsg = it.getString("errorMsg")
                                    Toast.makeText(requireContext(),errorMsg,Toast.LENGTH_SHORT).show()
                                }
                                else{
                                    val result = it.get("result") as JSONObject

                                    val title:MutableList<StateLabel> = LinkedList<StateLabel>()
                                    val items:MutableMap<StateLabel,MutableList<Branch>> = HashMap<StateLabel,MutableList<Branch>>()

                                    for(state in result.keys()){
                                        var branches = result.get(state) as JSONArray
                                        val label = StateLabel(state,branches.length())
                                        title.add(label)
                                        //var branches:List<Map<String,String>> = it.get(state) as List<Map<String,String>>
                                        for(i in 0 until branches.length()){
                                            var branch = Branch.toObject(branches.get(i) as JSONObject)
                                            if(branch != null){
                                                if(items.containsKey(label)){
                                                    items[label]!!.add(branch!!)
                                                }
                                                else{
                                                    val list = mutableListOf<Branch>(branch!!)
                                                    items[label] = list
                                                }
                                            }
                                            else{
                                                Toast.makeText(requireContext(),"Unable to retrieve complete data from server.",Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                    val adapter = ExpandableListAdapter(requireContext(),title,items)
                                    binding.expandList.setAdapter(adapter)
                                    adapter.filter.filter("")

                                    binding.searchView.setOnQueryTextListener(object: androidx.appcompat.widget.SearchView.OnQueryTextListener {
                                        override fun onQueryTextSubmit(query: String?): Boolean {
                                            //Do Nothing
                                            return true
                                        }

                                        override fun onQueryTextChange(newText: String?): Boolean {
                                            adapter.filter.filter(newText)
                                            return true
                                        }

                                    })
                                    binding.expandList.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
                                        val item = items[title.get(groupPosition)]!![childPosition]

                                        val intent = Intent(requireContext(),MoviesFromBranchActivity::class.java)
                                        intent.putExtra("branchName",item.branchName)
                                        intent.putExtra("operatingHour",item.toBusinessHour())
                                        intent.putExtra("branchId",item.branchId)

                                        startActivity(intent)
                                        true
                                    }
                                }
                            }
                            catch (ex: JSONException){
                                Log.e(TAG,ex.stackTraceToString())
                                Toast.makeText(requireContext(),"Unable to process the data from server. Please try again later.",Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    {
                        hideLoading()
                        if (it is TimeoutError || it is NoConnectionError) {
                            Toast.makeText(requireContext(), "Request timed out. Please try again later.",Toast.LENGTH_LONG).show()
                        } else if (it is AuthFailureError) {
                            if(isAdded){
                                expiredDialog.show()
                            }
                        } else if (it is ServerError) {
                            Toast.makeText(requireContext(),"Unexpected error occurred. Please try again later.",Toast.LENGTH_LONG).show()
                        } else if (it is NetworkError) {
                            Toast.makeText(requireContext(),"Unexpected error occurred. Please try again later.",Toast.LENGTH_LONG).show()
                        } else if (it is ParseError) {
                            Toast.makeText(requireContext(),"Received unexpected response from server. Please try again later.",Toast.LENGTH_LONG).show()
                        }
                        else{
                            try{
                                when (it.networkResponse.statusCode) {
                                    HttpURLConnection.HTTP_BAD_REQUEST -> {
                                        Toast.makeText(requireContext(),"Unable to process your request. Please try again later.",Toast.LENGTH_SHORT).show()
                                    }
                                    HttpURLConnection.HTTP_NOT_FOUND -> {
                                        Toast.makeText(requireContext(),"Unable to locate the service you request. Please try again later.",Toast.LENGTH_SHORT).show()
                                    }
                                    HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                                        Toast.makeText(requireContext(),"Unknown error occurred. Please try again later.",Toast.LENGTH_SHORT).show()
                                    }
                                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                                        Toast.makeText(requireContext(),"Account not found. Please try again.",Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            catch(ex:Exception){
                                Log.e(TAG,ex.stackTraceToString())
                                Toast.makeText(requireContext(),"Unexpected error occurred. Please try again later.",Toast.LENGTH_SHORT).show()
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
}