package com.example.cineverseprototype.ticket

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.example.cineverseprototype.*
import com.example.cineverseprototype.R
import com.example.cineverseprototype.databinding.FragmentTicketBinding
import com.example.cineverseprototype.payment.Payment
import com.example.cineverseprototype.payment.PaymentCardRecyclerAdapter
import com.example.cineverseprototype.payment.PaymentGateway
import com.example.cineverseprototype.payment.ViewPaymentActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONException
import java.net.HttpURLConnection

class TicketFragment : Fragment() {

    private lateinit var binding:FragmentTicketBinding
    private val TAG = javaClass.name
    private lateinit var expiredDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTicketBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment
        binding.progress.hide()

        expiredDialog = Util.createSessionExpiredDialog(requireActivity())

        binding.refreshBtn.setOnRefreshListener {
            retrievePaymentInfo()
        }

        if(isAdded){
            retrievePaymentInfo()
        }

        return binding.root
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

    private fun showOrHideSchedule(isShow:Boolean){
        if(isShow){
            binding.emptyContainer.visibility = View.GONE
            binding.contentContainer.visibility = View.VISIBLE
        }
        else{
            binding.emptyContainer.visibility = View.VISIBLE
            binding.contentContainer.visibility = View.GONE
        }
    }

    private fun retrievePaymentInfo(){
        val preference = Singleton.getInstance(requireContext()).preference
        val domainName = preference.getString(Constant.WEB_SERVICE_DOMAIN_NAME,null)
        if(domainName == null){
            ToastUtil.initializeToast(requireContext(),"No connection established. Please specify the connection in Setting.",
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
                val api = "$domainName/api/getUpcomingSchedule"
                val request = object: JsonObjectRequest(Request.Method.GET,api,null,
                    {
                        try{
                            if(isAdded){
                                hideLoading()
                                if(!it.isNull("errorMsg")){
                                    val errorMsg = it.getString("errorMsg")
                                    showOrHideSchedule(false)
                                    ToastUtil.initializeToast(requireContext(),errorMsg,
                                        Toast.LENGTH_LONG).show()
                                }
                                else {
                                    if(it.isNull("result")){
                                        showOrHideSchedule(false)
                                    }
                                    else{
                                        val result = it.getJSONObject("result")

                                        val paymentInfo = Payment.toObject(result)
                                        if(paymentInfo != null){
                                            binding.movieName.text = paymentInfo.movieName
                                            binding.branchName.text = paymentInfo.getBranchInfo()
                                            binding.theatreType.text = paymentInfo.getSeatInfo()
                                            binding.scheduleTime.text = paymentInfo.getScheduleTime()
                                            binding.scheduleDate.text = paymentInfo.getScheduleDate()

                                            binding.qrCode.setImageBitmap(generateQRCode(paymentInfo.paymentId))

                                            showOrHideSchedule(true)
                                        }
                                        else{
                                            showOrHideSchedule(false)
                                        }
                                    }
                                }
                            }
                        }
                        catch(ex: JSONException){
                            Log.e(TAG,ex.stackTraceToString())
                            showOrHideSchedule(false)
                            ToastUtil.initializeToast(requireContext(),"Unable to process the data from server. Please try again later.",
                                Toast.LENGTH_LONG).show()
                        }
                    },
                    {
                        hideLoading()
                        if (it is TimeoutError || it is NoConnectionError) {
                            ToastUtil.initializeToast(requireContext(),"Request timed out. Please try again later.",
                                Toast.LENGTH_LONG).show()
                        } else if (it is AuthFailureError) {
                            if(isAdded){
                                expiredDialog.show()
                            }
                        } else if (it is ServerError) {
                            ToastUtil.initializeToast(requireContext(),"Unexpected error occurred. Please try again later.",
                                Toast.LENGTH_LONG).show()
                        } else if (it is NetworkError) {
                            ToastUtil.initializeToast(requireContext(),"Unexpected error occurred. Please try again later.",
                                Toast.LENGTH_LONG).show()
                        } else if (it is ParseError) {
                            ToastUtil.initializeToast(requireContext(),"Received unexpected response from server. Please try again later.",
                                Toast.LENGTH_LONG).show()
                        }
                        else{
                            try{
                                when (it.networkResponse.statusCode) {
                                    HttpURLConnection.HTTP_BAD_REQUEST -> {
                                        ToastUtil.initializeToast(requireContext(),"Unable to process your request. Please try again later.",
                                            Toast.LENGTH_LONG).show()
                                    }
                                    HttpURLConnection.HTTP_NOT_FOUND -> {
                                        ToastUtil.initializeToast(requireContext(),"Unable to locate the service you request. Please try again later.",
                                            Toast.LENGTH_LONG).show()
                                    }
                                    HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                                        ToastUtil.initializeToast(requireContext(),"Unknown error occurred. Please try again later.",
                                            Toast.LENGTH_LONG).show()
                                    }
                                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                                        ToastUtil.initializeToast(requireContext(),"Account not found. Please try again.",
                                            Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                            catch(ex:Exception){
                                Log.e(TAG,ex.stackTraceToString())
                                ToastUtil.initializeToast(requireContext(),"Unexpected error occurred. Please try again later.",
                                    Toast.LENGTH_LONG).show()
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

    private fun generateQRCode(transactionId:String): Bitmap {
        val size = 512 //pixels
        val hints = hashMapOf<EncodeHintType, Int>().also { it[EncodeHintType.MARGIN] = 1 } // Make the QR code buffer border narrower
        val bits = QRCodeWriter().encode(transactionId, BarcodeFormat.QR_CODE, size, size, hints)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }
}