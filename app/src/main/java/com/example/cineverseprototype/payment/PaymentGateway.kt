package com.example.cineverseprototype.payment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.cineverseprototype.MainActivity
import com.example.cineverseprototype.databinding.ActivityPaymentGatewayBinding

class PaymentGateway : AppCompatActivity() {

    private lateinit var binding:ActivityPaymentGatewayBinding
    private val TAG = javaClass.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentGatewayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val transactionId:String? = intent.getStringExtra("transactionId")

    }

    override fun onBackPressed() {
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}