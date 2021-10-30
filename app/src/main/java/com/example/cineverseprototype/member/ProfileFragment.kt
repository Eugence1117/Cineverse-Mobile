package com.example.cineverseprototype.member

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import com.example.cineverseprototype.databinding.FragmentProfileBinding
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.ethanhua.skeleton.Skeleton
import com.ethanhua.skeleton.SkeletonScreen
import com.example.cineverseprototype.*
import com.example.cineverseprototype.R
import com.example.cineverseprototype.payment.Payment
import com.example.cineverseprototype.payment.PaymentCardListAdapter
import com.example.cineverseprototype.picasso.CircleTransform
import com.squareup.picasso.Callback
import java.net.HttpURLConnection
import java.text.SimpleDateFormat

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val TAG = javaClass.name
    private var skeletonScreen:SkeletonScreen? = null
    private var skeletionProfilePicture:SkeletonScreen? = null
    private lateinit var expiredDialog:AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment
        binding.progress.hide()

        expiredDialog = Util.createSessionExpiredDialog(requireActivity())

        binding.refreshBtn.setOnRefreshListener {
            retrieveData()
            retrievePaymentInfo()
        }
        retrieveData()
        retrievePaymentInfo()
        return binding.root
    }


    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.i(TAG,"Received ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent = result.data!!
            var response = data!!.getBooleanExtra("response",false)
            if(response){
                retrieveData()
            }
        }
    }

    private fun retrievePaymentInfo(){
        val preference = Singleton.getInstance(requireContext()).preference
        val domainName = preference.getString(Constant.WEB_SERVICE_DOMAIN_NAME,null)
        if(domainName == null){
            Toast.makeText(requireContext(),"No connection established. Please specify the connection in Setting.",Toast.LENGTH_LONG).show()
        }
        else{
            val cookie = preference.getString(Constant.SESSION_COOKIE,null)
            if(cookie.isNullOrEmpty()){
                if(isAdded){
                    expiredDialog.show()
                }
            }
            else{
                val queue = Singleton.getInstance(requireContext()).requestQueue
                val api = "$domainName/api/getLastPayment"
                val request = object:JsonObjectRequest(Request.Method.GET,api,null,
                    {
                        if(isAdded){
                            if(!it.isNull("errorMsg")){
                                val errorMsg = it.getString("errorMsg")
                                Toast.makeText(requireContext(),errorMsg, Toast.LENGTH_SHORT).show()
                            }
                            else {
                                if(it.isNull("result")){
                                    binding.emptyPaymentContainer.visibility = View.VISIBLE
                                    binding.paymentContainer.visibility = View.GONE
                                }
                                else{
                                    val result = it.getJSONObject("result")

                                    val paymentInfo = Payment.toObject(result)
                                    if(paymentInfo != null){

                                        binding.emptyPaymentContainer.visibility = View.GONE
                                        binding.paymentContainer.visibility = View.VISIBLE

                                        val array = arrayListOf(paymentInfo!!)
                                        val adapter = PaymentCardListAdapter(requireContext(),array)
                                        binding.paymentContainer.adapter = adapter
                                    }
                                    else{
                                        binding.emptyPaymentContainer.visibility = View.VISIBLE
                                        binding.paymentContainer.visibility = View.GONE
                                    }
                                }
                            }
                        }
                    },
                    {
                        if (it is TimeoutError || it is NoConnectionError) {
                            Singleton.getInstance(requireContext()).showToast("Request timed out. Please try again later.",Toast.LENGTH_LONG)
                        } else if (it is AuthFailureError) {
                            if(isAdded){
                                expiredDialog.show()
                            }
                        } else if (it is ServerError) {
                            Singleton.getInstance(requireContext()).showToast("Unexpected error occurred. Please try again later.",Toast.LENGTH_LONG)
                        } else if (it is NetworkError) {
                            Singleton.getInstance(requireContext()).showToast("Unexpected error occurred. Please try again later.",Toast.LENGTH_LONG)
                        } else if (it is ParseError) {
                            Singleton.getInstance(requireContext()).showToast("Received unexpected response from server. Please try again later.",Toast.LENGTH_LONG)
                        }
                        else{
                            try{
                                when (it.networkResponse.statusCode) {
                                    HttpURLConnection.HTTP_BAD_REQUEST -> {
                                        Singleton.getInstance(requireContext()).showToast("Unable to process your request. Please try again later.",Toast.LENGTH_LONG)
                                        Toast.makeText(requireContext(),"",Toast.LENGTH_SHORT).show()
                                    }
                                    HttpURLConnection.HTTP_NOT_FOUND -> {
                                        Singleton.getInstance(requireContext()).showToast("Unable to locate the service you request. Please try again later.",Toast.LENGTH_LONG)
                                    }
                                    HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                                        Singleton.getInstance(requireContext()).showToast("Unknown error occurred. Please try again later.",Toast.LENGTH_LONG)
                                    }
                                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                                        Singleton.getInstance(requireContext()).showToast("Account not found. Please try again.",Toast.LENGTH_LONG)
                                    }
                                }
                            }
                            catch(ex:Exception){
                                Log.e(TAG,ex.stackTraceToString())
                                Singleton.getInstance(requireContext()).showToast("Unexpected error occurred. Please try again later.",Toast.LENGTH_LONG)
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

    private fun retrieveData(){
        val preference = Singleton.getInstance(requireContext()).preference
        val domainName = preference.getString(Constant.WEB_SERVICE_DOMAIN_NAME,null)
        if(domainName == null){
            Toast.makeText(requireContext(),"No connection established. Please specify the connection in Setting.",Toast.LENGTH_LONG).show()
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
                val api = "$domainName/api/getUserInfo"
                val request = object:JsonObjectRequest(Request.Method.GET,api,null,
                    {
                        if(isAdded){
                            hideLoading()
                            val member = Member.toObject(it)
                            if(member == null){
                                Toast.makeText(requireContext(),"Unable to retrieve data from server. Please contact with the support team.",Toast.LENGTH_SHORT).show()
                            }
                            else{
                                insertData(member!!)
                                binding.editProfileBtn.setOnClickListener {
                                    val intent = Intent(requireContext(),EditProfileActivity::class.java)
                                    intent.putExtra("full_name",member.name)
                                    intent.putExtra("username",member.username)
                                    intent.putExtra("picture",member.picUrl)
                                    intent.putExtra("birthDate",member.dateOfBirth.time)
                                    intent.putExtra("email",member.email)

                                    resultLauncher.launch(intent)
                                }
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

    private fun showPictureLoading(){
        skeletionProfilePicture = Skeleton.bind(binding.profilePic).load(R.layout.profile_picture_skeleton).show()
    }

    private fun hidePictureLoading(){
        if(skeletionProfilePicture != null){
            skeletionProfilePicture!!.hide()
        }
    }


    private fun insertData(member: Member){
        showPictureLoading()
        Log.i(TAG,"Member URL${member.picUrl}")
        if(member.picUrl.isNullOrEmpty()){
            Singleton.getInstance(requireContext()).picasso.load(R.drawable.baseline_account_circle_grey_300_48dp).placeholder(R.drawable.baseline_account_circle_grey_300_48dp).into(binding.profilePic,object:Callback{
                override fun onSuccess() {
                    hidePictureLoading()
                }

                override fun onError(e: java.lang.Exception?) {
                    hidePictureLoading()
                }

            })
        }
        else{
            Singleton.getInstance(requireContext()).picasso.load(member.picUrl).transform(CircleTransform()).placeholder(R.drawable.baseline_account_circle_grey_300_48dp).into(binding.profilePic,object:Callback{
                override fun onSuccess() {
                    hidePictureLoading()
                }

                override fun onError(e: java.lang.Exception?) {
                    Singleton.getInstance(requireContext()).picasso.load(R.drawable.baseline_account_circle_grey_300_48dp).placeholder(R.drawable.baseline_account_circle_grey_300_48dp).transform(CircleTransform()).into(binding.profilePic,object:Callback{
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
        binding.fullName.text = member.name
        binding.email.text = member.email

        val uiDateFormat = SimpleDateFormat("dd MMMM yyyy")
        binding.joinDate.text = "Joined On:  ${uiDateFormat.format(member.createDate)}"
    }

    private fun showLoading(){
        binding.editProfileBtn.isEnabled = false
        binding.progress.show()
        skeletonScreen = Skeleton.bind(binding.scrollView).load(R.layout.profile_skeleton).show()
    }

    private fun hideLoading(){
        if(binding.refreshBtn.isRefreshing){
            binding.refreshBtn.isRefreshing = false
        }
        binding.editProfileBtn.isEnabled = true
        binding.progress.hide()
        if(skeletonScreen != null){
            skeletonScreen!!.hide()
        }
    }
}