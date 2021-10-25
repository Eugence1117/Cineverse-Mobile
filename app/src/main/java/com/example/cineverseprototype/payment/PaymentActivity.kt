package com.example.cineverseprototype.payment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.cineverseprototype.databinding.ActivityPaymentBinding

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding:ActivityPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPaymentBinding.inflate(layoutInflater)
    }
}