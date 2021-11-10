package com.example.cineverseprototype

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.cineverseprototype.databinding.ActivityMainBinding
import org.json.JSONObject
import java.net.HttpURLConnection
import java.util.HashMap
import kotlin.math.log

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    private val TAG = javaClass.name
    private lateinit var exitDialog:AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navigationDrawer.setNavigationItemSelectedListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            when(it.itemId){
                R.id.settingButton -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
                R.id.scanButton -> {
                    val intent = Intent(this,QrCodeScannerActivity::class.java)
                    startActivity(intent)
                }
                R.id.logoutButton -> {
                    logout(true)
                }
            }
            true
        }

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Are you sure to exit ?")
        dialogBuilder.setTitle("Exit")
        dialogBuilder.setPositiveButton("Yes") { dialog, _ ->
            dialog.dismiss()
            logout(false)
            finishAffinity()
        }
        dialogBuilder.setNegativeButton("Cancel"){
            dialog,_ -> dialog.dismiss()
        }

        exitDialog = dialogBuilder.create()

        val navController = (supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment).navController
        binding.botNavigationView.setupWithNavController(navController)
        
    }

    private fun logout(returnLogin:Boolean){
        val expiredDialog = Util.createSessionExpiredDialog(this)
        val preference = Singleton.getInstance(this).preference
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
                val api = "$domain/logout"
                val requestQueue = Volley.newRequestQueue(this)

                val request = object: StringRequest(
                    Request.Method.GET,api,
                    { jsonString ->
                        Log.i(TAG,"Device log out.")
                        val intent = Intent(this,LoginActivity::class.java)
                        val editor = preference.edit()
                        editor.remove(Constant.SESSION_COOKIE)
                        editor.commit()

                        if(returnLogin){
                            startActivity(intent)
                            finish()
                        }
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
                                        ToastUtil.initializeToast(applicationContext,"Unable to process your request. Please try again later.",Toast.LENGTH_SHORT).show()
                                    }
                                    HttpURLConnection.HTTP_NOT_FOUND -> {
                                        ToastUtil.initializeToast(applicationContext,"Unable to locate the service you request. Please try again later.",Toast.LENGTH_SHORT).show()
                                    }
                                    HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                                        ToastUtil.initializeToast(applicationContext,"Unknown error occurred. Please try again later.",Toast.LENGTH_SHORT).show()
                                    }
                                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                                        ToastUtil.initializeToast(applicationContext,"Account not found. Please try again.",Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            catch(ex:Exception){
                                Log.e(TAG,ex.stackTraceToString())
                                ToastUtil.initializeToast(applicationContext,"Unexpected error occurred. Please try again later.",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }){
                }
                requestQueue.add(request)
            }

        }
    }

    override fun onBackPressed() {
        exitDialog.show()
    }
}